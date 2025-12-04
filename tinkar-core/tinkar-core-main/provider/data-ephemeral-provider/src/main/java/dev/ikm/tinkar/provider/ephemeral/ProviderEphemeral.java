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
package dev.ikm.tinkar.provider.ephemeral;

import dev.ikm.tinkar.collection.KeyType;
import dev.ikm.tinkar.collection.SpinedIntIntMapAtomic;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.NidGenerator;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.ints2long.IntsInLong;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.provider.search.Indexer;
import dev.ikm.tinkar.provider.search.RecreateIndex;
import dev.ikm.tinkar.provider.search.Searcher;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.ObjIntConsumer;


public class ProviderEphemeral implements PrimitiveDataService, NidGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderEphemeral.class);
    protected static AtomicReference<ProviderEphemeral> providerReference = new AtomicReference<>();
    protected static ProviderEphemeral singleton;
    protected static LongAdder writeSequence = new LongAdder();
    // TODO I don't think the spines need to be atomic for this use case of nids -> elementIndices.
    //  There is no update after initial value set...
    final SpinedIntIntMapAtomic nidToPatternNidMap = new SpinedIntIntMapAtomic(KeyType.NID_KEY);
    /**
     * Using "citing" instead of "referencing" to make the field names more distinct.
     */
    final ConcurrentHashMap<Integer, long[]> nidToCitingComponentsNidMap = ConcurrentHashMap.newMap();
    final ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> patternToElementNidsMap = ConcurrentHashMap.newMap();
    final Indexer indexer;
    final Searcher searcher;
    final ConcurrentHashSet<Integer> patternNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> conceptNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> semanticNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> stampNids = new ConcurrentHashSet();
    private final ConcurrentHashMap<Integer, byte[]> nidComponentMap = ConcurrentHashMap.newMap();
    private final ConcurrentHashMap<UUID, Integer> uuidNidMap = new ConcurrentHashMap<>();
    private final AtomicInteger nextNid = new AtomicInteger(PrimitiveDataService.FIRST_NID);

    private ProviderEphemeral() throws IOException {
        LOG.info("Constructing ProviderEphemeral");
        this.indexer = new Indexer();
        this.searcher = new Searcher();
    }

    public static PrimitiveDataService provider() {
        if (singleton == null) {
            singleton = providerReference.updateAndGet(providerEphemeral -> {
                if (providerEphemeral == null) {
                    try {
                        return new ProviderEphemeral();
                    } catch (IOException e) {
                        LOG.error("Error starting ProviderEphemeral", e);
                        throw new RuntimeException(e);
                    }
                }
                return providerEphemeral;
            });
        }
        return singleton;
    }

    @Override
    public long writeSequence() {
        return writeSequence.sum();
    }

    @Override
    public void close() {
        try {
            this.providerReference.set(null);
            this.singleton = null;
            this.indexer.commit();
            this.indexer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int nidForUuids(UUID... uuids) {
        return PrimitiveDataService.nidForUuids(uuidNidMap, this, uuids);
    }

    @Override
    public boolean hasUuid(UUID uuid) {
        return uuidNidMap.containsKey(uuid);
    }

    @Override
    public int nidForUuids(ImmutableList<UUID> uuidList) {
        return PrimitiveDataService.nidForUuids(uuidNidMap, this, uuidList);
    }

    @Override
    public boolean hasPublicId(PublicId publicId) {
        return publicId.asUuidList().stream().anyMatch(uuidNidMap::containsKey);
    }

    @Override
    public void forEach(ObjIntConsumer<byte[]> action) {
        nidComponentMap.forEach((integer, bytes) -> action.accept(bytes, integer));
    }

    @Override
    public void forEachParallel(ObjIntConsumer<byte[]> action) {
        int threadCount = TinkExecutor.threadPool().getMaximumPoolSize();
        List<Procedure2<Integer, byte[]>> blocks = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            blocks.add((Procedure2<Integer, byte[]>) (integer, bytes) -> action.accept(bytes, integer));
        }
        nidComponentMap.parallelForEachKeyValue(blocks, TinkExecutor.threadPool());
    }

    @Override
    public void forEachParallel(ImmutableIntList nids, ObjIntConsumer<byte[]> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(int nid) {
        return nidComponentMap.get(nid);
    }

    @Override
    public byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject, DataActivity activity) {
        if (!nidToPatternNidMap.containsKey(nid)) {
            this.nidToPatternNidMap.put(nid, patternNid);
            if (patternNid != Integer.MAX_VALUE) {

                this.nidToPatternNidMap.put(nid, patternNid);
                if (patternNid != Integer.MAX_VALUE) {
                    long citationLong = IntsInLong.ints2Long(nid, patternNid);
                    this.nidToCitingComponentsNidMap.merge(referencedComponentNid, new long[]{citationLong},
                            PrimitiveDataService::mergeCitations);
                    this.patternToElementNidsMap.getIfAbsentPut(nid, () -> new ConcurrentSkipListSet<>()).add(nid);
                }
            }
        }
        if (sourceObject instanceof ConceptEntity concept) {
            this.conceptNids.add(concept.nid());
        } else if (sourceObject instanceof SemanticEntity semanticEntity) {
            this.semanticNids.add(semanticEntity.nid());
        } else if (sourceObject instanceof PatternEntity patternEntity) {
            this.patternNids.add(patternEntity.nid());
        } else if (sourceObject instanceof StampEntity stampEntity) {
            this.stampNids.add(stampEntity.nid());
        }
        byte[] mergedBytes = nidComponentMap.merge(nid, value, PrimitiveDataService::merge);
        writeSequence.increment();
        indexer.index(sourceObject);
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
        nidToPatternNidMap.forEach((nid, setNid) -> {
            if (patternNid == setNid) {
                procedure.accept(nid);
            }
        });
    }

    @Override
    public void forEachPatternNid(IntProcedure procedure) {
        this.patternNids.forEach(procedure::accept);
    }

    @Override
    public void forEachConceptNid(IntProcedure procedure) {
        this.conceptNids.forEach(procedure::accept);
    }

    @Override
    public void forEachStampNid(IntProcedure procedure) {
        this.stampNids.forEach(procedure::accept);
    }

    @Override
    public void forEachSemanticNid(IntProcedure procedure) {
        this.semanticNids.forEach(procedure::accept);
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
        return "Ephemeral data";
    }

    @Override
    public int newNid() {
        return nextNid.getAndIncrement();
    }
}
