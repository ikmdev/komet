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

open module dev.ikm.tinkar.events {
    exports dev.ikm.tinkar.events;

    requires dev.ikm.tinkar.entity;
    requires org.slf4j;
    requires dev.ikm.tinkar.common;

    provides dev.ikm.tinkar.events.EvtBus
            with dev.ikm.tinkar.events.DefaultEvtBus;

    uses dev.ikm.tinkar.events.EvtBus;
}
