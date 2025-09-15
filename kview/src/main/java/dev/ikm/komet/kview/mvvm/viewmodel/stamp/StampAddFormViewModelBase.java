package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.CLEAR_RESET_BUTTON_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.CURRENT_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.FORM_TIME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.IS_STAMP_VALUES_THE_SAME_OR_EMPTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.TIME;

public abstract class StampAddFormViewModelBase extends StampFormViewModelBase {

    public StampAddFormViewModelBase(StampFormViewModelBase.StampType stampType) {
        super(stampType);

        // Add Properties
        addProperty(CURRENT_STAMP, (Stamp) null);

        addProperty(CLEAR_RESET_BUTTON_TEXT, "RESET");
    }

    @Override
    protected String getClearOrResetConfirmationMsg() {
        return "Are you sure you want to reset the form? All entered data will be lost.";
    }

    @Override
    protected String getClearOrResetConfirmationTitle() {
        return "Confirm Reset Form";
    }

    @Override
    protected void doInit(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        // entityFocusProperty from DetailsNode often calls init with a null entity.
        if (entity == null || entity == this.entityFacade) {
            return; // null entity or the entity hasn't changed
        } else {
            this.entityFacade = entity;
        }

        loadStamp();
        loadStampValuesFromDB();

        setPropertyValue(FORM_TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));

        save(true);
    }

    protected void loadStamp() {
        EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
        StampEntity stampEntity = latestVersion.stamp();

        setPropertyValue(CURRENT_STAMP, stampEntity);
    }

    protected void loadStampValuesFromDB() {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        setPropertyValue(STATUS, stampEntity.state());
        setPropertyValue(TIME, stampEntity.time());
        setPropertyValue(AUTHOR, stampEntity.author());
        setPropertyValue(MODULE, stampEntity.module());
        setPropertyValue(PATH, stampEntity.path());
    }

    @Override
    protected boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, same);

        if (same) {
            setPropertyValue(FORM_TITLE, "Latest " + stampType.getTextDescription() + " Version");
            setPropertyValue(FORM_TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));
            setPropertyValue(AUTHOR, stampEntity.author());
        } else {
            setPropertyValue(FORM_TITLE, "New " + stampType.getTextDescription() + " Version");
            setPropertyValue(FORM_TIME_TEXT, "Uncommitted");
            ConceptFacade authorConcept = viewProperties.nodeView().editCoordinate().getAuthorForChanges();
            setPropertyValue(AUTHOR, authorConcept);
        }

        return same;
    }
}