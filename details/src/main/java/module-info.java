import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.details {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.details;
    exports dev.ikm.komet.details;
    exports dev.ikm.komet.details.concept;

    provides KometNodeFactory
        with DetailsNodeFactory; //, ConceptDetailsNodeFactory; // IKM-544 hide concept detail node from option
}