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
package dev.ikm.komet.framework;

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;

public interface SetupNode {
    /**
     * Call to setup Komet specific framework items. Will be executed after the
     * FXML initialize() method.
     * TODO: refactor setup to require reading the activity stream key from node preferences.
     *  @param windowView Either used directly, or an overridable ObservableView is created by the KometNode
     *
     * @param nodePreferences   persistent preferences to save and restore KometNode state.
     * @param activityStreamKey Key for the activity stream
     * @param alertBroadcaster       from the parent node.
     */
    void setup(ObservableViewNoOverride windowView,
               KometPreferences nodePreferences,
               PublicIdStringKey<ActivityStream> activityStreamKey,
               Broadcaster<AlertObject> alertBroadcaster);
}
