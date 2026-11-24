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
import dev.ikm.komet.layout.storage.KlStorage;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.boundary.DataKind;
import dev.ikm.tinkar.entity.boundary.EntityBoundaries;
import dev.ikm.tinkar.entity.scope.Scope;
import dev.ikm.tinkar.entity.scope.ScopeOperations;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * The {@code Export to file} action: writes a group of local data that is not saved in a file, such
 * as a conversation with the assistant, to one file that {@link ImportFromFile} reads into another
 * knowledge base (IKE-Network/ike-issues#1313, step 2 of {@code plan-2026-11-22-remove-export-import-data}).
 *
 * <p>The input takes the {@code File export input pattern}: its subject is any component the group
 * is found by, such as a journal's anchor, and its one field is the file. Nothing in the knowledge
 * base changes. The file is written whole or not at all.
 *
 * <p>Refused: data that is not local, data saved in a file, whose file is its record, an edit not
 * yet committed, and a reference to other shared or local data, which the file would not have.
 */
public final class ExportToFile implements KometAction {

    /** For the service loader. */
    public ExportToFile() { }

    @Override
    public ConceptFacade concept() {
        return KometConcept.EXPORT_TO_FILE;
    }

    /**
     * @param subject a component the group is found by
     * @param file    the file to write
     * @return a new input asking for the export
     */
    public static KometActionInput input(PublicId subject, Path file) {
        return new KometActionInput(PublicIds.newRandom(), KometConcept.EXPORT_TO_FILE, EntityProxy.make("data", subject),
                Lists.immutable.of(file.toString()));
    }

    @Override
    public KometActionResult perform(KometActionInput input, ViewCalculator view, EditCoordinate edit) {
        if (input.fields().size() != 1 || !(input.fields().getFirst() instanceof String location) || location.isBlank()) {
            return KometActionResult.refused(input, "Exporting takes one field, the file to write.");
        }
        PublicId subject = input.subject().publicId();
        if (!PrimitiveData.get().hasPublicId(subject) || EntityHandle.get(PrimitiveData.nid(subject)).entity().isEmpty()) {
            return KometActionResult.refused(input, "It is not in the knowledge base.");
        }
        PublicId root = KlStorage.rootOf(subject);
        DataKind kind = EntityBoundaries.kindOf(PrimitiveData.nid(root));
        Optional<String> refusal = switch (kind) {
            case LOCAL -> Optional.empty();
            case FILE -> Optional.of("It is saved in a file, which is its record. Copy the file instead.");
            case SHARED -> Optional.of("It is shared, so others get it when the knowledge base is synced.");
            case KNOWLEDGE -> Optional.of("It is knowledge. File › Export Dataset… exports knowledge.");
        };
        if (refusal.isPresent()) {
            return KometActionResult.refused(input, refusal.get());
        }
        Path file = Path.of(location).toAbsolutePath().normalize();
        Path partial = null;
        try {
            Scope scope = Scope.of(PrimitiveData.nid(root));
            if (!scope.exportable()) {
                return KometActionResult.refused(input, "It refers to other shared or local data, which the file would not have.");
            }
            if (uncommitted(scope)) {
                return KometActionResult.refused(input, "An edit of it is not yet committed. Commit the edit or withdraw it first.");
            }
            partial = Files.createTempFile(file.getParent(), ".export", ".zip");
            ScopeOperations.Exported exported = ScopeOperations.export(scope, partial.toFile(), KlStorage.exportEntries(root));
            Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING);
            return new KometActionResult(input.id(), KometConcept.ACTION_COMPLETED, Optional.empty(), "Exported to " + file + ", "
                    + StorageGestures.size(exported.counts()) + ". File › Import Dataset… reads it into another knowledge base.");
        } catch (IOException | RuntimeException e) {
            if (partial != null) {
                try {
                    Files.deleteIfExists(partial);
                } catch (IOException left) {
                    partial.toFile().deleteOnExit();
                }
            }
            return KometActionResult.failed(input, "Could not export to " + file.getFileName() + ": " + e.getMessage());
        }
    }

    private static boolean uncommitted(Scope scope) {
        return scope.members().anySatisfy(member -> EntityHandle.get(member).entity().stream()
                .flatMap(entity -> entity.versions().stream()).anyMatch(version -> ((EntityVersion) version).time() == Long.MAX_VALUE));
    }
}
