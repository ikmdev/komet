package dev.ikm.komet.framework.propsheet.editor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ToolBar;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.controlsfx.property.editor.PropertyEditor;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.VersionProxy;
import dev.ikm.tinkar.entity.VersionProxyFactory;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;
import java.util.Set;

public abstract class IntIdCollectionEditor<T extends IntIdCollection> implements PropertyEditor<T> {
    private static final Logger LOG = LoggerFactory.getLogger(IntIdCollectionEditor.class);
    protected final BorderPane editorPane = new BorderPane();
    protected final ToolBar editorToolbar = new ToolBar();
    protected final ListView<Integer> listView = new ListView();
    protected final ViewProperties viewProperties;
    SimpleObjectProperty<T> entitiesCollectionProperty;
    TransferMode[] transferMode = null;
    Background originalBackground;

    public IntIdCollectionEditor(ViewProperties viewProperties, SimpleObjectProperty<T> entitiesCollectionProperty) {
        this.editorPane.setCenter(listView);
        this.entitiesCollectionProperty = entitiesCollectionProperty;
        this.editorPane.setTop(editorToolbar);
        this.viewProperties = viewProperties;
        listView.setCellFactory(param -> new IntIdCollectionEditor.EntityCell());
        updateListView(entitiesCollectionProperty.getValue());
        this.entitiesCollectionProperty.addListener((observable, oldValue, newValue) -> {
            updateListView(newValue);
        });

        listView.setOnDragDetected(this::handleDragDetected);
        listView.setOnDragDone(this::handleDragDone);
        listView.setOnDragEntered(this::handleDragEntered);
        listView.setOnDragExited(this::handleDragExited);
        listView.setOnDragOver(this::handleDragOver);
        listView.setOnDragDropped(this::dragDropped);

        listView.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DELETE ||
                    event.getCode() == KeyCode.BACK_SPACE) {
                deleteSelectedItems(listView.getSelectionModel());
            }
        });
    }

    abstract void updateListView(T newValue);

    private void handleDragDetected(MouseEvent event) {
        LOG.debug("Drag detected: " + event);

        if (!listView.getSelectionModel().getSelectedIndices().isEmpty()) {
            int nid = listView.getSelectionModel().getSelectedIndices().get(0);
            Dragboard db = listView.startDragAndDrop(TransferMode.COPY);
            DragImageMaker dragImageMaker = new DragImageMaker(listView);
            db.setDragView(dragImageMaker.getDragImage());
            KometClipboard content = new KometClipboard((Entity) Entity.getFast(nid));
            db.setContent(content);
        }
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        LOG.debug("Dragging done: " + event);
    }

    private void handleDragEntered(DragEvent event) {
        LOG.debug("Dragging entered: " + event);
        this.originalBackground = listView.getBackground();

        Color backgroundColor;
        Set<DataFormat> contentTypes = event.getDragboard()
                .getContentTypes();

        if (KometClipboard.containsAny(contentTypes, KometClipboard.CONCEPT_TYPES)) {
            backgroundColor = Color.AQUA;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else if (KometClipboard.containsAny(contentTypes, KometClipboard.SEMANTIC_TYPES)) {
            backgroundColor = Color.OLIVEDRAB;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else {
            backgroundColor = Color.RED;
            this.transferMode = null;
        }

        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);

        listView.setBackground(new Background(fill));
    }

    private void handleDragExited(DragEvent event) {
        LOG.debug("Dragging exited: " + event);
        listView.setBackground(originalBackground);
        this.transferMode = null;
    }

    private void handleDragOver(DragEvent event) {
        // LOG.debug("Dragging over: " + event );
        if (this.transferMode != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    private void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        OptionalInt optionalNid = OptionalInt.empty();
        if (db.hasContent(KometClipboard.KOMET_CONCEPT_PROXY)) {
            EntityProxy.Concept conceptProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_CONCEPT_PROXY));
            optionalNid = OptionalInt.of(conceptProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_SEMANTIC_PROXY)) {
            EntityProxy.Semantic semanticProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_SEMANTIC_PROXY));
            optionalNid = OptionalInt.of(semanticProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_PATTERN_PROXY)) {
            EntityProxy.Pattern patternProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_PATTERN_PROXY));
            optionalNid = OptionalInt.of(patternProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_CONCEPT_VERSION_PROXY)) {
            VersionProxy.Concept conceptProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_CONCEPT_VERSION_PROXY));
            optionalNid = OptionalInt.of(conceptProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_SEMANTIC_VERSION_PROXY)) {
            VersionProxy.Semantic semanticProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_SEMANTIC_VERSION_PROXY));
            optionalNid = OptionalInt.of(semanticProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_PATTERN_VERSION_PROXY)) {
            VersionProxy.Pattern patternProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_PATTERN_VERSION_PROXY));
            optionalNid = OptionalInt.of(patternProxy.nid());
        }
        /* let the source know if the dropped item was successfully
         * transferred and used */
        optionalNid.ifPresentOrElse(nid -> {
            event.setDropCompleted(true);
            T oldIds = entitiesCollectionProperty.getValue();
            switch (oldIds) {
                case IntIdList oldList -> {
                    IntIdList newList = IntIds.list.of(oldList, nid);
                    setValue((T) newList);
                }
                case IntIdSet oldSet -> {
                    IntIdSet newSet = IntIds.set.of(oldSet, nid);
                    setValue((T) newSet);
                }
                default -> throw new IllegalStateException("Unexpected value: " + oldIds);
            }
        }, () -> event.setDropCompleted(false));

        event.consume();
    }

    abstract void deleteSelectedItems(MultipleSelectionModel<Integer> selectionModel);

    public MultipleSelectionModel<Integer> getSelectionModel() {
        return this.listView.getSelectionModel();
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public T getValue() {
        return entitiesCollectionProperty.getValue();
    }

    @Override
    public void setValue(T value) {
        entitiesCollectionProperty.setValue(value);
    }

    public ObservableList<Integer> getItems() {
        return this.listView.getItems();
    }

    class EntityCell extends ListCell<Integer> {

        int entityNid = Integer.MIN_VALUE;
        Latest<EntityVersion> latestEntity;
        String entityText;

        public EntityCell() {
            setOnDragDetected(this::handleDragDetected);
            setOnDragDone(this::handleDragDone);
        }

        private void handleDragDetected(MouseEvent event) {
            LOG.debug("Drag detected: " + event);

            DragImageMaker dragImageMaker = new DragImageMaker(this);
            Dragboard db = startDragAndDrop(TransferMode.COPY);

            db.setDragView(dragImageMaker.getDragImage());

            KometClipboard content = new KometClipboard((Entity) Entity.getFast(entityNid));
            db.setContent(content);
            event.consume();
        }

        private void handleDragDone(DragEvent event) {
            LOG.debug("Dragging done: " + event);
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                entityNid = Integer.MIN_VALUE;
                setText("");
                entityText = null;
                setGraphic(null);
                this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, false);
            } else {
                if (item != entityNid) {
                    entityNid = item;
                    latestEntity = viewProperties.calculator().latest(entityNid);
                    entityText = viewProperties.calculator().getDescriptionTextOrNid(entityNid);
                    setText(entityText);
                    this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, latestEntity.get().inactive());
                }
            }
        }
    }
}
