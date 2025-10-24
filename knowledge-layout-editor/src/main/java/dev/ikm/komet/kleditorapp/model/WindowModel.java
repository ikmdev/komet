package dev.ikm.komet.kleditorapp.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WindowModel {

    private static final WindowModel INSTANCE = new WindowModel();

    private WindowModel() { }

    public static WindowModel instance() { return INSTANCE; }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- title
    private final StringProperty title = new SimpleStringProperty("Untitled");
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title);}
}