package dev.ikm.komet.framework.controls;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.Region;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TextAreaSkinNoScroller extends TextAreaSkin {

    Region contentView;

    private static Method computePrefHeightMethod = null;

    public TextAreaSkinNoScroller(TextArea control) {
        super(control);
        ScrollPane scrollPane = (ScrollPane) getChildren().get(0);
        this.contentView = (Region) scrollPane.getContent();
        scrollPane.fitToWidthProperty().setValue(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        if (computePrefHeightMethod == null) {
            try {
                computePrefHeightMethod = contentView.getClass().getDeclaredMethod("computePrefHeight", double.class);
                computePrefHeightMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Region getContentView() {
        return contentView;
    }

    @Override
    public double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    public double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    public double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    public double computePrefHeight(double width) {
        try {
            return (Double) computePrefHeightMethod.invoke(contentView, width);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
