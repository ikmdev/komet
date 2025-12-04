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
package dev.ikm.tinkar.coordinate.logic;

import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PremiseSet implements ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<PremiseSet, PremiseSet> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static final PremiseSet INFERRED_ONLY = make(PremiseType.INFERRED);
    public static final PremiseSet STATED_ONLY = make(PremiseType.STATED);
    public static final PremiseSet STATED_AND_INFERRED = make(PremiseType.INFERRED, PremiseType.STATED);


    private int[] flags;
    private long bits = 0;

    private PremiseSet(PremiseType... premises) {
        flags = new int[premises.length];
        for (int i = 0; i < premises.length; i++) {
            PremiseType premise = premises[i];
            bits |= (1L << premise.ordinal());
            flags[i] = TaxonomyFlag.getFlagsFromPremiseType(premise);
        }
    }

    private PremiseSet(Collection<? extends PremiseType> premises) {
        flags = new int[premises.size()];
        Iterator<? extends PremiseType> premiseIterator = premises.iterator();
        for (int i = 0; i < premises.size(); i++) {
            PremiseType premise = premiseIterator.next();
            bits |= (1L << premise.ordinal());
            flags[i] = TaxonomyFlag.getFlagsFromPremiseType(premise);
        }
    }


    @Decoder
    public static Object decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                int size = in.readVarInt();
                List<PremiseType> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(PremiseType.valueOf(in.readString()));
                }
                return SINGLETONS.computeIfAbsent(new PremiseSet(values), statusSet -> statusSet);
        }
    }

    public static PremiseSet of(PremiseType... premises) {
        return make(premises);
    }

    public static PremiseSet make(PremiseType... premises) {
        return SINGLETONS.computeIfAbsent(new PremiseSet(premises), premiseSet -> premiseSet);
    }

    public static PremiseSet of(Collection<? extends PremiseType> premises) {
        return make(premises);
    }

    public static PremiseSet make(Collection<? extends PremiseType> premises) {
        return SINGLETONS.computeIfAbsent(new PremiseSet(premises), premiseSet -> premiseSet);
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        EnumSet<PremiseType> premiseSet = toEnumSet();
        out.writeVarInt(premiseSet.size());
        for (PremiseType premise : premiseSet) {
            out.writeString(premise.name());
        }
    }

    public EnumSet<PremiseType> toEnumSet() {
        EnumSet<PremiseType> result = EnumSet.noneOf(PremiseType.class);
        for (PremiseType premise : PremiseType.values()) {
            if (contains(premise)) {
                result.add(premise);
            }
        }
        return result;
    }

    public boolean contains(PremiseType status) {
        return (bits & (1L << status.ordinal())) != 0;
    }

    public int[] getFlags() {
        return flags;
    }

    public PremiseType[] toArray() {
        EnumSet<PremiseType> statusSet = toEnumSet();
        return statusSet.toArray(new PremiseType[statusSet.size()]);
    }

    public boolean containsAll(Collection<PremiseType> c) {
        for (PremiseType premise : c) {
            if (!contains(premise)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAny(Collection<PremiseType> c) {
        for (PremiseType premise : c) {
            if (contains(premise)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiseSet that = (PremiseSet) o;
        return bits == that.bits;
    }

    @Override
    public String toString() {
        return "PremiseSet{" +
                toEnumSet() +
                '}';
    }

    public String toUserString() {
        StringBuilder sb = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        addIfPresent(sb, count, PremiseType.INFERRED);
        addIfPresent(sb, count, PremiseType.STATED);
        return sb.toString();
    }

    private void addIfPresent(StringBuilder sb, AtomicInteger count, PremiseType premise) {
        if (this.contains(premise)) {
            if (count.getAndIncrement() > 0) {
                sb.append(" and ");
            }
            sb.append(premise);
        }
    }
}
