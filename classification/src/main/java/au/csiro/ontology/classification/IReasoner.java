package au.csiro.ontology.classification;

import au.csiro.ontology.Ontology;
import au.csiro.ontology.model.Axiom;
import dev.ikm.tinkar.common.service.TrackingCallable;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * This interface represents the functionality of a reasoner. It uses the internal ontology model. This interface is
 * included in this package because it could have several implementations.
 *
 * @author Alejandro Metke
 */
public interface IReasoner {

    /**
     * Loads and indexes the supplied axioms.
     *
     * @param axioms
     * @param trackingCallable
     */
    public void loadAxioms(Set<Axiom> axioms, TrackingCallable trackingCallable);

    /**
     * Loads and indexes the supplied axioms.
     *
     * @param axioms
     * @param trackingCallable
     */
    public void loadAxioms(Iterator<Axiom> axioms, TrackingCallable trackingCallable);

    /**
     * Loads and indexes the supplied axioms.
     *
     * @param ont
     * @param trackingCallable
     */
    public void loadAxioms(Ontology ont, TrackingCallable trackingCallable);

    /**
     * Classifies the current axioms.
     *
     * @param trackingCallable
     * @return
     */
    public IReasoner classify(TrackingCallable trackingCallable);

    /**
     * Removes all the state in the classifier except the taxonomy generated
     * after classification. If the classification process has not been run then
     * this method has no effect. Once pruned, it is no longer possible to run
     * an incremental classification. Doing so will generate a
     * {@link RuntimeException}.
     */
    public void prune();

    /**
     * Returns an {@link IOntology} that represents the generated taxonomy.
     * If no axioms have yet been classified it throws a
     * {@link RuntimeException}.
     *
     * @param trackingCallable
     * @return The classified ontology.
     */
    public Ontology getClassifiedOntology(TrackingCallable trackingCallable);

    /**
     * Returns an {@link IOntology} that represents the generated taxonomy.
     * If no axioms have yet been classified it throws a
     * {@link RuntimeException}.
     *
     * @return The classified ontology.
     */
    public Ontology getClassifiedOntology(Ontology ont, TrackingCallable trackingCallable);

    /**
     * Saves this reasoner to the specified {@link OutputStream}.
     *
     * @param out The {@link OutputStream}.
     */
    public void save(OutputStream out);

    /**
     * Determines if the ontology has been classified.
     *
     * @return boolean True if the ontology has been classified or false if it
     * hasn't.
     */
    public boolean isClassified();

}
