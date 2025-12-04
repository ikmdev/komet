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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.terms.*;

import java.util.Arrays;
import java.util.UUID;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

public class VersionProxy extends EntityProxy implements Version {
    private UUID[] stampUuids;
    private int cachedStampNid = 0;

    public VersionProxy(int nid, int stampNid) {
        super(nid);
        this.cachedStampNid = stampNid;
    }

    public VersionProxy(String description, UUID[] uuids, UUID[] stampUuids) {
        super(description, uuids);
        this.stampUuids = stampUuids;
    }

    public VersionProxy(String description, PublicId publicId, PublicId stampPublicId) {
        super(description, publicId);
        this.stampUuids = stampPublicId.asUuidArray();
    }

    public static VersionProxy make(String name, PublicId publicId, PublicId stampPublicId) {
        return new VersionProxy(name, publicId, stampPublicId);
    }

    public static VersionProxy make(int nid, int stampNid) {
        return new VersionProxy(nid, stampNid);
    }

    public static VersionProxy make(String name, UUID[] uuids, UUID[] stampUuids) {
        return new VersionProxy(name, uuids, stampUuids);
    }

    @Override
    public Stamp stamp() {
        return Entity.getStamp(stampNid());
    }

    public final int stampNid() {
        if (cachedStampNid == 0) {
            cachedStampNid = ScopedValue
                    .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern().publicId())
                    .call(() -> PrimitiveData.nid(stampUuids));
        }
        return cachedStampNid;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (super.equals(o)) {
            if (o instanceof VersionProxy other) {
                if (this.cachedStampNid == 0 && other.cachedStampNid == 0) {
                    return Arrays.equals(this.stampUuids, other.stampUuids);
                }
                return this.cachedStampNid == other.cachedStampNid;
            }
        }
        return false;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.setLength(sb.length() - 1);
        StampEntity stampEntity = Entity.getStamp(stampNid());
        sb.append(" ");
        sb.append(stampEntity.describe());
        sb.append("}");
        return sb.toString();
    }

    public String toXmlFragment() {
        return VersionProxyFactory.toXmlFragment(this);
    }

    public static class Concept extends VersionProxy implements ConceptFacade {
        public Concept(int nid, int stampNid) {
            super(nid, stampNid);
        }

        public Concept(String description, UUID[] uuids, UUID[] stampUuids) {
            super(description, uuids, stampUuids);
        }

        public Concept(String description, PublicId publicId, PublicId stampPublicId) {
            super(description, publicId, stampPublicId);
        }

        public static VersionProxy.Concept make(String name, PublicId publicId, PublicId stampPublicId) {
            return new VersionProxy.Concept(name, publicId, stampPublicId);
        }

        public static VersionProxy.Concept make(int nid, int stampNid) {
            return new VersionProxy.Concept(nid, stampNid);
        }

        public static VersionProxy.Concept make(String name, UUID[] uuids, UUID[] stampUuids) {
            return new VersionProxy.Concept(name, uuids, stampUuids);
        }
    }

    public static class Pattern extends VersionProxy implements PatternFacade {
        public Pattern(int nid, int stampNid) {
            super(nid, stampNid);
        }

        public Pattern(String description, UUID[] uuids, UUID[] stampUuids) {
            super(description, uuids, stampUuids);
        }

        public Pattern(String description, PublicId publicId, PublicId stampPublicId) {
            super(description, publicId, stampPublicId);
        }

        public static VersionProxy.Pattern make(String name, PublicId publicId, PublicId stampPublicId) {
            return new VersionProxy.Pattern(name, publicId, stampPublicId);
        }

        public static VersionProxy.Pattern make(int nid, int stampNid) {
            return new VersionProxy.Pattern(nid, stampNid);
        }

        public static VersionProxy.Pattern make(String name, UUID[] uuids, UUID[] stampUuids) {
            return new VersionProxy.Pattern(name, uuids, stampUuids);
        }
    }

    public static class Semantic extends VersionProxy implements SemanticFacade {
        public Semantic(int nid, int stampNid) {
            super(nid, stampNid);
        }

        public Semantic(String description, UUID[] uuids, UUID[] stampUuids) {
            super(description, uuids, stampUuids);
        }

        public Semantic(String description, PublicId publicId, PublicId stampPublicId) {
            super(description, publicId, stampPublicId);
        }

        public static VersionProxy.Semantic make(String name, PublicId publicId, PublicId stampPublicId) {
            return new VersionProxy.Semantic(name, publicId, stampPublicId);
        }

        public static VersionProxy.Semantic make(int nid, int stampNid) {
            return new VersionProxy.Semantic(nid, stampNid);
        }

        public static VersionProxy.Semantic make(String name, UUID[] uuids, UUID[] stampUuids) {
            return new VersionProxy.Semantic(name, uuids, stampUuids);
        }
    }

}
