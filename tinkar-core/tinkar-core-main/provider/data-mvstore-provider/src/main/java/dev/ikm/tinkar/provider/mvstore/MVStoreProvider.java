/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.provider.mvstore;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.NidGenerator;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.ints2long.IntsInLong;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.provider.search.Indexer;
import dev.ikm.tinkar.provider.search.RecreateIndex;
import dev.ikm.tinkar.provider.search.Searcher;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.ObjIntConsumer;

/**
 * TODO: Maybe also consider making use of: https://blogs.oracle.com/javamagazine/creating-a-java-off-heap-in-memory-database?source=:em:nw:mt:::RC_WWMK200429P00043:NSL400123121
 */
public class MVStoreProvider implements PrimitiveDataService, NidGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(MVStoreProvider.class);
    private static final File defaultDataDirectory = new File("target/mvstore/");
    private static final String databaseFileName = "mvstore.dat";
    private static final UUID nextNidKey = new UUID(Long.MAX_VALUE, Long.MIN_VALUE);
    protected static MVStoreProvider singleton;
    protected final AtomicInteger nextNid;
    final OffHeapStore offHeap;
    final MVStore store;
    final MVMap<Integer, byte[]> nidToComponentMap;
    final MVMap<UUID, Integer> uuidToNidMap;
    final MVMap<UUID, Integer> stampUuidToNidMap;
    final MVMap<Integer, Integer> nidToPatternNidMap;
    /**
     * Using "citing" instead of "referencing" to make the field names more distinct.
     */
    final MVMap<Integer, long[]> nidToCitingComponentsNidMap;
    final MVMap<Integer, int[]> patternToElementNidsMap;
    final Indexer indexer;
    final Searcher searcher;
    final String name;
    protected LongAdder writeSequence = new LongAdder();
    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> patternElementNidsMap = ConcurrentHashMap.newMap();


    public MVStoreProvider() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Opening MVStoreProvider");
        this.offHeap = new OffHeapStore();
        File configuredRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT, defaultDataDirectory);
        configuredRoot.mkdirs();
        this.name = configuredRoot.getName();
        File databaseFile = new File(configuredRoot, databaseFileName);
        LOG.info("Starting MVStoreProvider from: " + databaseFile.getAbsolutePath());
        this.offHeap.open(databaseFile.getAbsolutePath(), false, null);
        this.store = new MVStore.Builder().fileName(databaseFile.getAbsolutePath()).open();

        this.nidToComponentMap = store.openMap("nidToComponentMap");
        this.uuidToNidMap = store.openMap("uuidToNidMap");
        this.stampUuidToNidMap = store.openMap("stampUuidToNidMap");
        this.nidToPatternNidMap = store.openMap("nidToPatternNidMap");
        this.nidToCitingComponentsNidMap = store.openMap("nidToCitingComponentsNidMap");
        this.patternToElementNidsMap = store.openMap("patternToElementNidsMap");
        for (int patternNid : patternToElementNidsMap.keySet()) {
            int[] elementNids = patternToElementNidsMap.get(patternNid);
            for (int elementNid : elementNids) {
                addToElementSet(patternNid, elementNid);
            }
        }

        if (this.uuidToNidMap.containsKey(nextNidKey)) {
            this.nextNid = new AtomicInteger(this.uuidToNidMap.get(nextNidKey));
        } else {
            this.nextNid = new AtomicInteger(PrimitiveDataService.FIRST_NID);
        }

        MVStoreProvider.singleton = this;
        stopwatch.stop();
        LOG.info("Opened MVStoreProvider in: " + stopwatch.durationString());

        File indexDir = new File(configuredRoot, "lucene");
        this.indexer = new Indexer(indexDir.toPath());
        this.searcher = new Searcher();
    }

    public boolean addToElementSet(int patternNid, int elementNid) {
        return null == patternElementNidsMap.getIfAbsentPut(patternNid, integer -> new ConcurrentHashMap<>())
                .put(elementNid, elementNid);
    }

    @Override
    public int newNid() {
        return nextNid.getAndIncrement();
    }

    @Override
    public long writeSequence() {
        return writeSequence.sum();
    }

    public void close() {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Closing MVStoreProvider");
        try {
            save();
            this.indexer.close();
            this.store.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopwatch.stop();
            LOG.info("Closed MVStoreProvider in: " + stopwatch.durationString());
        }
    }

    public void save() {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Saving MVStoreProvider");
        try {
            this.uuidToNidMap.put(nextNidKey, nextNid.get());
            for (Pair<Integer, ConcurrentHashMap<Integer, Integer>> keyValue : patternElementNidsMap.keyValuesView()) {
                patternToElementNidsMap.put(keyValue.getOne(), keyValue.getTwo().keySet()
                        .stream().mapToInt(value -> (int) value).toArray());
            }
            this.store.commit();
            this.offHeap.sync();
            this.indexer.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            stopwatch.stop();
            LOG.info("Saved MVStoreProvider in: " + stopwatch.durationString());
        }
    }

    @Override
    public int nidForUuids(UUID... uuids) {
        return PrimitiveDataService.nidForUuids(uuidToNidMap, this, uuids);
    }

    @Override
    public int nidForUuids(ImmutableList<UUID> uuidList) {
        return PrimitiveDataService.nidForUuids(uuidToNidMap, this, uuidList);
    }

    @Override
    public boolean hasUuid(UUID uuid) {
        return uuidToNidMap.containsKey(uuid);
    }

    @Override
    public boolean hasPublicId(PublicId publicId) {
        return publicId.asUuidList().stream().anyMatch(uuidToNidMap::containsKey);
    }

    @Override
    public void forEach(ObjIntConsumer<byte[]> action) {
        nidToComponentMap.entrySet().forEach(entry -> action.accept(entry.getValue(), entry.getKey()));
    }

    @Override
    public void forEachParallel(ObjIntConsumer<byte[]> action) {
        nidToComponentMap.entrySet().stream().parallel().forEach(entry -> action.accept(entry.getValue(), entry.getKey()));
    }

    @Override
    public void forEachParallel(ImmutableIntList nids, ObjIntConsumer<byte[]> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(int nid) {
        return this.nidToComponentMap.get(nid);
    }

    @Override
    public byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject, DataActivity dataActivity) {
        if (!nidToPatternNidMap.containsKey(nid)) {
            this.nidToPatternNidMap.put(nid, patternNid);
            if (patternNid != Integer.MAX_VALUE) {

                this.nidToPatternNidMap.put(nid, patternNid);
                if (patternNid != Integer.MAX_VALUE) {
                    long citationLong = IntsInLong.ints2Long(nid, patternNid);
                    this.nidToCitingComponentsNidMap.merge(referencedComponentNid, new long[]{citationLong},
                            PrimitiveDataService::mergeCitations);
                    // TODO this will be slow merge for large sets. Consider alternatives.
                    this.addToElementSet(patternNid, nid);
                }
            }
        }
        byte[] mergedBytes = nidToComponentMap.merge(nid, value, PrimitiveDataService::merge);
        writeSequence.increment();
        this.indexer.index(sourceObject);
        return mergedBytes;
    }

    @Override
    public PrimitiveDataSearchResult[] search(String query, int maxResultSize) throws Exception {
        return this.searcher.search(query, maxResultSize);
    }

    @Override
    public CompletableFuture<Void> recreateLuceneIndex() throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TinkExecutor.ioThreadPool().submit(new RecreateIndex(this.indexer)).get();
            } catch (InterruptedException | ExecutionException ex) {
                AlertStreams.dispatchToRoot(new CompletionException("Error encountered while creating Lucene indexes." +
                        "Search and Type Ahead Suggestions may not function as expected.", ex));
            }
            return null;
        }, TinkExecutor.ioThreadPool());
    }

    @Override
    public void forEachSemanticNidOfPattern(int patternNid, IntProcedure procedure) {
        Set<Integer> elementNids = getElementNidsForPatternNid(patternNid);
        if (elementNids != null && elementNids.size() > 0) {
            for (int elementNid : elementNids) {
                procedure.accept(elementNid);
            }
        } else {
            Entity entity = Entity.getFast(patternNid);
            if (entity instanceof PatternEntity == false) {
                throw new IllegalStateException("Trying to iterate elements for entity that is not a pattern: " + entity);
            }

        }
    }

    public Set<Integer> getElementNidsForPatternNid(int patternNid) {
        if (patternElementNidsMap.containsKey(patternNid)) {
            return patternElementNidsMap.get(patternNid).keySet();
        }
        return Set.of();
    }

    @Override
    public void forEachPatternNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachConceptNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachStampNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNidForComponent(int componentNid, IntProcedure procedure) {
        long[] citationLongs = this.nidToCitingComponentsNidMap.get(componentNid);
        if (citationLongs != null) {
            for (long citationLong : citationLongs) {
                int citingComponentNid = (int) (citationLong >> 32);
                procedure.accept(citingComponentNid);
            }
        }
    }

    @Override
    public void forEachSemanticNidForComponentOfPattern(int componentNid, int patternNid, IntProcedure procedure) {
        long[] citationLongs = this.nidToCitingComponentsNidMap.get(componentNid);
        if (citationLongs != null) {
            for (long citationLong : citationLongs) {
                int citingComponentNid = (int) (citationLong >> 32);
                int citingComponentPatternNid = (int) citationLong;
                if (patternNid == citingComponentPatternNid) {
                    procedure.accept(citingComponentNid);
                }
            }
        }
    }

    @Override
    public String name() {
        return name;
    }
}
