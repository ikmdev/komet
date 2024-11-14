package dev.ikm.komet.kview.mvvm.view.pattern;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

public class InstancesController {
    @InjectViewModel
    private SimpleViewModel simpleViewModel;

    @FXML
    private ListView instancesListView;

}