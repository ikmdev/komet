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
package dev.ikm.tinkar.ext.lang.owl;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;
import static dev.ikm.tinkar.terms.TinkarTermV2.STAMP_PATTERN;
import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_EOL;
import static java.io.StreamTokenizer.TT_NUMBER;
import static java.io.StreamTokenizer.TT_WORD;

@Deprecated
public class SctOwlUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(SctOwlUtilities.class);
  
    public static final String TRANSITIVEOBJECTPROPERTY = "transitiveobjectproperty";
    public static final String PREFIX = "prefix";
    public static final String ONTOLOGY = "ontology";
    public static final String REFLEXIVEOBJECTPROPERTY = "reflexiveobjectproperty";
    public static final String SUBCLASSOF = "subclassof";
    public static final String SUBANNOTATIONPROPERTYOF = "subannotationpropertyof";
    public static final String SUBOBJECTPROPERTYOF = "subobjectpropertyof";

    public static final String SUBDATAPROPERTYOF = "subdatapropertyof";
    public static final String OBJECTPROPERTYCHAIN = "objectpropertychain";
    public static final String OBJECTINTERSECTIONOF = "objectintersectionof";
    public static final String OBJECTSOMEVALUESFROM = "objectsomevaluesfrom";
    public static final String EQUIVALENTCLASSES = "equivalentclasses";
    public static final String DATAHASVALUE = "datahasvalue";

    public static StreamTokenizer getParser(String stringToParse) {
        BufferedReader sctOwlReader = new BufferedReader(new StringReader(stringToParse));
        StreamTokenizer t = new StreamTokenizer(sctOwlReader);
        t.resetSyntax();
        t.wordChars('0', '9');
        t.wordChars('a', 'z');
        t.wordChars('A', 'Z');
        t.wordChars('-', '-'); // Used to represent UUID as a single token
        t.wordChars(128 + 32, 255);
        t.whitespaceChars(0, ' ');
        t.commentChar('#');
        t.eolIsSignificant(false);
        t.quoteChar('"');
        t.slashSlashComments(true);
        t.slashStarComments(true);
        return t;
    }

    public static LogicalExpression sctToLogicalExpression(String owlClassExpressionsToProcess,
                                                           String owlPropertyExpressionsToProcess) throws IOException {


        String originalExpression = owlClassExpressionsToProcess + " " + owlPropertyExpressionsToProcess;
        
        final LogicalExpressionBuilder leb = new LogicalExpressionBuilder();


        StreamTokenizer tokenizer = getParser(owlClassExpressionsToProcess);
        tokenizer.nextToken();
        while (tokenizer.ttype == TT_WORD) {
            switch (tokenizer.sval.toLowerCase()) {
                case EQUIVALENTCLASSES:
                    processSet(EQUIVALENTCLASSES, leb, tokenizer, originalExpression);
                    break;
                case SUBCLASSOF, SUBANNOTATIONPROPERTYOF:
                    if (tokenizer.sval.toLowerCase().equals(SUBANNOTATIONPROPERTYOF)) {
                        LOG.warn("Converting SUBANNOTATIONPROPERTYOF to SUBCLASSOF. SUBANNOTATIONPROPERTYOF is not yet supported.");
                    }
                    processSet(SUBCLASSOF, leb, tokenizer, originalExpression);
                    break;
                case PREFIX:
                case ONTOLOGY:
                    parseToCloseParen(tokenizer);
                    tokenizer.pushBack();
                    break;
                case TRANSITIVEOBJECTPROPERTY:
                case REFLEXIVEOBJECTPROPERTY:
                    leb.NecessarySet(processObjectProperties(leb, tokenizer, originalExpression));
                    tokenizer.pushBack();
                    break;

                default:
                    throwIllegalStateException("Expecting equivalentclasses or subclassof.", tokenizer, originalExpression);

            }
            tokenizer.nextToken();
            if (tokenizer.ttype != ')') {
                if (tokenizer.ttype == TT_EOF) {
                    // OK alternative to conclude processing of expressions.
                } else {
                    throwIllegalStateException("Expecting closure of set with ).", tokenizer, originalExpression);
                }
            }
            while (tokenizer.ttype == ')') {
                tokenizer.nextToken();
            }
        }
        if (tokenizer.ttype != TT_EOF) {
            throwIllegalStateException("Expecting TT_WORD. Found: ", tokenizer, originalExpression);
        }


        tokenizer = getParser(owlPropertyExpressionsToProcess);
        tokenizer.nextToken();
        while (tokenizer.ttype == TT_WORD) {
            switch (tokenizer.sval.toLowerCase()) {

                case SUBOBJECTPROPERTYOF: // TODO: Temporary addition, pending discussion with Michael Lawley
                case SUBANNOTATIONPROPERTYOF: // TODO: Temporary addition, pending discussion with Michael Lawley
                case SUBDATAPROPERTYOF:
                case REFLEXIVEOBJECTPROPERTY:
                case TRANSITIVEOBJECTPROPERTY:
                    tokenizer.pushBack();
                    leb.PropertySet(processPropertySet(leb, tokenizer, originalExpression));
                    break;

                default:
                    throwIllegalStateException("Expecting equivalentclasses or subclassof.", tokenizer, originalExpression);

            }
            tokenizer.nextToken();
            if (tokenizer.ttype != ')') {
                if (tokenizer.ttype == TT_EOF) {
                    // OK alternative to conclude processing of expressions.
                } else {
                    throwIllegalStateException("Expecting closure of set with ). ", tokenizer, originalExpression);
                }
            }
            while (tokenizer.ttype == ')') {
                tokenizer.nextToken();
            }
        }
        if (tokenizer.ttype != TT_EOF) {
            throwIllegalStateException("Expecting TT_WORD. ", tokenizer, originalExpression);
        }

        LogicalExpression expression = leb.build();

        return expression;
    }

    private static LogicalAxiom.Atom.ConceptAxiom handleSubclassOf(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original) throws IOException {
        if (t.nextToken() != '(') {
            throw new IllegalStateException("Expecting '(' found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() != ':') {
            throw new IllegalStateException("Expecting ':' found: " + t.ttype + " " + t.sval);
        }
        // TODO: Why do we always expect this to be an identifier and not a TT_WORD like OBJECTINTERSECTIONOF?
        // Skip concept id...
        if (t.nextToken() != '[') {
            throw new IllegalStateException("Expecting concept identifier found: " + t.ttype + " " + t.sval);
        }
        parseAndDiscardPublicId(t, original);
        if (t.nextToken() != ':') {
            throw new IllegalStateException("Expecting ':' found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() == '[') {
            int subclassConceptNid = ScopedValue
                    .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                    .call(() -> PrimitiveData.nid(processPublicId(t, original)));
            return logicalExpressionBuilder.ConceptAxiom(subclassConceptNid);
        } else {
            throwIllegalStateException("Expecting concept identifier. ", t, original);
        }
        throw new IllegalStateException("unreachable");
    }


    private static LogicalAxiom.Atom.Connective.And processObjectProperties(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original) throws IOException {
        List<LogicalAxiom.Atom> andList = new ArrayList<>();

        switch (t.sval.toLowerCase()) {

            case TRANSITIVEOBJECTPROPERTY:
                parseAndDiscardOpenParen(t, original);
                parseAndDiscardColon(t, original);
                parseAndDiscardWord(t, original);
                andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.TRANSITIVE_PROPERTY));
                break;
            case REFLEXIVEOBJECTPROPERTY:
                parseAndDiscardOpenParen(t, original);
                parseAndDiscardColon(t, original);
                parseAndDiscardWord(t, original);
                andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.REFLEXIVE_PROPERTY));
                break;
            case SUBCLASSOF:
                andList.add(handleSubclassOf(logicalExpressionBuilder, t, original));
                break;
            default:
                throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf. ", t, original);
        }

        while (t.ttype != TT_EOF) {
            t.nextToken();
            if (t.ttype == TT_WORD) {
                handleNextObjectPropertyClause(logicalExpressionBuilder, t, original, andList);
                parseToCloseParen(t);
            }
        }

        return logicalExpressionBuilder.And(andList.toArray(new LogicalAxiom.Atom[andList.size()]));
    }

    private static void handleNextObjectPropertyClause(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original, List<LogicalAxiom.Atom> andList) throws IOException {
        switch (t.ttype) {
            case TT_EOF:
                break;
            case TT_WORD:
                switch (t.sval.toLowerCase()) {

                    case TRANSITIVEOBJECTPROPERTY:
                        parseAndDiscardOpenParen(t, original);
                        parseAndDiscardColon(t, original);
                        parseAndDiscardWord(t, original);
                        andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.TRANSITIVE_PROPERTY));
                        break;
                    case REFLEXIVEOBJECTPROPERTY:
                        parseAndDiscardOpenParen(t, original);
                        parseAndDiscardColon(t, original);
                        parseAndDiscardWord(t, original);
                        andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.REFLEXIVE_PROPERTY));
                        break;
                    case SUBCLASSOF:
                        andList.add(handleSubclassOf(logicalExpressionBuilder, t, original));
                        break;
                    default:
                        throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", t, original);
                }
                parseToCloseParen(t);
                break;
            default:
                throwIllegalStateException(t, original);
        }
    }

    private static void throwIllegalStateException(StreamTokenizer t, String original) {
        throwIllegalStateException(Optional.empty(), t, original);
    }

    private static void throwIllegalStateException(String prefix, StreamTokenizer t, String original) {
        throwIllegalStateException(Optional.of(prefix), t, original);
    }

    private static void throwIllegalStateException(Optional<String> prefix, StreamTokenizer t, String original) {
        StringBuilder sb = new StringBuilder();
        if (prefix.isPresent()) {
            sb.append(prefix.get());
            sb.append(" ");
        }
        sb.append("Found: ");
        switch (t.ttype) {
            case TT_EOF:
                sb.append("TT_EOF");
                break;
            case TT_EOL:
                sb.append("TT_EOL");
                break;
            case TT_NUMBER:
                sb.append("TT_NUMBER: ");
                sb.append(t.nval);
                break;
            case TT_WORD:
                sb.append("TT_WORD: ");
                sb.append(t.sval);
                break;
            default:
                for (char c : Character.toChars(t.ttype)) {
                    sb.append(c);
                }
        }
        sb.append("\nOriginal: ");
        sb.append(original);
        throw new IllegalStateException(sb.toString());
    }

    private static LogicalAxiom.Atom.Connective.And processPropertySet(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        List<LogicalAxiom.Atom> andList = new ArrayList<>();
        while (tokenizer.nextToken() != TT_EOF) {
            switch (tokenizer.ttype) {
                case TT_WORD:
                    switch (tokenizer.sval.toLowerCase()) {
                        case SUBDATAPROPERTYOF:
                        case SUBOBJECTPROPERTYOF:
                        case SUBANNOTATIONPROPERTYOF:
                            // SubObjectPropertyOf(ObjectPropertyChain(:127489000 :738774007) :127489000)
                            // SubPropertyOf( ObjectPropertyChain( :locatedIn :partOf ) :locatedIn )
                            // If x is located in y and y is part of z then x is located in z, for example a disease located in a part is located in the whole.
                            // If x is "located in" y and y is "part of" z then x is "located in" z, for example a disease located in a part is located in the whole.

                            // If X "located in" Y and Y "part of" Z then X "located in" Z, for example a disease located in a part is located in the whole.
                            // WHEN PATTERN THEN IMPLICATION
                            // PATTERN = NODE LIST? Ordered list as opposed to unordered and...
                            // SubObjectPropertyOf( ObjectPropertyChain( a:hasMother a:hasSister ) a:hasAunt )
                            //
                            // SubObjectPropertyOf(:738774007 :762705008)
                            if (tokenizer.nextToken() != '(') {
                                throwIllegalStateException("Expecting (.", tokenizer, original);
                            }
                            switch (tokenizer.nextToken()) {
                                case ':':
                                    // Skip concept id...
                                    if (tokenizer.nextToken() != '[') {
                                        throwIllegalStateException("Expecting PublicId.", tokenizer, original);
                                    }
                                    parseAndDiscardPublicId(tokenizer, original);
                                    if (tokenizer.nextToken() != ':') {
                                        throwIllegalStateException("Expecting :.", tokenizer, original);
                                    }
                                    if (tokenizer.nextToken() != '[') {
                                        throwIllegalStateException("Expecting PublicId.", tokenizer, original);
                                    }
                                    int conceptNid = ScopedValue
                                            .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                                            .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));
                                    andList.add(logicalExpressionBuilder.ConceptAxiom(conceptNid));
                                    parseToCloseParen(tokenizer);

                                    break;
                                case TT_WORD:
                                    if (!tokenizer.sval.equalsIgnoreCase(OBJECTPROPERTYCHAIN)) {
                                        throwIllegalStateException("Expected ObjectPropertyChain.", tokenizer, original);
                                    }
                                    andList.add(processObjectPropertyChain(logicalExpressionBuilder, tokenizer, original));
                                    parseToCloseParen(tokenizer);
                                    break;
                            }

                            break;

                        case REFLEXIVEOBJECTPROPERTY:
                            andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.REFLEXIVE_PROPERTY));
                            parseToCloseParen(tokenizer);
                            break;

                        case TRANSITIVEOBJECTPROPERTY:
                            andList.add(logicalExpressionBuilder.ConceptAxiom(TinkarTerm.TRANSITIVE_PROPERTY));
                            // TransitiveObjectProperty(:774081006)
                            parseToCloseParen(tokenizer);
                            break;

                        default:
                            throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                    }
                    break;

                default:
                    throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", tokenizer, original);
            }
        }
        return logicalExpressionBuilder.And(andList.toArray(new LogicalAxiom.Atom[andList.size()]));
    }

    private static LogicalAxiom.Atom processObjectPropertyChain(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        // parse pattern; then parse implication.
        // objectpropertychain
        // ObjectPropertyChain(:363701004 :738774007) :363701004

        if (tokenizer.nextToken() != '(') {
            throwIllegalStateException("Expected (.", tokenizer, original);
        }
        final List<ConceptFacade> propertyPatternList = new ArrayList<>();

        while (tokenizer.nextToken() == ':') {
            if (tokenizer.nextToken() != '[') {
                throwIllegalStateException("Expected PublicId.", tokenizer, original);
            }
            int conceptNid = ScopedValue
                    .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                    .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));
            Optional<? extends ConceptFacade> optionalPatternPart = EntityService.get().getEntity(conceptNid);

            if (optionalPatternPart.isPresent()) {
                propertyPatternList.add(optionalPatternPart.get());
            } else {
                throw new IllegalStateException("Pattern part not in database: " + tokenizer.sval);
            }
        }

        if (tokenizer.ttype != ')') {
            throwIllegalStateException("Expected ).", tokenizer, original);
        }
        if (tokenizer.nextToken() != ':') {
            throwIllegalStateException("Expected :.", tokenizer, original);
        }
        if (tokenizer.nextToken() != '[') {
            throwIllegalStateException("Expected PublicId.", tokenizer, original);
        }
        int propertyImplicationNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));
        Optional<ConceptEntity<ConceptEntityVersion>> optionalPropertyImplication
                = EntityService.get().getEntity(propertyImplicationNid);
        if (optionalPropertyImplication.isPresent()) {
            ConceptFacade propertyImplication = optionalPropertyImplication.get();
            ConceptFacade[] propertyPattern = new ConceptFacade[propertyPatternList.size()];
            for (int i = 0; i < propertyPattern.length; i++) {
                propertyPattern[i] = propertyPatternList.get(i);
            }
            return logicalExpressionBuilder.PropertySequenceImplicationAxiom(
                    Lists.immutable.of(propertyPattern),
                    propertyImplication);
        } else {
            throw new IllegalStateException("Pattern implication not in database: " + tokenizer.sval);
        }

    }

    private static void parseToCloseParen(StreamTokenizer tokenizer) throws IOException {

        while (tokenizer.ttype != ')' && tokenizer.ttype != TT_EOF) {
            // loop
            tokenizer.nextToken();
        }
    }


    private static LogicalAxiom.Atom.LogicalSet processSet(String priorToken, LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != '(') {
            throwIllegalStateException("Expecting (.", tokenizer, original);
        }
        switch (tokenizer.nextToken()) {
            case ':':
                break;
            case TT_WORD:
                switch (tokenizer.sval.toLowerCase()) {
                    case OBJECTINTERSECTIONOF:
                        // in this case, the order of the AND and is swapped, and the identifier of the
                        // component being defined comes last...
                        List<LogicalAxiom.Atom> andList = new ArrayList<>();
                        andList.addAll(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original));
                        // now an identifier for the concept being defined should be found...
                        if (tokenizer.nextToken() != ':') {
                            throw new IllegalStateException("Expecting :. Found: " + tokenizer + "\n" + original);
                        }
                        if (tokenizer.nextToken() != '[') {
                            throw new IllegalStateException("Expecting identifier. Found: " + tokenizer + "\n" + original);
                        }
                        parseAndDiscardPublicId(tokenizer, original);

                        return logicalExpressionBuilder.InclusionSet(logicalExpressionBuilder.And(andList.toArray(new LogicalAxiom.Atom[andList.size()])));

                    default:
                        throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                }

            default:
                throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", tokenizer, original);
        }
        if (tokenizer.nextToken() == '[') {
            // the identifier for the concept being defined.
            parseAndDiscardPublicId(tokenizer, original);
        } else {
            throwIllegalStateException("Expecting identifier.", tokenizer, original);
        }

        List<LogicalAxiom.Atom> andList = new ArrayList<>();
        // can be either ObjectIntersectionOf or : for single concept
        switch (tokenizer.nextToken()) {
            case ':':
                andList.add(getConceptAssertion(logicalExpressionBuilder, tokenizer, original));
                break;
            case TT_WORD:
                if (tokenizer.sval.equalsIgnoreCase(OBJECTINTERSECTIONOF)) {
                    andList.addAll(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original));
                } else {
                    throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                }

                break;
            default:
                throwIllegalStateException("Expecting identifier or ObjectIntersectionOf.", tokenizer, original);
        }

        // Necessary or sufficient...
        if (priorToken.equals(EQUIVALENTCLASSES)) {
            return logicalExpressionBuilder.SufficientSet(logicalExpressionBuilder.And(andList.toArray(new LogicalAxiom.Atom[andList.size()])));
        }
        return logicalExpressionBuilder.NecessarySet(logicalExpressionBuilder.And(andList.toArray(new LogicalAxiom.Atom[andList.size()])));
    }

    private static List<LogicalAxiom.Atom> processObjectIntersectionOf(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        List<LogicalAxiom.Atom> assertionList = new ArrayList<>();
        tokenizer.nextToken();
        while (tokenizer.ttype != ')') {
            switch (tokenizer.ttype) {
                case ':':
                    assertionList.add(getConceptAssertion(logicalExpressionBuilder, tokenizer, original));
                    break;
                case TT_WORD:
                    switch (tokenizer.sval.toLowerCase()) {
                        case OBJECTINTERSECTIONOF:
                            processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original);
                            break;
                        case OBJECTSOMEVALUESFROM:
                            assertionList.add(getSomeRole(logicalExpressionBuilder, tokenizer, original));
                            break;
                        case DATAHASVALUE:
                            assertionList.add(getDataHasValue(logicalExpressionBuilder, tokenizer, original));
                            break;
                    }
                    break;
                case TT_EOF:
                    throwIllegalStateException("Illegal EOF", tokenizer, original);
                    break;
            }

            tokenizer.nextToken();
        }
        //TODO finish...
        return assertionList;
    }

    private static LogicalAxiom.Atom.ConceptAxiom getConceptAssertion(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != '[') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting PublicId.", tokenizer, original);
        }
        int conceptNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));
        return logicalExpressionBuilder.ConceptAxiom(conceptNid);
    }

    private static LogicalAxiom.Atom.TypedAtom.Role getSomeRole(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != '(') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting (.", tokenizer, original);
        }
        if (tokenizer.nextToken() != ':') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting :.", tokenizer, original);
        }
        if (tokenizer.nextToken() != '[') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting PublicId String.", tokenizer, original);
        }
        int roleType = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));

        Optional<? extends ConceptFacade> optionalRoleType = EntityService.get().getEntity(roleType);

        if (optionalRoleType.isPresent()) {
            LogicalAxiom.Atom.TypedAtom.Role someRole = logicalExpressionBuilder.SomeRole(optionalRoleType.get(), getRestriction(logicalExpressionBuilder, tokenizer, original));
            if (tokenizer.nextToken() != ')') {
                // the identifier for the concept being defined.
                throwIllegalStateException("Expecting ).", tokenizer, original);
            }
            return someRole;
        } else {
            throw new IllegalStateException("Role type not in the database: " + tokenizer.sval);
        }
    }

    private static LogicalAxiom.Atom.TypedAtom.Feature getDataHasValue(LogicalExpressionBuilder logicalExpressionBuilder,
                                                                       StreamTokenizer tokenizer, String original) throws IOException {
        // DataHasValue(:1142135004 "60"^^xsd:decimal)
        if (tokenizer.nextToken() != '(') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting (.", tokenizer, original);
        }
        if (tokenizer.nextToken() != ':') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting :.", tokenizer, original);
        }
        if (tokenizer.nextToken() != '[') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting PublicId String.", tokenizer, original);
        }
        int conceptNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> PrimitiveData.nid(processPublicId(tokenizer, original)));

        Optional<? extends ConceptFacade> optionalDataType = EntityService.get().getEntity(conceptNid);

        if (optionalDataType.isPresent()) {
            // ConceptFacade featureType, ConceptFacade concreteDomainOperator,
            //                                                            Object literalValue
            LogicalAxiom.Atom.TypedAtom.Feature typedDataFeature =
                    logicalExpressionBuilder.FeatureAxiom(optionalDataType.get(),
                    TinkarTerm.EQUAL_TO,
                    getValue(logicalExpressionBuilder, tokenizer, original));
            return typedDataFeature;
        } else {
            throw new IllegalStateException("Role type not in the database: " + tokenizer.sval);
        }
    }

    private static Object getValue(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == '"') {
            String stringValue = tokenizer.sval;
            if (tokenizer.nextToken() != '^') {
                throwIllegalStateException("Expecting '^'.", tokenizer, original);
            }
            if (tokenizer.nextToken() != '^') {
                throwIllegalStateException("Expecting '^'.", tokenizer, original);
            }
            if (tokenizer.nextToken() != TT_WORD && !tokenizer.sval.equalsIgnoreCase("xsd")) {
                throwIllegalStateException("Expecting '^'.", tokenizer, original);
            }
            if (tokenizer.nextToken() != ':') {
                throwIllegalStateException("Expecting '^'.", tokenizer, original);
            }
            if (tokenizer.nextToken() == TT_WORD) {
                Object returnValue = switch (tokenizer.sval) {
                    case "datetime" ->
                        DateTimeUtil.epochMsToInstant(DateTimeUtil.parse(stringValue));
                    case "decimal", "double" -> Double.parseDouble(stringValue);
                    case "float" -> Float.parseFloat(stringValue);
                    case "integer" -> Integer.parseInt(stringValue);
                    case "string" -> stringValue;
                    case "boolean" -> Boolean.parseBoolean(stringValue);
                    default -> throw new IllegalStateException("Can't handle " + tokenizer.sval);
                };
                if (tokenizer.nextToken() != ')') {
                    throwIllegalStateException("Expecting ')'.", tokenizer, original);
                }
                return returnValue;
            } else {
                throwIllegalStateException("Expecting '^'.", tokenizer, original);
            }
        } else {
            throwIllegalStateException("Expecting '\"'.", tokenizer, original);
        }
        throw new IllegalStateException("Current token " + tokenizer.sval + " \n\nOriginal: " + original);
    }

    private static LogicalAxiom.Atom getRestriction(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        switch (tokenizer.nextToken()) {
            case ':':
                return getConceptAssertion(logicalExpressionBuilder, tokenizer, original);
            case TT_WORD:
                switch (tokenizer.sval.toLowerCase()) {
                    case OBJECTINTERSECTIONOF:
                        return logicalExpressionBuilder.And(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original).toArray(new LogicalAxiom.Atom[1]));
                    case OBJECTSOMEVALUESFROM:
                        return logicalExpressionBuilder.And(getSomeRole(logicalExpressionBuilder, tokenizer, original));
                }
            default:
                throwIllegalStateException(tokenizer, original);

        }
        throw new IllegalStateException("unreachable");
    }


    public static String logicalExpressionToSctOwlStr(LogicalExpression expression) {
        throw new UnsupportedOperationException();
    }


    private static void parseAndDiscardOpenParen(StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == '(') {
            return;
        }
        throwIllegalStateException("Expecting '('.", tokenizer, original);
    }

    private static void parseAndDiscardColon(StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == ':') {
            return;
        }
        throwIllegalStateException("Expecting ':'.", tokenizer, original);
    }

    private static void parseAndDiscardWord(StreamTokenizer tokenizer, String original) throws IOException {
        switch (tokenizer.nextToken()) {
            case TT_WORD:
                return;
            case '[':
                parseAndDiscardPublicId(tokenizer, original);
                break;
            default:
                throwIllegalStateException("Expecting TT_WORD.", tokenizer, original);
        }
    }


    private static void parseAndDiscardPublicId(StreamTokenizer tokenizer, String original) throws IOException {
        processPublicId(tokenizer, original);
    }

    private static PublicId processPublicId(StreamTokenizer tokenizer, String original) throws IOException {
        List<UUID> uuids = new ArrayList<>();
        while (tokenizer.nextToken() != ']') {
            switch (tokenizer.ttype) {
                case ',':
                    continue;
                case TT_WORD:
                    uuids.add(UUID.fromString(tokenizer.sval));
                    break;
                case TT_EOF:
                    throwIllegalStateException("Illegal EOF", tokenizer, original);
                    break;
                default:
                    throwIllegalStateException("Expecting ']' to end PublicId definition.", tokenizer, original);
            }
        }
        if (uuids.isEmpty()) {
            throwIllegalStateException("Expecting UUID.", tokenizer, original);
        }
        return PublicIds.of(uuids.toArray(new UUID[0]));
    }
}
