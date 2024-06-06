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

import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Node;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import static dev.ikm.komet.framework.graphics.Icon.*;

public class ActivityStreams {
    public static final int marshalVersion = 1;

    public static final PublicIdStringKey<ActivityStream> ANY = new PublicIdStringKey(PublicIds.of("721339e8-f0f9-4187-bfcc-e1e9467a9286"), "any");
    public static final PublicIdStringKey<ActivityStream> UNLINKED = new PublicIdStringKey(PublicIds.of("2b6a31af-4023-4129-a3f5-7f99ef958c4a"), "unlinked");
    public static final PublicIdStringKey<ActivityStream> SEARCH = new PublicIdStringKey(PublicIds.of("b4289694-c8be-45c8-9a59-9834f3a3c647"), "search");
    public static final PublicIdStringKey<ActivityStream> NAVIGATION = new PublicIdStringKey(PublicIds.of("9775b416-f6f9-469d-b14f-a1bced986fec"), "navigation");
    public static final PublicIdStringKey<ActivityStream> KVIEW_NAVIGATION = new PublicIdStringKey(PublicIds.of("8f15fb22-80ce-51d0-9244-ba5a15688e08"), "kview-navigation");
    public static final PublicIdStringKey<ActivityStream> REASONER = new PublicIdStringKey(PublicIds.of("9575ae84-c36a-4c7d-9b10-1c7e581ca9e1"), "reasoner");
    public static final PublicIdStringKey<ActivityStream> CORRELATION = new PublicIdStringKey(PublicIds.of("aef2bd4e-e270-4824-8d39-401b714d1a33"), "correlation");
    public static final PublicIdStringKey<ActivityStream> LIST = new PublicIdStringKey(PublicIds.of("26b42c6d-b9ea-4a20-9446-c283a2d5383c"), "collection");
    public static final PublicIdStringKey<ActivityStream> BUILDER = new PublicIdStringKey(PublicIds.of("f1291277-abe8-465a-87f4-3434c51b6540"), "builder");
    public static final PublicIdStringKey<ActivityStream> FLWOR = new PublicIdStringKey(PublicIds.of("736ae53f-fbac-4fe8-97ea-44d77bda59d7"), "flwor");
    public static final PublicIdStringKey<ActivityStream> PREFERENCES = new PublicIdStringKey(PublicIds.of("6c4753f5-3abd-49f1-ada5-69aa021306da"), "preferences");

    public static final ImmutableList<PublicIdStringKey<ActivityStream>> KEYS =
            Lists.immutable.of(ANY, UNLINKED, SEARCH, NAVIGATION, REASONER, CORRELATION, LIST, FLWOR, BUILDER, PREFERENCES);


    private static ImmutableMap<PublicIdStringKey<ActivityStream>, ActivityStream> activityStreamMap;
    static {
        MutableMap<PublicIdStringKey<ActivityStream>, ActivityStream> tempMap = Maps.mutable.ofInitialCapacity(KEYS.size());
        tempMap.put(ANY, new ActivityStream(ANY_ACTIVITY_STREAM.styleId(), ANY));
        tempMap.put(UNLINKED, new ActivityStream(UNLINKED_ACTIVITY_STREAM.styleId(), UNLINKED));
        tempMap.put(SEARCH, new ActivityStream(SEARCH_ACTIVITY_STREAM.styleId(), SEARCH));
        tempMap.put(NAVIGATION, new ActivityStream(NAVIGATION_ACTIVITY_STREAM.styleId(), NAVIGATION));
        tempMap.put(KVIEW_NAVIGATION, new ActivityStream(NAVIGATION_ACTIVITY_STREAM.styleId(), KVIEW_NAVIGATION));
        tempMap.put(REASONER, new ActivityStream(CLASSIFICATION_ACTIVITY_STREAM.styleId(), REASONER));
        tempMap.put(CORRELATION, new ActivityStream(CORRELATION_ACTIVITY_STREAM.styleId(), CORRELATION));
        tempMap.put(LIST, new ActivityStream(LIST_ACTIVITY_STREAM.styleId(), LIST));
        tempMap.put(BUILDER, new ActivityStream(BUILDER_ACTIVITY_STREAM.styleId(), BUILDER));
        tempMap.put(FLWOR, new ActivityStream(FLWOR_ACTIVITY_STREAM.styleId(), FLWOR));
        tempMap.put(PREFERENCES, new ActivityStream(PREFERENCES_ACTIVITY_STREAM.styleId(), PREFERENCES));

        ActivityStreams.activityStreamMap = tempMap.toImmutable();
    }
    private static MutableMap<PublicIdStringKey<ActivityStream>, ActivityStream> dynamicActivityStreamMap = Maps.mutable.empty();

    public static final ImmutableList<ActivityStream> ACTIVITY_STREAMS() {
        MutableMap<PublicIdStringKey<ActivityStream>, ActivityStream> allActivityStreamMap = Maps.mutable.empty();
        return allActivityStreamMap.withMap(activityStreamMap.toMap()).withMap(dynamicActivityStreamMap).toList().toImmutable();
    }

    public static Node getActivityIcon(PublicIdStringKey<ActivityStream> key) {
        return get(key).getStreamIcon();
    }

    public static ActivityStream get(PublicIdStringKey<ActivityStream> activityStreamKey) {
        ActivityStream standardActivityStream = ActivityStreams.activityStreamMap.get(activityStreamKey);
        if (standardActivityStream != null) {
            return standardActivityStream;
        }
        // go through any dynamically generated activity streams.
        ActivityStream dynActivityStream = dynamicActivityStreamMap.get(activityStreamKey);
        return dynActivityStream;
    }

    /**
     * Returns a newly created activity stream. Create unique activity streams. This will not throw an error if the key
     * already exists.
     *
     * @param key A unique public id for activity stream.
     * @return An ActivityStream instance.
     */
    public static ActivityStream create(PublicIdStringKey<ActivityStream> key) {
        if (dynamicActivityStreamMap.get(key) == null) {
            ActivityStream activityStream = new ActivityStream(ANY_ACTIVITY_STREAM.styleId(), key);
            dynamicActivityStreamMap.put(key, activityStream);
        }
        return dynamicActivityStreamMap.get(key);
    }
    public static void delete(PublicIdStringKey<ActivityStream> key) {
        ActivityStream activityStream = dynamicActivityStreamMap.get(key);
        if (activityStream != null) {
            ActivityStream publisher = dynamicActivityStreamMap.remove(key);
            // TODO Not sure how to unsubscribe subscribers?
        }
    }
}
