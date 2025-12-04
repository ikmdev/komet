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
package dev.ikm.tinkar.common.util.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleBroadcaster<T> implements Broadcaster<T>, Subscriber<T>{

    private static final Logger LOG = LoggerFactory.getLogger(SimpleBroadcaster.class);
    final CopyOnWriteArrayList<WeakReference<Subscriber<T>>> subscriberWeakReferenceList = new CopyOnWriteArrayList<>();
    // TODO-aks8m: Address the issue of a race condition based on spawning threads that aren't blocking
    public void dispatch(T item) {
        subscriberWeakReferenceList.forEach(subscriberWeakReference -> {
            try {
                Subscriber<T> subscriber = subscriberWeakReference.get();
                if (subscriber==null) {
                    subscriberWeakReferenceList.remove(subscriberWeakReference);
                } else {
                    subscriber.onNext(item);
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
                subscriberWeakReferenceList.remove(subscriberWeakReference);
            }
        });
    }

    @Override
    public void onNext(T item) {
        this.dispatch(item);
    }

    public void addSubscriberWithWeakReference(Subscriber<T> subscriber) {
        LOG.debug(subscriber + " subscribing to " + this);
        for (WeakReference<Subscriber<T>> subscriberWeakReference: subscriberWeakReferenceList) {
            if (subscriberWeakReference.get() == subscriber) {
                throw new IllegalStateException("Trying to add duplicate listener: " + subscriber);
            }
        }
        subscriberWeakReferenceList.add(new WeakReference<>(subscriber));
    }
    public void removeSubscriber(Subscriber<T> subscriber) {
        LOG.debug("Removing " + subscriber + " from " + this);
        for (WeakReference<Subscriber<T>> subscriberWeakReference: subscriberWeakReferenceList) {
            if (subscriberWeakReference.get() == subscriber) {
                subscriberWeakReferenceList.remove(subscriberWeakReference);
            }
        }
    }
}
