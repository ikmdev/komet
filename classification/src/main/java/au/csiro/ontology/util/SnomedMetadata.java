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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class contains the default meta-data that is necessary to successfully 
 * import the SNOMED distribution files into a DL model.
 * 
 * @author Alejandro Metke
 * 
 */
public class SnomedMetadata {
    
    public static final SnomedMetadata INSTANCE = new SnomedMetadata();

    protected Properties props = new Properties();

    protected Set<String> neverGroupedIds = new HashSet<String>();

    protected Map<String, String> rightIdentities = 
            new HashMap<String, String>();

    /**
     * Constructor.
     */
    private SnomedMetadata() {
        try {
            props.load(this.getClass().getResourceAsStream("/metadata.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the root concept id for roles.
     * 
     * @return The root concept id for roles.
     */
    public String getConceptModelAttId() {
        return props.getProperty("conceptModelAttId");
    }

    /**
     * Returns the role id for is-a relationships.
     * 
     * @return The role id for is-a relationships.
     */
    public String getIsAId() {
        return props.getProperty("isAId");
    }

    public String getLateralityId() {
        return props.getProperty("lateralityId");
    }

    /**
     * Returns the id of the enumeration in SNOMED used to indicate that a
     * concept is fully defined.
     * 
     * @return Id of the enumeration in SNOMED used to indicate that a concept
     *         is fully defined.
     */
    public String getConceptDefinedId() {
        return props.getProperty("conceptDefinedId");
    }

    /**
     * Returns the id of the enumeration in SNOMED used to represent an
     * existential quantification.
     * 
     * @return Id of enumeration in SNOMED used to represent an existential
     *         quantification.
     */
    public String getSomeId() {
        return props.getProperty("someId");
    }

    /**
     * Returns the id of the enumeration in SNOMED used to indicate that a
     * description is a fully specified name.
     * 
     * @return The id of the enumeration in SNOMED used to indicate that a
     *         description is a fully specified name.
     */
    public String getFsnId() {
        return props.getProperty("fsnId");
    }

    /**
     * Returns the id of the enumeration in SNOMED used to indicate that a
     * description is a synonym.
     * 
     * @return Id of the enumeration in SNOMED used to indicate that a
     *         description is a synonym.
     */
    public String getSynonymId() {
        return props.getProperty("synonymId");
    }

    /**
     * Returns the id of the enumeration in SNOMED used to indicate that a
     * description is a definition.
     * 
     * @return Id of enumeration in SNOMED used to indicate that a description
     *         is a definition.
     */
    public String getDefinitionId() {
        return props.getProperty("definitionId");
    }
    
    /**
     * Returns the raw property value for the never grouped ids.
     * 
     * @return
     */
    public String getNeverGroupedIdsString() {
        return props.getProperty("neverGroupedIds");
    }

    /**
     * Returns the set of ids of SNOMED roles that should never be placed in a
     * role group.
     * 
     * @return Set of ids of SNOMED roles that should never be placed in a role
     *         group.
     */
    public Set<String> getNeverGroupedIds() {
        String s = props.getProperty("neverGroupedIds");
        if(s == null) return Collections.emptySet();
        
        Set<String> res = new HashSet<String>();
        String[] parts = s.split("[,]");
        for(String part : parts) {
            res.add(part);
        }
        return res;
    }
    
    /**
     * Returns the raw property value for the right identity ids.
     * 
     * @return
     */
    public String getRightIdentityIdsString() {
        return props.getProperty("rightIdentityIds");
    }

    /**
     * Returns the right identity axioms that cannot be represented in RF1 or
     * RF2 formats. An example is direct-substance o has-active-ingredient [
     * direct-substance. The key of the returned map is the first element in the
     * LHS and the value is the second element in the LHS. The RHS, because it
     * is a right identity axiom, is always the same as the first element in the
     * LHS.
     * 
     * @return The right identity axioms.
     */
    public Map<String, String> getRightIdentityIds() {
        String s = props.getProperty("rightIdentityIds");
        if(s == null) return Collections.emptyMap();
        
        Map<String, String> res = new HashMap<String, String>();
        String[] parts = s.split("[,]");
        res.put(parts[0], parts[1]);
        return res;
    }

    /**
     * Returns the id for the "role group" role.
     * 
     * @return The id for the "role group" role.
     */
    public String getRoleGroupId() {
        return props.getProperty("roleGroupId");
    }

}
