package dev.ikm.komet.set;


import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import dev.ikm.komet.collection.CollectionNode;
import dev.ikm.komet.collection.CollectionType;
import dev.ikm.komet.framework.propsheet.editor.IntIdCollectionEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdSetEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetNode extends CollectionNode<IntIdSet> {
    private static final Logger LOG = LoggerFactory.getLogger(SetNode.class);
    protected static final String TITLE = "Set Manager";

    public SetNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        this.collectionItemsProperty.set(IntIds.set.empty());
    }

    @Override
    protected IntIdCollectionEditor<IntIdSet> getCollectionEditor(ViewProperties viewProperties,
                                                                  SimpleObjectProperty<IntIdSet> collectionItems) {
        return new IntIdSetEditor(viewProperties, collectionItems);
    }

    @Override
    protected CollectionType getCollectionType() {
        return CollectionType.SET;
    }

    public Node getMenuIconGraphic() {
        Label menuIcon = new Label("{…}");
        return menuIcon;
    }

    @Override
    public Class factoryClass() {
        return SetNodeFactory.class;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }
}