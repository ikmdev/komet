package dev.ikm.komet.framework.search;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public record SearchControllerAndNode(BorderPane searchPanelPane, SearchPanelController controller) {
    public SearchControllerAndNode() throws IOException {
        this(new FXMLLoader(SearchControllerAndNode.class.getResource("/dev/ikm/komet/framework/search/SearchPanel.fxml")));
    }

    public SearchControllerAndNode(FXMLLoader loader) throws IOException {
        this(loader.load(), loader.getController());
    }
}
