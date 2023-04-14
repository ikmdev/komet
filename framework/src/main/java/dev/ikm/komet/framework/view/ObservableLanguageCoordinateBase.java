package dev.ikm.komet.framework.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

public abstract class ObservableLanguageCoordinateBase extends ObservableCoordinateAbstract<LanguageCoordinateRecord>
        implements ObservableLanguageCoordinate {

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptFacade> languageConceptChangedListener = this::languageConceptChanged;
    private final ListChangeListener<PatternFacade> descriptionPatternPreferenceListListener = this::descriptionPatternPreferenceListChanged;
    private final ListChangeListener<ConceptFacade> descriptionTypePreferenceListListener = this::descriptionTypePreferenceListChanged;
    private final ListChangeListener<PatternFacade> dialectPatternPreferenceListChangedListener = this::dialectPatternPreferenceListChanged;
    private final ListChangeListener<ConceptFacade> modulePreferenceListChangedListener = this::modulePreferenceListChanged;


    /**
     * The language concept nid property.
     */
    private final SimpleEqualityBasedObjectProperty<ConceptFacade> languageProperty;

    /**
     * The dialect assemblage preference list property.
     */
    private final SimpleEqualityBasedListProperty<PatternFacade> dialectPatternPreferenceListProperty;

    /**
     * The description type preference list property.
     */
    private final SimpleEqualityBasedListProperty<ConceptFacade> descriptionTypePreferenceListProperty;

    private final SimpleEqualityBasedListProperty<PatternFacade> descriptionPatternPreferenceListProperty;

    private final SimpleEqualityBasedListProperty<ConceptFacade> modulePreferenceListProperty;


    protected ObservableLanguageCoordinateBase(LanguageCoordinate languageCoordinate, String coordinateName) {
        super(languageCoordinate.toLanguageCoordinateRecord(), coordinateName);
        this.languageProperty = makeLanguageProperty(languageCoordinate);
        this.descriptionPatternPreferenceListProperty = makeDescriptionPatternPreferenceListProperty(languageCoordinate);
        this.dialectPatternPreferenceListProperty = makeDialectPatternPreferenceListProperty(languageCoordinate);
        this.descriptionTypePreferenceListProperty = makeDescriptionTypePreferenceListProperty(languageCoordinate);
        this.modulePreferenceListProperty = makeModulePreferenceListProperty(languageCoordinate);
        addListeners();
    }
    /**
     * The language concept nid property.
     */
    protected abstract SimpleEqualityBasedObjectProperty<ConceptFacade> makeLanguageProperty(LanguageCoordinate languageCoordinate);

    /**
     * The dialect assemblage preference list property.
     */
    protected abstract SimpleEqualityBasedListProperty<PatternFacade> makeDialectPatternPreferenceListProperty(LanguageCoordinate languageCoordinate);
    protected abstract SimpleEqualityBasedListProperty<PatternFacade> makeDescriptionPatternPreferenceListProperty(LanguageCoordinate languageCoordinate);

    /**
     * The description type preference list property.
     */
    protected abstract SimpleEqualityBasedListProperty<ConceptFacade> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate);

    protected abstract SimpleEqualityBasedListProperty<ConceptFacade> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate);

    @Override
    protected void addListeners() {
        this.languageConceptProperty().addListener(this.languageConceptChangedListener);
        this.descriptionPatternPreferenceListProperty().addListener(this.descriptionPatternPreferenceListListener);
        this.dialectPatternPreferenceListProperty().addListener(this.dialectPatternPreferenceListChangedListener);
        this.descriptionTypePreferenceListProperty().addListener(this.descriptionTypePreferenceListListener);
        this.modulePreferenceListForLanguageProperty().addListener(this.modulePreferenceListChangedListener);
    }

    @Override
    protected void removeListeners() {
        this.languageConceptProperty().removeListener(this.languageConceptChangedListener);
        this.descriptionPatternPreferenceListProperty().removeListener(this.descriptionPatternPreferenceListListener);
        this.dialectPatternPreferenceListProperty().removeListener(this.dialectPatternPreferenceListChangedListener);
        this.descriptionTypePreferenceListProperty().removeListener(this.descriptionTypePreferenceListListener);
        this.modulePreferenceListForLanguageProperty().removeListener(this.modulePreferenceListChangedListener);
     }

    private void modulePreferenceListChanged(ListChangeListener.Change<? extends ConceptFacade> c) {
        this.setValue(LanguageCoordinateRecord.make(languageConceptNid(),
                descriptionPatternPreferenceNidList(),
                descriptionTypePreferenceNidList(),
                dialectPatternPreferenceNidList(),
                IntIds.list.of(c.getList().stream().mapToInt(value -> value.nid()).toArray())));
    }
    private void descriptionPatternPreferenceListChanged(ListChangeListener.Change<? extends PatternFacade> c) {
        this.setValue(LanguageCoordinateRecord.make(languageConceptNid(),
                IntIds.list.of(c.getList().stream().mapToInt(value -> value.nid()).toArray()),
                descriptionTypePreferenceNidList(),
                dialectPatternPreferenceNidList(),
                modulePreferenceNidListForLanguage()));
    }
    private void descriptionTypePreferenceListChanged(ListChangeListener.Change<? extends ConceptFacade> c) {
        this.setValue(LanguageCoordinateRecord.make(languageConceptNid(),
                descriptionPatternPreferenceNidList(),
                IntIds.list.of(c.getList().stream().mapToInt(value -> value.nid()).toArray()),
                dialectPatternPreferenceNidList(),
                modulePreferenceNidListForLanguage()));
    }

    private void dialectPatternPreferenceListChanged(ListChangeListener.Change<? extends PatternFacade> c) {
        this.setValue(LanguageCoordinateRecord.make(languageConceptNid(),
                descriptionPatternPreferenceNidList(),
                descriptionTypePreferenceNidList(),
                IntIds.list.of(c.getList().stream().mapToInt(value -> value.nid()).toArray()),
                modulePreferenceNidListForLanguage()));
    }

    private void languageConceptChanged(ObservableValue<? extends ConceptFacade> observable,
                                        ConceptFacade oldLanguageConcept,
                                        ConceptFacade newLanguageConcept) {
        this.setValue(LanguageCoordinateRecord.make(newLanguageConcept.nid(),
                descriptionPatternPreferenceNidList(),
                descriptionTypePreferenceNidList(),
                dialectPatternPreferenceNidList(),
                modulePreferenceNidListForLanguage()));
    }

    @Override
    public ListProperty<ConceptFacade> modulePreferenceListForLanguageProperty() {
        return this.modulePreferenceListProperty;
    }

    @Override
    public LanguageCoordinateRecord getLanguageCoordinate() {
        return getValue();
    }

    @Override
    public ListProperty<ConceptFacade> descriptionTypePreferenceListProperty() {
        return this.descriptionTypePreferenceListProperty;
    }

    @Override
    public ListProperty<PatternFacade> dialectPatternPreferenceListProperty() {
        return this.dialectPatternPreferenceListProperty;
    }

    @Override
    public ListProperty<PatternFacade> descriptionPatternPreferenceListProperty() {
        return this.descriptionPatternPreferenceListProperty;
    }

    @Override
    public ObjectProperty<ConceptFacade> languageConceptProperty() {
        return this.languageProperty;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ObservableLanguageCoordinateBase{" + this.getValue().toString() + '}';
    }

    @Override
    public LanguageCoordinateRecord toLanguageCoordinateRecord() {
        return getValue();
    }

}
