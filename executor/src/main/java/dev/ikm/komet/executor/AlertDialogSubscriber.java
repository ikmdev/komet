/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.executor;

import com.google.auto.service.AutoService;
import javafx.application.Platform;
import dev.ikm.komet.framework.Dialogs;
import dev.ikm.tinkar.common.alert.*;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Flow;

/**
 * Presents dialogs for alerts
 */
@AutoService(AlertReportingService.class)
public class AlertDialogSubscriber implements AlertReportingService {
    private static final Logger LOG = LoggerFactory.getLogger(AlertDialogSubscriber.class);

    public AlertDialogSubscriber() {
        this(AlertStreams.ROOT_ALERT_STREAM_KEY);
    }

    public AlertDialogSubscriber(PublicIdStringKey<Broadcaster<AlertObject>> alertStreamKey) {
        LOG.info("Constructing AlertDialogSubscriber");
        AlertStreams.get(alertStreamKey).addSubscriberWithWeakReference(this);
    }

    @Override
    public void onNext(AlertObject item) {
        Platform.runLater(() -> Dialogs.showDialogForAlert(item));
    }
}
