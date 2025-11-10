package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.*;
import dev.ikm.komet.layout.component.KlChronologyArea;
import dev.ikm.komet.layout.component.KlMultiComponentArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.layout.area.KlMultiVersionArea;
import dev.ikm.komet.layout.area.KlAreaForVersion;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.window.KlRenderView;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Region;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code KlArea} interface represents a fundamental building block within
 * the Knowledge Layout framework, intended for managing and organizing user interface
 * regions. This sealed interface defines the contract for various specialized
 * pane or area types, enabling structured, type-safe layouts and interactions
 * within the framework.
 * <p>
 * Implementations of this interface can serve distinct purposes, such as managing
 * components, versions, fields, or supplemental layouts. By leveraging a type-safe
 * hierarchy, the {@code KlArea} interface ensures consistent and extensible design
 * across different areas of the framework.
 * <p>
 * <b>Design Rationale: Why {@code Region} instead of {@code Parent}?</b>
 * <p>
 * The type parameter {@code FX} is constrained to {@code Region} rather than {@code Parent} for several critical reasons:
 * <ul>
 *   <li><b>CSS Styling Support:</b> {@code Region} provides full CSS styling capabilities (backgrounds, borders, padding),
 *       which {@code Parent} lacks. This enables consistent visual theming across the application.</li>
 *   <li><b>Bindable Dimension Properties:</b> {@code Region} exposes dimension properties ({@code prefWidthProperty()},
 *       {@code prefHeightProperty()}, etc.) that can be bound to other properties for reactive layout. {@code Parent}
 *       only provides dimension methods ({@code prefWidth()}, {@code prefHeight()}) which return values but cannot
 *       be bound, making dynamic layouts difficult.</li>
 *   <li><b>Layout Management:</b> {@code Region} provides sophisticated layout capabilities including padding, content area
 *       management, and insets, which are essential for proper UI component spacing and organization.</li>
 *   <li><b>Visual Customization:</b> {@code Region} supports backgrounds, borders, shapes, and pixel snapping for crisp
 *       rendering, enabling rich visual customization of areas.</li>
 * </ul>
 * The requirement for bindable properties was the primary driver—JavaFX's reactive programming model depends on
 * property binding, and {@code Parent}'s lack of property accessors makes it unsuitable for dynamic, data-driven layouts.
 * <p>
 * @param <FX> the type of JavaFX {@code Region} that serves as the base node for this area
 * <p>
 * @see KlChronologyArea
 * @see KlAreaForVersion
 * @see KlSupplementalArea
 */
public sealed interface KlArea<FX extends Region>
        extends KlView<FX>, KlPeerToRegion<FX>
        permits KlParent, KlAreaForAssociation, KlAreaForFeature, KlAreaForList, KlAreaForVersion,
        KlGenericArea, KlMultiVersionArea, KlSupplementalArea, KlChronologyArea, KlMultiComponentArea, KlField {

    /**
     * Keys for objects that {@code KlWidget}'s will store in the properties of their associated
     * JavaFx {@code Node}s. Some of these objects will provide caching and computation
     * functionality (behavior), and may not be strictly data carriers. In those cases,
     * where the state must be saved and restored, the {@code KlWidget}'s class is responsible for
     * populating the properties with objects derived from, and saved to, a corresponding
     * KometPreferencesNode with a corresponding PropertyKey.
     */
    enum PropertyKeys {

        /**
         * Represents a property key used to associate the master {@code KnowledgeLayout} object with a JavaFX {@code Node}.
         * The {@code KL_MASTER_LAYOUT} key is intended to enable dynamic attachment of a master layout object
         * or behavior, which could include layout configurations, preferences, or contextual information.
         * This key facilitates the management of layout-specific data and behaviors within the system,
         * supporting complex UI arrangements or hierarchical relationships between components.
         * <p>
         * A Master Layout contains the LayoutOverrides that is passed to all dependent {@code LayoutComputer} objects.
         */
        MASTER_LAYOUT,
        LAYOUT_OVERRIDES_IN_MEMORY
    }

    /**
     * Enumeration for defining preference keys used in a GridLayout configuration. Each key is
     * associated with a default value, which can be used when the specific property is not explicitly set.
     *
     * This enum implements the PropertyWithDefault interface, allowing for retrieval of default values
     * associated with each specific preference key.
     *
     * The keys and their defaults represent different layout properties such as column index, row index,
     * column and row spans, growth behavior, alignments, margins, dimensions, and fill behaviors,
     * commonly used in grid-based layouts.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        COLUMN_INDEX(AreaGridSettings.DEFAULT.columnIndex()),
        ROW_INDEX(AreaGridSettings.DEFAULT.rowIndex()),
        COLUMN_SPAN(AreaGridSettings.DEFAULT.columnSpan()),
        ROW_SPAN(AreaGridSettings.DEFAULT.rowSpan()),
        H_GROW(AreaGridSettings.DEFAULT.hGrow()),
        V_GROW(AreaGridSettings.DEFAULT.vGrow()),
        H_ALIGNMENT(AreaGridSettings.DEFAULT.hAlignment()),
        V_ALIGNMENT(AreaGridSettings.DEFAULT.vAlignment()),
        MARGIN(AreaGridSettings.DEFAULT.margin()),
        MAX_HEIGHT(AreaGridSettings.DEFAULT.maxHeight()),
        MAX_WIDTH(AreaGridSettings.DEFAULT.maxWidth()),
        PREFERRED_HEIGHT(AreaGridSettings.DEFAULT.preferredHeight()),
        PREFERRED_WIDTH(AreaGridSettings.DEFAULT.preferredWidth()),
        FILL_HEIGHT(AreaGridSettings.DEFAULT.fillHeight()),
        FILL_WIDTH(AreaGridSettings.DEFAULT.fillWidth()),
        VISIBLE(AreaGridSettings.DEFAULT.visible()),
        LAYOUT_KEY(AreaGridSettings.DEFAULT.layoutKeyForArea()),
        LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES(null);

        final Object defaultValue;
        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }
        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }
    }

    default LayoutOverrides getLayoutOverrides() {
        Optional<KlArea<?>> optionalAreaWithOverrides =  findKlSelfOrAncestor(
                klArea -> klArea.preferences().hasKey(PreferenceKeys.LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES));

        if (optionalAreaWithOverrides.isPresent()) {
            if (this.properties().containsKey(PropertyKeys.LAYOUT_OVERRIDES_IN_MEMORY)) {
                return (LayoutOverrides) this.properties().get(PropertyKeys.LAYOUT_OVERRIDES_IN_MEMORY);
            }
            LayoutOverrides layoutOverrides = LayoutOverrides.restore(optionalAreaWithOverrides.get().preferences());
            this.properties().put(PropertyKeys.LAYOUT_OVERRIDES_IN_MEMORY, layoutOverrides);
            return layoutOverrides;
        }
        LayoutOverrides layoutOverrides = LayoutOverrides.make(preferences());
        this.preferences().putObject(PreferenceKeys.LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES, layoutOverrides);
        this.setLayoutOverrides(layoutOverrides);
        return layoutOverrides;
    }

    default void setLayoutOverrides(LayoutOverrides layoutOverrides) {
        preferences().putObject(PreferenceKeys.LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES, layoutOverrides);
    }

    default ObservableMap<Object, Object> properties() {
        return fxObject().getProperties();
    }

    default void setId(String id) {
        fxObject().setId(id);
    }

    default String getId() {
        return fxObject().getId();
    }

    default KlArea.Factory areaFactory() {
        try {
            Class factoryClass = PluggableService.forName(getFactoryClassName());
            Constructor constructor = factoryClass.getConstructor();
            return (KlArea.Factory) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Unbinds the current KlArea and all its descendants in the knowledge layout hierarchy.
     * <p>
     * This method ensures that the knowledge layout bindings are removed from the current KlArea
     * by invoking {@code knowledgeLayoutUnbind()} on itself. It then performs a depth-first traversal
     * of all descendant elements, applying the {@code knowledgeLayoutUnbind()} method to each.
     * <p>
     * The traversal is carried out using {@code dfsProcessKlAreaDescendents}, a utility method that
     * processes each descendant node and executes the specified action—in this case, unbinding its
     * knowledge layout.
     * <p>
     * This method is useful for cleaning up resources and removing bindings when the KlArea
     * and its descendants are no longer needed or are being replaced in the context of the knowledge
     * layout. It helps prevent resource leaks and ensures proper disconnection from event sources.
     */
    default void unbindSelfAndKnowledgeLayoutDescendents() {
        this.knowledgeLayoutUnbind();
        dfsProcessKlAreaDescendents(klView -> klView.knowledgeLayoutUnbind());
    }

    /**
     * Binds the knowledge layout to the current KlArea and all its descendant elements in the
     * knowledge layout hierarchy.
     * <p>
     * This method first invokes {@code knowledgeLayoutBind()} on the current KlArea,
     * ensuring its layout is properly bound. It then performs a depth-first traversal
     * of all descendant KlArea elements, applying the {@code knowledgeLayoutBind()} method
     * to each descendant.
     * <p>
     * During the traversal, the method utilizes {@code dfsProcessKlAreaDescendents},
     * which systematically processes each descendant node and applies the provided action.
     * <p>
     * The purpose of this method is to establish bindings across the entire structure
     * of the knowledge layout, ensuring that all elements are correctly bound to their
     * respective layout configurations.
     */
    default void bindSelfAndKnowledgeLayoutDescendents() {
        this.knowledgeLayoutBind();
        dfsProcessKlAreaDescendents(klView -> klView.knowledgeLayoutBind());
    }

    /**
     * This method unbinds knowledge layout for all descendant components or views in
     * the knowledge layout hierarchy. It performs a depth-first traversal of
     * knowledge layout descendants and calls {@code knowledgeLayoutUnbind()}
     * on each encountered descendant.
     * <p>
     * The method is defined as a default method, allowing implementing classes
     * to inherit the behavior without requiring an explicit implementation.
     * <p>
     * It makes use of {@code dfsProcessKlAreaDescendents}, which is responsible
     * for depth-first traversal and processing of descendant components.
     */
    default void unbindKnowledgeLayoutDescendents() {
        dfsProcessKlAreaDescendents(klView -> klView.knowledgeLayoutUnbind());
    }

    /**
     * Binds the knowledge layout to all descendant elements of the knowledge layout structure.
     * <p>
     * This method iterates recursively over all descendants in the knowledge layout structure
     * using a depth-first search approach. Each descendant element is processed to bind its
     * respective knowledge layout by invoking the `knowledgeLayoutBind` method.
     * <p>
     * The method achieves this by calling the `dfsProcessKlAreaDescendents` with a lambda
     * function as an argument. The lambda function specifies the operation to perform on each
     * descendant, which in this case is invoking `knowledgeLayoutBind` on the provided
     * knowledge layout view.
     */
    default void bindKnowledgeLayoutDescendents() {
        dfsProcessKlAreaDescendents(klView -> klView.knowledgeLayoutBind());
    }
    default KometPreferences preferences() {
        // TODO eliminate this after refactoring existing KlWidgets to support KlGadget, and factories with preferences.
        throw new UnsupportedOperationException("Please override and implement...");
    }

    default void setId(LayoutComputer.LayoutElement layoutElement) {
        layoutElement.optionalFeature().ifPresentOrElse(feature ->
                        this.fxObject().setId(feature.featureKey().toString()),
                () -> {
                    String factoryAsId = layoutElement.areaGridSettings().areaFactoryClassName();
                    factoryAsId = factoryAsId.substring(factoryAsId.lastIndexOf('.') + 1);
                    this.fxObject().setId(factoryAsId);
                });
        layoutElement.optionalFeature().ifPresent(feature -> {
            if (this instanceof KlFeaturePropertyForArea propertyArea) {
                propertyArea.setFeatureProperty(feature.featureProperty());
            }
        });
    }


    sealed interface Factory<FX extends Region, KL extends KlArea<FX>> extends KlView.Factory<FX, KL>
            permits KlAreaForAssociation.Factory, KlAreaForFeature.Factory, KlAreaForList.Factory, KlAreaForVersion.Factory, KlMultiVersionArea.Factory, KlSupplementalArea.Factory, KlChronologyArea.Factory, KlMultiComponentArea.Factory {

        /**
         * Provides the default {@code AreaGridSettings} for the factory. The default settings
         * will include the factory's class name as the area factory class name.
         *
         * @return An {@code AreaGridSettings} object configured with the default settings
         * including the factory's class name as the area factory class name.
         */
        default AreaGridSettings defaultAreaGridSettings() {
            return AreaGridSettings.DEFAULT.withAreaFactoryClassName(this.getClass().getName());
        }

        /**
         * Create new {@code KL} object with default {@code AreaGridSettings}
         * @param preferencesFactory
         * @return a {@code KL} object.
         */
        default KL create(KlPreferencesFactory preferencesFactory) {
            return create(preferencesFactory, defaultAreaGridSettings().with(this.getClass()));
        }


        /**
         * Creates a new area of type {@code KL} using the specified preferences factory and area layout.
         *
         * @param preferencesFactory the {@code KlPreferencesFactory} that provides preferences for the new area
         * @param areaGridSettings the layout information used to configure the new area
         * @return a new area of type {@code KL} configured using the provided preferences factory and layout
         */
        KL create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings);

        default KL createAndAddToParent(AreaGridSettings areaGridSettings, KlParent<?> parentArea) {
            Objects.requireNonNull(areaGridSettings, "areaLayout is null");
            Objects.requireNonNull(parentArea, "parentArea is null");

            KlPreferencesFactory preferencesFactory =
                    KlPreferencesFactory.create(parentArea.preferences(), this.getClass().getEnclosingClass());

            KL klView = this.create(preferencesFactory, areaGridSettings);
            parentArea.addChild(klView);
            return klView;
        }

        default KL createAndAddToParent(KlParent<?> parentArea) {
            Objects.requireNonNull(parentArea, "parentArea is null");

            KlPreferencesFactory preferencesFactory =
                    KlPreferencesFactory.create(parentArea.preferences(), this.getClass().getEnclosingClass());

            KL klView = this.create(preferencesFactory, defaultAreaGridSettings().with(this.getClass()));
            parentArea.addChild(klView);
            return klView;
        }

        default KL createAndAddToParent(KlRenderView parentArea) {
            Objects.requireNonNull(parentArea, "parentArea is null");

            KlPreferencesFactory preferencesFactory =
                    KlPreferencesFactory.create(parentArea.preferences(), this.getClass().getEnclosingClass());

            KL klView = this.create(preferencesFactory, defaultAreaGridSettings().with(this.getClass()));
            parentArea.addChild(klView);
            return klView;
        }

    }
}
