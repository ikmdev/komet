import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.search.SearchNodeFactory;

module dev.ikm.komet.search {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.search;
    exports dev.ikm.komet.search;
    
    provides KometNodeFactory
            with SearchNodeFactory;

}