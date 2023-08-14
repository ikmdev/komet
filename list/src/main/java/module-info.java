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
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.set.SetNodeFactory;
import dev.ikm.komet.table.TableNodeFactory;

module dev.ikm.komet.list {

    requires transitive dev.ikm.komet.framework;

    opens dev.ikm.komet.list;
    exports dev.ikm.komet.list;

    opens dev.ikm.komet.set;
    exports dev.ikm.komet.set;

    opens dev.ikm.komet.table;
    exports dev.ikm.komet.table;

    provides KometNodeFactory
            with ListNodeFactory, SetNodeFactory, TableNodeFactory;

}