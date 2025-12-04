package dev.ikm.tinkar.provider.changeset;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The ChangeSetWriterProvider class is responsible for writing and managing entity change sets.
 * It provides functionalities to persist entity changes, manage service lifecycles, and ensure
 * proper handling of change set files using zip compression and metadata generation. The class
 * is designed to handle entities efficiently through a service thread, leveraging internal queues
 * for processing and persistence.
 */
public class ChangeSetWriterProvider implements ChangeSetWriterService, SaveState {
    private static final Logger LOG = LoggerFactory.getLogger(ChangeSetWriterProvider.class);
    private static final long INACTIVITY_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(5);

    /**
     * Represents the various states of the ChangeSetWriterProvider during its lifecycle.
     * <p>
     * The STATE enum defines the operational phases for tracking the current status
     * of the service. Each state corresponds to a specific stage or condition
     * during the execution of writing and processing changes.
     * <p>
     * The states are:
     * <p>
     * <p> - INITIALIZING: The service is starting up and performing initialization tasks.
     * <p> - RUNNING: The service is actively processing and writing entity changes.
     * <p> - ROTATING: The service is in the process of rotating or transitioning its resources,
     *             such as creating a new change set file.
     * <p> - STOPPED: The service has been intentionally stopped and is no longer processing.
     * <p> - FAILED: The service encountered an error or failure preventing it from continuing operation.
     */
    private enum STATE {
        INITIALIZING,
        RUNNING,
        ROTATING,
        STOPPED,
        FAILED
    }

    final AtomicReference<Thread> serviceThread = new AtomicReference<>();
    private final Map<Thread, Semaphore> threadSemaphoreMap = new ConcurrentHashMap<>();
    private final Map<Thread, STATE> threadStateMap = new ConcurrentHashMap<>();

    /**
     * Initialization-on-demand holder idiom:
     * This is the preferred approach in most cases as it provides lazy initialization with thread safety.
     */
    private static class ChangeSetWriterHolder {
        public static final ChangeSetWriterProvider INSTANCE = new ChangeSetWriterProvider();
    }

    public static ChangeSetWriterProvider provider() {
        return ChangeSetWriterHolder.INSTANCE;
    }

    private final File changeSetFolder;
    private final LinkedBlockingQueue<Entity<EntityVersion>> entitiesToWrite = new LinkedBlockingQueue<>();

    private ChangeSetWriterProvider() {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isPresent()) {
            // TODO: Use Paths and Files classes to simplify code and handle file creation errors
            this.changeSetFolder = optionalDataStoreRoot.get().toPath().resolve("changeSets", "src", "main", "resources").toFile();
            if (!changeSetFolder.exists()) {
                changeSetFolder.mkdirs();
            }
            PrimitiveData.getStatesToSave().add(this);
            startService();
        } else {
            throw new IllegalStateException("ServiceKeys.DATA_STORE_ROOT not provided.");
        }
    }

    /**
     * Writes an entity to the change set based on the specified activity.
     * <p>
     * Depending on the provided activity, the method will determine whether the
     * entity should be queued for writing or ignored. Specifically, entities with
     * the activity {@code SYNCHRONIZABLE_EDIT} are added to the internal queue for
     * processing. All other activity types do not trigger any action.
     *
     * @param entity the entity to be written or processed based on the activity
     * @param activity the type of data activity that determines the operation
     *                 on the entity; supported activities include SYNCHRONIZABLE_EDIT,
     *                 LOADING_CHANGE_SET, INITIALIZE, LOCAL_EDIT, and DATA_REPAIR
     * @throws RuntimeException if the thread is interrupted during the operation
     */
    @Override
    public void writeToChangeSet(Entity entity, DataActivity activity) {
        try {
            switch (activity) {
                case SYNCHRONIZABLE_EDIT -> {
                    this.entitiesToWrite.put(entity);
                    LOG.trace("ChangeSetWriterProvider queued entity for changeset write: {}", entity);
                }
                case LOADING_CHANGE_SET, INITIALIZE, LOCAL_EDIT, DATA_REPAIR -> {
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     *
     * @param length the length of the random string to generate
     * @return a randomly generated alphanumeric string of the given length
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }

    /**
     * Creates a new ZIP file in the change set folder with a unique name. The file name is generated
     * using the following pattern: [USER description] [current timestamp] [random alphanumeric string] ike-cs.zip.
     * <p>
     * The method ensures that the generated file name is unique and based on the current time and
     * additional random characters to prevent name collisions.
     *
     * @return a {@code File} object pointing to the newly created ZIP file in the change set folder
     */
    private File newZipFile() {
        return new File(changeSetFolder,
                TinkarTerm.USER.description() + " " +
                        DateTimeUtil.nowWithZoneCompact().replace(':', '\uA789') +
                        " " + generateRandomString(3) + " ike-cs.zip");
    }

    /**
     * Starts the main service thread responsible for processing and writing entity data changes
     * into a ZIP file format. This method creates and manages a virtual thread, utilizing the
     * Thread API with a specific thread name for identification. The service remains operational
     * until explicitly stopped, processing entities passed to it for handling and writing.
     * <p>
     * The method performs the following tasks:
     * <p> - Acquires necessary resources for thread execution.
     * <p> - Processes entities from a queue for writing. Handles both committed and uncommitted entities
     *   separately, ensuring uncommitted entities are managed at the end.
     * <p> - Writes data into a ZIP file located in the change set folder, creating a primary "Entities"
     *   entry and a manifest file for metadata when entities are successfully written.
     * <p> - Monitors and aggregates statistical counts including the number of entities, concepts,
     *   semantics, patterns, stamps, and associated module and author identifiers.
     * <p> - Cleans up resources and deletes the ZIP file if no entities were processed or if the file
     *   is empty.
     * <p> - Ensures thread execution is properly controlled, including handling interruptions and failure
     *   scenarios with appropriate state transitions.
     * <p>
     * This method is core to managing and persisting changes in the change set, leveraging thread-safe
     * mechanisms and efficient I/O handling.
     */
    private void startService() {
        Thread.ofVirtual().name("ChangeSetWriterProvider-ServiceThread").start(() -> {
            LOG.trace("Starting ChangeSetWriterProvider on service thread: {}", Thread.currentThread());
            Semaphore changeSetWriter = new Semaphore(1);
            try {
                changeSetWriter.acquireUninterruptibly();
                threadSemaphoreMap.put(Thread.currentThread(), changeSetWriter);
                final MutableMultimap<Integer, Entity<EntityVersion>> uncommittedEntitiesByStamp = Multimaps.mutable.set.empty();
                serviceThread.set(Thread.currentThread());
                threadStateMap.put(Thread.currentThread(), STATE.RUNNING);
                final LongAdder entityCount = new LongAdder();
                final LongAdder conceptsCount = new LongAdder();
                final LongAdder semanticsCount = new LongAdder();
                final LongAdder patternsCount = new LongAdder();
                final LongAdder stampsCount = new LongAdder();

                final Set<PublicId> moduleList = new HashSet<>();
                final Set<PublicId> authorList = new HashSet<>();
                final EntityToTinkarSchemaTransformer entityTransformer =
                        EntityToTinkarSchemaTransformer.getInstance();

                final File zipfile = newZipFile();
                LOG.trace("ChangeSetWriterProvider starting new zip file: {}", zipfile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(zipfile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos);
                     ZipOutputStream zos = new ZipOutputStream(bos)) {
                    // Create a single entry for all changes in this zip file
                    final ZipEntry zipEntry = new ZipEntry("Entities");
                    zos.putNextEntry(zipEntry);
                    try {
                        AtomicLong lastWriteTimeMillis = new AtomicLong(System.currentTimeMillis());
                        while (threadStateMap.get(Thread.currentThread()) == STATE.RUNNING) {
                            final Entity<EntityVersion> entityToWrite = this.entitiesToWrite.poll(250, TimeUnit.MILLISECONDS);
                            if (entityToWrite != null) {
                                lastWriteTimeMillis.set(System.currentTimeMillis());
                                if (entityToWrite.uncommitted()) {
                                    // We will write uncommitted versions at the end of the thread to prevent bloat from uncommitted changes,
                                    // unless they are committed before the thread stops.
                                    ImmutableIntList uncommittedStampNids = entityToWrite.uncommittedStampNids();
                                    uncommittedStampNids.forEach(stampNid -> {
                                        LOG.trace("ChangeSetWriterProvider caching uncommitted entity for stampNid {}:\n{}", stampNid, entityToWrite);
                                        uncommittedEntitiesByStamp.remove(stampNid, entityToWrite);
                                        uncommittedEntitiesByStamp.put(stampNid, entityToWrite);
                                    });
                                } else {
                                    writeEntity(entityCount, entityToWrite, conceptsCount, semanticsCount, patternsCount, stampsCount, moduleList, authorList, entityTransformer, zos);
                                    // If a committed stamp comes through, then see if any previously uncommitted versions for that stamp exist, and write them if so.
                                    if (entityToWrite instanceof StampEntity stampEntity && uncommittedEntitiesByStamp.containsKey(stampEntity.nid())) {
                                        uncommittedEntitiesByStamp.removeAll(stampEntity.nid()).forEach(entity ->
                                                writeEntity(entityCount, entity, conceptsCount, semanticsCount, patternsCount,
                                                        stampsCount, moduleList, authorList, entityTransformer, zos));
                                    }
                                }
                            }
                            if (System.currentTimeMillis() - lastWriteTimeMillis.get() > INACTIVITY_THRESHOLD_MILLIS) {
                                LOG.info("Rotating ChangeSetWriterProvider, no activity for {} minutes.",
                                        TimeUnit.MILLISECONDS.toMinutes(INACTIVITY_THRESHOLD_MILLIS));
                                TinkExecutor.threadPool().submit(this::save);
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                    // Write any uncommitted entities.
                    uncommittedEntitiesByStamp.forEachValue(entityToWrite ->
                            writeEntity(entityCount, entityToWrite, conceptsCount, semanticsCount, patternsCount,
                                    stampsCount, moduleList, authorList, entityTransformer, zos));
                    zos.closeEntry();
                    if (entityCount.sum() > 0) {
                        LOG.debug("Data zipEntry size: " + zipEntry.getSize());
                        LOG.debug("Data zipEntry compressed size: " + zipEntry.getCompressedSize());

                        // Write Manifest File
                        final ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
                        zos.putNextEntry(manifestEntry);
                        zos.write(generateManifestContent(entityCount,
                                conceptsCount,
                                semanticsCount,
                                patternsCount,
                                stampsCount,
                                moduleList,
                                authorList).getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                    // Cleanup
                    zos.flush();
                    zos.finish();

                } catch (IOException e) {
                    threadStateMap.put(Thread.currentThread(), STATE.FAILED);
                    throw new RuntimeException(e);
                } finally {
                    if (zipfile.exists()) {
                        if (entityCount.sum() == 0) {
                            zipfile.delete();
                        }
                    }
                }
            } finally {
                changeSetWriter.release();
                threadStateMap.remove(Thread.currentThread());
            }
        });

    }

    /**
     * Writes a given entity to a ZIP output stream after transforming it,
     * while incrementing corresponding statistical counters for entity types and
     * collecting module and author public IDs for manifest generation.
     *
     * @param entityCount the counter for the total number of entities processed
     * @param entityToWrite the entity to be written
     * @param conceptsCount the counter for the number of concept entities processed
     * @param semanticsCount the counter for the number of semantic entities processed
     * @param patternsCount the counter for the number of pattern entities processed
     * @param stampsCount the counter for the number of stamp entities processed
     * @param moduleList the set to collect module public IDs for manifest generation
     * @param authorList the set to collect author public IDs for manifest generation
     * @param entityTransformer the transformer used to convert entities to Tinkar schema messages
     * @param zos the ZIP output stream to write the entity data into
     */
    private static void writeEntity(LongAdder entityCount,
                                    Entity<EntityVersion> entityToWrite,
                                    LongAdder conceptsCount,
                                    LongAdder semanticsCount,
                                    LongAdder patternsCount,
                                    LongAdder stampsCount,
                                    Set<PublicId> moduleList,
                                    Set<PublicId> authorList,
                                    EntityToTinkarSchemaTransformer entityTransformer,
                                    ZipOutputStream zos) {
        entityCount.increment();
        switch (entityToWrite) {
            case ConceptEntity _ -> conceptsCount.increment();
            case SemanticEntity _ -> semanticsCount.increment();
            case PatternEntity _ -> patternsCount.increment();
            case StampEntity stampEntity -> {
                stampsCount.increment();
                // Store Module & Author Dependencies for Manifest
                moduleList.add(stampEntity.module().publicId());
                authorList.add(stampEntity.author().publicId());
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + entityToWrite);
            }
        }
        // Transform and write data
        try {
            TinkarMsg tinkarMsg = entityTransformer.transform(entityToWrite);
            tinkarMsg.writeDelimitedTo(zos);
            LOG.debug("ChangeSetWriterProvider wrote Entity:\n{}", entityToWrite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the content for a manifest file based on the provided entity counts, module data,
     * and author data. This method aggregates the totals and passes them to another utility
     * method that constructs the final manifest content.
     *
     * @param entityCount the counter for the total number of entities processed
     * @param conceptsCount the counter for the total number of concepts processed
     * @param semanticsCount the counter for the total number of semantics processed
     * @param patternsCount the counter for the total number of patterns processed
     * @param stampsCount the counter for the total number of stamps processed
     * @param moduleList the set of module public IDs collected for the manifest
     * @param authorList the set of author public IDs collected for the manifest
     * @return the generated manifest content as a String
     */
    private String generateManifestContent(LongAdder entityCount,
                                           LongAdder conceptsCount,
                                           LongAdder semanticsCount,
                                           LongAdder patternsCount,
                                           LongAdder stampsCount,
                                           Set<PublicId> moduleList,
                                           Set<PublicId> authorList) {
        return ExportEntitiesToProtobufFile.generateManifestContent(entityCount.sum(),
                conceptsCount.sum(),
                semanticsCount.sum(),
                patternsCount.sum(),
                stampsCount.sum(),
                moduleList,
                authorList);
    }

    /**
     * Saves the current state of the change set writer by initiating a checkpoint.
     * <p>
     * The method performs the following actions:
     * <p> - If a service thread is currently running, it interrupts the thread to ensure
     *   any ongoing processing is stopped, and all changes are written to the open zip file.
     * <p> - If the restart parameter is set to true, it initiates a new service thread
     *   by calling the {@code startService} method, effectively resetting the processing
     *   state. The reset writer will write to a new zip file.
     */
    @Override
    public CompletableFuture<Void> save() {
        if (serviceThread.get() != null) {
            LOG.trace("Rotating ChangeSetWriterProvider on service thread: {}", serviceThread.get());
        }
        return checkpoint(true);
    }

    /**
     * Shuts down the ChangeSetWriterProvider, ensuring any necessary finalization
     * or cleanup steps are completed.
     * <p>
     * This method performs the following actions:
     * <p> - Logs the initiation of the shutdown process.
     * <p> - Executes a checkpoint operation to save the current state without restarting the service.
     * <p> - Acquires the maximum allowed writer permits, effectively blocking further writing operations.
     * <p> - Logs the completion of the shutdown process.
     * <p>
     * This implementation ensures that all active resources are managed or released properly
     * to facilitate a clean shutdown of the service.
     */
    @Override
    public void shutdown() {
        LOG.info("Start shutdown of ChangeSetWriterProvider");
        try {
            checkpoint(false).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Finish shutdown of ChangeSetWriterProvider");
    }

    /**
     * Manages the checkpoint operation for the ChangeSetWriterProvider service.
     * The method either interrupts the current service thread to prepare for state change
     * or transitions the state of the service based on the {@code restart} parameter.
     *
     * @param restart a flag indicating whether the service should restart after the checkpoint.
     *                If {@code true}, the service state transitions to {@code ROTATING},
     *                and a new service thread is started to continue operations.
     *                If {@code false}, the service state transitions to {@code STOPPED}.
     */
    private CompletableFuture<Void> checkpoint(boolean restart) {
        return CompletableFuture.supplyAsync(() -> {
            Thread interruptedThread = null;
            switch (serviceThread.get()) {
                case Thread thread -> {
                    interruptedThread = thread;
                }
                case null -> {
                }
            }
            if (interruptedThread != null) {
                LOG.trace("Stopping ChangeSetWriterProvider on service thread: {}", interruptedThread);
                threadStateMap.put(interruptedThread, (restart?STATE.ROTATING:STATE.STOPPED));
            }
            if (restart) {
                // start a new thread with a new file
                startService();
            }

            if (interruptedThread != null) {
                Semaphore changeSetWriter = threadSemaphoreMap.remove(interruptedThread);
                if (changeSetWriter != null) {
                    changeSetWriter.acquireUninterruptibly();
                    LOG.trace("Stopped ChangeSetWriterProvider on service thread: {}", interruptedThread);
                }
            }
            return null;
        }, TinkExecutor.ioThreadPool());
    }
}
