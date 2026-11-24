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
package dev.ikm.komet.kview.storage;

import dev.ikm.komet.actions.KometAction;
import dev.ikm.komet.actions.KometActionInput;
import dev.ikm.komet.actions.KometActionResult;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.layout.storage.KlStorage;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.boundary.DataKind;
import dev.ikm.tinkar.entity.scope.ScopeOperations;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.KernelConcept;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent.GLOBAL_REFRESH;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;

/**
 * The {@code Import from file} action: reads a file {@link ExportToFile} wrote into this knowledge
 * base (IKE-Network/ike-issues#1313, step 2 of {@code plan-2026-11-22-remove-export-import-data}).
 *
 * <p>The input takes the {@code File import input pattern}: its subject is the Local module, the
 * parent the data is kept under, and its fields are the file and whether to replace a copy this
 * knowledge base has. Nothing is read in unless {@link ScopeOperations#check} allows it, and the
 * data is written to the knowledge base's own change sets too, so a rebuild keeps it.
 */
public final class ImportFromFile implements KometAction {

    private static final Logger LOG = LoggerFactory.getLogger(ImportFromFile.class);

    /** For the service loader. */
    public ImportFromFile() { }

    @Override
    public ConceptFacade concept() {
        return KometConcept.IMPORT_FROM_FILE;
    }

    /**
     * @param file    the file to read
     * @param replace whether to remove the copy this knowledge base has, if it has one
     * @return a new input asking for the import
     */
    public static KometActionInput input(Path file, boolean replace) {
        return new KometActionInput(PublicIds.newRandom(), KometConcept.IMPORT_FROM_FILE, KernelConcept.LOCAL_MODULE,
                Lists.immutable.of(file.toString(), replace));
    }

    /**
     * Whether a file can be imported, reading nothing in.
     *
     * @param file an exported file
     * @throws ScopeOperations.AlreadyHereException when it can be once the copy here is replaced
     * @throws ScopeOperations.ImportRefused        why it cannot be
     */
    static ScopeOperations.Manifest check(File file) {
        ScopeOperations.Manifest manifest = ScopeOperations.manifest(file);
        if (manifest.kind() != null && manifest.kind() != DataKind.KNOWLEDGE && manifest.kind() != DataKind.LOCAL) {
            throw new NotLocal(manifest.kind());
        }
        ScopeOperations.check(manifest);
        return manifest;
    }

    /** The file has data of another kind than the one these gestures are for. */
    private static final class NotLocal extends IllegalArgumentException {
        NotLocal(DataKind kind) {
            super(kind == DataKind.SHARED ? "It has shared data, which comes with a sync of the knowledge base."
                    : "It has data saved in a file. Open the file instead.");
        }
    }

    @Override
    public KometActionResult perform(KometActionInput input, ViewCalculator view, EditCoordinate edit) {
        if (input.fields().size() != 2 || !(input.fields().get(0) instanceof String location) || location.isBlank()
                || !(input.fields().get(1) instanceof Boolean replace)) {
            return KometActionResult.refused(input, "Importing takes two fields, the file to read and whether to replace a copy already here.");
        }
        File file = Path.of(location).toFile();
        if (!file.isFile()) {
            return KometActionResult.refused(input, "There is no file " + file + ".");
        }
        if (!ScopeOperations.exportedScope(file)) {
            return KometActionResult.refused(input, file.getName() + " is not a file that Export to File wrote.");
        }
        try {
            ScopeOperations.Manifest manifest;
            try {
                manifest = check(file);
            } catch (ScopeOperations.AlreadyHereException here) {
                if (!replace) {
                    return KometActionResult.refused(input, "Not imported. " + here.getMessage());
                }
                manifest = ScopeOperations.manifest(file);
            }
            ScopeOperations.Imported imported = ScopeOperations.importScope(file, replace);
            // The calculators first, so what shows it reads it with what was just loaded.
            EvtBusFactory.getDefaultEvtBus().publish(CALCULATOR_CACHE_TOPIC, new RefreshCalculatorCacheEvent(this, GLOBAL_REFRESH));
            String what = KlStorage.imported(imported, manifest.entries()).orElse(file.getName());
            return new KometActionResult(input.id(), KometConcept.ACTION_COMPLETED, Optional.empty(), (imported.replaced() ? "Replaced " : "Imported ")
                    + what + ", " + StorageGestures.size(imported.counts()) + ".");
        } catch (ScopeOperations.ImportRefused | IllegalArgumentException refused) {
            return KometActionResult.refused(input, "Not imported. " + refused.getMessage());
        } catch (RuntimeException e) {
            LOG.warn("Importing {} failed", file, e);
            return KometActionResult.failed(input, "Could not import " + file.getName() + ": " + e.getMessage());
        }
    }
}
