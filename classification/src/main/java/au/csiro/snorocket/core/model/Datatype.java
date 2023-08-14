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
package au.csiro.snorocket.core.model;

import au.csiro.ontology.model.Operator;


/**
 * A datatype expression that represents a set of individuals that have a property with a certain value. The expression 
 * consists of a feature and a literal value. The literal value is a range of values.
 * 
 * @author Alejandro Metke
 * 
 */
public class Datatype extends AbstractConcept {
    
    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    
    private int feature;
    private Operator operator;
    private AbstractLiteral literal;

    /**
         * 
         */
    public Datatype(int feature, Operator operator, AbstractLiteral literal) {
        this.feature = feature;
        this.operator = operator;
        this.literal = literal;
    }

    public int getFeature() {
        return feature;
    }

    public Operator getOperator() {
        return operator;
    }

    public AbstractLiteral getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return feature + ".(" + operator + "," + literal + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + feature;
        result = prime * result + ((literal == null) ? 0 : literal.hashCode());
        result = prime * result
                + ((operator == null) ? 0 : operator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Datatype other = (Datatype) obj;
        if (feature != other.feature)
            return false;
        if (literal == null) {
            if (other.literal != null)
                return false;
        } else if (!literal.equals(other.literal))
            return false;
        if (operator != other.operator)
            return false;
        return true;
    }

    @Override
    int compareToWhenHashCodesEqual(AbstractConcept other) {
        assert hashCode() == other.hashCode();
        assert other instanceof Datatype;

        final Datatype otherDatatype = (Datatype) other;

        final int featureCompare = feature - otherDatatype.feature;

        if (featureCompare == 0) {
            final int operatorCompare = operator.compareTo(
                    otherDatatype.operator);
            if (operatorCompare == 0) {
                return literal.toString().compareTo(otherDatatype.literal.toString());
            } else {
                return operatorCompare;
            }
        } else {
            return featureCompare;
        }
    }

}
