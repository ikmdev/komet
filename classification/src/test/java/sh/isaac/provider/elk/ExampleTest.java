package sh.isaac.provider.elk;

//import org.junit.jupiter.api.Test;
//import org.semanticweb.elk.owlapi.ElkReasonerFactory;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.*;
//import org.semanticweb.owlapi.reasoner.InferenceType;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;

class ExampleTest {
    /*
    @Test
    void incrementalClassify() {

        System.out.println(System.getProperty("user.dir"));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
// Load your ontology
            //OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/bevon-0.8.owl"));
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/RDF/OpenGALEN8_CRM.owl"));
// Create an ELK reasoner.
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            OWLReasoner reasoner = reasonerFactory.createReasoner(ont);
// Classify the ontology.
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLClass subClass = factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/galen#AbsoluteShapeState"));
            //OWLAxiom removed = factory.getOWLSubClassOfAxiom(subClass, factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/galen#ShapeState")));

            OWLAxiom added = factory.getOWLSubClassOfAxiom(subClass, factor y.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/galen#GeneralisedStructure")));
// Remove an existing axiom, add a new axiom
            manager.addAxiom(ont, added);
            //manager.removeAxiom(ont, removed);
// This is a buffering reasoner, so you need to flush the changes
            reasoner.flush();

// Re-classify the ontology, the changes should be accommodated
// incrementally (i.e. without re-inferring all subclass relationships)
// You should be able to see it from the log output
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

// Terminate the worker threads used by the reasoner.
            reasoner.dispose();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }
*/
}