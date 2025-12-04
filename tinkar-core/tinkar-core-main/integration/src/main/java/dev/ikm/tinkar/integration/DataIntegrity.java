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
package dev.ikm.tinkar.integration;

import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.ConceptToDataType;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataIntegrity {
    private static final Logger LOG = LoggerFactory.getLogger(DataIntegrity.class);

    public static void main(String[] args) throws InterruptedException {
        startup(new File(System.getProperty("user.home") + "/Solor/" + "MayConnectathon-5-15-2024_reasoned"));
        TinkExecutor.threadPool().awaitTermination(5, TimeUnit.SECONDS);

        List<Integer> aggregatedNullNidList = new ArrayList<>();
        Map<String, List<? extends Entity>> typeNameEntityMap = new HashMap<>();
        typeNameEntityMap.put("Stamp", validateStampReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Concept", validateConceptReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic", validateSemanticReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Pattern", validatePatternReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic Field Data Type", validateSemanticFieldDataTypes());

        breakdown();

        LOG.info("Report for {} database:", PrimitiveData.get().name());
        typeNameEntityMap.forEach((typeString, misconfiguredList) -> {
            LOG.info("Found {} {}s containing incorrect references.", misconfiguredList.size(), typeString);
            if (!misconfiguredList.isEmpty()) {
                LOG.info("Misconfigured {} PublicIds: ", typeString);
                misconfiguredList.stream().map(Entity::publicId).map(PublicId::idString).forEach(LOG::info);
            }
        });
        LOG.info("Found {} Nids containing incorrect references.", aggregatedNullNidList.size());
        LOG.info("Misconfigured Nids:");
        aggregatedNullNidList.stream().map(String::valueOf).forEach(LOG::info);
        LOG.info("Concepts Missing Stamps:");
        aggregatedNullNidList.stream().map(String::valueOf).forEach(LOG::info);
    }

    public static void startup(File datastoreRootLocation){
        LOG.info("Datastore location: " + datastoreRootLocation);
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastoreRootLocation);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
        PrimitiveData.start();
    }

    public static void breakdown(){
        PrimitiveData.stop();
    }

    public static List<StampEntity> validateStampReferences(List<Integer> nullNidList) {
        LOG.info("Validating Stamp References");
        List<StampEntity> listMisconfiguredStamps = new ArrayList<>();
        PrimitiveData.get().forEachStampNid((stampNid) -> {
            EntityService.get().getEntity(stampNid).ifPresentOrElse((entity) -> {
                if (entity instanceof StampEntity stampEntity) {
                    if (isMisconfiguredStamp(stampEntity)) {
                        listMisconfiguredStamps.add(stampEntity);
                    }
                }
            }, () -> {nullNidList.add(stampNid);});
        });
        return listMisconfiguredStamps;
    }

    public static boolean isMisconfiguredStamp(StampEntity<? extends StampEntityVersion> stampEntity) {
        AtomicBoolean isNullReferences = new AtomicBoolean(false);
        stampEntity.versions().stream().forEach((version) -> {
            if (referencedEntityIsNull(version.stateNid()) ||
                referencedEntityIsNull(version.authorNid()) ||
                referencedEntityIsNull(version.moduleNid()) ||
                referencedEntityIsNull(version.pathNid())) {
                    isNullReferences.set(true);
            }
        });
        return isNullReferences.get();
    }

    public static List<Entity> validateConceptReferences(List<Integer> nullNidList) {
        LOG.info("Validating Concept References");
        List<Entity> listMisconfiguredConcepts = new ArrayList<>();
        PrimitiveData.get().forEachConceptNid((conceptNid) -> {
            try {
                EntityService.get().getEntity(conceptNid).ifPresentOrElse((entity) -> {
                    if (entity instanceof ConceptEntity conceptEntity) {
                        if (isMisconfiguredConcept(conceptEntity)) {
                            listMisconfiguredConcepts.add(conceptEntity);
                        }
                    }
                }, () -> {
                    nullNidList.add(conceptNid);
                });
            } catch (Exception e) {
                e.printStackTrace();
                nullNidList.add(conceptNid);
            }
        });
        return listMisconfiguredConcepts;
    }

    public static boolean isMisconfiguredConcept(ConceptEntity<? extends ConceptEntityVersion> conceptEntity) {
        AtomicBoolean isNullReferences = new AtomicBoolean(false);
        conceptEntity.versions().stream().forEach((version) -> {
            if (referencedEntityIsNull(version.nid()) ||
                referencedEntityIsNull(version.stampNid())) {
                    isNullReferences.set(true);
            }
        });
        return isNullReferences.get();
    }

    public static List<Entity> validateSemanticReferences(List<Integer> nullNidList) {
        LOG.info("Validating Semantics References");
        List<Entity> listMisconfiguredSemantics = new ArrayList<>();
        PrimitiveData.get().forEachSemanticNid((semanticNid) -> {
            EntityService.get().getEntity(semanticNid).ifPresentOrElse((entity) -> {
                if (entity instanceof SemanticEntity semanticEntity) {
                    if (isMisconfiguredSemantic(semanticEntity)) {
                        listMisconfiguredSemantics.add(semanticEntity);
                    }
                }
            }, () -> {nullNidList.add(semanticNid);});
        });
        return listMisconfiguredSemantics;
    }

    public static boolean isMisconfiguredSemantic(SemanticEntity<? extends SemanticEntityVersion> semanticEntity) {
        AtomicBoolean isNullReferences = new AtomicBoolean(false);
        semanticEntity.versions().stream().forEach((version) -> {
            if (referencedEntityIsNull(version.nid()) ||
                referencedEntityIsNull(version.stampNid()) ||
                referencedEntityIsNull(version.patternNid()) ||
                referencedEntityIsNull(version.referencedComponentNid())) {
                    isNullReferences.set(true);
            }

            version.fieldValues().forEach((fieldVal) -> {
                if (fieldVal instanceof Instant ||
                    fieldVal instanceof String ||
                    fieldVal instanceof Long ||
                    fieldVal instanceof Integer) {
                        // No references to check
                        isNullReferences.get();
                } else if (fieldVal instanceof DiTree diTree) {
                    List<Integer> diTreeRefs = new ArrayList<>();
                    ImmutableList<Vertex> vertexList = diTree.vertexMap();
                    vertexList.forEach(vertex -> {
                        diTreeRefs.add(PrimitiveData.nid(vertex.meaning().publicId()));
                        vertex.propertyKeys().forEach(propKey -> {
                            diTreeRefs.add(PrimitiveData.nid(propKey.publicId()));
                            // If present check only because a key does not always need a value
                            vertex.property(propKey).ifPresent(propVal -> {
                                if (propVal instanceof Component propValComponent) {
                                    diTreeRefs.add(PrimitiveData.nid(propValComponent.publicId()));
                                } else if (propVal instanceof IntIdCollection intIdCollection) {
                                    intIdCollection.forEach(diTreeRefs::add);
                                }
                            });
                        });
                    });
                    diTreeRefs.forEach(nid -> {
                        if (referencedEntityIsNull(nid)) {
                            isNullReferences.set(true);
                        }
                    });
                } else if (fieldVal instanceof IntIdSet nidSet) {
                    nidSet.forEach((nid) -> {
                        if (referencedEntityIsNull(nid)) {
                            isNullReferences.set(true);
                        }
                    });
                } else if (fieldVal instanceof IntIdList nidList) {
                    nidList.forEach((nid) -> {
                        if (referencedEntityIsNull(nid)) {
                            isNullReferences.set(true);
                        }
                    });
                } else if (fieldVal instanceof PublicId pubId) {
                    try {
                        EntityService.get().getEntity(pubId.asUuidList()).ifPresentOrElse((ignored)->{}, () -> {isNullReferences.set(true);});
                    } catch (NullPointerException e) {
                        isNullReferences.set(true);
                    }
                } else {
                    LOG.info("Semantic Field Value '{}' not handled: {}", fieldVal.getClass().getSimpleName(), fieldVal);
                }
            });
        });
        return isNullReferences.get();
    }

    public static List<Entity> validatePatternReferences(List<Integer> nullNidList) {
        LOG.info("Validating Pattern References");
        List<Entity> listMisconfiguredPatterns = new ArrayList<>();
        PrimitiveData.get().forEachPatternNid((patternNid) -> {
            EntityService.get().getEntity(patternNid).ifPresentOrElse((entity) -> {
                if (entity instanceof PatternEntity patternEntity) {
                    if (isMisconfiguredPattern(patternEntity)) {
                        listMisconfiguredPatterns.add(patternEntity);
                    }
                }
            }, () -> {nullNidList.add(patternNid);});
        });
        return listMisconfiguredPatterns;
    }

    public static boolean isMisconfiguredPattern(PatternEntity<? extends PatternEntityVersion> patternEntity) {
        AtomicBoolean isNullReferences = new AtomicBoolean(false);
        patternEntity.versions().stream().forEach((version) -> {
            if (referencedEntityIsNull(version.nid()) ||
                referencedEntityIsNull(version.stampNid()) ||
                referencedEntityIsNull(version.semanticMeaningNid()) ||
                referencedEntityIsNull(version.semanticPurposeNid())) {
                    isNullReferences.set(true);
            }

            version.fieldDefinitions().forEach((fieldDef) -> {
                if (referencedEntityIsNull(fieldDef.dataTypeNid()) ||
                    referencedEntityIsNull(fieldDef.meaningNid()) ||
                    referencedEntityIsNull(fieldDef.purposeNid())) {
                        isNullReferences.set(true);
                }
            });
        });
        return isNullReferences.get();
    }

    public static boolean referencedEntityIsNull(int nid) {
        AtomicBoolean result = new AtomicBoolean(true);
        try {
            EntityService.get().getEntity(nid).ifPresent((ignored) -> {
                result.set(false);
            });
        } catch (Throwable t) {
            System.out.println("WTF");
        }
        return result.get();
    }

    public static List<SemanticEntity> validateSemanticFieldDataTypes() {
        LOG.info("Validating Semantic Field Values align with Pattern Field Definitions");
        List<SemanticEntity> misconfiguredSemanticsSet = new ArrayList<>();
        PrimitiveData.get().forEachSemanticNid((semanticNid) -> {
            EntityService.get().getEntity(semanticNid).ifPresent((entity) -> {
                if (entity instanceof SemanticEntity semanticEntity) {
                    if (!validateSemanticFieldDataType(semanticEntity)) {
                        misconfiguredSemanticsSet.add(semanticEntity);
                    }
                }
            });
        });
        return misconfiguredSemanticsSet;
    }

    public static boolean validateSemanticFieldDataType(SemanticEntity<? extends SemanticEntityVersion> semanticEntity) {
        AtomicBoolean isValidFieldDataTypes = new AtomicBoolean(true);
        semanticEntity.versions().stream().forEach((version) -> {
            if (version instanceof SemanticEntityVersion semanticEntityVersion) {
                latestPatternVersionForSemanticVersion(semanticEntityVersion).ifPresentOrElse((patternEntityVersion) -> {
                    if (!semanticFieldDefinitionsMatchPattern(patternEntityVersion, semanticEntityVersion)) {
                        isValidFieldDataTypes.set(false);
                    }
                }, () -> {
                    LOG.info("SemanticVersion does not have associated PatternVersion: " + semanticEntityVersion);
                    isValidFieldDataTypes.set(false);
                });
            } else {
                isValidFieldDataTypes.set(false);
            }
        });
        return isValidFieldDataTypes.get();
    }

    public static Latest<PatternEntityVersion> latestPatternVersionForSemanticVersion(SemanticEntityVersion semanticEntityVersion) {
        StampPositionRecord stampPositionRecord = new StampPositionRecord(semanticEntityVersion.time(), semanticEntityVersion.pathNid());
        StampCalculatorWithCache stampCalculator = StampCoordinateRecord.make(StateSet.ACTIVE, stampPositionRecord).stampCalculator();
        return stampCalculator.latest(semanticEntityVersion.patternNid());
    }

    public static boolean semanticFieldDefinitionsMatchPattern(PatternEntityVersion patternEntityVersion, SemanticEntityVersion semanticEntityVersion) {
        if (patternEntityVersion.fieldDefinitions().size() != semanticEntityVersion.fieldValues().size()) {
            return false;
        }
        AtomicBoolean check = new AtomicBoolean(true);
        patternEntityVersion.fieldDefinitions().forEachWithIndex((fieldDef, i) -> {
            FieldDataType fieldDefDataType = ConceptToDataType.convert(fieldDef.dataType());
            FieldDataType semanticDefDataType = semanticEntityVersion.fieldDataType(i);
            if(semanticDefDataType != fieldDefDataType) {
                if (!(fieldDefDataType.clazz.isAssignableFrom(semanticDefDataType.clazz))) {
                    check.set(false);
                }
            }
        });
        return check.get();
    }
}
