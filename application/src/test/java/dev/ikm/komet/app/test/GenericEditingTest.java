package dev.ikm.komet.app.test;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.*;
import javafx.application.Platform;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericEditingTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericEditingTest.class);

    private static StampCalculator stampCalculator;
    private static LanguageCalculator languageCalculator;


    public static void setUpBefore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/September2024_ConnectathonDataset_v1");
        CachingService.clearAll();
        LOG.info("Setup SpinedAray Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");

        PrimitiveData.start();
    }

    public static void tearDownAfter() {
        PrimitiveData.stop();
    }

    public static void main(String[] args) {

        Platform.startup(() ->{
            setUpBefore();
            try {
                //Create General Dev Latest STAMP Calculator
                stampCalculator = StampCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatest());
                //Create Dev Latest US English FQN Language Calculator
                languageCalculator = LanguageCalculatorWithCache.getCalculator(
                        Coordinates.Stamp.DevelopmentLatest(),
                        Lists.mutable.of(Coordinates.Language.UsEnglishFullyQualifiedName()).toImmutableList());

                //f9cb7be4-da79-35fc-9189-85a645f3e81f - Needle Biopsy (Semantic Chronology)
                PublicId semanticPublicId = PublicIds.of(UUID.fromString("f9cb7be4-da79-35fc-9189-85a645f3e81f"));

                //Get the Latest Semantic Entity Version
                int entityNid = EntityService.get().nidForPublicId(semanticPublicId);
                Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(entityNid);

                //Build a List of Field Records that contain Objects as values
                List<FieldRecord<Object>> fieldRecords;
                //Latest may not be availablebased on calculator
                if (semanticEntityVersionLatest.isPresent()) {
                    SemanticEntityVersion semanticEntityVersion = semanticEntityVersionLatest.get();
                    Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());

                    //Latest may not be available based on calculator
                    if (patternEntityVersionLatest.isPresent()) {
                        PatternEntityVersion patternEntityVersion = patternEntityVersionLatest.get();
                        fieldRecords = fieldRecords(semanticEntityVersion, patternEntityVersion);
                        String semanticDetails = semanticDetails(semanticEntityVersion, patternEntityVersion);
                        String semanticFieldsDetails = semanticFieldsDetails(fieldRecords);
                        System.out.println(semanticDetails);
                        System.out.println(semanticFieldsDetails);
                    }
                }

            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
    }

    private static String semanticDetails(SemanticEntityVersion semanticEntityVersion, PatternEntityVersion patternEntityVersion) {
        String patternMeaning = text(patternEntityVersion.semanticMeaningNid());
        String patternPurpose = text(patternEntityVersion.semanticPurposeNid());
        return new StringBuilder()
                .append("---Semantic Detail---").append("\n")
                .append("Title: ").append(patternMeaning).append(" to ").append(patternPurpose).append("\n")
                .append("Meaning: ").append(patternMeaning).append("\n")
                .append("Purpose: ").append(text(patternEntityVersion.semanticPurposeNid())).append("\n")
                .toString();
    }

    private static String semanticFieldsDetails(List<FieldRecord<Object>> fieldRecords) {
        StringBuilder sb = new StringBuilder();
        fieldRecords.forEach(fieldRecord ->
                sb.append("---Field Detail---").append("\n")
                        .append(text(fieldRecord.meaningNid())).append(": ").append(fieldRecord.value().toString()).append("\n")
                        .append("Hover Over: ").append(text(fieldRecord.purposeNid())).append("\n\n")
        );
        return sb.toString();
    }

    private static List<FieldRecord<Object>> fieldRecords(SemanticEntityVersion semanticEntityVersion, PatternEntityVersion patternEntityVersion) {
        List<FieldRecord<Object>> fieldRecords = new ArrayList<>();
        ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitionForEntities = patternEntityVersion.fieldDefinitions();
        for (int i = 0; i < semanticEntityVersion.fieldValues().size(); i++) {
            fieldRecords.add(new FieldRecord(
                    semanticEntityVersion.fieldValues().get(i),
                    semanticEntityVersion.nid(),
                    semanticEntityVersion.stampNid(),
                    fieldDefinitionForEntities.get(i))
            );
        }
        return fieldRecords;
    }

    private static String text(int nid) {
        return languageCalculator.getDescriptionText(nid).orElse("No Description found");
    }
}
