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
package dev.ikm.tinkar.provider.executor;

import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertReportingService;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertLogSubscriber implements AlertReportingService {
    private static final Logger LOG = LoggerFactory.getLogger(AlertLogSubscriber.class);

    public AlertLogSubscriber() {
        this(AlertStreams.ROOT_ALERT_STREAM_KEY);
    }

    public AlertLogSubscriber(PublicIdStringKey<Broadcaster<AlertObject>> alertStreamKey) {
        LOG.info("Constructing AlertLogSubscriber");
        AlertStreams.get(alertStreamKey).addSubscriberWithWeakReference(this);
    }

    @Override
    public void onNext(AlertObject item) {
        LOG.info("AlertLogSubscriber: \n" + item.toString());
    }
}
