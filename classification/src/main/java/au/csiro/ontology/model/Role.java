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
package au.csiro.ontology.model;

import java.io.Serializable;

/**
 * Base class for features.
 * 
 * @author Alejandro Metke
 *
 */
public abstract class Role implements Comparable<Role>, Serializable {
    
    private static final long serialVersionUID = 1L;

    abstract public String toString();

    abstract public int hashCode();

    abstract public boolean equals(Object o);
    
}
