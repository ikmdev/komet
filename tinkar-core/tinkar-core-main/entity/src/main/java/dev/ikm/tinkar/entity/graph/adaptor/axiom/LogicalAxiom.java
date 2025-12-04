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
package dev.ikm.tinkar.entity.graph.adaptor.axiom;

import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

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
         * Property Sequence -> Implication is Tinkar class for implementing property chains.
         * <p>
         * A property chain is a rule that allows you to infer the existence of a property (the implication)
         * from a chain of properties (the property sequence). For example, "x has parent y" and "y has parent z"
         * implies "x has grandparent z" (which may be written as |has parent|ο|has parent|→|has grandparent|).
         * SNOMED CT includes the property chain:
         * <p>
         * |direct substance|ο|has active ingredient|→|direct substance|
         */
        sealed interface PropertySequenceImplication extends Atom permits LogicalAxiomAdaptor.PropertySequenceImplicationAdaptor {
            ImmutableList<ConceptFacade> propertySequence();

            ConceptFacade implication();

            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.PROPERTY_SEQUENCE_IMPLICATION;
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
            
            sealed interface IntervalRole extends TypedAtom permits LogicalAxiomAdaptor.IntervalRoleAxiomAdaptor {

                String interval();

                default LogicalAxiomSemantic axiomSemantic() {
                    return LogicalAxiomSemantic.INTERVAL_ROLE;
                }

            }

            sealed interface Feature extends TypedAtom permits LogicalAxiomAdaptor.FeatureAxiomAdaptor {

                Object literal();

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
    }

    sealed interface DefinitionRoot extends LogicalAxiom permits LogicalAxiomAdaptor.DefinitionRootAdaptor {
        ImmutableSet<LogicalSet> sets();

        default LogicalAxiomSemantic axiomSemantic() {
            return LogicalAxiomSemantic.DEFINITION_ROOT;
        }

    }

    sealed interface LogicalSet extends LogicalAxiom {
        ImmutableSet<Atom.Connective> elements();

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
        
        sealed interface DataPropertySet extends LogicalSet permits LogicalAxiomAdaptor.DataPropertySetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.DATA_PROPERTY_SET;
            }
        }

        
        sealed interface IntervalPropertySet extends LogicalSet permits LogicalAxiomAdaptor.IntervalPropertySetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.INTERVAL_PROPERTY_SET;
            }
        }

        sealed interface InclusionSet extends LogicalSet permits LogicalAxiomAdaptor.InclusionSetAdaptor {
            default LogicalAxiomSemantic axiomSemantic() {
                return LogicalAxiomSemantic.INCLUSION_SET;
            }
        }
    }

}
