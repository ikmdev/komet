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

public enum ConcreteDomainOperators {
    /**
     * The equals.
     */
    EQUALS("="),

    /**
     * The less than.
     */
    LESS_THAN("<"),

    /**
     * The less than equals.
     */
    LESS_THAN_EQUALS("≤"),

    /**
     * The greater than.
     */
    GREATER_THAN(">"),

    /**
     * The greater than equals.
     */
    GREATER_THAN_EQUALS("≥");

    final String symbol;

    private ConcreteDomainOperators(String symbol) {
        this.symbol = symbol;
    }


    @Override
    public String toString() {
        return symbol;
    }
}
