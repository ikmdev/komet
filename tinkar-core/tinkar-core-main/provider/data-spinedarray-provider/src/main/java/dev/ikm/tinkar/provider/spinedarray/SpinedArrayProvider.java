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
package dev.ikm.tinkar.provider.spinedarray;

import dev.ikm.tinkar.collection.KeyType;
import dev.ikm.tinkar.collection.SpinedByteArrayMap;
import dev.ikm.tinkar.collection.SpinedIntIntMap;
import dev.ikm.tinkar.collection.SpinedIntLongArrayMap;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.NidGenerator;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataRepair;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.ints2long.IntsInLong;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.provider.search.Indexer;
import dev.ikm.tinkar.provider.search.RecreateIndex;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.provider.search.TypeAheadSearch;
import dev.ikm.tinkar.provider.spinedarray.internal.Get;
import dev.ikm.tinkar.terms.State;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.ObjIntConsumer;

/**
 * Maybe a hybrid of SpinedArrayProvider and MVStoreProvider is worth considering.
 * <p>
 * SpinedArrayProvider is performing horribly because of dependency on ConcurrentUuidIntHashMap serialization.
 * TODO: consider if we remove ConcurrentUuidIntHashMap, or improve.
 * <p>
 * MVStore performs worse when iterating over entities.
 */
public class SpinedArrayProvider implements PrimitiveDataService, NidGenerator, PrimitiveDataRepair {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayProvider.class);
    protected static final File defaultDataDirectory = new File("target/spinedarrays/");

    public enum Lifecycle {
        UNINITIALIZED, STARTING, RUNNING, STOPPING, STOPPED
    }

    public static AtomicReference<Lifecycle> lifecycle = new AtomicReference<>(Lifecycle.UNINITIALIZED);

    private static final StableValue<SpinedArrayProvider> spinedArrayProvider = StableValue.of();
    public static SpinedArrayProvider get() {
        // Set lifecycle to STARTING before attempting initialization
        lifecycle.compareAndSet(Lifecycle.UNINITIALIZED, Lifecycle.STARTING);

        return spinedArrayProvider.orElseSet(() -> {
            try {
                LOG.info("StableValue.orElseSet: Creating new SpinedArrayProvider instance");
                return new SpinedArrayProvider();
            } catch (IOException | ExecutionException | InterruptedException e) {
                // Reset lifecycle on failure
                lifecycle.set(Lifecycle.UNINITIALIZED);
                LOG.error("Failed to create SpinedArrayProvider", e);
                throw new RuntimeException(e);
            }
        });
    }

    protected static LongAdder writeSequence = new LongAdder();
    protected final CountDownLatch uuidsLoadedLatch = new CountDownLatch(1);
    final AtomicInteger nextNid = new AtomicInteger(PrimitiveDataService.FIRST_NID);

    final ConcurrentHashMap<UUID, Integer> uuidToNidMap = ConcurrentHashMap.newMap();
    final ConcurrentHashSet<Integer> patternNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> conceptNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> semanticNids = new ConcurrentHashSet();
    final ConcurrentHashSet<Integer> stampNids = new ConcurrentHashSet();
    final ConcurrentHashMap<Integer, ConcurrentHashSet<Integer>> patternElementNidsMap = ConcurrentHashMap.newMap();

    final SpinedByteArrayMap entityToBytesMap;
    final SpinedIntIntMap nidToPatternNidMap;
    /**
     * Using "citing" instead of "referencing" to make the field names more distinct.
     */
    final SpinedIntLongArrayMap nidToCitingComponentsNidMap;

    final File nidToPatternNidMapDirectory;
    final File nidToByteArrayMapDirectory;
    final File nidToCitingComponentNidMapDirectory;
    final File nextNidKeyFile;
    final Indexer indexer;
    final Searcher searcher;
    final String name;
    final ImmutableList<ChangeSetWriterService> changeSetWriterServices;

    private SpinedArrayProvider() throws IOException, ExecutionException, InterruptedException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Opening SpinedArrayProvider on thread: {}", Thread.currentThread().getName());
        File configuredRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT, defaultDataDirectory);
        name = configuredRoot.getName();
        configuredRoot.mkdirs();
        LOG.info("Datastore root: " + configuredRoot.getAbsolutePath());
        Path indexPath = Path.of(configuredRoot.getPath(),"lucene");
        boolean indexExists = Files.exists(indexPath);
        Indexer indexer;
        try {
            indexer = new Indexer(indexPath);
        } catch (IllegalArgumentException ex) {
            // If Indexer Codec does not match, then delete and rebuild with new Codec
            FileUtil.recursiveDelete(indexPath.toFile());
            indexExists = Files.exists(indexPath);
            indexer = new Indexer(indexPath);
        }
        this.indexer = indexer;
        this.searcher = new Searcher();

        this.nidToPatternNidMapDirectory = new File(configuredRoot, "nidToPatternNidMap");
        this.nidToPatternNidMapDirectory.mkdirs();
        this.nidToByteArrayMapDirectory = new File(configuredRoot, "nidToByteArrayMap");
        this.nidToByteArrayMapDirectory.mkdirs();
        this.nidToCitingComponentNidMapDirectory = new File(configuredRoot, "nidToCitingComponentNidMap");
        this.nidToCitingComponentNidMapDirectory.mkdirs();
        this.nextNidKeyFile = new File(configuredRoot, "nextNidKeyFile");

        this.entityToBytesMap = new SpinedByteArrayMap(new ByteArrayFileStore(nidToByteArrayMapDirectory));
        this.nidToPatternNidMap = new SpinedIntIntMap(KeyType.NID_KEY);
        this.nidToPatternNidMap.read(this.nidToPatternNidMapDirectory);
        this.nidToCitingComponentsNidMap = new SpinedIntLongArrayMap(new IntLongArrayFileStore(nidToCitingComponentNidMapDirectory));

        if (nextNidKeyFile.exists()) {
            String nextNidString = Files.readString(this.nextNidKeyFile.toPath());
            nextNid.set(Integer.valueOf(nextNidString));
        }
        LOG.info("Submitting UUID loading task to thread pool...");
        try {
            TinkExecutor.threadPool().submit(() -> {
                Stopwatch uuidNidMapFromEntitiesStopwatch = new Stopwatch();
                LOG.info("Starting UUID strategy 2 on thread: {}", Thread.currentThread().getName());
                UuidNidCollector uuidNidCollector = new UuidNidCollector(uuidToNidMap,
                        patternNids, conceptNids, semanticNids, stampNids, patternElementNidsMap);
                try {
                    LOG.info("Executing entityToBytesMap.forEachParallel...");
                    this.entityToBytesMap.forEachParallel(uuidNidCollector);
                    LOG.info("Completed entityToBytesMap.forEachParallel, counting down latch");
                    this.uuidsLoadedLatch.countDown();
                } catch (ExecutionException | InterruptedException e) {
                    LOG.error("Error during UUID loading: " + e.getLocalizedMessage(), e);
                } finally {
                    uuidNidMapFromEntitiesStopwatch.stop();
                    LOG.info("Finished UUID strategy 2 in: " + uuidNidMapFromEntitiesStopwatch.durationString());
                    LOG.info(uuidNidCollector.report());
                }
                LOG.info("Starting virtual thread for listAndCancelUncommittedStamps");
                Thread.ofVirtual().start(this::listAndCancelUncommittedStamps);
                LOG.info("UUID loading task completed");
            }).get();
            LOG.info("UUID loading task .get() returned successfully");
        } catch (Exception e) {
            LOG.error("Failed to complete UUID loading task", e);
            throw e;
        }

        ServiceLoader<ChangeSetWriterService> changeSetServiceLoader = PluggableService.load(ChangeSetWriterService.class);
        MutableList<ChangeSetWriterService> changeSetWriters = Lists.mutable.empty();
        changeSetServiceLoader.stream().forEach(changeSetProvider -> {
            changeSetWriters.add(changeSetProvider.get());
        });
        this.changeSetWriterServices = changeSetWriters.toImmutable();
        LOG.info("\n\nLoaded {} ChangeSetWriterService(s)\n\n", changeSetWriters.size());
        if (this.changeSetWriterServices.notEmpty()) {
            LOG.info("ChangeSetWriterService(s): ", changeSetWriters);
        }

        if (!indexExists) {
            try {
                LOG.info("Lucene index does not exist, creating...");
                //this.recreateLuceneIndex().get();
                LOG.info("Recreated Lucene index successfully...");
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        // Prime TypeAheadSearch
        TypeAheadSearch.get();

        stopwatch.stop();
        LOG.info("Opened SpinedArrayProvider in: " + stopwatch.durationString());
        lifecycle.set(Lifecycle.RUNNING);

    }

    @Override
    public boolean hasUuid(UUID uuid) {
        try {
            this.uuidsLoadedLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return uuidToNidMap.containsKey(uuid);
    }

    private void listAndCancelUncommittedStamps() {
        LOG.debug("Searching for canceled stamps in set of size " + stampNids.size());
        int[] stampNidArray = stampNids.stream().sorted().mapToInt(value -> (int) value).toArray();
        for (int stampNid : stampNidArray) {
            StampRecord stamp = Entity.getStamp(stampNid);
            if (stamp.lastVersion() == null) {
                LOG.info("Null last version for stamp with nid: " + stampNid);
            } else {
//                LOG.info("Stamp: " + stamp);
                if (stamp.time() == Long.MAX_VALUE && Transaction.forStamp(stamp).isEmpty()) {
                    // Uncommitted stamp found outside a transaction on restart. Set to canceled.
                    cancelUncommittedStamp(stampNid, stamp);
                }
                if (stamp.lastVersion().stateNid() == State.CANCELED.nid()) {
                    PrimitiveData.get().addCanceledStampNid(stampNid);
                }
            }
        }
    }

    private void cancelUncommittedStamp(int stampNid, StampRecord stamp) {
        LOG.warn("Canceling uncommitted stamp: " + stamp.publicId().asUuidList());
        StampVersionRecord lastVersion = stamp.lastVersion();
        StampVersionRecord canceledVersion = lastVersion.with().time(Long.MIN_VALUE).stateNid(State.CANCELED.nid()).build();
        byte[] stampBytes = stamp
                .without(lastVersion)
                .with(canceledVersion)
                .build().getBytes();
        this.entityToBytesMap.put(stampNid, stampBytes);
    }

    @Override
    public long writeSequence() {
        return writeSequence.sum();
    }

     @Override
    public void close() {
        if (lifecycle.compareAndSet(Lifecycle.RUNNING, Lifecycle.STOPPING) ||
            lifecycle.compareAndSet(Lifecycle.STARTING, Lifecycle.STOPPING)) {
            Stopwatch stopwatch = new Stopwatch();
            LOG.info("Closing SpinedArrayProvider");
            try {
                this.changeSetWriterServices.forEach(ChangeSetWriterService::shutdown);
                save();
                listAndCancelUncommittedStamps();
                entityToBytesMap.close();
                this.indexer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                lifecycle.set(Lifecycle.STOPPED);
                stopwatch.stop();
                LOG.info("Closed SpinedArrayProvider in: " + stopwatch.durationString());
            }
        } else {
            LOG.warn("SpinedArrayProvider is not running, cannot close: " + lifecycle.get());
        }
    }

    public void save() {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Saving SpinedArrayProvider");
        try {
            Files.writeString(this.nextNidKeyFile.toPath(), Integer.toString(nextNid.get()));
            nidToPatternNidMap.write(this.nidToPatternNidMapDirectory);
            this.entityToBytesMap.write();
            this.nidToCitingComponentsNidMap.write();
            this.indexer.commit();
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            stopwatch.stop();
            LOG.info("Save SpinedArrayProvider in: " + stopwatch.durationString());
        }
    }

    @Override
    public int nidForUuids(UUID... uuids) {
        try {
            this.uuidsLoadedLatch.await();
            if (uuids.length == 1) {
                return uuidToNidMap.computeIfAbsent(uuids[0], uuidKey -> newNid());
            }

            OptionalInt optionalNid = optionalNid(uuids);

            int nid = optionalNid.isPresent() ? optionalNid.getAsInt(): Integer.MAX_VALUE;

            for (UUID uuid : uuids) {
                if (nid == Integer.MAX_VALUE) {
                    nid = uuidToNidMap.computeIfAbsent(uuids[0], uuidKey -> newNid());
                } else {
                    uuidToNidMap.put(uuid, nid);
                }
            }
            if (nid == Integer.MIN_VALUE) {
                throw new IllegalStateException("nid cannot be Integer.MIN_VALUE");
            }
            return nid;
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private OptionalInt optionalNid(UUID... uuids) {
        for (UUID uuid : uuids) {
            if (uuidToNidMap.containsKey(uuid)) {
                return OptionalInt.of(uuidToNidMap.get(uuid));
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public int newNid() {
        return nextNid.getAndIncrement();
    }

    @Override
    public int nidForUuids(ImmutableList<UUID> uuidList) {
        try {
            this.uuidsLoadedLatch.await();
            if (uuidList.size() == 1) {
                return uuidToNidMap.computeIfAbsent(uuidList.get(0), uuidKey -> newNid());
            }

            OptionalInt optionalNid = optionalNid(uuidList.toArray(new UUID[uuidList.size()]));

            int nid = optionalNid.isPresent() ? optionalNid.getAsInt(): Integer.MAX_VALUE;

            for (UUID uuid : uuidList) {
                if (nid == Integer.MAX_VALUE) {
                    nid = uuidToNidMap.computeIfAbsent(uuid, uuidKey -> newNid());
                } else {
                    uuidToNidMap.put(uuid, nid);
                }
            }
            if (nid == Integer.MIN_VALUE) {
                throw new IllegalStateException("nid cannot be Integer.MIN_VALUE");
            }
            return nid;
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPublicId(PublicId publicId) {
        try {
            this.uuidsLoadedLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return publicId.asUuidList().stream().anyMatch(uuidToNidMap::containsKey);
    }

    @Override
    public void forEach(ObjIntConsumer<byte[]> action) {
        this.entityToBytesMap.forEach(action);
    }

    @Override
    public void forEachParallel(ObjIntConsumer<byte[]> action) {
        try {
            this.entityToBytesMap.forEachParallel(action);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forEachParallel(ImmutableIntList nids, ObjIntConsumer<byte[]> action) {
        try {
            this.entityToBytesMap.forEachParallel(nids, action);
        } catch (ExecutionException | InterruptedException e) {
            AlertStreams.dispatchToRoot(e);
        }
    }

    @Override
    public byte[] getBytes(int nid) {
        return this.entityToBytesMap.get(nid);
    }

    @Override
    public byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject, DataActivity activity) {
        if (nid == Integer.MIN_VALUE) {
            LOG.error("NID should not be Integer.MIN_VALUE");
            throw new IllegalStateException("NID should not be Integer.MIN_VALUE");
        }
        if (!this.entityToBytesMap.containsKey(nid)) {
            this.nidToPatternNidMap.put(nid, patternNid);
            if (patternNid != Integer.MAX_VALUE) {
                long citationLong = IntsInLong.ints2Long(nid, patternNid);
                this.nidToCitingComponentsNidMap.accumulateAndGet(referencedComponentNid, new long[]{citationLong},
                        PrimitiveDataService::mergeCitations);
                addToPatternElementSet(patternNid, nid);
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
        }
        byte[] mergedBytes = this.entityToBytesMap.accumulateAndGet(nid, value, PrimitiveDataService::merge);
        this.writeSequence.increment();
        this.changeSetWriterServices.forEach(writerService -> writerService.writeToChangeSet((Entity) sourceObject, activity));
        this.indexer.index(sourceObject);
        return mergedBytes;
    }

    public boolean addToPatternElementSet(int patternNid, int elementNid) {

        return patternElementNidsMap.getIfAbsentPut(patternNid, integer -> new ConcurrentHashSet())
                .add(elementNid);
    }

    @Override
    public PrimitiveDataSearchResult[] search(String query, int maxResultSize) throws Exception {
        return this.searcher.search(query, maxResultSize);
    }

    @Override
    public CompletableFuture<Void> recreateLuceneIndex() {
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
    public int[] semanticNidsOfPattern(int patternNid) {
        IntSet elementNids = getElementNidsForPatternNid(patternNid);
        if (elementNids.notEmpty()) {
            MutableIntList elementNidList = IntLists.mutable.withInitialCapacity(elementNids.size());
            elementNids.forEach(integer -> elementNidList.add(integer));
            return elementNidList.toArray();
        }
        return new int[0];
    }

    public IntSet getElementNidsForPatternNid(int patternNid) {
        if (patternElementNidsMap.containsKey(patternNid)) {
            return IntSets.immutable.ofAll(patternElementNidsMap.get(patternNid).stream().mapToInt(value -> (int) value));
        }
        return IntSets.immutable.empty();
    }

    @Override
    public void forEachSemanticNidOfPattern(int patternNid, IntProcedure procedure) {
        EntityHandle.get(patternNid).expectPattern("Trying to iterate elements for entity that is not a pattern: ");

        IntSet elementNids;
        if (LOG.isTraceEnabled()) {
            Stopwatch sw = new Stopwatch();
            elementNids = getElementNidsForPatternNid(patternNid);
            LOG.atTrace().log("getElementNidsForPatternNid " + PrimitiveData.text(patternNid) +
                    " time: " + sw.durationString());
        } else {
            elementNids = getElementNidsForPatternNid(patternNid);
        }
        if (elementNids.notEmpty()) {
            elementNids.forEach(procedure);
        }
    }

    @Override
    public void forEachPatternNid(IntProcedure procedure) {
        try {
            this.uuidsLoadedLatch.await();
            this.patternNids.forEach(patternNid -> procedure.accept(patternNid));
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forEachConceptNid(IntProcedure procedure) {
        try {
            this.uuidsLoadedLatch.await();
            this.conceptNids.forEach(conceptNid -> procedure.accept(conceptNid));
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forEachStampNid(IntProcedure procedure) {
        try {
            this.uuidsLoadedLatch.await();
            this.stampNids.forEach(stampNid -> procedure.accept(stampNid));
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forEachSemanticNid(IntProcedure procedure) {
        try {
            this.uuidsLoadedLatch.await();
            this.semanticNids.forEach(semanticNid -> procedure.accept(semanticNid));
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
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

    @Override
    public void erase(int nid) {
        this.entityToBytesMap.put(nid, null);
        this.nidToPatternNidMap.put(nid, Integer.MAX_VALUE);
        this.nidToCitingComponentsNidMap.put(nid, null);
        this.conceptNids.remove(nid);
        this.semanticNids.remove(nid);
        this.patternNids.remove(nid);
        this.stampNids.remove(nid);
        this.nidToCitingComponentsNidMap.forEach((nidPatternsCitingComponent, referencedComponentNid) -> {
            MutableLongList nidPatternInLongToRemove = LongLists.mutable.withInitialCapacity(2);
            for (long nidPatternInLong : nidPatternsCitingComponent) {
                // The longs contain int nid, int patternNid in each long
                int nidCitingComponent = IntsInLong.int1FromLong(nidPatternInLong);
                if (nidCitingComponent == nid) {
                    nidPatternInLongToRemove.add(nidPatternInLong);
                }
            }
            if (nidPatternInLongToRemove.notEmpty()) {
                MutableLongSet longSet = LongSets.mutable.of(nidPatternsCitingComponent);
                longSet.removeAll(nidPatternInLongToRemove);
                this.nidToCitingComponentsNidMap.put(referencedComponentNid, longSet.toArray());
            }
        });

    }

    @Override
    public void mergeThenErase(int nidToErase, int nidToMergeInto) {


        byte[] mergedBytes = merge(PrimitiveData.get().getBytes(nidToMergeInto), PrimitiveData.get().getBytes(nidToErase), DataActivity.DATA_REPAIR);
        erase(nidToErase);
        put(nidToMergeInto, mergedBytes);
        EntityService.get().invalidateCaches(nidToErase, nidToMergeInto);
    }

    byte[] merge(byte[] bytesToMergeInto, byte[] bytesToBeErased, DataActivity activity) {
        if (bytesToBeErased == null) {
            return bytesToMergeInto;
        }
        ByteBuf readBufToMergeInto = ByteBuf.wrapForReading(bytesToMergeInto);
        int mergeIntoArrayCount = readBufToMergeInto.readInt();
        int mergeIntoChronicleArrayElementByteCount = readBufToMergeInto.readInt();
        byte mergeIntoEntityFormatVersion = readBufToMergeInto.readByte(); // Entity format version in a byte.
        byte mergeIntoEntityTypeToken = readBufToMergeInto.readByte(); // Entity type token

        int mergeIntoNid = readBufToMergeInto.readInt();
        ImmutableList<UUID> mergeIntoUuids = getUuidsFromBytes(readBufToMergeInto);

        ByteBuf readBufToBeErased = ByteBuf.wrapForReading(bytesToBeErased);
        int eraseComponentArrayCount = readBufToBeErased.readInt();
        int eraseComponentChronicleArrayElementByteCount = readBufToBeErased.readInt();
        byte eraseComponentEntityFormatVersion = readBufToBeErased.readByte(); // Entity format version in a byte.
        byte eraseComponentEntityTypeToken = readBufToBeErased.readByte(); // Entity type token
        int eraseComponentNid = readBufToBeErased.readInt();
        ImmutableList<UUID> uuidsFromBytesToBeErased = getUuidsFromBytes(readBufToBeErased);

        // Create final set of uuids...
        MutableSet<UUID> uuidSet = Sets.mutable.ofAll(mergeIntoUuids);
        uuidSet.addAll(uuidsFromBytesToBeErased.castToList());
        ImmutableList<UUID> mergedUuids = uuidSet.toImmutableList();

        // Note first array (the chronicle fields) will be larger by the number of additional UUIDs...

        // Create bytes for merge into...
        ByteBuf outputBytesToMergeInto = ByteBufPool.allocate(bytesToMergeInto.length + (mergedUuids.size() * 16));
        int additionalMergedBytes = (mergedUuids.size() - mergeIntoUuids.size()) * 16;
        outputBytesToMergeInto.writeInt(mergeIntoArrayCount);
        outputBytesToMergeInto.writeInt(mergeIntoChronicleArrayElementByteCount + additionalMergedBytes);
        outputBytesToMergeInto.writeByte(mergeIntoEntityFormatVersion);
        outputBytesToMergeInto.writeByte(mergeIntoEntityTypeToken);
        outputBytesToMergeInto.writeInt(mergeIntoNid);
        writeUuidsAndRemaining(mergedUuids, outputBytesToMergeInto, readBufToMergeInto);

        // Create bytes for to be erased
        ByteBuf outputBytesToBeErased = ByteBufPool.allocate(bytesToBeErased.length + (mergedUuids.size() * 16));
        int additionalErasedBytes = (mergedUuids.size() - uuidsFromBytesToBeErased.size()) * 16;
        outputBytesToBeErased.writeInt(eraseComponentArrayCount);
        outputBytesToBeErased.writeInt(eraseComponentChronicleArrayElementByteCount + additionalErasedBytes);
        outputBytesToBeErased.writeByte(eraseComponentEntityFormatVersion);
        outputBytesToBeErased.writeByte(eraseComponentEntityTypeToken);
        outputBytesToBeErased.writeInt(mergeIntoNid);
        writeUuidsAndRemaining(mergedUuids, outputBytesToBeErased, readBufToBeErased);

        /*
        Need to manage possible time duplicates on merge. This may require creating a new stamp and nudging the time
        by a second to make the result unique. Since we need a new stamp, that is managed at the entity, rather than
        the primitive level.
         */

        return PrimitiveDataService.merge(outputBytesToMergeInto.asArray(), outputBytesToBeErased.asArray());
    }

    /**
     *
     */
    void writeUuidsAndRemaining(ImmutableList<UUID> mergedUuids, ByteBuf outputBytes, ByteBuf readBuf) {
        /*
         * Merging the UUIDs outside of a versioned object creates challenges wrt data integrity.
         * Consider how we could version uuids within a public id. Maybe additional UUIDs are added
         * to the public ID and has its own time stamp?
         */
        long[] additionalUuidLongs = UuidUtil.asArray(mergedUuids);
        outputBytes.writeLong(additionalUuidLongs[0]); // The initial UUID is always present
        outputBytes.writeLong(additionalUuidLongs[1]);
        outputBytes.writeByte((byte) (additionalUuidLongs.length - 2));
        for (int i = 2; i < additionalUuidLongs.length; i++) {
            outputBytes.writeLong(additionalUuidLongs[i]);
        }
        int readBufToMergeIntoRemaining = readBuf.readRemaining();
        for (int i = 0; i < readBufToMergeIntoRemaining; i++) {
            outputBytes.writeByte(readBuf.get());
        }
    }

    ImmutableList<UUID> getUuidsFromBytes(ByteBuf readBuf) {
        MutableList<UUID> uuids = Lists.mutable.withInitialCapacity(2);
        long msb = readBuf.readLong();
        long lsb = readBuf.readLong();
        uuids.add(new UUID(msb, lsb));
        int additionalUuidLongSize = readBuf.readByte();
        if (additionalUuidLongSize > 0) {
            long[] additionalUuidLongs = new long[additionalUuidLongSize];
            for (int i = 0; i < additionalUuidLongSize; i++) {
                additionalUuidLongs[i] = readBuf.readLong();
            }
            ImmutableList<UUID> additionalUuids = UuidUtil.toList(additionalUuidLongs);
            uuids.addAll(additionalUuids.castToList());
        }
        return uuids.toImmutableList();
    }

    @Override
    public void put(int nid, byte[] bytesToOverwrite) {
        this.entityToBytesMap.put(nid, bytesToOverwrite);
    }
}
