/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.dnd;

import dev.ikm.tinkar.entity.EntityHandle;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntSupplier;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DragDetectedCellEventHandler}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DragDetectedCellEventHandler
        implements EventHandler<MouseEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DragDetectedCellEventHandler.class);

    private IntSupplier nidSupplier;

    public DragDetectedCellEventHandler() {
    }

    public DragDetectedCellEventHandler(IntSupplier nidSupplier) {
        this.nidSupplier = nidSupplier;
    }

    //~--- methods -------------------------------------------------------------

    /**
     * @param event
     * @see javafx.event.EventHandler#handle(javafx.event.Event)
     */
    @Override
    public void handle(MouseEvent event) {
        /* drag was detected, start a drag-and-drop gesture */
        /* allow any transfer mode */
        Node eventNode = null;
        EntityHandle entityHandle = null;
        if (nidSupplier != null) {
            entityHandle = EntityHandle.get(nidSupplier.getAsInt());
            if (event.getSource() instanceof Node) {
                eventNode = (Node) event.getSource();
            } else {
                LOG.warn("Non node source of drag? {}", event.getSource());
            }
        } else if (event.getSource() instanceof TreeCell) {
            eventNode = (Node) event.getSource();
            Object item = ((TreeCell<?>) event.getSource()).getItem();
            if (item instanceof Integer nid) {
                entityHandle = EntityHandle.get(nid);
            } else if (item instanceof EntityFacade entityFacade) {
                entityHandle = EntityHandle.get(entityFacade);
            } else if (item instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                entityHandle = EntityHandle.get(nidTextRecord.nid());
            } else if (item instanceof LatestVersionSearchResult latestVersionSearchResult &&
                    latestVersionSearchResult.latestVersion().isPresent()) {
                entityHandle = EntityHandle.get(latestVersionSearchResult.latestVersion().get().nid());
            }
        } else if (event.getSource() instanceof TableCell) {
            eventNode = (TableCell) event.getSource();
            Object item = ((TableCell) eventNode).getItem();
            if (item instanceof String) {
                entityHandle = EntityHandle.get((EntityFacade) ((TableCell) eventNode).getTableRow().getItem());
            }
        } else if (event.getSource() instanceof TableView) {
            TableView<EntityFacade> tableView = (TableView) event.getSource();

            if (tableView.getSelectionModel()
                    .getSelectedItem() instanceof EntityFacade) {
                entityHandle = EntityHandle.get(tableView.getSelectionModel().getSelectedItem());
                eventNode = event.getPickResult()
                        .getIntersectedNode();
                eventNode = eventNode.getParent();
            }
        } else if (event.getSource() instanceof ListView) {

            ListView listView = (ListView) event.getSource();
            Object selectedObject = listView.getSelectionModel().getSelectedItem();
            if (selectedObject instanceof EntityFacade entityFacade) {
                entityHandle = EntityHandle.get(entityFacade);
                eventNode = event.getPickResult()
                        .getIntersectedNode().getParent().getParent();

            } else {
                LOG.warn("unhandled selected object type {}" + selectedObject);
            }


        } else {
            LOG.warn("unhandled event source {}" + event.getSource());
        }

        if (eventNode != null) {

            Dragboard db = eventNode.startDragAndDrop(TransferMode.COPY);

            // Snapshot the cell's graphic when present (trims the tree indent / disclosure area);
            // otherwise the node itself. Canonical placement — standard size, cursor just right of
            // the identicon on the image's bottom border — replacing the old grab-point-relative
            // centered offset so a tree/list drag looks like every other concept drag.
            Node imageNode = (eventNode instanceof Cell<?> cell && cell.getGraphic() != null)
                    ? cell.getGraphic() : eventNode;
            KonceptDragSource.setDragView(db, imageNode);

            /* Put a string on a dragboard */
            if (entityHandle.isPresent()) {
                String drag = entityHandle.expectEntity().publicId().toString();
                if ((drag != null) && (drag.length() > 0)) {
                    KometClipboard content = new KometClipboard(entityHandle.expectEntity());

                    db.setContent(content);
                    DragRegistry.dragStart();
                    event.consume();
                }
            }
        }
    }
}
