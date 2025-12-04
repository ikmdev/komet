package dev.ikm.komet.layout.component.field;

import dev.ikm.komet.layout.KlFactory;
import javafx.scene.layout.Region;



/**
 * Represents a factory interface for creating and managing instances of
 * {@link KlFieldArea}, which is a Knowledge Layout framework-specific component
 * that binds data of type {@code T} to an associated JavaFX {@link Region}.
 *
 * @param <DT>  The data type handled by the field pane.
 * @param <FX> The JavaFX {@link Region} subclass associated with the field pane.
 * @param <KL> The {@link KlFieldArea} implementation managed by this factory.
 */
public interface KlFieldPaneArea<DT, FX extends Region, KL extends KlFieldArea<DT, FX>>
        extends KlFactory<KL> {

}
