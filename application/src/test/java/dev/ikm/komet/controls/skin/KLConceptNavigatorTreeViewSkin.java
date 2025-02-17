package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.KLConceptNavigatorControl;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<String> {

    private final Label header;
    private ConceptNavigatorVirtualFlow virtualFlow;
    private Group sheet;

    public KLConceptNavigatorTreeViewSkin(TreeView<String> treeView) {
        super(treeView);
        header = new Label();
        header.getStyleClass().add("concept-header");
        header.textProperty().bind(((KLConceptNavigatorControl) treeView).headerProperty());

        getChildren().add(header);
    }

    @Override
    protected VirtualFlow<TreeCell<String>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutInArea(header, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.TOP);
    }

    public ConceptNavigatorVirtualFlow getConceptNavigatorVirtualFlow() {
        return virtualFlow;
    }

    public Group getSheet() {
        if (sheet == null) {
            sheet = (Group) virtualFlow.lookup(".sheet");
        }
        return sheet;
    }

}
