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
package dev.ikm.komet.framework.panel.axiom;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Comparator;

/**
 * 
 */
public class AxiomComparator implements Comparator<ClauseView> {
    final DiTree<EntityVertex> diGraph;
    final ViewProperties viewProperties;

    public AxiomComparator(DiTree<EntityVertex> diGraph, ViewProperties viewProperties) {
        this.diGraph = diGraph;
        this.viewProperties = viewProperties;
    }

    @Override
    public int compare(ClauseView o1, ClauseView o2) {
        EntityVertex vertex1 = o1.axiomVertex;
        EntityVertex vertex2 = o2.axiomVertex;

        int x = compare(vertex1, vertex2);
        if (x != 0) return x;
        return o1.titleLabel.getText().compareTo(o2.titleLabel.getText());
    }

    private int compare(EntityVertex vertex1, EntityVertex vertex2) {
        if (vertex1.getMeaningNid() != vertex2.getMeaningNid()) {
            if (vertex1.getMeaningNid() == TinkarTerm.AND.nid()) {
                return -1;
            }
            if (vertex2.getMeaningNid() == TinkarTerm.AND.nid()) {
                return 1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid()) {
                return -1;
            }
            if (vertex2.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid()) {
                return 1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.NECESSARY_SET.nid()) {
                return -1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.NECESSARY_SET.nid()) {
                return 1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                return -1;
            }
            if (vertex2.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                return 1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.ROLE.nid()) {
                return -1;
            }
            if (vertex2.getMeaningNid() == TinkarTerm.ROLE.nid()) {
                return 1;
            }
            if (vertex1.getMeaningNid() == TinkarTerm.FEATURE.nid()) {
                return -1;
            }
            if (vertex2.getMeaningNid() == TinkarTerm.FEATURE.nid()) {
                return 1;
            }
        } else {
            // Meanings are the same... Compare on the details...
            // get children nodes.
            ImmutableList<EntityVertex> children1 = diGraph.successors(vertex1);
            ImmutableList<EntityVertex> children2 = diGraph.successors(vertex2);

            if (vertex1.getMeaningNid() == TinkarTerm.NECESSARY_SET.nid() ||
                    vertex1.getMeaningNid() == TinkarTerm.AND.nid() ||
                    vertex1.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid() ||
                    vertex1.getMeaningNid() == TinkarTerm.NECESSARY_SET.nid() ||
                    vertex1.getMeaningNid() == TinkarTerm.FEATURE.nid()) {
                if (children1.isEmpty()) {
                    if (children2.isEmpty()) {
                        return 0;
                    }
                    return -1;
                }
                if (children2.isEmpty()) {
                    return 1;
                }
                return children1.size() - children2.size();
            }

            if (vertex1.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                ConceptFacade concept1 = vertex1.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                ConceptFacade concept2 = vertex2.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                return viewProperties.calculator().getDescriptionTextOrNid(concept1).compareTo(
                        viewProperties.calculator().getDescriptionTextOrNid(concept2)
                );
            }


            // Node is a role of some type...
            if (vertex1.getMeaningNid() == TinkarTerm.ROLE.nid() &&
                    vertex2.getMeaningNid() == TinkarTerm.ROLE.nid()) {
                ConceptFacade roleOperator1 = vertex1.propertyFast(TinkarTerm.ROLE_OPERATOR);
                ConceptFacade roleType1 = vertex1.propertyFast(TinkarTerm.ROLE_TYPE);
                ConceptFacade roleOperator2 = vertex2.propertyFast(TinkarTerm.ROLE_OPERATOR);
                ConceptFacade roleType2 = vertex2.propertyFast(TinkarTerm.ROLE_TYPE);

                if (roleOperator1.nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid() &&
                        roleOperator2.nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid()) {
                    if (roleType1.nid() == roleType2.nid()) {
                        // need to access children...
                        if (children1.isEmpty()) {
                            if (children2.isEmpty()) {
                                return 0;
                            }
                            return -1;
                        }
                        if (children2.isEmpty()) {
                            return 1;
                        }
                        if (children1.size() == children2.size()) {
                            return compare(children1.get(0), children2.get(0));
                        }
                        return children1.size() - children2.size();
                    }
                    if (roleType1.nid() == TinkarTerm.ROLE_GROUP.nid() &&
                            roleType2.nid() != TinkarTerm.ROLE_GROUP.nid()) {
                        return -1;
                    }
                    if (roleType2.nid() == TinkarTerm.ROLE_GROUP.nid() &&
                            roleType1.nid() != TinkarTerm.ROLE_GROUP.nid()) {
                        return 1;
                    }
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(roleType1),
                            viewProperties.calculator().getDescriptionTextOrNid(roleType2));
                }
                if (roleOperator1.nid() == TinkarTerm.UNIVERSAL_RESTRICTION.nid()) {
                    return -1;
                }
                if (roleOperator2.nid() == TinkarTerm.UNIVERSAL_RESTRICTION.nid()) {
                    return 1;
                }
            }
            throw new UnsupportedOperationException("Can't sort \no1: " + vertex1 + "\n\no2: " + vertex2);
        }
        return 0;
    }

}
