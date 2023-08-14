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

import java.math.BigInteger;

/**
 * @author Alejandro Metke
 *
 */
public class BigIntegerLiteral extends AbstractLiteral {
    private final BigInteger value;

    /**
     * Constructor.
     * 
     * @param type
     */
    public BigIntegerLiteral(BigInteger value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public BigInteger getValue() {
        return value;
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
        BigIntegerLiteral other = (BigIntegerLiteral) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int compareTo(AbstractLiteral o) {
        return value.compareTo(((BigIntegerLiteral) o).value);
    }
}
