import dev.ikm.komet.builder.ConceptBuilderNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.builder {
 //   requires static dev.ikm.jpms.autoservice;
    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.builder;
    exports dev.ikm.komet.builder;
    
    provides KometNodeFactory
            with ConceptBuilderNodeFactory;

}