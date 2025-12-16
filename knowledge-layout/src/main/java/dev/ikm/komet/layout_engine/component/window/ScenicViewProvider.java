package dev.ikm.komet.layout_engine.component.window;

import dev.ikm.komet.layout.KlSceneEnhancer;
import javafx.scene.Scene;

public class ScenicViewProvider implements KlSceneEnhancer {

    @Override
    public void accept(Scene scene) {
        //ScenicView.show(scene);
        //TODO: Handle scenic view better, maybe as a plugin.
    }
}
