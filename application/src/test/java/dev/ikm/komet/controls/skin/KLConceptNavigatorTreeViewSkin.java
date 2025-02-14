package dev.ikm.komet.controls.skin;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<String> {

    private ConceptNavigatorVirtualFlow virtualFlow;

    public KLConceptNavigatorTreeViewSkin(TreeView<String> treeView) {
        super(treeView);
    }

    @Override
    protected VirtualFlow<TreeCell<String>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    public ConceptNavigatorVirtualFlow getConceptNavigatorVirtualFlow() {
        return virtualFlow;
    }

}
