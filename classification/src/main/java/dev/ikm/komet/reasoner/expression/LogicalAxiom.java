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
package dev.ikm.komet.reasoner.expression;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.time.Instant;
import java.util.UUID;

public sealed interface LogicalAxiom permits LogicalAxiom.Atom, LogicalAxiom.DefinitionRoot,
        LogicalAxiom.LogicalSet, LogicalAxiomAdaptor {
    int vertexIndex();

    UUID vertexUUID();

    LogicalAxiomSemantic axiomSemantic();

    /**
     * An object that is not a set, but that may be an element of a set.
     * https://en.wikipedia.org/wiki/Urelement uses Atom as an alternative name for ur-element
     */
    sealed interface Atom extends LogicalAxiom {

        sealed interface Connective extends Atom {
            ImmutableSet<Atom> elements();

            sealed interface And extends Connective permits LogicalAxiomAdaptor.AndAdaptor {
                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.AND;
                }
            }

            sealed interface Or extends Connective permits LogicalAxiomAdaptor.OrAdaptor {
                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.OR;
                }
            }
        }

        sealed interface ConceptAxiom extends Atom permits LogicalAxiomAdaptor.ConceptAxiomAdaptor {
            ConceptFacade concept();

            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.CONCEPT;
            }
        }

        sealed interface DisjointWithAxiom extends Atom permits LogicalAxiomAdaptor.DisjointWithAxiomAdaptor {
            ConceptFacade disjointWith();

            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.DISJOINT_WITH;
            }

        }


        /**
         * Property Pattern -> Implication is Tinkar class for implementing property chains.
         * <p>
         * A property chain is a rule that allows you to infer the existence of a property (the implication)
         * from a chain of properties (the property pattern). For example, "x has parent y" and "y has parent z"
         * implies "x has grandparent z" (which may be written as |has parent|ο|has parent|→|has grandparent|).
         * SNOMED CT includes the property chain:
         * <p>
         * |direct substance|ο|has active ingredient|→|direct substance|
         */
        sealed interface PropertyPatternImplication extends Atom permits LogicalAxiomAdaptor.PropertyPatternImplicationAdaptor {
            ImmutableList<ConceptFacade> propertyPattern();

            ConceptFacade implication();

            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.PROPERTY_PATTERN_IMPLICATION;
            }

        }

        sealed interface TypedAtom extends Atom {
            ConceptFacade type();

            sealed interface Role extends TypedAtom permits LogicalAxiomAdaptor.RoleAxiomAdaptor {
                ConceptFacade roleOperator();

                Atom restriction();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.ROLE;
                }

            }

            sealed interface Feature extends TypedAtom permits LogicalAxiomAdaptor.FeatureAxiomAdaptor {

                Literal literal();

                /**
                 * Greater than, less than, equal...
                 *
                 * @return The concept representing the operator.
                 */
                ConceptFacade concreteDomainOperator();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.FEATURE;
                }

            }
        }

        sealed interface Literal extends Atom {

            sealed interface LiteralBoolean extends Literal permits LogicalAxiomAdaptor.LiteralBooleanAdaptor {
                boolean value();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.LITERAL_BOOLEAN;
                }

            }

            sealed interface LiteralFloat extends Literal permits LogicalAxiomAdaptor.LiteralFloatAdaptor {
                float value();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.LITERAL_FLOAT;
                }

            }

            sealed interface LiteralInstant extends Literal permits LogicalAxiomAdaptor.LiteralInstantAdaptor {
                Instant value();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.LITERAL_INSTANT;
                }

            }

            sealed interface LiteralInteger extends Literal permits LogicalAxiomAdaptor.LiteralIntegerAdaptor {
                int value();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.LITERAL_INTEGER;
                }

            }

            sealed interface LiteralString extends Literal permits LogicalAxiomAdaptor.LiteralStringAdaptor {
                String value();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.LITERAL_STRING;
                }

            }
        }
    }

    sealed interface DefinitionRoot extends LogicalAxiom permits LogicalAxiomAdaptor.DefinitionRootAdaptor {
        ImmutableSet<LogicalSet> sets();

        default LogicalAxiomSemantic axiomSemantic() {
            return LogicalAxiomSemantic.DEFINITION_ROOT;
        }

    }

    sealed interface LogicalSet extends LogicalAxiom {
        ImmutableSet<Atom> elements();

        sealed interface NecessarySet extends LogicalSet permits LogicalAxiomAdaptor.NecessarySetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.NECESSARY_SET;
            }
        }

        sealed interface SufficientSet extends LogicalSet permits LogicalAxiomAdaptor.SufficientSetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.SUFFICIENT_SET;
            }
        }

        sealed interface PropertySet extends LogicalSet permits LogicalAxiomAdaptor.PropertySetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.PROPERTY_SET;
            }
        }
    }

}
