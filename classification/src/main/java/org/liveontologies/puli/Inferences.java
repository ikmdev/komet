/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli;

import java.util.List;
import java.util.Set;

import java.util.Objects;
import java.util.function.Function;

/**
 * A collection of static methods for working with {@link Inference}s
 * 
 * @author Yevgeny Kazakov
 */
public class Inferences {

	/**
	 * Creates an {@link Inference} with the supplied parameters
	 * 
	 * @param name
	 * @param conclusion
	 * @param premises
	 * @return a new {@link Inference} with the given name, conclusion, and
	 *         premises
	 * @see Inference#getName()
	 * @see Inference#getConclusion()
	 * @see Inference#getPremises()
	 */
	public static <C> Inference<C> create(String name, C conclusion,
			List<? extends C> premises) {
		return new BaseInference<C>(name, conclusion, premises);
	}

	/**
	 * Creates an {@link AxiomPinpointingInference} with the supplied parameters
	 * 
	 * @param name
	 * @param conclusion
	 * @param premises
	 * @param justification
	 * @return a new {@link AxiomPinpointingInference} with the given name,
	 *         conclusion, premises, and justification
	 * @see Inference#getName()
	 * @see Inference#getConclusion()
	 * @see Inference#getPremises()
	 * @see AxiomPinpointingInference#getJustification()
	 */
	public static <C, A> AxiomPinpointingInference<C, A> create(String name,
			C conclusion, List<? extends C> premises,
			Set<? extends A> justification) {
		return new BaseAxiomPinpointingInference<C, A>(name, conclusion,
				premises, justification);
	}

	/**
	 * Tests if the given {@link Inference} is asserted, i.e., its name is equal
	 * to {@link AssertedConclusionInference#NAME}
	 * 
	 * @param inference
	 * @return {@code true} if the inference is asserted and {@code false}
	 *         otherwise
	 */
	public static boolean isAsserted(Inference<?> inference) {
		return AssertedConclusionInference.NAME.equals(inference.getName());
	}

	/**
	 * @param inference
	 * @param function
	 * @return an {@link Inference} obtained from the supplied one by replacing
	 *         conclusion and premises according to the provided function.
	 */
	public static <F, T> Inference<T> transform(
			final Inference<? extends F> inference,
			final Function<? super F, ? extends T> function) {
		return new TransformedInference<F, T>(inference, function);
	}

	/**
	 * Tests equality of an given {@link Inference} to a given object.
	 * 
	 * @param inference
	 * @param o
	 * @return {@code true} if the object is an inference and has the same name,
	 *         conclusion, and premises as the given inference
	 * 
	 * @see Inference#getName()
	 * @see Inference#getConclusion()
	 * @see Inference#getPremises()
	 * @see #hashCode()
	 */
	public static boolean equals(Inference<?> inference, Object o) {
		if (inference == o) {
			return true;
		}
		if (o instanceof Inference<?>) {
			Inference<?> other = (Inference<?>) o;
			return (inference.getName().equals(other.getName())
					&& inference.getConclusion().equals(other.getConclusion())
					&& inference.getPremises().equals(other.getPremises()));
		}
		// else
		return false;
	}

	/**
	 * Computes the hash value of an {@link Inference}. As usual, the hash
	 * function is compatible with the equality function, that is, if two
	 * inferences are equal then the same hash values are returned for them.
	 * 
	 * @param inference
	 * @return an integer number uniquely determined by the inference name, its
	 *         conclusion, and its premises
	 * 
	 * @see Inference#getName()
	 * @see Inference#getConclusion()
	 * @see Inference#getPremises()
	 * @see #equals(Object)
	 */
	public static int hashCode(Inference<?> inference) {
		if (inference == null) {
			return 0;
		}
		return inference.getName().hashCode()
				+ inference.getConclusion().hashCode()
				+ inference.getPremises().hashCode();
	}

	/**
	 * Compute a string representation of the given {@link Inference}
	 * 
	 * @param inference
	 * @return the string representing the inference uniquely determined by its
	 *         name, conclusion, and premises
	 * 
	 * @see Inference#getName()
	 * @see Inference#getConclusion()
	 * @see Inference#getPremises()
	 */
	public static String toString(Inference<?> inference) {
		Objects.requireNonNull(inference);
		return inference.getConclusion() + " -| " + inference.getPremises()
				+ " by " + inference.getName();
	}

}
