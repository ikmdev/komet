package dev.ikm.komet.details.concept;

import javafx.scene.Node;

public abstract class BadgedVersionPaneModel {
    public abstract Node getBadgedPane();

    public abstract void doExpandAllAction(ExpandAction newValue);
}
