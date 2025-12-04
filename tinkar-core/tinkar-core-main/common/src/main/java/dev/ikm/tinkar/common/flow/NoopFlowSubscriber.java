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
package dev.ikm.tinkar.common.flow;

import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;

import java.util.concurrent.Flow;

public class NoopFlowSubscriber implements Flow.Subscriber<Object> {
    Flow.Subscription subscription;

    public Flow.Subscription subscription() {
        return subscription;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Object item) {
        // Do nothing with item, but request another...
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        AlertStreams.getRoot().dispatch(AlertObject.makeError(throwable));
    }

    @Override
    public void onComplete() {
        // Do nothing
    }
}
