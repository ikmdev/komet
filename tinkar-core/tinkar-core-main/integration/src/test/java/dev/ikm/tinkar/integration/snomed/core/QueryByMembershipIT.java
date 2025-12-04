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
package dev.ikm.tinkar.integration.snomed.core;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class QueryByMembershipIT {

    private static final Logger LOG = LoggerFactory.getLogger(
            QueryByMembershipIT.class);
    private static final File DATASTORE_ROOT = new File(System.getProperty("user.home") + "/Solor/snomed-starter-data");

    private final EntityToTinkarSchemaTransformer entityTransformer = EntityToTinkarSchemaTransformer.getInstance();

    final AtomicInteger exportPatternCount = new AtomicInteger();
    final AtomicInteger exportConceptCount = new AtomicInteger();
    final AtomicInteger exportSemanticCount = new AtomicInteger();
    final AtomicInteger exportStampCount = new AtomicInteger();

    final AtomicInteger nullEntities = new AtomicInteger(0);

    static final int VERBOSE_ERROR_COUNT = 10;

    @BeforeAll
    public static void beforeAll() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
    }

    @Test
    @DisplayName("Exporting filtered entities to  protobuf file")
    @Disabled("Enable the tests after figuring out the way to load the DataStores in Jenkins. or test directory")
    public void exportProtobufTest() {
        AtomicInteger verboseErrors = new AtomicInteger(0);
        int patternNidMembership = TinkarTerm.PATHS_PATTERN.nid();
        Set<Entity<? extends EntityVersion>> entitiesFromMembership = filterbyMembership(patternNidMembership);   //input pattern nid
        File protobufFile = new File(System.getProperty("user.home") + "/Solor/membership-filter.pb.zip");
        try(FileOutputStream fileOutputStream = new FileOutputStream(protobufFile);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            ZipOutputStream zos = new ZipOutputStream(bos)) {
            ZipEntry zipEntry = new ZipEntry(protobufFile.getName().replace(".zip", ""));
            zos.putNextEntry(zipEntry);
            entitiesFromMembership.forEach(entity -> {
                try {
                    if(entity != null){
                        TinkarMsg pbTinkarMsg = entityTransformer.transform(entity);
                        pbTinkarMsg.writeDelimitedTo(zos);
                        switch(entity)
                        {
                            case StampEntity stamp ->{ exportStampCount.incrementAndGet(); }
                            case PatternEntity pattern ->{exportPatternCount.incrementAndGet();}
                            case ConceptEntity concept->{exportConceptCount.incrementAndGet();}
                            case SemanticEntity semanticntic->{exportSemanticCount.incrementAndGet();}

                            default -> throw new IllegalStateException("Unexpected value: " + entity);
                        }
                    } else {
                        nullEntities.incrementAndGet();
                        if (verboseErrors.get() < VERBOSE_ERROR_COUNT) {
                            LOG.warn("No pattern entity for: " + entity);
                            verboseErrors.incrementAndGet();
                        }
                    }
                }catch (UnsupportedOperationException | IllegalStateException exception){
                    LOG.info("Processing patternNid: " + entity);
                    exception.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

            LOG.info("Zip entry size: " + zipEntry.getSize());
            // finalize zip file
            zos.closeEntry();
            zos.flush();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        LOG.info("Exported Semantic count: "+exportSemanticCount);
        LOG.info("Exported Stamp count: "+exportStampCount);
        LOG.info("Exported Pattern count: "+exportPatternCount);
        LOG.info("Exported Concept count: "+exportConceptCount);
        PrimitiveData.stop();
        Assertions.assertTrue(exportSemanticCount.get()>0);
        Assertions.assertTrue(exportConceptCount.get()>0);

    }

    public Set<Entity<? extends EntityVersion>> filterbyMembership(int patternNid){
        Set<Entity<? extends EntityVersion>> entitySet = new HashSet<>();

        EntityService.get().forEachSemanticOfPattern(patternNid,semanticEntity -> {
            entitySet.add(semanticEntity.referencedComponent());
            LOG.info("Concepts: " + semanticEntity.referencedComponent());//Concepts
            semanticEntity.referencedComponent().stampNids().forEach(stampNid ->{        //Stamps for concepts
                entitySet.add(EntityService.get().getStampFast(stampNid));
                LOG.info("Stamps for concept: " + EntityService.get().getStampFast(stampNid));
            });

            System.out.println ("Print patterns:  " + semanticEntity.pattern());
            entitySet.add(semanticEntity.pattern());    //Adds patterns to the Hashset. Duplicates are avoided

            semanticEntity.stampNids().forEach(stampNid -> {                  //Stamps for Semantics
                StampEntity<? extends StampEntityVersion> stamp = EntityService.get().getStampFast(stampNid);
                entitySet.add(stamp);
                LOG.info("Stamps for semantics: " + stamp);});
            EntityService.get().forEachSemanticForComponent(semanticEntity.referencedComponent().nid(),entity->{
                entitySet.add(entity);
                entity.pattern().stampNids().forEach(     //Adds Stamps for pattern

                        stampNid->{ entitySet.add(EntityService.get().getStampFast(stampNid));
                            LOG.info("Stamps for patterns: " + EntityService.get().getStampFast(stampNid));});
                    });
            });

        return entitySet;
    }

}
