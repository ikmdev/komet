package dev.ikm.komet.framework.fileimport;

import static dev.ikm.tinkar.common.service.CachingService.LOG;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.concurrent.Task;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import java.io.File;

public class ImportViewModel extends SimpleViewModel {

    public static Task<Boolean> createWorker(File selectedFile) {
        return new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                try {
                    LoadEntitiesFromProtobufFile loadEntities = new LoadEntitiesFromProtobufFile(selectedFile);
                    loadEntities.compute();
                } catch (Exception e) {
                    LOG.error("Exception has been thrown, see below:", e);
                }
                updateProgress(100, 100);
                return true;
            }
        };
    }
}
