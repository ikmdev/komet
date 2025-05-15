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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * A task for synchronizing medical terminology data between a local repository and remote source.
 * <p>
 * This class handles the complete synchronization workflow including:
 * <ul>
 *   <li>Environment validation</li>
 *   <li>Pulling changes from remote repository</li>
 *   <li>Loading changesets from files</li>
 *   <li>Running the reasoning engine to infer relationships</li>
 *   <li>Optionally pushing changes back to the remote repository</li>
 * </ul>
 *
 * The task provides detailed progress tracking through each phase of the process.
 */
public class SyncTask extends TrackingCallable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SyncTask.class);

    /**
     * The total amount of work units for progress tracking.
     */
    private static final double TOTAL_WORK = 100.0;

    /**
     * Represents the different phases of the sync process with their
     * associated progress boundaries.
     */
    private enum SyncPhase {
        VALIDATION(0.0, 5.0),
        PULL(5.0, 25.0),
        LOAD(25.0, 50.0),
        REASONER(50.0, 80.0),
        PUSH(80.0, 100.0);

        private final double startPercentage;
        private final double endPercentage;

        SyncPhase(double startPercentage, double endPercentage) {
            this.startPercentage = startPercentage;
            this.endPercentage = endPercentage;
        }

        public double getStartPercentage() {
            return startPercentage;
        }

        public double getEndPercentage() {
            return endPercentage;
        }
    }

    // The path to the folder containing changesets.
    private final Path changeSetFolder;

    // Flag indicating whether changes should be pushed back to the remote repository.
    private final boolean push;

    /**
     * Creates a new synchronization task.
     * <p>
     * Initializes the task with the specified changeset folder and push flag,
     * and sets up initial progress tracking.
     *
     * @param changeSetFolder The folder containing changesets to synchronize
     * @param push Whether changes should be pushed back to the remote repository
     */
    public SyncTask(Path changeSetFolder, boolean push) {
        this.changeSetFolder = changeSetFolder;
        this.push = push;
        updateTitle("Exchange %s in progress...".formatted(push ? "Sync" : "Pull"));
        updateProgress(0, TOTAL_WORK);
    }

    /**
     * Executes the sync task, performing all phases of the synchronization process.
     * <p>
     * The process follows these phases in order:
     * <ol>
     *   <li>Environment validation (0-5%)</li>
     *   <li>Pull changes from remote (5-25%)</li>
     *   <li>Load changesets (25-50%)</li>
     *   <li>Run reasoner (50-80%)</li>
     *   <li>Push changes (if enabled) (80-100%)</li>
     * </ol>
     *
     * @return Null as this callable doesn't return a value
     * @throws Exception If any errors occur during the synchronization process
     */
    @Override
    protected Void compute() throws Exception {
        updateMessage("Validating environment...");
        // Start at the beginning
        updateProgress(0, TOTAL_WORK);

        if (!validateEnvironment()) {
            // Complete validation phase even if it fails
            updatePhaseProgress(SyncPhase.VALIDATION.getStartPercentage(),
                    SyncPhase.VALIDATION.getEndPercentage(), 1.0);
            cancel();
            return null;
        }

        // Validation complete
        updatePhaseProgress(SyncPhase.VALIDATION.getStartPercentage(),
                SyncPhase.VALIDATION.getEndPercentage(), 1.0);

        try {
            updateMessage("Starting %s process.".formatted(push ? "synchronization" : "pulling"));

            // Pull phase
            pull();

            // Load changesets phase
            loadChangesets();

            // Run reasoner phase
            runReasoner();

            // Push phase (if enabled)
            push();

            // Ensure we reach 100% at the end
            updateProgress(TOTAL_WORK, TOTAL_WORK);
            updateMessage("%s completed successfully.".formatted(push ? "Synchronization" : "Pulling"));
        } catch (Exception ex) {
            // Ensure progress is updated even when an error occurs
            updateProgress(TOTAL_WORK, TOTAL_WORK);
            updateMessage("%s failed: ".formatted(push ? "Synchronization" : "Pulling") + ex.getLocalizedMessage());
            LOG.error("{} failed", (push ? "Synchronization" : "Pulling"), ex);
            throw ex;
        }
        return null;
    }

    /**
     * Sets the current progress within a phase range.
     * <p>
     * This method calculates the absolute progress percentage based on the current
     * phase's boundaries and the relative progress within that phase.
     *
     * @param phaseStart start percentage of the phase
     * @param phaseEnd end percentage of the phase
     * @param phaseProgress fractional progress within phase (0.0 to 1.0)
     */
    private void updatePhaseProgress(double phaseStart, double phaseEnd, double phaseProgress) {
        // Ensure phaseProgress is between 0 and 1
        phaseProgress = Math.max(0.0, Math.min(1.0, phaseProgress));

        // Calculate progress within this phase
        double phaseWidth = phaseEnd - phaseStart;
        double currentProgress = phaseStart + (phaseWidth * phaseProgress);

        // Update the progress tracker
        updateProgress(currentProgress, TOTAL_WORK);
    }

    /**
     * Identifies files in a directory matching the specified pattern that are valid changesets.
     * <p>
     * A valid changeset must have a META-INF/MANIFEST.MF file within its zip structure.
     *
     * @param directory The directory to search for files
     * @param pattern The file extension pattern to match
     * @return An immutable list of relative paths to valid changeset files
     */
    ImmutableList<String> filesToAdd(Path directory, String pattern) {
        try (Stream<Path> filesStream = Files.walk(directory)) {
            return Lists.immutable.ofAll(filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(pattern))
                    .filter(this::isValidChangeset)
                    .map(path -> directory.relativize(path).toString())
                    .toList());
        } catch (IOException e) {
            LOG.error("Error searching for files", e);
            return Lists.immutable.empty();
        }
    }

    /**
     * Validates whether a file is a valid changeset archive.
     * <p>
     * A valid changeset must contain a META-INF/MANIFEST.MF file.
     *
     * @param file The path to the file to check
     * @return true if the file is a valid changeset, false otherwise
     */
    private boolean isValidChangeset(Path file) {
        try (FileSystem fs = FileSystems.newFileSystem(file)) {
            return Files.exists(fs.getPath("META-INF", "MANIFEST.MF"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Pulls the latest changes from the remote Git repository.
     * <p>
     * This method connects to the remote repository, authenticates using GitHub credentials,
     * and downloads any new changes. Progress is reported between VALIDATION_PHASE and
     * PULL_PHASE boundaries.
     */
    private void pull() {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }
        // Starting the pull phase
        updatePhaseProgress(SyncPhase.VALIDATION.getEndPercentage(),
                SyncPhase.PULL.getEndPercentage(), 0.0);

        try (Git git = Git.open(changeSetFolder.toFile())) {
            updateMessage("Connecting to remote repository...");
            updatePhaseProgress(SyncPhase.VALIDATION.getEndPercentage(),
                    SyncPhase.PULL.getEndPercentage(), 0.2);

            // Create a progress monitor for Git operations
            GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,                   // TrackingCallable
                    SyncPhase.VALIDATION.getEndPercentage(),       // Range start (%)
                    SyncPhase.PULL.getEndPercentage(),             // Range end (%)
                    TOTAL_WORK);            // Total work units

            PullCommand pullCommand = git.pull();
            pullCommand.setProgressMonitor(progressMonitor);
            pullCommand.setRemoteBranchName("main");
            pullCommand.setCredentialsProvider(new GitHubCredentialsProvider());

            updateMessage("Pulling changes from remote...");
            updatePhaseProgress(SyncPhase.VALIDATION.getEndPercentage(),
                    SyncPhase.PULL.getEndPercentage(), 0.3);

            PullResult pullResult = pullCommand.call();

            if (pullResult.isSuccessful()) {
                updateMessage("Pull operation completed successfully.");
                updatePhaseProgress(SyncPhase.VALIDATION.getEndPercentage(),
                        SyncPhase.PULL.getEndPercentage(), 1.0);
            } else {
                handlePhaseError("Pull", new Exception(pullResult.getMergeResult().getMergeStatus().toString()),
                        SyncPhase.VALIDATION.getEndPercentage(),
                        SyncPhase.PULL.getEndPercentage());
            }
        } catch (Exception ex) {
            handlePhaseError("Pull", ex, SyncPhase.VALIDATION.getEndPercentage(),
                    SyncPhase.PULL.getEndPercentage());
        }
    }

    /**
     * Loads changesets from files in the changeset folder.
     * <p>
     * This method identifies valid changeset files, loads them into the system,
     * and updates progress throughout the loading phase.
     */
    private void loadChangesets() {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updatePhaseProgress(SyncPhase.PULL.getEndPercentage(),
                SyncPhase.LOAD.getEndPercentage(), 0.0);
        MutableList<EntityCountSummary> loadResults = Lists.mutable.empty();

        try {
            File[] pbFiles = changeSetFolder.toFile().listFiles((dir, name) -> name.endsWith("ike-cs.zip"));
            if (pbFiles == null || pbFiles.length == 0) {
                updateMessage("No changeset files found to load.");
                updatePhaseProgress(SyncPhase.PULL.getEndPercentage(),
                        SyncPhase.LOAD.getEndPercentage(), 1.0);
                return;
            }

            int total = pbFiles.length;
            int current = 0;

            for (File file : pbFiles) {
                current++;
                updateMessage("Loading changeset " + current + " of " + total + ": " + file.getName());
                // Update progress based on current file's position in the total
                double loadProgress = (double) current / total;
                updatePhaseProgress(SyncPhase.PULL.getEndPercentage(),
                        SyncPhase.LOAD.getEndPercentage(), loadProgress * 0.9); // Reserve 10% for completion

                try {
                    if (isValidChangeset(file.toPath())) {
                        EntityCountSummary ecs = new LoadEntitiesFromProtobufFile(file).compute();
                        loadResults.add(ecs);
                        LOG.info("Loaded changeset: {}", file.getName());
                    }
                } catch (Exception ex) {
                    LOG.error("Failed to load changeset: {}", file.getName(), ex);
                }
            }

            updateMessage("Successfully loaded " + loadResults.size() + " of " + total + " changesets.");
            updatePhaseProgress(SyncPhase.PULL.getEndPercentage(),
                    SyncPhase.LOAD.getEndPercentage(), 1.0);
        } catch (Exception e) {
            handlePhaseError("Loading changesets", e, SyncPhase.PULL.getEndPercentage(),
                    SyncPhase.LOAD.getEndPercentage());
        }
    }

    /**
     * Runs the reasoning process on the loaded data.
     * <p>
     * This method loads reasoner services, processes the data with them,
     * and updates progress throughout the reasoning phase.
     */
    private void runReasoner() {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updateMessage("Starting reasoning process...");
        updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                SyncPhase.REASONER.getEndPercentage(), 0.0);

        ImmutableList<ReasonerService> reasoners = loadReasonerServices();
        processWithReasoners(reasoners);

        updateMessage("Reasoning process completed successfully.");
        updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                SyncPhase.REASONER.getEndPercentage(), 1.0);
    }

    /**
     * Loads available reasoner services of the specified type.
     *
     * @return A list of available reasoner services
     */
    private ImmutableList<ReasonerService> loadReasonerServices() {
        String reasonerType = "ElkSnomedReasoner";
        ImmutableList<ReasonerService> reasoners = Lists.immutable
                .ofAll(PluggableService.load(ReasonerService.class).stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(reasoner -> reasoner.getName().contains(reasonerType))
                        .sorted(Comparator.comparing(ReasonerService::getName)).toList());

        LOG.info("Number of reasoners {}", reasoners.size());
        updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                SyncPhase.REASONER.getEndPercentage(), 0.1);

        return reasoners;
    }

    /**
     * Processes data with the available reasoners.
     *
     * @param reasoners The list of reasoners to use
     */
    private void processWithReasoners(ImmutableList<ReasonerService> reasoners) {
        MutableList<ClassifierResults> resultList = Lists.mutable.empty();
        int reasonerCount = reasoners.size();

        for (int i = 0; i < reasonerCount; i++) {
            ReasonerService rs = reasoners.get(i);
            LOG.info("Reasoner service: {}", rs);

            // Each reasoner gets an equal portion of the reasoning phase
            double reasonerStartPercent = 0.1 + ((0.9 / reasonerCount) * i);
            double reasonerEndPercent = 0.1 + ((0.9 / reasonerCount) * (i + 1));

            rs.init(Calculators.View.Default(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);

            // Use this task as the progress updater
            rs.setProgressUpdater(this);

            try {
                // Extract
                updateMessage("Extracting data for reasoning...");
                rs.extractData();
                updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                        SyncPhase.REASONER.getEndPercentage(),
                        reasonerStartPercent + (reasonerEndPercent - reasonerStartPercent) * 0.25);

                // Load
                updateMessage("Loading data for reasoning...");
                rs.loadData();
                updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                        SyncPhase.REASONER.getEndPercentage(),
                        reasonerStartPercent + (reasonerEndPercent - reasonerStartPercent) * 0.5);

                // Compute
                updateMessage("Computing inferences...");
                rs.computeInferences();
                updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                        SyncPhase.REASONER.getEndPercentage(),
                        reasonerStartPercent + (reasonerEndPercent - reasonerStartPercent) * 0.75);

                // Build NNF
                updateMessage("Building necessary normal form...");
                rs.buildNecessaryNormalForm();
                updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                        SyncPhase.REASONER.getEndPercentage(),
                        reasonerStartPercent + (reasonerEndPercent - reasonerStartPercent) * 0.9);

                // Write inferred results
                updateMessage("Writing inferred results...");
                ClassifierResults results = rs.writeInferredResults();
                updatePhaseProgress(SyncPhase.LOAD.getEndPercentage(),
                        SyncPhase.REASONER.getEndPercentage(),
                        reasonerEndPercent);

                LOG.info("After Size of ConceptSet: {}", rs.getReasonerConceptSet().size());
                LOG.info("ClassifierResults: inferred changes size {}", results.getConceptsWithInferredChanges().size());
                LOG.info("ClassifierResults: navigation changes size {}", results.getConceptsWithNavigationChanges().size());
                LOG.info("ClassifierResults: classificationconcept size {}", results.getClassificationConceptSet().size());
                resultList.add(results);
            } catch (Exception ex) {
                LOG.error("Failed to load data for reasoning", ex);
            }
        }
    }

    /**
     * Pushes changes to the remote Git repository if enabled.
     * <p>
     * This method saves the current state, adds changed files to Git,
     * commits them, and pushes to the remote repository with appropriate
     * authentication.
     */
    private void push() {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                SyncPhase.PUSH.getEndPercentage(), 0.0);

        if (!push) {
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 1.0);
            return;
        }

        try {
            updateMessage("Saving current change set...");
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 0.2);

            if (!saveCurrentChangeSet()) {
                updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                        SyncPhase.PUSH.getEndPercentage(), 1.0);
                return;
            }

            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 0.4);

            pushToRemoteRepository();
        } catch (Exception ex) {
            handlePhaseError("Push", ex, SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage());
        }
    }

    /**
     * Saves the current change set using the ChangeSetWriterService.
     *
     * @return true if the save operation was successful, false otherwise
     */
    private boolean saveCurrentChangeSet() {
        ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
        if (changeSetWriterService == null) {
            updateMessage("No ChangeSetWriterService available. Cannot save changes.");
            LOG.error("No ChangeSetWriterService found, cannot save changes");
            return false;
        }

        if (changeSetWriterService instanceof SaveState savableChangeSetWriterService) {
            savableChangeSetWriterService.save();
        }
        return true;
    }

    /**
     * Pushes changes to the remote Git repository.
     * <p>
     * This method adds files to Git, commits them, and pushes to the remote
     * repository with appropriate authentication.
     *
     * @throws Exception if any Git operations fail
     */
    private void pushToRemoteRepository() throws Exception {
        try (Git git = Git.open(changeSetFolder.toFile())) {
            ImmutableList<String> filesToAdd = filesToAdd(changeSetFolder, "ike-cs.zip");

            if (filesToAdd.isEmpty()) {
                LOG.info("No changeset files found to add");
                updateMessage("No changeset files found to push.");
                updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                        SyncPhase.PUSH.getEndPercentage(), 1.0);
                return;
            }

            // Create a more descriptive commit message
            String commitMessage = "Added " + filesToAdd.size() + " changesets on " + new Date();

            // Add files to staging
            updateMessage("Adding files to Git staging area...");
            AddCommand addCommand = git.add();
            addCommand.setUpdate(false);
            filesToAdd.forEach(addCommand::addFilepattern);
            addCommand.call();
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 0.6);

            // Commit changes
            updateMessage("Committing changes...");
            CommitCommand commitCommand = git.commit();
            commitCommand.setMessage(commitMessage);
            commitCommand.setAll(true);
            commitCommand.call();
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 0.7);

            // Push changes
            updateMessage("Pushing changes to remote repository...");
            PushCommand pushCommand = git.push();

            // Set a progress monitor for Git operations
            GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,
                    SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(),
                    TOTAL_WORK);

            pushCommand.setProgressMonitor(progressMonitor);
            pushCommand.setCredentialsProvider(new GitHubCredentialsProvider());
            pushCommand.call();
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 0.95);

            updateMessage("Changes successfully pushed to remote repository.");
            updatePhaseProgress(SyncPhase.REASONER.getEndPercentage(),
                    SyncPhase.PUSH.getEndPercentage(), 1.0);
        }
    }

    /**
     * Validates that the environment is properly set up for the sync task.
     * <p>
     * Checks that the changeset folder exists, is a directory, and is a valid
     * Git repository.
     *
     * @return true if the environment is valid, false otherwise
     */
    private boolean validateEnvironment() {
        if (!Files.exists(changeSetFolder)) {
            updateMessage("Error: Changeset folder does not exist: " + changeSetFolder);
            LOG.error("Changeset folder does not exist: {}", changeSetFolder);
            return false;
        }

        if (!Files.isDirectory(changeSetFolder)) {
            updateMessage("Error: Changeset path is not a directory: " + changeSetFolder);
            LOG.error("Changeset path is not a directory: {}", changeSetFolder);
            return false;
        }

        File gitDir = new File(changeSetFolder.toFile(), ".git");
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            updateMessage("Error: Not a Git repository: " + changeSetFolder);
            LOG.error("Not a Git repository: {}", changeSetFolder);
            return false;
        }

        return true;
    }

    /**
     * Handles phase errors consistently.
     *
     * @param phaseName The name of the phase that encountered an error
     * @param ex The exception that occurred
     * @param phaseStart The starting percentage of the phase
     * @param phaseEnd The ending percentage of the phase
     */
    private void handlePhaseError(String phaseName, Exception ex, double phaseStart, double phaseEnd) {
        String errorMessage = phaseName + " failed: " + ex.getLocalizedMessage();
        LOG.error(errorMessage, ex);
        updateMessage(errorMessage);
        updatePhaseProgress(phaseStart, phaseEnd, 1.0);
        cancel();
    }
}