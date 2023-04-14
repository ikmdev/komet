package dev.ikm.komet.reasoner;

/**
 * The Interface SubstitutionFieldSpecification.
 * <p>
 * Placeholder for now. Implementation would pull value from a semantic when
 * extracting axioms.
 *
 * @author kec
 */
public interface SubstitutionFieldSpecification
        extends Comparable<SubstitutionFieldSpecification> {
    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    byte[] getBytes();
}

