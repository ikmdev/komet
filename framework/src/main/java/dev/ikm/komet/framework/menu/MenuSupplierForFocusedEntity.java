package dev.ikm.komet.framework.menu;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import dev.ikm.komet.framework.MenuItemWithText;
import dev.ikm.komet.framework.context.AddToContextMenu;
import dev.ikm.komet.framework.docbook.DocBook;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * See also AddToContextMenu TODO should they merge? Note LOG.warn("Port method from old project");
 */
public class MenuSupplierForFocusedEntity implements AddToContextMenu {
    private static final Logger LOG = LoggerFactory.getLogger(MenuSupplierForFocusedEntity.class);
    private static AddToContextMenu SINGLETON;
    private static AddToContextMenu[] SINGLETON_ARRAY;

    private MenuSupplierForFocusedEntity() {
    }

    public static AddToContextMenu[] getArray() {
        if (SINGLETON_ARRAY == null) {
            SINGLETON_ARRAY = new AddToContextMenu[]{get()};
        }
        return SINGLETON_ARRAY;
    }

    public static AddToContextMenu get() {
        if (SINGLETON == null) {
            SINGLETON = new MenuSupplierForFocusedEntity();
        }
        return SINGLETON;
    }

    private static void setupHistoryMenuItem(ViewProperties viewProperties,
                                             SimpleListProperty<EntityFacade> historyCollection,
                                             Menu historyMenu,
                                             SimpleObjectProperty<EntityFacade> identifiedObjectFocusProperty,
                                             SimpleIntegerProperty selectionIndexProperty,
                                             Runnable unlink) {
        LOG.warn("Port method from old project");
    }

    public static Menu makeCopyMenuItem(Optional<Entity<EntityVersion>> concept, ViewProperties viewProperties) {
        Menu copyMenu = new Menu("copy");
        MenuItem conceptLoincCodeMenuItem = new MenuItemWithText("Concept LOINC code");
        copyMenu.getItems().add(conceptLoincCodeMenuItem);
        conceptLoincCodeMenuItem.setOnAction((event) -> {
            LOG.warn("Port method from old project");
        });

        MenuItem conceptSnomedCodeItem = new MenuItemWithText("Concept SNOMED code");
        copyMenu.getItems().add(conceptSnomedCodeItem);
        conceptSnomedCodeItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                LOG.warn("Port method from old project");
            }
        });

        MenuItem conceptFQNMenuItem = new MenuItemWithText("Concept Fully Qualified Name");
        copyMenu.getItems().add(conceptFQNMenuItem);
        conceptFQNMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                String fqnString = viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(concept.get());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(fqnString);
                clipboard.setContent(content);
            }
        });

        MenuItem conceptUuidMenuItem = new MenuItemWithText("Concept UUID");
        copyMenu.getItems().add(conceptUuidMenuItem);
        conceptUuidMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                String uuidStr = concept.get().asUuidList().toString();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(uuidStr);
                clipboard.setContent(content);
            }
        });

        MenuItem docBookInlineReferenceMenuItem = new MenuItemWithText("Docbook inline concept reference");
        copyMenu.getItems().add(docBookInlineReferenceMenuItem);
        docBookInlineReferenceMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                String docbookXml = DocBook.getInlineEntry(concept.get(),
                        viewProperties);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });

        MenuItem copyDocBookMenuItem = new MenuItemWithText("Docbook glossary entry");
        copyMenu.getItems().add(copyDocBookMenuItem);
        copyDocBookMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                String docbookXml = DocBook.getGlossentry(concept.get(),
                        viewProperties);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });

        MenuItem copyJavaShortSpecMenuItem = new MenuItemWithText("Java concept specification");
        copyMenu.getItems().add(copyJavaShortSpecMenuItem);
        copyJavaShortSpecMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                EntityFacade conceptSpec = concept.get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString("new ConceptProxy(\"" +
                        viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptSpec) +
                        "\", UUID.fromString(\"" +
                        conceptSpec.publicId().asUuidList().get(0).toString() +
                        "\"))");
                clipboard.setContent(content);
            }
        });

        MenuItem copyJavaSpecMenuItem = new MenuItemWithText("Java qualified concept specification");
        copyMenu.getItems().add(copyJavaSpecMenuItem);
        copyJavaSpecMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                EntityFacade conceptSpec = concept.get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString("new sh.isaac.api.ConceptProxy(\"" +
                        conceptSpec.toXmlFragment() +
                        "\")");
                clipboard.setContent(content);
            }
        });

        MenuItem copyConceptDetailedInfoItem = new MenuItemWithText("Copy concept detailed info");
        copyMenu.getItems().add(copyConceptDetailedInfoItem);
        copyConceptDetailedInfoItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                LOG.warn("Port method from old project");
            }
        });
        return copyMenu;
    }

    @Override
    public void addToContextMenu(Control control, ContextMenu contextMenu, ViewProperties viewProperties, ObservableValue<EntityFacade> conceptFocusProperty, SimpleIntegerProperty selectionIndexProperty, Runnable unlink) {
        LOG.warn("Port method from old project");
    }

}
