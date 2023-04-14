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
