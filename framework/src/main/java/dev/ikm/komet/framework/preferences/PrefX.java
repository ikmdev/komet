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
package dev.ikm.komet.framework.preferences;

import java.util.HashMap;
import java.util.Map;

public class PrefX {
    private Map<Enum, Object> pref = new HashMap<>();
    private PrefX(){

    }
    public static PrefX create() { return new PrefX();}
    public <T> PrefX setValue(Enum e, T value) {
        pref.put(e, value);
        return this;
    }
    public <T> T getValue(Enum e) {
        return (T) pref.get(e);
    }
    public String toString() {
        return pref.toString(); // may have to iterate
    }
}
