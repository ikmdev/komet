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
package au.csiro.ontology.model;


/**
 * A datatype expression that represents a set of individuals that have a
 * property with a certain value. The expression consists of a feature, an
 * operator (=, <, <=, >, >=), and a literal value.
 *
 * @author Alejandro Metke
 */

public class Datatype extends Concept {

    private static final long serialVersionUID = 1L;

    private Feature feature;

    private Operator operator;

    private Literal literal;

    /**
     *
     */
    public Datatype() {

    }

    /**
     * @param feature
     * @param operator
     * @param literal
     */
    public Datatype(Feature feature, Operator operator, Literal literal) {
        this.feature = feature;
        this.operator = operator;
        this.literal = literal;
    }

    public Feature getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Operator getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Literal getLiteral() {
        return literal;
    }

    /**
     * @param literal the literal to set
     */
    public void setLiteral(Literal literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return feature + ".(" + operator + "," + literal + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        result = prime * result + ((literal == null) ? 0 : literal.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
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

    @SuppressWarnings({"rawtypes"})
    public int compareTo(Concept o) {
        Class thisClass = this.getClass();
        Class otherClass = o.getClass();
        if (thisClass.equals(otherClass)) {
            Datatype other = (Datatype) o;
            int res = 0;
            res = feature.compareTo(other.feature);
            if (res != 0) return res;
            res = operator.compareTo(other.operator);
            if (res != 0) return res;
            res = literal.compareTo(other.literal);
            return res;
        } else {
            return thisClass.toString().compareTo(otherClass.toString());
        }
    }

}
