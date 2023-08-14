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

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * This class represents a date literal.
 *
 * @author Alejandro Metke
 */

public class DateLiteral extends Literal {

    private static final long serialVersionUID = 1L;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Calendar value;

    /**
     *
     */
    public DateLiteral() {

    }

    /**
     * @param value
     */
    public DateLiteral(Calendar value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public Calendar getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Calendar value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return sdf.format(value.getTime());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        DateLiteral other = (DateLiteral) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(Literal o) {
        DateLiteral dl = (DateLiteral) o;
        Calendar otherValue = dl.value;
        return value.compareTo(otherValue);
    }

}
