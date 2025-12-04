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

import dev.ikm.tinkar.common.util.broadcast.Subscriber;

import java.util.function.Consumer;

public class FlowSubscriber<T> implements Subscriber<T> {

    Consumer<T> action;

    public FlowSubscriber(Consumer<T> action) {
        this.action = action;
    }


    @Override
    public void onNext(T next) {
        action.accept(next);
    }
}
