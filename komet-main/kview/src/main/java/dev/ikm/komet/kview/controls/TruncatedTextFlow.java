package dev.ikm.komet.kview.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TruncatedTextFlow extends TextFlow {

    private static final String ELLIPSIS_STRING = "\u2026";

    private final TextFlow textFlow;
    private final Font regularFont;

    public TruncatedTextFlow() {
        getStyleClass().add("truncated-text-flow");
        textFlow = new TextFlow();
        regularFont = Font.font("Noto Sans", 14);
    }

    // textProperty
    private final ObjectProperty<String> textProperty = new SimpleObjectProperty<>(this, "text") {

        @Override
        protected void invalidated() {
            fitNodes();
        }
    };
    public final ObjectProperty<String> textProperty() {
       return textProperty;
    }
    public final String getText() {
       return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
    }

    // maxContentHeightProperty
    private final DoubleProperty maxContentHeightProperty = new SimpleDoubleProperty(this, "maxContentHeight") {
        @Override
        protected void invalidated() {
            if (get() > 0) {
                fitNodes();
            }
        }
    };
    public final DoubleProperty maxContentHeightProperty() {
       return maxContentHeightProperty;
    }
    public final double getMaxContentHeight() {
       return maxContentHeightProperty.get();
    }
    public final void setMaxContentHeight(double value) {
        maxContentHeightProperty.set(value);
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets insets = getInsets();
        double top = snapSpaceY(insets.getTop());
        double bottom = snapSpaceY(insets.getBottom());
        double height = snapSizeY(textFlow.getHeight());
        return top + height + bottom;
    }

    @Override
    protected void setWidth(double v) {
        super.setWidth(v);
    }

    private void fitNodes() {
        Text textNode = textNodeFactory(getText());
        textFlow.getChildren().setAll(textNode);
        double prefWidth = Math.min(getContentPrefWidth(textFlow), getWidth() <= 0 ? getMaxWidth() : getWidth());
        textFlow.setPrefWidth(prefWidth);
        setPrefWidth(prefWidth);
        textFlow.autosize();
        double maxHeight = getMaxContentHeight();
        while (textFlow.getHeight() > maxHeight) {
            textFlow.autosize();
            String text = textNode.getText();
            if (textFlow.getHeight() > maxHeight) {
                int iter = text.length() - 1;
                while (iter >= 0 && textFlow.getHeight() > maxHeight) {
                    textNode.setText(text.substring(0, iter--) + ELLIPSIS_STRING);
                    textFlow.autosize();
                }
            } else {
                textNode.setText(text + ELLIPSIS_STRING);
            }
        }
        getChildren().setAll(textFlow.getChildren());
        Tooltip tooltip = new Tooltip(getText());
        tooltip.setPrefWidth(300);
        tooltip.setWrapText(true);
        if (!textNode.getText().equals(getText())) {
            Tooltip.install(this, tooltip);
        } else {
            Tooltip.uninstall(this, tooltip);
        }
    }

    private Text textNodeFactory(String text) {
        Text textNode = new Text(text);
        textNode.getStyleClass().add("text");
        textNode.setFont(regularFont);
        return textNode;
    }

    private double getContentPrefWidth(TextFlow textFlow) {
        textFlow.setMaxWidth(getMaxWidth());
        textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        textFlow.autosize();
        textFlow.layout();
        textFlow.applyCss();
        double maxChildX = 0;
        for (Node child : textFlow.getChildren()) {
            if (child.isManaged()) {
                maxChildX = Math.max(maxChildX, child.getBoundsInParent().getMaxX());
            }
        }
        return maxChildX + getInsets().getLeft() + getInsets().getRight() + 1;
    }

}
