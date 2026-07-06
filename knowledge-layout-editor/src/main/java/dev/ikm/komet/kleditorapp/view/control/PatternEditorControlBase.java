package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Common base for the editor-side representations of a pattern. Holds the parts shared by every
 * representation — the title, its visibility, the parent-section link and the titled container — while
 * leaving the body to subclasses: {@link PatternStandardEditorControl} fills it with an author-sized grid of field
 * tiles, {@link PatternTableEditorControl} with an actual table. Subclasses install their body via
 * {@link #setContent(Node)}.
 */
public abstract class PatternEditorControlBase extends GridBaseControl {
    public static final PseudoClass TITLE_VISIBLE = PseudoClass.getPseudoClass("title-visible");

    private final VBox patternContainer = new VBox();
    private final Label patternTitle = new Label();

    protected PatternEditorControlBase() {
        patternContainer.getStyleClass().add("pattern-container");
        patternTitle.getStyleClass().add("pattern-title");

        patternTitle.textProperty().bind(title);
        patternContainer.getChildren().add(patternTitle);

        getChildren().add(patternContainer);
    }

    /**
     * Installs the representation-specific body below the title. The title is always the first child; the
     * body is the second, replaced on subsequent calls.
     */
    protected void setContent(Node content) {
        if (patternContainer.getChildren().size() > 1) {
            patternContainer.getChildren().set(1, content);
        } else {
            patternContainer.getChildren().add(content);
        }
    }

    @Override
    public void delete() {
        getParentSection().getPatterns().remove(this);
    }

    @Override
    protected void layoutContent(double contentX, double contentY, double contentWidth, double contentHeight) {
        patternContainer.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    // -- parent section
    private final ReadOnlyObjectWrapper<SectionViewControl> parentSection = new ReadOnlyObjectWrapper<>();
    public SectionViewControl getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<SectionViewControl> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(SectionViewControl parentSection) { this.parentSection.set(parentSection); }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- title visible
    private final BooleanProperty titleVisible = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(TITLE_VISIBLE, get());
        }
    };
    public boolean isTitleVisible() { return titleVisible.get(); }
    public BooleanProperty titleVisibleProperty() { return titleVisible; }
    public void setTitleVisible(boolean value) { titleVisible.setValue(value); }
}
