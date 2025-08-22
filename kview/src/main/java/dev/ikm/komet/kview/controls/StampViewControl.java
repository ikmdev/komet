package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.StampViewControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;

public class StampViewControl extends Control {
    private static final PseudoClass STAMP_SELECTED = PseudoClass.getPseudoClass("selected");

    public StampViewControl() {
        getStyleClass().add("stamp-view-control");
    }

    @Override
    protected StampViewControlSkin createDefaultSkin() {
        return new StampViewControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return StampViewControl.class.getResource("stamp-view-control.css").toExternalForm();
    }

    public void clear() {
        setStatus("");
        setLastUpdated("");
        setAuthor("");
        setModule("");
        setPath("");
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- parent container
    private final ObjectProperty<Node> parentContainer = new SimpleObjectProperty<>();
    public Node getParentContainer() { return parentContainer.get(); }
    public void setParentContainer(Node parent) { parentContainer.set(parent); }
    public ObjectProperty<Node> parentContainerProperty() { return parentContainer; }

    // -- status
    private final StringProperty status = new SimpleStringProperty(this, "status", "");
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }

    // -- last updated
    private final StringProperty lastUpdated = new SimpleStringProperty(this, "lastUpdated", "");
    public StringProperty lastUpdatedProperty() { return lastUpdated; }
    public String getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(String value) { lastUpdated.set(value); }

    // -- author
    private final StringProperty author = new SimpleStringProperty(this, "author", "");
    public StringProperty authorProperty() { return author; }
    public String getAuthor() { return author.get(); }
    public void setAuthor(String value) { author.set(value); }

    // -- module
    private final StringProperty module = new SimpleStringProperty(this, "module", "");
    public StringProperty moduleProperty() { return module; }
    public String getModule() { return module.get(); }
    public void setModule(String value) { module.set(value); }

    // -- path
    private final StringProperty path = new SimpleStringProperty(this, "path", "");
    public StringProperty pathProperty() { return path; }
    public String getPath() { return path.get(); }
    public void setPath(String value) { path.set(value); }

    // -- selected
    private final BooleanProperty selected = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(STAMP_SELECTED, get());
        }
    };
    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean value) { selected.set(value); }
}