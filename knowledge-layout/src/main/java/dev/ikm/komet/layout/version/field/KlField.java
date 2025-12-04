package dev.ikm.komet.layout.version.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxy;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;
import javafx.util.Subscription;

import java.util.*;
import java.util.function.*;

/**
 * Represents an observable attribute in the Komet framework.
 * <p>
 * This interface is parameterized with the type of the value. It extends the
 * KlWidget interface, providing a method to access the underlying attribute.
 *
 * @param <DT> The data type of the attribute's value.
 */
@RegularName("Knowledge Layout Field")
@ParentProxy(parentName = "Komet panels (SOLOR)",
        parentPublicId = @PublicIdAnnotation(@UuidAnnotation("b3d1cdf6-27a5-502d-8f16-ed026a7b9d15")))
public non-sealed interface KlField<DT> extends KlArea, ClassConceptBinding {
    ObservableField<DT> field();
    ObservableField.Editable<DT> fieldEditable();

    /**
     * A convenience method to perform an action when the fieldEditable().editableValueProperty() is changed.
     * A JavaFX Subscription is added to a list to be later unsubscribed.
     * It is an equivalent method to perform action on change with old and new values of the editable value property.
     * {@snippet :
     *   Subscription subscription = fieldEditable()
     *                 .editableValueProperty()
     *                 .subscribe( (oldValue, newValue) -> {
     *                   // Callers code block to perform action on change.
     *                 });
     * }
     *
     * When rebinding occurs the Subscription can be unsubscribed and reapplied to a new ObservableField.Editable instance.
     *
     * Tech Note: According to JavaFX docs, when using subscribe with the single Consumer parameter, the listener will
     * be invoked initially. The other subscribe() methods (BiConsumer and Runnable) available will not invoke the
     * listeners initially.
     *
     * Here is a summary of the behaviors when using the different subscribe() methods:
     * <ul>
     *   <li>Consumer will invoke handler code initially. Creates a Change Listener.</li>
     *   <li>BiConsumer will not invoke handler code. Creates a Change Listener.</li>
     *   <li>Runnable will not invoke handler code. Creates an Invalidation Listener.</li>
     * </ul>
     * Invalidation listeners are more lightweight than Change Listeners, but they do not provide old and new values.
     * Choose the appropriate listener type based on your requirements.
     *
     * @param changeValueConsumer - Callers code block to perform action on change.
     *
     * @return A Subscription or change listener when field value changes.
     */
    default Subscription doOnEditableValuePropertyChange(BiConsumer<Optional<DT>, Optional<DT>> changeValueConsumer) {
        Subscription subscription = fieldEditable()
                .editableValueProperty()
                .subscribe( (oldValue, newValue) ->
                        changeValueConsumer.accept(Optional.ofNullable(oldValue), Optional.ofNullable(newValue)));
        return subscription;
    }

    /**
     * A convenience method to perform an action when the fieldEditable().editableValueProperty() is changed.
     * @param codeBlock - Callers code block to perform action on change.
     * @return A Subscription or change listener when field value changes.
     */
    default Subscription doOnEditableValuePropertyChange(Runnable codeBlock) {
        if (fieldEditable() == null) {
            return null;
        }
        Subscription subscription = fieldEditable()
                .editableValueProperty()
                .subscribe(codeBlock);
        return subscription;
    }

    /**
     * This method throws an UnsupportedOperationException if it is not overridden by a Kl Editable type control.
     * A derived class should implement/override the method to allow editable UI Controls to be able to unbind
     * JavaFX subscriptions to properties and swap out its {@link ObservableField.Editable<DT>} fieldEditable object.
     * @param newFieldEditable A field editable from an ObservableSemantic.
     */
    default void rebind(ObservableField.Editable<DT> newFieldEditable) {
        throw new UnsupportedOperationException("Rebinding is not supported with " + this.getClass().getName());
    }

}
