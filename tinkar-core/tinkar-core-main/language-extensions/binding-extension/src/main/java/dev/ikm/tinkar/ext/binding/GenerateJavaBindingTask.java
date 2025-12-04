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
package dev.ikm.tinkar.ext.binding;

import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.ext.binding.interpolation.Interpolator;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.ikm.tinkar.ext.binding.JavaTemplates.JAVA_BINDING_TEMPLATE;
import static dev.ikm.tinkar.ext.binding.JavaTemplates.JAVA_COMMENT_BLOCK_UUID_TEMPLATE;
import static dev.ikm.tinkar.ext.binding.JavaTemplates.JAVA_CONCEPT_COMMENT_TEMPLATE;
import static dev.ikm.tinkar.ext.binding.JavaTemplates.JAVA_PATTERN_COMMENT_TEMPLATE;
import static dev.ikm.tinkar.ext.binding.JavaTemplates.JAVA_PATTERN_FIELD_DEFINITION_TEMPLATE;

public class GenerateJavaBindingTask extends TrackingCallable<Void> {

    private final Stream<Entity<? extends EntityVersion>> conceptEntities;
    private final Stream<Entity<? extends EntityVersion>> patternEntities;
    private final Stream<Entity<? extends EntityVersion>> semanticEntities;
    private final String author;
    private final String packageName;
    private final String className;
    private final UUID namespace;
    private final Consumer<String> outputConsumer;
    private final BindingHelper bindingHelper;

    public GenerateJavaBindingTask(Stream<Entity<? extends EntityVersion>> conceptEntities,
                                   Stream<Entity<? extends EntityVersion>> patternEntities,
                                   Stream<Entity<? extends EntityVersion>> semanticEntities,
                                   String author,
                                   String packageName,
                                   String className,
                                   UUID namespace,
                                   Consumer<String> outputConsumer,
                                   BindingHelper bindingHelper) {
        this.conceptEntities = conceptEntities;
        this.patternEntities = patternEntities;
        this.semanticEntities = semanticEntities;
        this.author = author;
        this.packageName = packageName;
        this.className = className;
        this.namespace = namespace;
        this.outputConsumer = outputConsumer;
        this.bindingHelper = bindingHelper;
    }

    @Override
    protected Void compute() throws Exception {

        Interpolator bindingInterpolator = new Interpolator.Builder(JAVA_BINDING_TEMPLATE)
                .empty("package", outputConsumer -> outputConsumer.accept("package " + packageName + ";"))
                .empty("author", outputConsumer -> outputConsumer.accept(author))
                .empty("imports",
                        outputConsumer -> outputConsumer.accept("""
                                import java.util.UUID;
                                import dev.ikm.tinkar.common.id.PublicIds;
                                import dev.ikm.tinkar.terms.EntityProxy.Concept;
                                import dev.ikm.tinkar.terms.EntityProxy.Pattern;"""
                        ))
                .empty("className", outputConsumer -> outputConsumer.accept(className))
                .empty("namespaceUUID", outputConsumer -> outputConsumer.accept(namespace.toString()))
                .stream("concepts", conceptEntities,
                        (input, outputConsumer) -> {
                            AtomicBoolean isFirstLOC = new AtomicBoolean(true);
                            Iterator<Entity<? extends EntityVersion>> iterator = input.iterator();

                            while (iterator.hasNext()) {
                                var entity = iterator.next();
                                String conceptBinding = createConceptBinding(entity);

                                if (iterator.hasNext()) {
                                    if (isFirstLOC.getAndSet(false)) {
                                        outputConsumer.accept(conceptBinding.substring(5));
                                    } else {
                                        outputConsumer.accept(conceptBinding);
                                    }
                                } else {
                                    outputConsumer.accept(conceptBinding.substring(0, conceptBinding.length() - 1));
                                }
                            }
                        })
                .stream("patterns", patternEntities,
                        (input, outputConsumer) -> {
                            AtomicBoolean isFirstLOC = new AtomicBoolean(true);
                            Iterator<Entity<? extends EntityVersion>> iterator = input.iterator();

                            while (iterator.hasNext()) {
                                var entity = iterator.next();
                                String patternBinding = createPatternBinding(entity);

                                if (iterator.hasNext()) {
                                    if (isFirstLOC.getAndSet(false)) {
                                        outputConsumer.accept(patternBinding.substring(4));
                                    } else {
                                        outputConsumer.accept(patternBinding);
                                    }
                                } else {
                                    outputConsumer.accept(patternBinding.substring(0, patternBinding.length() - 1));
                                }
                            }
                        })
                .empty("semantics", outputConsumer -> outputConsumer.accept("//No Semantics configured for binding"))
                .build();

        bindingInterpolator.run(outputConsumer);

        return null;
    }

    private String createConceptBinding(Entity<? extends EntityVersion> conceptEntity) {
        final StringBuilder stringBuffer = new StringBuilder();

        Interpolator concepInterpolator = new Interpolator.Builder(JAVA_CONCEPT_COMMENT_TEMPLATE)
                .empty("conceptDescription", outputConsumer -> outputConsumer.accept(bindingHelper.getText(conceptEntity.nid())))
                .empty("conceptCommentDescription", outputConsumer -> outputConsumer.accept(bindingHelper.getText(conceptEntity.nid())))
                .list("uuids", conceptEntity.publicId().asUuidList().castToList(),
                        (input, outputConsumer) -> {
                            AtomicBoolean isFirst = new AtomicBoolean(true);
                            Iterator<UUID> iterator = input.iterator();

                            while (iterator.hasNext()) {
                                var uuid = iterator.next();
                                String uuidString = createCommentUUID(uuid);
                                if (isFirst.getAndSet(false)) {
                                    uuidString = uuidString.substring(5);
                                }
                                if (!iterator.hasNext()) {
                                    uuidString = uuidString.replace("\n", "");
                                }
                                outputConsumer.accept(uuidString);
                            }
                        })
                .single("conceptVariable", conceptEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.createVariableName(conceptEntity.nid())))
                .single("conceptDescription", conceptEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.getText(conceptEntity.nid())))
                .single("conceptPublicId", conceptEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.createPublicId(conceptEntity)))
                .build();

        concepInterpolator.run(stringBuffer::append);
        return stringBuffer.toString();
    }

    private String createPatternBinding(Entity<? extends EntityVersion> patternEntity) {
        final StringBuilder stringBuffer = new StringBuilder();

        Interpolator patternCommentInterpolator = new Interpolator.Builder(JAVA_PATTERN_COMMENT_TEMPLATE)
                .empty("patternCommentDescription", outputConsumer -> outputConsumer.accept(bindingHelper.getText(patternEntity.nid())))
                .empty("patternDescription", outputConsumer -> outputConsumer.accept(bindingHelper.getText(patternEntity.nid())))
                .list("uuids", patternEntity.publicId().asUuidList().castToList(),
                        (input, outputConsumer) -> {
                            AtomicBoolean isFirst = new AtomicBoolean(true);
                            Iterator<UUID> iterator = input.iterator();

                            while (iterator.hasNext()) {
                                var uuid = iterator.next();
                                String uuidString = createCommentUUID(uuid);
                                if (isFirst.getAndSet(false)) {
                                    uuidString = uuidString.substring(5);
                                }
                                if (!iterator.hasNext()) {
                                    uuidString = uuidString.replace("\n", "");
                                }
                                outputConsumer.accept(uuidString);
                            }
                        })
                .list("patternFieldDefinitions", bindingHelper.getPatternFieldDefinitions(patternEntity.nid()),
                        (input, outputConsumer) -> {
                            AtomicBoolean isFirstFieldDefinition = new AtomicBoolean(true);
                            if (input.size() == 0) {
                                outputConsumer.accept("* <li>No Field Definitions defined");
                            }
                            var iterator = input.iterator();

                            while (iterator.hasNext()) {
                                var fieldDefinition = iterator.next();
                                String fieldDefinitionString = createPatternDefinitionLOC(fieldDefinition);
                                if (isFirstFieldDefinition.getAndSet(false)) {
                                    fieldDefinitionString = fieldDefinitionString.substring(5);
                                }
                                if (!iterator.hasNext()) {
                                    fieldDefinitionString = fieldDefinitionString.replace("\n", "");
                                }
                                outputConsumer.accept(fieldDefinitionString);
                            }
                        })
                .single("patternVariable", patternEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.createVariableName(patternEntity.nid())))
                .single(
                        "patternDescription", patternEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.getText(patternEntity.nid())))
                .single(
                        "patternPublicId", patternEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.createPublicId(patternEntity)))
                .build();

        patternCommentInterpolator.run(stringBuffer::append);
        return stringBuffer.toString();
    }

    private String createCommentUUID(UUID uuid) {
        final StringBuilder stringBuffer = new StringBuilder();

        Interpolator conceptCommentUUIDInterpolator = new Interpolator.Builder(JAVA_COMMENT_BLOCK_UUID_TEMPLATE)
                .single("entityUUID", uuid,
                        (input, outputConsumer) -> outputConsumer.accept(input.toString()))
                .build();
        conceptCommentUUIDInterpolator.run(stringBuffer::append);
        return stringBuffer.toString();
    }

    private String createPatternDefinitionLOC(FieldDefinitionForEntity fieldDefinitionForEntity) {
        final StringBuilder stringBuilder = new StringBuilder();

        Interpolator patternDefinitionLOCInterpolator = new Interpolator.Builder(JAVA_PATTERN_FIELD_DEFINITION_TEMPLATE)
                .single("fieldIndex", fieldDefinitionForEntity,
                        (input, outputConsumer) -> outputConsumer.accept(String.valueOf(input.indexInPattern())))
                .single("fieldDataType", fieldDefinitionForEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.getText(input.dataTypeNid())))
                .single("fieldMeaning", fieldDefinitionForEntity,
                        (input, outputConsumer) -> outputConsumer.accept(bindingHelper.getText(input.meaningNid())))
                .build();

        patternDefinitionLOCInterpolator.run(stringBuilder::append);
        return stringBuilder.toString();
    }
}
