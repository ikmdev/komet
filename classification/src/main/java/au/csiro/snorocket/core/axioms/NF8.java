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
 * Normal form 8. Feature f with operator op and value v subsumes B.
 * 
 * @author Alejandro Metke
 * 
 */
public final class NF8 extends NormalFormGCI {

    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    
    final public Datatype lhsD;
    final public int rhsB;

    /**
	 * 
	 */
    public NF8(Datatype lhsD, int rhsB) {
        this.lhsD = lhsD;
        this.rhsB = rhsB;
    }

    public static NF8 getInstance(Datatype d, int b) {
        return new NF8(d, b);
    }

    public String toString() {
        return lhsD.getFeature() + ".(" + lhsD.getLiteral() + ")" + " [ " + rhsB;
    }

    @Override
    public int[] getConceptsInAxiom() {
        return new int[] { rhsB };
    }

}
