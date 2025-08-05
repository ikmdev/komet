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
package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import one.jpro.platform.auth.core.authentication.User;

/**
 * Event representing a user sign-in action.
 * <p>
 * This event is published when a user successfully signs in. It contains
 * information about the authenticated user and is used to notify other components
 * within the application about the sign-in event.
 * </p>
 *
 * @see Evt
 * @see EvtType
 * @see User
 */
public class SignInUserEvent extends Evt {

    /**
     * The event type for a user sign-in event.
     * <p>
     * This constant defines the specific type of the {@code SignInUserEvent},
     * which can be used for event filtering and handling.
     * </p>
     */
    public static final EvtType<SignInUserEvent> SIGN_IN_USER = new EvtType<>(Evt.ANY, "SIGN_IN_USER");

    private final User user;

    /**
     * Constructs a new {@code SignInUserEvent}.
     *
     * @param source  The source object that generated the event.
     * @param evtType The type of the event, typically {@link #SIGN_IN_USER}.
     * @param user    The authenticated {@link User} associated with this event.
     */
    public SignInUserEvent(Object source, EvtType<? extends Evt> evtType, User user) {
        super(source, evtType);
        this.user = user;
    }

    /**
     * Returns the authenticated user associated with this event.
     *
     * @return the {@link User} who has signed in.
     */
    public User getUser() {
        return user;
    }
}
