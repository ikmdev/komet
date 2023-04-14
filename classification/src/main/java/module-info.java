import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.reasoner.ReasonerResultsNodeFactory;

module dev.ikm.komet.classification {
    requires transitive dev.ikm.komet.framework;
    requires dev.ikm.tinkar.collection;
    requires org.roaringbitmap;
    requires org.jgrapht.core;

    opens dev.ikm.komet.reasoner;
    exports dev.ikm.komet.reasoner;
    exports dev.ikm.komet.reasoner.expression;
    opens dev.ikm.komet.reasoner.expression;
    exports dev.ikm.komet.reasoner.elk;
    opens dev.ikm.komet.reasoner.elk;
    exports dev.ikm.komet.reasoner.sorocket;
    opens dev.ikm.komet.reasoner.sorocket;

    provides KometNodeFactory
            with ReasonerResultsNodeFactory;

}