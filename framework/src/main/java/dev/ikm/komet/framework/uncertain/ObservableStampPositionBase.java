package dev.ikm.komet.framework.uncertain;

import java.util.Objects;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import dev.ikm.komet.framework.view.ObservableCoordinateAbstract;
import dev.ikm.tinkar.coordinate.stamp.StampPosition;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

/**
 * The Class  ObservableStampPositionBase.
 *
 * @author kec
 */
public abstract class ObservableStampPositionBase
        extends ObservableCoordinateAbstract<StampPositionRecord>
        implements ObservableStampPosition {

    /**
     * The time property.
     */
    private final LongProperty timeProperty;

    /**
     * The stamp path nid property.
     */
    private final ObjectProperty<ConceptFacade> pathConceptProperty;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable stamp position impl.
     *
     * @param stampPosition the stamp position
     */
    public ObservableStampPositionBase(StampPosition stampPosition, String coordinateName) {
        super(stampPosition.toStampPositionImmutable(), coordinateName);
        this.pathConceptProperty = makePathConceptProperty(stampPosition);
        this.timeProperty = makeTimeProperty(stampPosition);
        addListeners();
    }

    protected abstract ObjectProperty<ConceptFacade> makePathConceptProperty(StampPosition stampPosition);

    protected abstract LongProperty makeTimeProperty(StampPosition stampPosition);

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptFacade> pathConceptChangedListener = this::pathConceptChanged;
    private final ChangeListener<Number> timeChangedListener = this::timeChanged;

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this.pathConceptChangedListener);
        this.timeProperty.addListener(this.timeChangedListener);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this.pathConceptChangedListener);
        this.timeProperty.removeListener(this.timeChangedListener);
    }

    @Override
    public StampPositionRecord getStampPosition() {
        return getValue();
    }

    @Override
    public StampPositionRecord toStampPositionImmutable() {
        return getValue();
    }

    private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
        this.setValue(StampPositionRecord.make(newTime.longValue(), getPathForPositionNid()));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptFacade> observablePathConcept,
                                    ConceptFacade oldPathConcept,
                                    ConceptFacade newPathConcept) {
        this.setValue(StampPositionRecord.make(time(), newPathConcept.nid()));
    }

    /**
     * Filter path nid property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptFacade> pathConceptProperty() {
        return this.pathConceptProperty;
    }

    /**
     * Time property.
     *
     * @return the long property
     */
    @Override
    public LongProperty timeProperty() {
        return this.timeProperty;
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ObservableStampPositionImpl{" + this.getValue().toString() + '}';
    }

    //~--- get methods ---------------------------------------------------------


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof StampPosition)) return false;
        StampPosition that = (StampPosition) o;
        return this.time() == that.time() &&
                this.getPathForPositionNid() == that.getPathForPositionNid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.time(), this.getPathForPositionNid());
    }
}

