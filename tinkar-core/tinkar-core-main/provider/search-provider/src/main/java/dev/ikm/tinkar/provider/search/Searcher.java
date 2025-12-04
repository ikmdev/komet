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
package dev.ikm.tinkar.provider.search;

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Searcher {
    private static final Logger LOG = LoggerFactory.getLogger(Searcher.class);
    public static final EntityProxy.Pattern LIDR_RECORD_PATTERN = EntityProxy.Pattern.make(null, UUID.fromString("c3d52f47-0565-5cfb-9b0b-d7501a33b35d"));
    public static final EntityProxy.Pattern DIAGNOSTIC_DEVICE_PATTERN = EntityProxy.Pattern.make(null, UUID.fromString("a507b3c7-eadb-5d54-84c0-c44f3155d0bc"));
    public static final EntityProxy.Pattern QUANTITATIVE_ALLOWED_RESULT_SET_PATTERN = EntityProxy.Pattern.make(null, UUID.fromString("9d40d06b-7776-5a56-97e4-0c27f5d574c7"));
    public static final EntityProxy.Pattern QUALITATIVE_ALLOWED_RESULT_SET_PATTERN = EntityProxy.Pattern.make(null, UUID.fromString("160a63a6-3cba-510e-83d1-235822045885"));
    QueryParser parser;
    private static final SearcherManager searcherManager;

    //TODO - refactor this class to not have static fields. Currently needed when using this SearcherManager class.
    static {
        try {
            searcherManager = new SearcherManager(Indexer.indexReader(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Searcher() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Opening lucene searcher");
        this.parser = new QueryParser("text", Indexer.analyzer());
        stopwatch.stop();
        LOG.info("Opened lucene searcher in: " + stopwatch.durationString());
    }

    public PrimitiveDataSearchResult[] search(String queryString, int maxResultSize) throws
            ParseException, IOException, InvalidTokenOffsetsException {
        boolean refreshOutcome = searcherManager.maybeRefresh();
        LOG.debug("Index Reader refresh outcome = {}", refreshOutcome);
        IndexSearcher indexSearcher = searcherManager.acquire();
        try {
            if (queryString != null & !queryString.isEmpty()) {
                PrimitiveDataSearchResult[] results;
                    Query query = parser.parse(queryString);
                    Formatter formatter = new SimpleHTMLFormatter();
                    QueryScorer scorer = new QueryScorer(query);
                    Highlighter highlighter = new Highlighter(formatter, scorer);
                    highlighter.setTextFragmenter(new NullFragmenter());

                    ScoreDoc[] hits = indexSearcher.search(query, maxResultSize).scoreDocs;
                    results = new PrimitiveDataSearchResult[hits.length];
                    for (int i = 0; i < hits.length; i++) {
                        int docId = hits[i].doc;
                        // Load only needed stored fields to avoid reading all fields
                        Set<String> fieldsToLoad = Set.of(
                                Indexer.NID,
                                Indexer.PATTERN_NID,
                                Indexer.RC_NID,
                                Indexer.FIELD_INDEX,
                                Indexer.TEXT_FIELD_NAME
                        );
                        Document hitDoc = indexSearcher.storedFields().document(docId, fieldsToLoad);
                        StoredField nidField = (StoredField) hitDoc.getField(Indexer.NID);
                        StoredField patternNidField = (StoredField) hitDoc.getField(Indexer.PATTERN_NID);
                        StoredField rcNidField = (StoredField) hitDoc.getField(Indexer.RC_NID);
                        StoredField fieldIndexField = (StoredField) hitDoc.getField(Indexer.FIELD_INDEX);
                        StoredField textField = (StoredField) hitDoc.getField(Indexer.TEXT_FIELD_NAME);
                        String highlightedString = highlighter.getBestFragment(
                                Indexer.analyzer(), Indexer.TEXT_FIELD_NAME, textField.stringValue());

                        results[i] = new PrimitiveDataSearchResult(
                                nidField.numericValue().intValue(),
                                rcNidField.numericValue().intValue(),
                                patternNidField.numericValue().intValue(),
                                fieldIndexField.numericValue().intValue(),
                                hits[i].score,
                                highlightedString
                        );
                    }
                return results;
            }
        } finally {
            searcherManager.release(indexSearcher);
        }
        return new PrimitiveDataSearchResult[0];
    }

    /**
     * Returns a default navigation calculator with coordinates for
     * inferred navigation, active stamps on development path, & english synonyms
     *
     * @return  NavigationCalculator
     */
    static NavigationCalculator defaultNavigationCalculator() {
        StampCoordinateRecord stampCoordinateRecord = Coordinates.Stamp.DevelopmentLatestActiveOnly();
        LanguageCoordinateRecord languageCoordinateRecord = Coordinates.Language.UsEnglishRegularName();
        NavigationCoordinateRecord navigationCoordinateRecord = Coordinates.Navigation.inferred().toNavigationCoordinateRecord();
        return NavigationCalculatorWithCache.getCalculator(stampCoordinateRecord, Lists.immutable.of(languageCoordinateRecord), navigationCoordinateRecord);
    }

    /**
     * Returns List of Children PublicIds for the Entity PublicId provided
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   parentConceptId PublicId of the parent concept
     * @return  List of PublicIds for the children concepts
     */
    public static List<PublicId> childrenOf(PublicId parentConceptId) {
        return childrenOf(defaultNavigationCalculator(), parentConceptId);
    }

    /**
     * Returns List of Children PublicIds for the Entity PublicId provided
     * using a supplied NavigationCalculator
     * <p>
     * Provided as an ease-of-use method to convert nids provided by the
     * {@link NavigationCalculator#childrenOf(int)} method to PublicIds
     *
     * @param   navCalc NavigationCalculator to calculate children
     * @param   parentConceptId PublicId of the parent concept
     * @return  List of PublicIds for the children concepts
     */
    public static List<PublicId> childrenOf(NavigationCalculator navCalc, PublicId parentConceptId) {
        List<PublicId> childIds = new ArrayList<>();
        int[] childNidList = navCalc.childrenOf(EntityService.get().nidForPublicId(parentConceptId)).toArray();
        for (int childNid : childNidList) {
            EntityService.get().getEntity(childNid).ifPresent((entity) -> childIds.add(entity.publicId()));
        }
        return childIds;
    }

    /**
     * Returns List of descendant PublicIds for the Entity PublicId provided
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   ancestorConceptId PublicId of the ancestor concept
     * @return  List of PublicIds for the descendant concepts
     */
    public static List<PublicId> descendantsOf(PublicId ancestorConceptId) {
        return descendantsOf(defaultNavigationCalculator(), ancestorConceptId);
    }

    /**
     * Returns List of descendant PublicIds for the Entity PublicId provided
     * using a supplied NavigationCalculator
     * <p>
     * Provided as an ease-of-use method to convert nids provided by the
     * {@link NavigationCalculator#descendentsOf(int)} method to PublicIds
     *
     * @param   navCalc NavigationCalculator to calculate descendants
     * @param   ancestorConceptId PublicId of the ancestor concept
     * @return  List of PublicIds for the descendant concepts
     */
    public static List<PublicId> descendantsOf(NavigationCalculator navCalc, PublicId ancestorConceptId) {
        List<PublicId> descendantIds = new ArrayList<>();
        int[] descendantNidList = navCalc.descendentsOf(EntityService.get().nidForPublicId(ancestorConceptId)).toArray();
        for (int descendantNid : descendantNidList) {
            EntityService.get().getEntity(descendantNid).ifPresent((entity) -> descendantIds.add(entity.publicId()));
        }
        return descendantIds;
    }

    /**
     * Returns List of Fully Qualified Name (FQN) Strings for the Entity PublicIds provided
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   conceptIds List of PublicIds for concepts with FQNs to return
     * @return  List of FQN Strings with indexes matching the supplied List of PublicIds
     */
    public static List<String> descriptionsOf(List<PublicId> conceptIds) {
        return descriptionsOf(defaultNavigationCalculator(), conceptIds);
    }

    /**
     * Returns List of Fully Qualified Name (FQN) Strings for the Entity PublicIds provided
     * using a supplied NavigationCalculator
     * <p>
     * Provided as an ease-of-use method to retrieve FQNs for multiple concepts at once
     * using the {@link NavigationCalculator#getFullyQualifiedNameText(int)} method
     *
     * @param   navCalc NavigationCalculator to calculate FQNs
     * @param   conceptIds List of PublicIds for concepts with FQNs to return
     * @return  List of FQN Strings with indexes matching the supplied List of PublicIds
     */
    public static List<String> descriptionsOf(NavigationCalculator navCalc, List<PublicId> conceptIds) {
        List<String> names = new ArrayList<>();
        for (PublicId pid : conceptIds) {
            navCalc.getFullyQualifiedNameText(EntityService.get().nidForPublicId(pid))
                    .ifPresentOrElse(names::add,
                        () -> {
                            LOG.warn("FQN not defined for " + pid.idString());
                            names.add("");
                        }
                    );
        }
        return names;
    }

    /**
     * Returns List of LIDR Record PublicIds for the provided Test Kit Device.
     *
     * @param   testKitId associated Test Kit Device PublicId
     * @return  List of Public Ids given test kit concept and lidr record pattern.
     */
    public static List<PublicId> getLidrRecordSemanticsFromTestKit(PublicId testKitId){
        List<PublicId> lidrRecordSemanticIds = new ArrayList<>();

        EntityService.get().getEntity(testKitId.asUuidArray()).ifPresent((testKitEntity) -> {
            EntityService.get().forEachSemanticForComponentOfPattern(testKitEntity.nid(), DIAGNOSTIC_DEVICE_PATTERN.nid(), diagnosticDeviceSemantic -> {
                EntityService.get().forEachSemanticForComponentOfPattern(diagnosticDeviceSemantic.nid(), LIDR_RECORD_PATTERN.nid(), lidrRecordSemantic -> {
                    lidrRecordSemanticIds.add(lidrRecordSemantic.publicId());
                });
            });
        });

        return lidrRecordSemanticIds;
    }

    /**
     * Returns List of Result Conformance PublicIds for all LIDR Records of the Test Kit Device
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   testKitId PublicId of the Test Kit Device
     * @return  List of PublicIds for all Result Conformances / Constraints for the Test Kit Device
     */
    public static List<PublicId> getResultConformanceFromTestKit(PublicId testKitId) {
        return getResultConformanceFromTestKit(defaultNavigationCalculator(), testKitId);
    }

    /**
     * Returns List of Result Conformance PublicIds for all LIDR Records of the Test Kit Device
     *
     * @param   navCalc NavigationCalculator for determining latest version
     * @param   testKitId PublicId of the Test Kit Device
     * @return  List of PublicIds for all Result Conformances / Constraints for the Test Kit Device
     */
    public static List<PublicId> getResultConformanceFromTestKit(NavigationCalculator navCalc, PublicId testKitId) {
        List<PublicId> resultConformanceList = new ArrayList<>();

        for (PublicId lidrRecordId : getLidrRecordSemanticsFromTestKit(testKitId)) {
            resultConformanceList.addAll(getResultConformancesFromLidrRecord(navCalc, lidrRecordId));
        }
        return resultConformanceList;
    }

    /**
     * Returns List of Result Conformance PublicIds for a LIDR Record
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   lidrRecordId PublicId of the LIDR Record
     * @return  List of PublicIds for Result Conformances of the LIDR Record
     */
    public static List<PublicId> getResultConformancesFromLidrRecord(PublicId lidrRecordId) {
        return getResultConformancesFromLidrRecord(defaultNavigationCalculator(), lidrRecordId);
    }

    /**
     * Returns List of Result Conformance PublicIds for a LIDR Record
     *
     * @param   navCalc NavigationCalculator for determining latest version
     * @param   lidrRecordId PublicId of the LIDR Record
     * @return  List of PublicIds for Result Conformances of the LIDR Record
     */
    public static List<PublicId> getResultConformancesFromLidrRecord(NavigationCalculator navCalc, PublicId lidrRecordId) {
        List<PublicId> resultConformanceList = new ArrayList<>();
        EntityService.get().getEntity(lidrRecordId.asUuidArray()).ifPresent((lidrRecordEntity) -> {
            navCalc.stampCalculator().latest(lidrRecordEntity)
                    .ifPresent((lidrRecordVersion) -> {
                        SemanticEntityVersion lidrRecordSemanticVersion = (SemanticEntityVersion) lidrRecordVersion;
                        int idxResultConformances = 5;
                        ((IntIdSet) lidrRecordSemanticVersion.fieldValues().get(idxResultConformances))
                                .map(PrimitiveData::publicId)
                                .forEach(resultConformanceList::add);
                    });
        });
        return resultConformanceList;
    }

    /**
     * Returns List of Allowed Results PublicIds for a Result Conformance
     * using the default NavigationCalculator from {@link #defaultNavigationCalculator()}
     *
     * @param   resultConformanceId PublicId of the Result Conformance
     * @return  List of PublicIds for Allowed Results of the Result Conformance
     */
    public static List<PublicId> getAllowedResultsFromResultConformance(PublicId resultConformanceId) {
        return getAllowedResultsFromResultConformance(defaultNavigationCalculator(), resultConformanceId);
    }

    /**
     * Returns List of Allowed Results PublicIds for a Result Conformance
     *
     * @param   navCalc NavigationCalculator for determining latest version
     * @param   resultConformanceId PublicId of the Result Conformance
     * @return  List of PublicIds for Allowed Results of the Result Conformance
     */
    public static List<PublicId> getAllowedResultsFromResultConformance(NavigationCalculator navCalc, PublicId resultConformanceId) {
        List<PublicId> allowedResultsList = new ArrayList<>();

        int resultConformanceNid = EntityService.get().nidForPublicId(resultConformanceId);

        EntityService.get().forEachSemanticForComponentOfPattern(resultConformanceNid, QUANTITATIVE_ALLOWED_RESULT_SET_PATTERN.nid(),
                (quantitativeResultSet) -> {
                    navCalc.stampCalculator().latest(quantitativeResultSet)
                            .ifPresent((latestQuantitativeResultSet) -> {
                                ((IntIdSet) latestQuantitativeResultSet.fieldValues().get(0))
                                        .map(PrimitiveData::publicId)
                                        .forEach(allowedResultsList::add);
                            });
                });
        EntityService.get().forEachSemanticForComponentOfPattern(resultConformanceNid, QUALITATIVE_ALLOWED_RESULT_SET_PATTERN.nid(),
                (qualitativeResultSet) -> {
                    navCalc.stampCalculator().latest(qualitativeResultSet)
                            .ifPresent((latestQualitativeResultSet) -> {
                                ((IntIdSet) latestQualitativeResultSet.fieldValues().get(0))
                                        .map(PrimitiveData::publicId)
                                        .forEach(allowedResultsList::add);
                            });
                });
        return allowedResultsList;
    }

    /**
     * Returns List of PublicIds for the Concepts tagged with the Membership Pattern
     *
     * @param   memberPatternId PublicId of the Membership Pattern
     * @return  List of PublicIds for the Concepts tagged with the Membership Pattern
     */
    public static List<PublicId> membersOf(PublicId memberPatternId) {
        if (PrimitiveData.get().hasPublicId(memberPatternId)) {
            List<PublicId> conceptIds = new ArrayList<>();
            EntityService.get().getEntity(memberPatternId.asUuidArray()).ifPresent((e) -> {
                if (e instanceof PatternEntity<?> patternEntity) {
                    EntityService.get().forEachSemanticOfPattern(patternEntity.nid(), (semanticEntityOfPattern) ->
                        conceptIds.add(semanticEntityOfPattern.referencedComponent().publicId()));
                }
            });

            return conceptIds;
        }
        return Collections.emptyList();
    }

    /**
     * Returns PublicId for the Concept associated with a Semantic containing fields with the given identifier source and value
     *
     * @param   identifierSource PublicId identifierSource
     * @param   identifierValue String identifierValue
     * @return  Optional wrapped PublicId for the Concept associated with the Semantic containing the identifier source and value
     */
    public static Optional<PublicId> getPublicId(PublicId identifierSource, String identifierValue) {
        ViewCalculator viewCalc = Calculators.View.Default();
        Latest<PatternEntityVersion> latestIdPattern = viewCalc.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);

        if (latestIdPattern.isAbsent()) {
            throw new RuntimeException("Identifier Pattern is absent from data set");
        }

        try {
            int[] semanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid());
            for (int nid : semanticNids) {
                EntityVersion entityVersion = viewCalc.latest(nid).get();
                if (entityVersion instanceof SemanticEntityVersion semanticEntityVersion) {
                    Object idValue = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, semanticEntityVersion);
                    if (identifierValue != null && identifierValue.equals(idValue)) {
                        Component idSource = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, semanticEntityVersion);
                        if (identifierSource != null && idSource != null && PublicId.equals(idSource.publicId(), identifierSource)) {
                            return Optional.of(semanticEntityVersion.referencedComponent().publicId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Encountered exception {}", e.getMessage());
        }

        return Optional.empty();
    }
}
