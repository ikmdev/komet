import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.set.SetNodeFactory;
import dev.ikm.komet.table.TableNodeFactory;

module dev.ikm.komet.list {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.list;
    exports dev.ikm.komet.list;

    opens dev.ikm.komet.set;
    exports dev.ikm.komet.set;

    opens dev.ikm.komet.table;
    exports dev.ikm.komet.table;

    provides KometNodeFactory
            with ListNodeFactory, SetNodeFactory, TableNodeFactory;

}