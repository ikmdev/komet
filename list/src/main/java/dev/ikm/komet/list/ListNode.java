package dev.ikm.komet.list;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import dev.ikm.komet.collection.CollectionNode;
import dev.ikm.komet.collection.CollectionType;
import dev.ikm.komet.framework.propsheet.editor.IntIdCollectionEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdListEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListNode extends CollectionNode<IntIdList> {
    private static final Logger LOG = LoggerFactory.getLogger(ListNode.class);
    protected static final String TITLE = "List Manager";

    public ListNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        this.collectionItemsProperty.set(IntIds.list.empty());
    }

    @Override
    protected IntIdCollectionEditor<IntIdList> getCollectionEditor(ViewProperties viewProperties, SimpleObjectProperty<IntIdList> listItems) {
        return new IntIdListEditor(viewProperties, listItems);
    }

    @Override
    protected CollectionType getCollectionType() {
        return CollectionType.LIST;
    }

    public Node getMenuIconGraphic() {
        Label menuIcon = new Label("(…)");
        return menuIcon;
    }

    @Override
    public Class factoryClass() {
        return ListNodeFactory.class;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }
}