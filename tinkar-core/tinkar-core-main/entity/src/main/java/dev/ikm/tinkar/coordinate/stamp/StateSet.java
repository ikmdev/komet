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
package dev.ikm.tinkar.coordinate.stamp;

import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import dev.ikm.tinkar.terms.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An immutable bitset implementation of a State set.
 */
public class StateSet implements ImmutableCoordinate, Iterable<State> {

    private static final ConcurrentReferenceHashMap<StateSet, StateSet> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static final StateSet ACTIVE = make(State.ACTIVE);
    public static final StateSet ACTIVE_AND_INACTIVE = make(State.ACTIVE, State.INACTIVE);
    public static final StateSet ACTIVE_INACTIVE_AND_WITHDRAWN = make(State.ACTIVE, State.INACTIVE, State.WITHDRAWN);
    public static final StateSet INACTIVE = make(State.INACTIVE);
    public static final StateSet WITHDRAWN = make(State.WITHDRAWN);
    private final UUID uuid;
    private long bits = 0;

    private StateSet(State... states) {
        for (State state : states) {
            bits |= (1L << state.ordinal());
        }
        uuid = UuidT5Generator.get(UUID.fromString("324d86b8-2905-4942-9bd1-8dcb06d76cfa"), Long.toString(bits));
    }

    private StateSet(Collection<? extends State> states) {
        for (State State : states) {
            bits |= (1L << State.ordinal());
        }
        uuid = UuidT5Generator.get(UUID.fromString("324d86b8-2905-4942-9bd1-8dcb06d76cfa"), Long.toString(bits));
    }

    @Decoder
    public static StateSet decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                int size = in.readVarInt();
                List<State> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(State.valueOf(in.readString()));
                }
                return SINGLETONS.computeIfAbsent(new StateSet(values), StateSet -> StateSet);
        }
    }

    public static StateSet of(State... states) {
        return make(states);
    }

    public static StateSet make(State... states) {
        return SINGLETONS.computeIfAbsent(new StateSet(states), StateSet -> StateSet);
    }

    public static StateSet of(Collection<? extends State> states) {
        return make(states);
    }

    public static StateSet make(Collection<? extends State> states) {
        return SINGLETONS.computeIfAbsent(new StateSet(states), StateSet -> StateSet);
    }

    public UUID stateSetUuid() {
        return uuid;
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        EnumSet<State> StateSet = toEnumSet();
        out.writeVarInt(StateSet.size());
        for (State State : StateSet) {
            out.writeString(State.name());
        }
    }

    public EnumSet<State> toEnumSet() {
        EnumSet<State> result = EnumSet.noneOf(State.class);
        for (State State : State.values()) {
            if (contains(State)) {
                result.add(State);
            }
        }
        return result;
    }

    public boolean contains(State state) {
        return (bits & (1L << state.ordinal())) != 0;
    }

    public State[] toArray() {
        EnumSet<State> stateSet = toEnumSet();
        return stateSet.toArray(new State[stateSet.size()]);
    }

    public boolean containsAll(Collection<State> c) {
        for (State state : c) {
            if (!contains(state)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAny(Collection<State> c) {
        for (State state : c) {
            if (contains(state)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActiveOnly() {
        return (this.bits ^ ACTIVE.bits) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateSet that = (StateSet) o;
        return bits == that.bits;
    }

    @Override
    public String toString() {
        return "StateSet{" +
                toEnumSet() +
                '}';
    }

    public String toUserString() {
        StringBuilder sb = new StringBuilder("[");
        AtomicInteger count = new AtomicInteger();
        addIfPresent(sb, count, State.ACTIVE);
        addIfPresent(sb, count, State.CANCELED);
        addIfPresent(sb, count, State.INACTIVE);
        addIfPresent(sb, count, State.PRIMORDIAL);
        addIfPresent(sb, count, State.WITHDRAWN);
        sb.append("]");
        return sb.toString();
    }

    private void addIfPresent(StringBuilder sb, AtomicInteger count, State State) {
        if (this.contains(State)) {
            if (count.getAndIncrement() > 0) {
                sb.append(", ");
            }
            sb.append(State);
        }
    }

    @Override
    public Iterator<State> iterator() {
        return toEnumSet().iterator();
    }

    public int size() {
        return toEnumSet().size();
    }
}
