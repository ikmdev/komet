import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.navigator.pattern.PatternNavigatorFactory;

module dev.ikm.komet.navigator {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.navigator.graph;
    opens dev.ikm.komet.navigator.pattern;
    exports dev.ikm.komet.navigator.pattern;
    exports dev.ikm.komet.navigator.graph;

    provides KometNodeFactory
            with GraphNavigatorNodeFactory,
                    PatternNavigatorFactory;

}