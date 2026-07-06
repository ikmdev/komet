package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class GenPurposeViewModel extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(GenEditingViewModel.class);

    // Property keys live on ViewModelKey; this class owns no key constants.

    public GenPurposeViewModel() {
        super();
        addProperty(ViewModelKey.VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC, (UUID) null)
                .addProperty(ViewModelKey.WINDOW_TOPIC, (UUID) null)
//                .addProperty(ViewModelKey.STAMP_VIEW_MODEL, (ViewModel) null)
                .addProperty(ViewModelKey.FIELDS_COLLECTION, new ArrayList<FieldRecord<Object>>()) // Ordered collection of Fields
                .addProperty(ViewModelKey.REF_COMPONENT, (EntityFacade) null)
                .addProperty(ViewModelKey.SEMANTIC, (EntityFacade) null)
                .addProperty(ViewModelKey.PATTERN, (EntityFacade) null)
                .addProperty(ViewModelKey.FIELD_INDEX, Integer.valueOf(-1))
                .addProperty(ViewModelKey.DEFAULT_FIELDS_HASH, (Integer) null)
                .addProperty(ViewModelKey.COMPOSER, (ObservableComposer) null)
        ;
    }

    public void loadPatternValues(){
    }

    public boolean createSemantic() {
        return false;
    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(ViewModelKey.VIEW_PROPERTIES);
    }

//    public void updateStamp() {
//        EntityFacade patternFacade = getPropertyValue(PATTERN);
//        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
//
//        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);
//
//        Stamp stamp = stampCalculator.latest(patternFacade).get().stamp();
//        stampViewModel.setValue(STATUS, stamp.state());
//        stampViewModel.setValue(TIME, stamp.time());
//        stampViewModel.setValue(AUTHOR, stamp.author());
//        stampViewModel.setValue(MODULE, stamp.module());
//        stampViewModel.setValue(PATH, stamp.path());
//    }

    // -- mode
    /**
     * The window's create/edit mode as a typed property (rather than the stringly-typed cognitive
     * {@code ViewModelKey.MODE} property). Set from how the window was opened — CREATE from the Journal's
     * "+" button, EDIT from a component's "Open as ..." — and flipped to EDIT once a semantic is committed.
     */
    private final ObjectProperty<FormMode> mode = new SimpleObjectProperty<>(FormMode.EDIT);
    @Override
    public FormMode getMode() { return mode.get(); }
    public ObjectProperty<FormMode> modeProperty() { return mode; }
    public void setMode(FormMode mode) { this.mode.set(mode); }
}
