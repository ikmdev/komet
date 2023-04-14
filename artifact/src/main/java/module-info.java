import dev.ikm.komet.artifact.ArtifactExportNodeFactory;
import dev.ikm.komet.artifact.ArtifactImportNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.artifact {

 //   requires static dev.ikm.jpms.autoservice;
    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.artifact;
    exports dev.ikm.komet.artifact;
    
    provides KometNodeFactory
            with ArtifactExportNodeFactory, ArtifactImportNodeFactory;

}