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
package au.csiro.ontology.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generic class used to collect performance statistics.
 * 
 * @author Alejandro Metke
 *
 */
public class Statistics {
    
    /**
     * Singleton instance.
     */
    public static final Statistics INSTANCE = new Statistics();
    
    /**
     * Map with performance values.
     */
    private final Map<String, Long> stats = new HashMap<String, Long>();
    
    /**
     * Private constructor.
     */
    private Statistics() {
        
    }
    
    /**
     * Sets a time for a performance measurement.
     * 
     * @param name
     * @param value
     */
    public void setTime(String name, long value) {
        stats.put(name, value);
    }
    
    /**
     * Gets a time for a performance measurement. If no measurement with the
     * specified name exists then it returns -1.
     * 
     * @param name
     * @return
     */
    public long getTime(String name) {
        Long res = stats.get(name);
        return (res == null) ? -1 : res.longValue();
    }
    
    /**
     * Returns the names of the performance measures.
     * 
     * @return
     */
    public Set<String> getNames() {
        return stats.keySet();
    }
    
    /**
     * Returns a string with all the current statistics.
     * @return
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        
        for(String key : stats.keySet()) {
            sb.append(key);
            sb.append(": ");
            sb.append(stats.get(key));
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public long getTotalTime() {
        long res = 0;
        for(String key : stats.keySet()) {
            res += stats.get(key);
        }
        return res;
    }
    
}
