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
package dev.ikm.komet.framework.activity;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;

public enum ActivityStreamOption  {
    PUBLISH(new PublicIdStringKey(PublicIds.of("3ea7167e-78b4-45fa-8864-004a87f32518"),
            "publish"), "publish-to-activityStream"),
    SUBSCRIBE(new PublicIdStringKey(PublicIds.of("3baebd44-bf78-4dfb-ad67-3d53fae3a0c6"),
            "subscribe"), "subscribe-to-activityStream"),
    SYNCHRONIZE(new PublicIdStringKey(PublicIds.of("0a2331f8-6d05-4337-95b5-275c2ff9659b"),
            "synchronize"), "synchronize-activityStream");

    final PublicIdStringKey<ActivityStreamOption> keyForOption;
    final String styleId;

    ActivityStreamOption(PublicIdStringKey<ActivityStreamOption> keyForOption, String styleId) {
        this.keyForOption = keyForOption;
        this.styleId = styleId;
    }

    public PublicIdStringKey<ActivityStreamOption> keyForOption() {
        return this.keyForOption;
    }

    public Label iconForOption() {
        return Icon.makeIcon(this.styleId);
    }

    public static ActivityStreamOption get(PublicIdStringKey<ActivityStreamOption> keyForOption) {
        for (ActivityStreamOption activityStreamOption: values()) {
            if (activityStreamOption.keyForOption.equals(keyForOption)) {
                return activityStreamOption;
            }
        }
        throw new IllegalStateException("No ActivityStreamOption for: " + keyForOption);
    }

    public static ImmutableList<ActivityStreamOption> optionsForStream(ActivityStream activityStream) {
                return optionsForStream(activityStream.activityStreamKey);
    }

    public static ImmutableList<ActivityStreamOption> optionsForStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (streamKey.equals(ActivityStreams.ANY)) {
            return Lists.immutable.of(SUBSCRIBE);
        }
        if (streamKey.equals(ActivityStreams.UNLINKED)) {
            return Lists.immutable.of(PUBLISH);
        }
        return Lists.immutable.of(PUBLISH, SUBSCRIBE, SYNCHRONIZE);
    }
}
