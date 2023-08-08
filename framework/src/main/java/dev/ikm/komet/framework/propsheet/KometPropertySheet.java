package dev.ikm.komet.framework.propsheet;

import dev.ikm.komet.framework.view.ViewProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.controlsfx.control.PropertySheet;

public class KometPropertySheet extends PropertySheet {
    private BooleanProperty hideLabels = new SimpleBooleanProperty(false);

    final ViewProperties viewProperties;

    public KometPropertySheet(ViewProperties viewProperties, boolean hideLabels) {
        this.viewProperties = viewProperties;
        this.hideLabels.set(hideLabels);

        setMode(PropertySheet.Mode.NAME);
        setSearchBoxVisible(false);
        setModeSwitcherVisible(false);
        setSkin(new KometPropertySheetSkin(this));
        setPropertyEditorFactory(new KometPropertyEditorFactory(viewProperties));
    }
    public KometPropertySheet(ViewProperties viewProperties) {
        this(viewProperties, false);
    }

    public boolean isHideLabels() {
        return hideLabels.get();
    }

    public BooleanProperty hideLabelsProperty() {
        return hideLabels;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels.set(hideLabels);
    }
}
