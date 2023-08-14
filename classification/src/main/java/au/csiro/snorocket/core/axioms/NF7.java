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
package au.csiro.snorocket.core.axioms;

import au.csiro.snorocket.core.model.Datatype;

/**
 * Normal form 7. A subsumes feature f with operator op and value v.
 * 
 * @author Alejandro Metke
 * 
 */
public final class NF7 extends NormalFormGCI implements IFeatureQueueEntry {

    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    
    final public int lhsA;
    final public Datatype rhsD;

    /**
	 * 
	 */
    public NF7(int lhsA, Datatype rhsD) {
        this.lhsA = lhsA;
        this.rhsD = rhsD;
    }

    public Datatype getD() {
        return rhsD;
    }

    static public NF7 getInstance(final int a, Datatype d) {
        return new NF7(a, d);
    }

    public String toString() {
        return lhsA + " [ " + rhsD.getFeature() + ".(" + rhsD.getLiteral() + ")";
    }

    @Override
    public int[] getConceptsInAxiom() {
        return new int[] { lhsA };
    }

}
