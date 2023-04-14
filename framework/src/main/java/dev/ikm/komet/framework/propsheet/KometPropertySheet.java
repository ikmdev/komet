package dev.ikm.komet.framework.propsheet;

import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import dev.ikm.komet.framework.view.ViewProperties;

public class KometPropertySheet extends PropertySheet {
    final ViewProperties viewProperties;

    {
        setMode(PropertySheet.Mode.NAME);
        setSearchBoxVisible(false);
        setModeSwitcherVisible(false);
        setSkin(new KometPropertySheetSkin(this));
    }

    public KometPropertySheet(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        setPropertyEditorFactory(new KometPropertyEditorFactory(viewProperties));
    }

    public KometPropertySheet(ObservableList<Item> items, ViewProperties viewProperties) {
        super(items);
        this.viewProperties = viewProperties;
    }
}
