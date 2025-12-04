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
package dev.ikm.tinkar.terms;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Component;

/**
 * <entity desc="" uuids=""/>
 */
public interface EntityFacade extends Component, ComponentWithNid {

    static EntityFacade make(int nid) {
        return EntityProxy.make(nid);
    }

    static int toNid(EntityFacade entityFacade) {
        return entityFacade.nid();
    }

    default String description() {
        return PrimitiveData.text(nid());
    }

    default String toXmlFragment() {
        return ProxyFactory.toXmlFragment(this);
    }

    default <T extends EntityProxy> T toProxy() {
        return ProxyFactory.fromFacade(this);
    }

}