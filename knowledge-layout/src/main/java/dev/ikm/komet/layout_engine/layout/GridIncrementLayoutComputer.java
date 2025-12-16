package dev.ikm.komet.layout_engine.layout;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.FeatureKey.ChronologyFeature;
import dev.ikm.komet.framework.observable.FeatureKey.ChronologyFeature.Chronology;
import dev.ikm.komet.framework.observable.FeatureKey.ChronologyFeature.PublicId;
import dev.ikm.komet.framework.observable.FeatureKey.ChronologyFeature.Version;
import dev.ikm.komet.framework.observable.FeatureKey.ChronologyFeature.VersionSet;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature.Pattern;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature.Semantic.FieldList;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature.Semantic.FieldListItem;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature.Stamp;
import dev.ikm.komet.framework.observable.FeatureKey.VersionFeature.VersionStamp;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KnowledgeLayout;
import dev.ikm.komet.layout.LayoutComputer;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.GridStep;
import dev.ikm.komet.layout.area.GridStepper;
import dev.ikm.komet.layout.component.KlChronologyArea;
import dev.ikm.komet.layout_engine.component.area.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GridIncrementLayoutComputer is an abstract class designed to compute and manage
 * grid-based layouts incrementally. It serves as a foundational class, providing
 * a structure for defining specific grid stepping behavior for layout management.
 * Implementing classes are expected to specify the stepping strategy by overriding
 * the abstract `step` method.
 * <p>
 * This class interacts with a designated KlWidget instance, tying its lifecycle to
 * the widget by setting itself as the layout computer during construction and clearing
 * the reference during closure.
 * <p>
 * Responsibilities of this class include:
 * <p> - Managing the computation of layout positions for a set of attribute locators.
 * <p> - Utilizing the provided `GridStepper` instance internally to increment and
 * <p>   decrement row and column positions based on specific grid stepping logic.
 * <p> - Handling different layout categories to determine the type of factories
 * (e.g., KlAttributeAreaFactory, KlSupplementalAreaFactory) required for creating
 * layout components.
 * <p>
 * Key Methods:
 * <p> - `create`: Accepts a list of attribute locators to generate the layout configuration
 * for the grid. It processes various layout categories and configures the appropriate
 * factories based on the category.
 * <p> - `step`: Abstract method to be implemented by subclasses to define the specific grid
 * stepping behavior (e.g., row-wise or column-wise increment).
 * <p>
 * Nested Class:
 * <p> - `Stepper`: A concrete implementation of the GridStepper interface to handle low-level
 * grid position operations. It tracks and manipulates the current row and column values
 * for grid layouts.
 */

public abstract class GridIncrementLayoutComputer implements LayoutComputer {
    private static final Logger LOG = LoggerFactory.getLogger(GridIncrementLayoutComputer.class);

    private final KnowledgeLayout masterLayout;
    public GridIncrementLayoutComputer(KnowledgeLayout masterLayout) {
        this.masterLayout = masterLayout;
    }

    @Override
    public KnowledgeLayout masterLayout() {
        return masterLayout;
    }

    protected abstract GridStep step();

    public ImmutableList<LayoutElement> layout(ImmutableList<? extends Feature> features,
                                               LayoutKey.AreaKeyProvider areaKeyProvider) {
        MutableList<LayoutElement> layoutList = Lists.mutable.empty();
        LayoutKey.ForArea layoutKeyForForArea = areaKeyProvider.make(this);

        GridStepper stepper = new Stepper(step());

        features.forEach(feature -> {
            // The layout computer can decide the type of factory based on the feature, or
            // can simply default to a generic layout choice known to the layout computer, and
            // all refinement by the user.

            Class<? extends KlArea.Factory> factoryClass = switch (feature.featureKey()) {
                case ChronologyFeature chronologyProperty -> handleChronologyProperty(chronologyProperty);
                case VersionFeature versionProperty -> handleVersionProperty(versionProperty);
            };

            AreaGridSettings areaGridSettings = layoutOverrides().getOrDefault(
                    stepper.nextForFeature(layoutKeyForForArea, feature.featureKey(), factoryClass));

            layoutList.add(new LayoutElement(areaGridSettings, feature));

            if (feature.featureKey() instanceof VersionSet ||
                    feature.featureKey()  instanceof FieldList ||
                    feature.featureKey()  instanceof Pattern.FieldDefinitionList) {
                // Add a filler below each list
                AreaGridSettings fillerAreaGridSettings = layoutOverrides().getOrDefault(stepper.nextForSupplemental(layoutKeyForForArea, MultiVersionArea.Factory.class.getName()));
                layoutList.add(new LayoutElement(fillerAreaGridSettings));
            }
        });

        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\nLayout for area: ").append(areaKeyProvider.toString());
            sb.append("\n");
            sb.append("Features: ");
            sb.append("\n");
            features.forEach(feature ->
                    sb.append("    ").append(feature.featureKey().toString()).append(": ")
                            .append(feature.featureProperty().getValue()).append("\n"));

            sb.append("\n");
            sb.append("Layout Elements: ");
            sb.append("\n");
            layoutList.forEach(layoutElement ->

                    sb.append("    ")
                      .append(layoutElement.optionalFeature().isPresent() ? layoutElement.optionalFeature().get() + "\n    ": "")
                      .append(layoutElement.areaGridSettings().toString()).append("\n\n"));
            LOG.info(sb.toString());

        }
        return layoutList.toImmutable();
    }

    private static Class<? extends KlArea.Factory> handleChronologyProperty(ChronologyFeature chronologyFeature) {
        return switch (chronologyFeature) {
            case Chronology _ -> KlChronologyArea.Factory.class;
            case PublicId _ -> PublicIdArea.Factory.class;
            case ChronologyFeature.Semantic.Pattern _,
                 ChronologyFeature.Semantic.ReferencedComponent _ -> GenericArea.Factory.class;
            case Version _ -> SimpleVersionArea.Factory.class;
            case VersionSet _ -> SimpleVersionList.Factory.class;
        };
    }

    private static Class<? extends KlArea.Factory> handleVersionProperty(VersionFeature version) {
        return switch (version) {
            case VersionFeature.Semantic semantic -> switch (semantic) {
                case FieldList _,
                     FieldListItem _ -> GenericArea.Factory.class;
            };

            case Stamp stamp -> switch (stamp) {
                case Stamp.Author _,
                     Stamp.Module _,
                     Stamp.Status _,
                     Stamp.Time _,
                     Stamp.Path _ -> GenericArea.Factory.class;
            };

            case Pattern pattern -> switch (pattern) {
                case Pattern.FieldDefinitionList _,
                     Pattern.PatternMeaning _,
                     Pattern.PatternPurpose _,
                     Pattern.FieldDefinitionListItem _ -> GenericArea.Factory.class;
            };

            case VersionStamp _ -> GenericArea.Factory.class;
        };
    }


    /**
     * The Stepper class provides an implementation of the GridStepper interface.
     * It maintains and manipulates a two-dimensional grid layout by tracking
     * the current row and column positions. The positions can be incremented,
     * decremented, reset or retrieved, while adhering to the GridStepper contract.
     */
    public static class Stepper implements GridStepper {
        final private AtomicInteger row = new AtomicInteger(0);
        final private AtomicInteger column = new AtomicInteger(0);
        private GridStep step = GridStep.ROW;

        public Stepper() {
        }

        public Stepper(GridStep step) {
            this.step = step;
        }

        @Override
        public void setStep(GridStep step) {
            this.step = step;
        }

        @Override
        public void reset() {
            row.set(0);
            column.set(0);
        }

        @Override
        public int row() {
            return row.get();
        }

        @Override
        public int column() {
            return column.get();
        }

        @Override
        public AreaGridSettings nextForFeature(LayoutKey.ForArea layoutKeyForArea, FeatureKey propertyLocator, String factoryName) {
            increment();
            LayoutKey.Property layoutKeyForProperty = layoutKeyForArea.makePropertyLayoutKey(propertyLocator);
            return new AreaGridSettings(column(), row(), layoutKeyForProperty.forArea(), factoryName);
        }

        @Override
        public AreaGridSettings nextForFeature(LayoutKey.ForArea forAreaLayoutKey, FeatureKey locator, Class factoryClass) {
            return nextForFeature(forAreaLayoutKey, locator, factoryClass.getName());
        }

        @Override
        public AreaGridSettings nextForSupplemental(LayoutKey.ForArea forAreaLayoutKey, Class factoryClass) {
            return nextForSupplemental(forAreaLayoutKey, factoryClass.getName());
        }

        @Override
        public AreaGridSettings nextForSupplemental(LayoutKey.ForArea layoutKeyForArea, String factoryName) {
            increment();
            AreaGridSettings areaGridSettings = new AreaGridSettings(this, LayoutKey.EMPTY, factoryName);
            LayoutKey.Supplemental supplementalLayoutKey = layoutKeyForArea.makeSupplementalLayoutKey(areaGridSettings);
            return areaGridSettings.withLayoutKeyForArea(supplementalLayoutKey.forArea());
        }

        private void increment() {
            switch (step) {
                case ROW -> row.incrementAndGet();
                case COLUMN -> column.incrementAndGet();
                case ROW_AND_COLUMN -> {
                    row.incrementAndGet();
                    column.incrementAndGet();
                }
            }
        }
    }
}
