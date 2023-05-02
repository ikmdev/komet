import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.details.concept.ConceptDetaisNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.details {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.details;
    exports dev.ikm.komet.details;
    exports dev.ikm.komet.details.concept;
    uses dev.ikm.tinkar.common.alert.AlertReportingService;

    provides KometNodeFactory
            with DetailsNodeFactory, ConceptDetaisNodeFactory;

}