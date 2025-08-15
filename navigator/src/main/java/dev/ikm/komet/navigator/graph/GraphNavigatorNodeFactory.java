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
package dev.ikm.komet.navigator.graph;

import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */

public class GraphNavigatorNodeFactory
        implements KometNodeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GraphNavigatorNodeFactory.class);
    protected static final String STYLE_ID = GraphNavigatorNode.STYLE_ID;
    protected static final String TITLE = GraphNavigatorNode.TITLE;
    
    public static GraphNavigatorNodeFactory provider() {
		return new GraphNavigatorNodeFactory();
	}

	public GraphNavigatorNodeFactory() {
		super();
	}

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public GraphNavigatorNode create(ObservableViewNoOverride windowViewReference, KometPreferences nodePreferences) {
        return reconstructor(windowViewReference, nodePreferences);
    }

    @Reconstructor
    public static GraphNavigatorNode reconstructor(ObservableViewNoOverride windowViewReference, KometPreferences nodePreferences) {
        return new GraphNavigatorNode(windowViewReference.makeOverridableViewProperties("GraphNavigatorNodeFactory.reconstructor"), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return GraphNavigatorNode.class;
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.of(ActivityStreams.NAVIGATION);
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (defaultActivityStreamChoices().contains(streamKey)) {
            return Lists.immutable.of(ActivityStreamOption.PUBLISH.keyForOption());
        }
        return Lists.immutable.empty();
    }

    @Override
    public String getMenuText() {
        return TITLE;
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }
}
