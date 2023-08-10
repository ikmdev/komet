import dev.ikm.komet.amplifydetails.AmplifyDetailsNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.amplifydetails {

    requires transitive dev.ikm.komet.framework;
    opens dev.ikm.komet.amplifydetails;
    exports dev.ikm.komet.amplifydetails;
    
    provides KometNodeFactory
            with AmplifyDetailsNodeFactory;

}