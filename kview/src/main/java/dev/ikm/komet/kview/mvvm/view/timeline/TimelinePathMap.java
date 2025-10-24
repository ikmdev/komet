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
package dev.ikm.komet.kview.mvvm.view.timeline;

import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;

import java.util.*;

/**
 * Path Name -> ModuleNid -> StampNid -> Set of VersionChangeRecords
 */
public class TimelinePathMap extends TreeMap<String, TreeMap<Integer, TreeMap<Integer, TreeSet<VersionChangeRecord>>>> {
    public List<Integer> getModuleNids(String pathName) {
        if (get(pathName) == null) {
            return  Collections.emptyList();
        }
        return get(pathName).keySet().stream().toList();
    }

    public LinkedHashMap<String, List<Integer>> getPathModulesNidOnlyMap() {
        LinkedHashMap<String, List<Integer>> collection = new LinkedHashMap<>();
        forEach( (path, moduleMap) -> {
            collection.put(path, this.getModuleNids(path));
        });
        return  collection;
    }
}
