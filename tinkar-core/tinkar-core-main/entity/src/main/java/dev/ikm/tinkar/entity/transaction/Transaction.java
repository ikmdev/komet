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
package dev.ikm.tinkar.entity.transaction;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampAnalogueBuilder;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

/**
 * Represents a transaction managing various components and stamps.
 * A Transaction is responsible for keeping track of its associated
 * unique identifier (UUID), name, active transactions, related stamps,
 * and components, as well as handling transactional operations.
 */
public class Transaction implements Comparable<Transaction>, Encodable {
    private static final int marshalVersion = 1;

    private static ConcurrentHashSet<Transaction> activeTransactions = new ConcurrentHashSet<>();
    private final UUID transactionUuid;
    private final String transactionName;
    ConcurrentHashSet<UUID> stampsInTransaction = new ConcurrentHashSet<>();
    ConcurrentHashSet<Integer> componentsInTransaction = new ConcurrentHashSet<>();
    private long commitTime = Long.MAX_VALUE;

    /**
     * Constructs a new Transaction instance with the specified UUID and name.
     * The created transaction is added to the list of active transactions.
     *
     * @param transactionUuid the unique identifier for this transaction
     * @param transactionName the name associated with this transaction
     */
    private Transaction(UUID transactionUuid, String transactionName) {
        this.transactionName = transactionName;
        this.transactionUuid = transactionUuid;
        activeTransactions.add(this);
    }

    /**
     * Constructs a new Transaction instance with the specified UUID, name, and commit time.
     * The created transaction is added to the list of active transactions.
     *
     * @param transactionUuid the unique identifier for this transaction
     * @param transactionName the name associated with this transaction
     * @param commitTime the commit time associated with this transaction
     */
    private Transaction(UUID transactionUuid, String transactionName, long commitTime) {
        this(transactionUuid, transactionName);
        this.commitTime = commitTime;
    }

    /**
     * Constructs a new Transaction instance with the specified transaction name.
     * A unique identifier is automatically generated for the transaction.
     *
     * @param transactionName the name of the transaction
     */
    public Transaction(String transactionName) {
        this(UUID.randomUUID(), transactionName);
    }

    /**
     * Default constructor for the Transaction class.
     * Creates a new Transaction instance with a randomly generated unique identifier (UUID)
     * and an empty description.
     */
    public Transaction() {
        this(UUID.randomUUID(), "");
    }

    /**
     * Retrieves a transaction associated with the given stamp identifier.
     *
     * @param stampId the public identifier for the stamp, expected to contain a single UUID
     * @return an Optional containing the corresponding Transaction if found; an empty Optional otherwise
     * @throws IllegalStateException if the provided stampId contains more than one UUID
     */
    public static Optional<Transaction> forStamp(PublicId stampId) {
        if (stampId.asUuidArray().length > 1) {
            throw new IllegalStateException("Can only handle one UUID for stamp. Found: " + stampId);
        }
        return forStamp(stampId.asUuidArray()[0]);
    }

    /**
     * Finds an active transaction that contains the specified stamp UUID.
     *
     * @param stampUuid the UUID of the stamp to search for in active transactions
     * @return an Optional containing the transaction if found, or an empty Optional if no transaction contains the given stamp UUID
     */
    public static Optional<Transaction> forStamp(UUID stampUuid) {
        for (Transaction transaction : activeTransactions) {
            if (transaction.stampsInTransaction.contains(stampUuid)) {
                return Optional.of(transaction);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves the transaction associated with the given entity version, if any.
     *
     * @param version the entity version for which the associated transaction is to be determined
     * @return an Optional containing the associated transaction if found, otherwise an empty Optional
     */
    public static Optional<Transaction> forVersion(EntityVersion version) {
        StampEntity stamp = version.stamp();
        UUID[] stampUuids = stamp.asUuidArray();
        if (stampUuids.length > 1) {
            throw new IllegalStateException("Can only handle one UUID for stamp. Found: " + version);
        }
        for (Transaction transaction : activeTransactions) {
            if (transaction.stampsInTransaction.contains(stampUuids[0])) {
                if (transaction.componentsInTransaction.contains(version.nid())) {
                    return Optional.of(transaction);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Creates and returns a new instance of the Transaction class.
     *
     * @return a new Transaction object
     */
    public static Transaction make() {
        return new Transaction();
    }

    /**
     * Creates and returns a new Transaction object with the specified name.
     *
     * @param transactionName the name of the transaction to be created
     * @return a new Transaction object with the provided name
     */
    public static Transaction make(String transactionName) {
        return new Transaction(transactionName);
    }

    /**
     * Compares this transaction with the specified transaction for order.
     * Returns a negative integer, zero, or a positive integer as this transaction's UUID
     * is less than, equal to, or greater than the specified transaction's UUID.
     *
     * @param o the transaction to be compared.
     * @return a negative integer, zero, or a positive integer as this transaction's UUID
     *         is less than, equal to, or greater than the specified transaction's UUID.
     */
    @Override
    public int compareTo(Transaction o) {
        return this.transactionUuid.compareTo(o.transactionUuid);
    }

    /**
     * Removes a component associated with the specified entity
     * from the components in transaction.
     *
     * @param entity the entity whose associated component is to be removed
     */
    public void removeComponent(Entity entity) {
        componentsInTransaction.remove(entity.nid());
    }

    /**
     * Retrieves the unique identifier for the transaction.
     *
     * @return the UUID representing the transaction's unique identifier
     */
    public UUID transactionUuid() {
        return transactionUuid;
    }

    /**
     * Calculates and returns the total number of stamps in the current transaction.
     *
     * @return the count of stamps in the transaction.
     */
    public int stampsInTransactionCount() {
        return stampsInTransaction.size();
    }

    /**
     * Returns the count of components involved in the transaction.
     *
     * @return the number of components in the transaction
     */
    public int componentsInTransactionCount() {
        return componentsInTransaction.size();
    }

    /**
     * Retrieves a StampEntity using the specified state, time, author, module, and path.
     *
     * @param state the state for which the stamp is to be retrieved
     * @param time the time for which the stamp is to be retrieved
     * @param author the author associated with the stamp
     * @param module the module associated with the stamp
     * @param path the path associated with the stamp
     * @return The retrieved or newly created StampEntity.
     */
    public StampEntity getStamp(State state, long time, ConceptFacade author, ConceptFacade module, ConceptFacade path) {
        checkState(state, time, author == null, module == null, path == null);
        return getStamp(state, time, author.publicId(), module.publicId(), path.publicId());
    }

    /**
     * Validates the given state and its associated parameters.
     *
     * @param state       the state to validate; must not be null
     * @param time        a timestamp representing the time; must not be Long.MIN_VALUE
     * @param authorNull  flag indicating if the author is null; must be false
     * @param moduleNull  flag indicating if the module is null; must be false
     * @param pathNull    flag indicating if the path is null; must be false
     * @throws IllegalStateException if any of the conditions are not met
     */
    private void checkState(State state, long time, boolean authorNull, boolean moduleNull, boolean pathNull) {
        if (state == null) throw new IllegalStateException("State cannot be null...");
        if (time == Long.MIN_VALUE) throw new IllegalStateException("Time cannot be Long.MIN_VALUE...");
        if (authorNull) throw new IllegalStateException("Author cannot be null...");
        if (moduleNull) throw new IllegalStateException("Module cannot be null...");
        if (pathNull) throw new IllegalStateException("Path cannot be null...");
    }

    /**
     * Retrieves or creates a StampEntity using the provided state, timestamp, author, module, and path identifiers.
     * If a StampEntity corresponding to the given parameters does not exist, it creates and stores one.
     *
     * @param state The state associated with the stamp.
     * @param time The timestamp for the stamp in milliseconds since epoch.
     * @param authorId The public identifier of the author for the stamp.
     * @param moduleId The public identifier of the module for the stamp.
     * @param pathId The public identifier of the path for the stamp.
     * @return The retrieved or newly created StampEntity.
     */
    public StampEntity getStamp(State state, long time, PublicId authorId, PublicId moduleId, PublicId pathId) {
        checkState(state, time, authorId == null, moduleId == null, pathId == null);
        UUID stampUuid = UuidT5Generator.forTransaction(transactionUuid, state.publicId(), time, authorId, moduleId, pathId);
        stampsInTransaction.add(stampUuid);
        if (PrimitiveData.get().hasUuid(stampUuid)) {
            return Entity.getStamp(PrimitiveData.nid(stampUuid));
        }
        StampEntity stamp = StampRecord.make(stampUuid, state, time, authorId, moduleId, pathId);
        Entity.provider().putEntity(stamp);
        return stamp;
    }

    /**
     * Retrieves a StampEntity based on the provided state, author, module, and path.
     *
     * @param state The state of the stamp to retrieve.
     * @param author The concept representing the author of the stamp.
     * @param module The concept representing the module of the stamp.
     * @param path The concept representing the path of the stamp.
     * @return The retrieved or newly created StampEntity.
     */
    public StampEntity getStamp(State state, ConceptFacade author, ConceptFacade module, ConceptFacade path) {
        return getStamp(state, Long.MAX_VALUE, author.publicId(), module.publicId(), path.publicId());
    }

    /**
     * Retrieves a StampEntity based on the provided state, author, module, and path identifiers.
     *
     * @param state The state of the stamp being retrieved.
     * @param authorNid The identifier of the author associated with the stamp.
     * @param moduleNid The identifier of the module associated with the stamp.
     * @param pathNid The identifier of the path associated with the stamp.
     * @return The retrieved or newly created StampEntity.
     */
    public StampEntity getStamp(State state, int authorNid, int moduleNid, int pathNid) {
        return getStamp(state, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
    }

    /**
     * Retrieves or creates a {@code StampEntity} based on the given parameters.
     * Validates input arguments to ensure they are within defined constraints.
     * <p>
     * Time can be Long.MAX_VALUE, and will be set at commit time, or Time can be a
     * time in the past, and on commit, time is preserved. This strategy allows transactions to
     * work on import of historic content.
     * <p>
     * @param state the state of the stamp to retrieve; must not be null
     * @param time the timestamp associated with the stamp; must not be {@code Long.MIN_VALUE}
     * @param authorNid the identifier of the author; must not be zero
     * @param moduleNid the identifier of the module; must not be zero
     * @param pathNid the identifier of the path; must not be zero
     * @return a {@code StampEntity} that matches the provided parameters
     * @throws IllegalStateException if any of the provided parameters are invalid
     */
    public StampEntity getStamp(State state, long time, int authorNid, int moduleNid, int pathNid) {
        if (state == null) throw new IllegalStateException("State cannot be null...");
        if (time == Long.MIN_VALUE) throw new IllegalStateException("Time cannot be Long.MIN_VALUE...");
        if (authorNid == 0) throw new IllegalStateException("Author cannot be zero...");
        if (moduleNid == 0) throw new IllegalStateException("Module cannot be zero...");
        if (pathNid == 0) throw new IllegalStateException("Path cannot be zero...");
        return getStamp(state, time, PrimitiveData.publicId(authorNid), PrimitiveData.publicId(moduleNid), PrimitiveData.publicId(pathNid));
    }

    /**
     * Retrieves or creates a StampEntity for the specified state, author, module, and path,
     * while associating it with components provided through the given entities.
     *
     * @param state        the state for the stamp
     * @param authorNid    the author identifier
     * @param moduleNid    the module identifier
     * @param pathNid      the path identifier
     * @param firstEntity  the primary entity to associate with the stamp
     * @param extraEntities additional entities to associate with the stamp
     * @return a StampEntity representing the combination of the specified parameters
     */
    public StampEntity getStampForEntities(State state, int authorNid, int moduleNid, int pathNid, EntityFacade firstEntity, EntityFacade... extraEntities) {
        StampEntity stampEntity = getStamp(state, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
        addComponent(firstEntity);
        for (EntityFacade entityFacade : extraEntities) {
            addComponent(entityFacade);
        }
        return stampEntity;
    }

    /**
     * Adds a component to the transaction by its entity's nid (numeric identifier).
     * Throws an IllegalStateException if the entity's nid is 0.
     *
     * @param entity the EntityFacade object whose nid is to be added to the transaction
     * @throws IllegalStateException if the nid of the provided entity is 0
     */
    public void addComponent(EntityFacade entity) {
        if (entity == null) {
            return; // Allow null for new entities that don't exist yet
        }
        if (entity.nid() == 0) {
            throw new IllegalStateException("Entity nid cannot = 0: " + entity);
        }
        componentsInTransaction.add(entity.nid());
    }

    /**
     * Adds a component identified by its unique integer identifier (nid) to the current transaction.
     * Throws an {@code IllegalStateException} if the provided nid is zero, as zero is considered invalid.
     *
     * @param entityNid the unique nid of the component to add to the transaction
     *                  (must not be zero).
     * @throws IllegalStateException if the entity nid is zero.
     */
    public void addComponent(int entityNid) {
        if (entityNid == 0) {
            throw new IllegalStateException("Entity nid cannot = 0. ");
        }
        componentsInTransaction.add(entityNid);
    }

    public long commitTime() {
        return this.commitTime;
    }

    /**
     * Commits the current transaction by finalizing all stamps included in it, recording the
     * commit timestamp, and notifying the system of the changes.
     * <p>
     * This method processes each stamp in the transaction by invoking the `commitStamp` method
     * with the appropriate commit time. It also updates the list of active transactions by
     * removing the committed transaction and triggers a notification to indicate that a refresh
     * is required.
     *
     * @return the total number of stamps that were finalized and committed.
     */
    public int commit() {
        AtomicInteger stampCount = new AtomicInteger();
        this.commitTime = System.currentTimeMillis();
        activeTransactions.remove(this);
        forEachStampInTransaction(stampUuid -> {
            commitStamp(stampUuid, this.commitTime);
            stampCount.incrementAndGet();
        });
        Entity.provider().notifyRefreshRequired(this);
        return stampCount.get();
    }

    public void forEachStampInTransaction(Consumer<? super UUID> action) {
        stampsInTransaction.forEach(action);
    }

    /**
     * Finalizes a stamp by committing it with the provided commit time. If the stamp's
     * current time is not set (represented by Long.MAX_VALUE), a new version of the stamp
     * is created with the commit time. This method ensures that the stamp's state and its
     * associated metadata are appropriately updated and stored.
     *
     * @param stampUuid   The unique identifier of the stamp being committed.
     * @param commitTime  The timestamp to be associated with the stamp upon commitment.
     */
    private void commitStamp(UUID stampUuid, long commitTime) {
        int stampNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern().publicId())
                .call(() -> PrimitiveData.nid(stampUuid));
        StampRecord stampEntity = Entity.getStamp(stampNid);
        StampEntityVersion stampVersion = stampEntity.lastVersion();
        if (stampVersion.time() == Long.MAX_VALUE) {
            StampAnalogueBuilder newStampBuilder = stampEntity.analogueBuilder();
            newStampBuilder.add(new StampVersionRecord(newStampBuilder.analogue(),
                    stampVersion.stateNid(),
                    commitTime, stampVersion.authorNid(), stampVersion.moduleNid(), stampVersion.pathNid()));
            StampRecord newStamp = newStampBuilder.build();
            Entity.provider().putEntity(newStamp);
        } else {
            // Transaction will retain current time of stamp. Used when importing with existing time.
        }
        //TODO support nested transactions
//        for (TransactionImpl childTransaction : transaction.getChildren()) {
//            processTransaction(uncommittedStamp, stampSequence, childTransaction);
//        }
    }

    /**
     * Applies the provided action to each component in the transaction. This method
     * processes all components that are part of the current transaction by passing
     * them to the given consumer action.
     *
     * @param action the {@code Consumer} to be applied to each component in the transaction.
     *               The action consumes an integer, which represents a component nid.
     */
    public void forEachComponentInTransaction(Consumer<? super Integer> action) {
        componentsInTransaction.forEach(action);
    }

    /**
     * Cancels all stamps associated with the transaction by setting their state
     * to "CANCELED." This operation updates each stamp to an analogue version
     * with the canceled state and notifies the entity provider that a refresh
     * is required. The transaction is also removed from the list of active transactions.
     *
     * @return the number of stamps that were processed during the cancellation.
     */
    public int cancel() {
        AtomicInteger stampCount = new AtomicInteger();
        forEachStampInTransaction(stampUuid -> {
            int stampNid = ScopedValue
                    .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern().publicId())
                    .call(() -> PrimitiveData.nid(stampUuid));
            StampRecord stampEntity = Entity.getStamp(stampNid);
            StampEntityVersion stampVersion = stampEntity.lastVersion();
            if (stampVersion.time() == Long.MIN_VALUE) {
                // already canceled.
            } else {
                StampAnalogueBuilder newStampBuilder = stampEntity.analogueBuilder();
                newStampBuilder.add(new StampVersionRecord(newStampBuilder.analogue(),
                        State.CANCELED.nid(),
                        Long.MIN_VALUE, stampVersion.authorNid(), stampVersion.moduleNid(), stampVersion.pathNid()));
                StampRecord newStamp = newStampBuilder.build();
                Entity.provider().putEntity(newStamp);
            }
            stampCount.incrementAndGet();
        });
        //TODO support nested transactions
//        for (TransactionImpl childTransaction : transaction.getChildren()) {
//            processTransaction(uncommittedStamp, stampSequence, childTransaction);
//        }
        activeTransactions.remove(this);
        Entity.provider().notifyRefreshRequired(this);
        return stampCount.get();
    }

    /**
     * Decodes a serialized Transaction object from the provided {@code DecoderInput}.
     * The method reads the transaction's data, including its marshal version, UUID, name,
     * and associated stamps and components, and reconstructs a Transaction instance
     * based on the encoded information.
     *
     * @param in the {@code DecoderInput} to read the serialized transaction data
     *           including the marshalled version, transaction UUID, name,
     *           and lists of stamps and components within the transaction
     * @return the reconstructed {@code Transaction} object based on the input stream
     * @throws UnsupportedOperationException if the marshalled version is unsupported
     */
    @Decoder
    public static Transaction decode(DecoderInput in) {
        int objectMarshalVersion = in.readInt();
        return switch (objectMarshalVersion) {
            case marshalVersion -> {
                Transaction transaction = new Transaction(in.readUuid(), in.readString(), in.readLong());
                int stampsInTransactionCount = in.readInt();
                for (int i = 0; i < stampsInTransactionCount; i++) {
                    transaction.stampsInTransaction.add(in.readUuid());
                }
                int componentsInTransactionCount = in.readInt();
                for (int i = 0; i < componentsInTransactionCount; i++) {
                    transaction.componentsInTransaction.add(in.readInt());
                }
                yield transaction;
            }
            default -> throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        };
    }

    /**
     * Encodes the current transaction data to the provided {@link EncoderOutput}.
     *
     * @param out the {@link EncoderOutput} to write the encoded data to
     *            including the transaction details such as marshal version,
     *            transaction UUID, transaction name, commit time, and the list
     *            of stamps and components involved in the transaction.
     */
    @Encoder
    @Override
    public void encode(EncoderOutput out) {
        out.writeInt(marshalVersion);
        out.writeUuid(this.transactionUuid);
        out.writeString(this.transactionName);
        out.writeLong(commitTime);
        out.writeInt(stampsInTransaction.size());
        for (UUID stampUuid : stampsInTransaction) {
            out.writeUuid(stampUuid);
        }
        out.writeInt(componentsInTransaction.size());
        for (int componentNid : componentsInTransaction) {
            out.writeInt(componentNid);
        }
    }

    /**
     * Saves the current active transactions to a file within the configured data store root directory.
     * <p>
     * This method retrieves the root directory from {@link ServiceProperties} using the
     * {@link ServiceKeys#DATA_STORE_ROOT} key. If the root directory is configured, it creates
     * or overwrites a file named "transactions.encoded" in this directory. The method then
     * serializes the transactions from the `activeTransactions` collection, encoding their data
     * and writing each transaction to the file.
     * <p>
     * Preconditions:
     * <p> - The `ServiceKeys.DATA_STORE_ROOT` must be properly configured and accessible.
     * <p> - The `activeTransactions` collection must contain valid transaction objects.
     * <p>
     * Postconditions:
     * <p> - A file named "transactions.encoded" will be created or overwritten in the configured root directory.
     * <p> - The file will contain serialized data for all currently active transactions.
     * <p>
     * Exceptions:
     * <p> - Throws {@link RuntimeException} if an I/O error or unexpected encoding issue occurs during the process.
     * <p>
     * Dependencies:
     * <p> - Relies on the configuration provided by {@link ServiceProperties} for determining the data store root directory.
     * <p> - Each transaction within the `activeTransactions` collection is expected to implement the `encode` method
     *   for proper serialization.
     */
    public static void save() {
        Optional<File> configuredRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (configuredRoot.isPresent()) {
            File transactionFile = new File(configuredRoot.get(), "transactions.encoded");
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(transactionFile))) {
                outputStream.writeInt(activeTransactions.size());
                for (Transaction transaction : activeTransactions) {
                    ByteBuf buf = ByteBufPool.allocate(1024);
                    EncoderOutput encoder = new EncoderOutput(buf);
                    transaction.encode(encoder);
                    byte[] bytes = buf.array();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Restores previously saved transactions from a specified file in the data store root directory.
     * <p>
     * This method retrieves the root directory from the service properties and looks for a file named
     * "transactions.encoded". If the file exists, it reads the transaction data, decodes each transaction,
     * and populates the list of active transactions.
     * <p>
     * Any errors during the file reading process are logged, and an {@link UncheckedIOException}
     * is thrown with detailed information about the failure.
     * <p>
     * <p> Preconditions:
     * <p> - The "transactions.encoded" file must be present in the configured data store root directory.
     * <p> - The file format must match the expected encoding of transaction data.
     * <p>
     * Postconditions:
     * <p> - Decoded transactions are added to the active transactions list.
     * <p>
     * Exceptions:
     * <p> - Throws {@link UncheckedIOException} if file reading or parsing encounters an I/O error.
     * <p>
     * Dependencies:
     * <p> - The method depends on the configuration provided by {@link ServiceProperties}.
     * <p> - The transactions are decoded using the {@link Transaction#decode(DecoderInput)} method.
     */
    public static void restore() {
        // Retrieve the configured root directory from the service properties
        Optional<File> configuredRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);

        if (configuredRoot.isPresent()) {
            File transactionFile = new File(configuredRoot.get(), "transactions.encoded");
            if (transactionFile.exists()) {
                try (DataInputStream dis = new DataInputStream(new FileInputStream(configuredRoot.get()))) {
                    int transactionCount = dis.readInt();
                    byte[] data = new byte[(int) transactionFile.length() - 4];
                    dis.readFully(data);
                    DecoderInput decoder = new DecoderInput(data);
                    for (int i = 0; i < transactionCount; i++) {
                        Transaction transaction = Transaction.decode(decoder);
                        activeTransactions.add(transaction);
                    }
                } catch (IOException e) {
                    // Handle file reading errors and log the exception
                    throw new UncheckedIOException("Failed to restore transactions from file: " + transactionFile, e);
                }
            }

        }

    }
}
