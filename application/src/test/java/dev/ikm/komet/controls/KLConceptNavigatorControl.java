package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.ConceptNavigatorVirtualFlow;
import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.scene.Group;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class KLConceptNavigatorControl extends TreeView<String> {

    private Group sheet;
    private ConceptNavigatorVirtualFlow conceptNavigatorVirtualFlow;

    public KLConceptNavigatorControl() {
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        setShowRoot(false);
        setRoot(root);
        setFixedCellSize(24);
        setCellFactory(p -> new KLConceptNavigatorTreeCell(this));

        getStyleClass().add("concept-navigator-control");
        getStylesheets().add(KLConceptNavigatorControl.class.getResource("concept-navigator.css").toExternalForm());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        KLConceptNavigatorTreeViewSkin skin = new KLConceptNavigatorTreeViewSkin(this);
        conceptNavigatorVirtualFlow = skin.getConceptNavigatorVirtualFlow();
        sheet = (Group) conceptNavigatorVirtualFlow.lookup(".sheet");
        return skin;
    }

    public Group getSheet() {
        return sheet;
    }

    public ConceptNavigatorVirtualFlow getConceptNavigatorVirtualFlow() {
        return conceptNavigatorVirtualFlow;
    }
}
