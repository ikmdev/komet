package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentSetControl;
import dev.ikm.komet.kview.controls.ConceptControl;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ComponentSetControlSkin extends SkinBase<ComponentSetControl> {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentSetControl.class);

    private final Label titleLabel;
    private final Button addEntryButton;
    private final int FIRST_CC_INDEX = 1;

    public ComponentSetControlSkin(ComponentSetControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());

        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(e -> {
            ConceptControl conceptControl = new ConceptControl();
            conceptControl.entityProperty().subscribe(entity -> {
                if (entity != null) {
                    int index = getChildren().indexOf(conceptControl) - FIRST_CC_INDEX;
                    if (index < control.getEntitiesList().size()) {
                        control.getEntitiesList().set(index, entity);
                    } else {
                        control.getEntitiesList().add(entity);
                    }
                }
            });
            conceptControl.setOnRemoveAction(ev -> {
                getChildren().remove(conceptControl);
                if (control.getEntitiesList().isEmpty()) {
                    addEntryButton.fire();
                }
            });
            getChildren().add(getChildren().size() - 1, conceptControl);
            getSkinnable().requestLayout();
        });
        getChildren().addAll(titleLabel, addEntryButton);
        getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded() && change.getAddedSize() == 1) {
                    Entity<?> entity = ((ConceptControl) change.getAddedSubList().getFirst()).getEntity();
                    if (entity != null) {
                        int index = change.getFrom() - FIRST_CC_INDEX;
                        if (index >= control.getEntitiesList().size()) {
                            control.getEntitiesList().add(entity);
                        } else {
                            control.getEntitiesList().add(index, entity);
                        }
                    }
                } else if (change.wasRemoved() && change.getRemovedSize() == 1) {
                    int index = change.getFrom() - FIRST_CC_INDEX;
                    if (index >= 0) {
                        control.getEntitiesList().remove(index);
                    }
                }
            }

        });
        addEntryButton.fire();
        // Only allow one empty ConceptControl
        addEntryButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                getChildren().stream().anyMatch(n -> n instanceof ConceptControl cc && cc.getEntity() == null),
                getChildren(), control.entitiesProperty()));

        getSkinnable().setOnMouseDragReleased(e -> {
            System.out.println(e);
            e.consume();
        });
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;
        double spacing = 10;
        for (ConceptControl conceptControl : getChildren().stream()
                .filter(ConceptControl.class::isInstance)
                .map(ConceptControl.class::cast)
                .toList()) {
            double conceptControlPrefWidth = contentWidth - padding.getRight() - x;
            double conceptControlPrefHeight = conceptControl.prefHeight(conceptControlPrefWidth);
            conceptControl.resizeRelocate(x, y, conceptControlPrefWidth, conceptControlPrefHeight);
            y += conceptControlPrefHeight + spacing;
        }
        double buttonPrefWidth = addEntryButton.prefWidth(-1);
        addEntryButton.resizeRelocate(contentWidth - buttonPrefWidth - padding.getRight(), y, buttonPrefWidth, addEntryButton.prefHeight(buttonPrefWidth));
    }

    private void updateEntitiesList() {
        List<? extends Entity<?>> list = getChildren().stream()
                .filter(ConceptControl.class::isInstance)
                .map(ConceptControl.class::cast)
                .map(ConceptControl::getEntity)
                .filter(Objects::nonNull)
                .toList();
        getSkinnable().entitiesProperty().get().clear();
        getSkinnable().entitiesProperty().get().setAll(list);
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-set-control").getString(key);
    }
}
