package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.Entity;
import javafx.scene.layout.Region;

/**
 * Factory interface for creating and managing instances of {@link KlComponentFieldArea}.
 * This interface extends {@link KlFieldPaneArea}, parameterized with a generic entity type {@link Entity},
 * a specific JavaFX {@link Region}, and the specialized {@link KlComponentFieldArea}.
 * It serves as a Knowledge Layout framework-specific factory for component-related field panes.
 *
 * The default implementation of {@link #klInterfaceClass()} returns the {@link KlComponentFieldArea} class.
 *
 * @param <P> the type of the JavaFX {@link Region} associated with this factory and its field panes
 */
public interface KlComponentFieldAreaFactory<P extends Region> extends
        KlFieldPaneArea<Entity, P, KlComponentFieldArea<Entity, P>> {

}
