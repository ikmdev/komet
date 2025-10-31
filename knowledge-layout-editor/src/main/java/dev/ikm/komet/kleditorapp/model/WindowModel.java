package dev.ikm.komet.kleditorapp.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class WindowModel {

    private static final WindowModel INSTANCE = new WindowModel();

    private ObservableList<SectionModel> sections = FXCollections.observableArrayList();

    private WindowModel() {
        SectionModel sectionModel = new SectionModel();
        this.sections.add(sectionModel);
    }

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

    // -- sections
    public ObservableList<SectionModel> getSections() { return sections; }
}