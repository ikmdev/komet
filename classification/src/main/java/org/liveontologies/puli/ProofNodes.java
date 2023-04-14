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

import java.util.Set;

/**
 * A collection of static utilities for working with {@link ProofNode}
 * 
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofNodes {

	/**
	 * Creates a {@link ProofNode} from a given {@link Proof} using with the
	 * given member. The inferences of the {@link ProofNode} correspond to the
	 * inferences in the proof that derive the member: they have the same name
	 * as the original inferences, and their premises are {@link ProofNode}s
	 * with members corresponding to the premises of the inference from the
	 * {@link Proof}. Essentially, the resulting {@link ProofNode} represent the
	 * part of the {@link Proof} structure relevant for the given member.
	 * 
	 * @param proof
	 * @param member
	 * @return the {@link ProofNode} for the given member with inferences from
	 *         the given {@link Proof}
	 * 
	 * @see ProofNode#getMember()
	 * @see ProofNode#getInferences()
	 */
	public static <C> ProofNode<C> create(
			Proof<? extends Inference<? extends C>> proof, C member) {
		return new BaseProofNode<C>(proof, member);
	}

	/**
	 * Adds {@link AssertedConclusionInference} if the members the given node
	 * appears in the given {@link Set} of asserted conclusions and,
	 * recursively, for {@link ProofNode}s of premises of the inferences of the
	 * given {@link ProofNode}. The arguments {@link ProofNode} and {@link Set}
	 * are not modified.
	 * 
	 * @param node
	 * @param assertedConclusions
	 * @return a {@link ProofNode} that has the same mamer and same inferences
	 *         as the original {@link ProofNode} plus the
	 *         {@link AssertedConclusionInference} deriving this
	 *         {@link ProofNode} if the member appears in the given {@link Set}
	 *         of asserted conclusions of. The premises {@link ProofNode}s of
	 *         the inferences are recursively transformed in the same way.
	 * 
	 * @see ProofNode#getMember()
	 * @see ProofNode#getInferences()
	 */
	public static <C> ProofNode<C> addAssertedInferences(ProofNode<C> node,
			Set<? extends C> assertedConclusions) {
		return new AddAssertedProofNode<C>(node, assertedConclusions);
	}

	/**
	 * Recursively removes the {@link AssertedConclusionInference}s from the
	 * inferences of the given {@link ProofNode} and inferences for the premises
	 * of such inferences, etc. The input {@link ProofNode} is not modified.
	 * 
	 * @param node
	 * @return a {@link ProofNode} that has the same member and same inferences
	 *         as the original {@link ProofNode} except that the
	 *         {@link AssertedConclusionInference}s (deriving this
	 *         {@link ProofNode}) are removed. The premises {@link ProofNode}s
	 *         of the inferences are recursively transformed in the same way.
	 * 
	 * 
	 * @see ProofNode#getMember()
	 * @see ProofNode#getInferences()
	 */
	public static <C> ProofNode<C> removeAssertedInferences(ProofNode<C> node) {
		return new RemoveAssertedProofNode<C>(node);
	}

	/**
	 * Checks if the given {@link ProofNode} is derivable. A {@link ProofNode}
	 * is derivable if it has an inference with the empty set of premises, or an
	 * inference whose all premise {@link ProofNode}s are (recursively)
	 * derivable. The input {@link ProofNode} is not modified. For example if an
	 * input node n1 has only inference n1 -| n1, n2 (conclusion n1, premises:
	 * n1 and n2), then node n1 is not derivable.
	 * 
	 * @param node
	 * @return {@code true} if the given {@link ProofNode} is derivable and
	 *         {@code false} otherwise
	 */
	public static <C> boolean isDerivable(ProofNode<C> node) {
		return new ProofNodeDerivabilityChecker().isDerivable(node);
	}

	/**
	 * Recursively removes {@link ProofNode}s reachable from the given
	 * {@link ProofNode} that are not derivable. The input {@link ProofNode} is
	 * not modified.
	 * 
	 * For example, if an input node n1 has inferences n1 -| n2 (conclusion: n1,
	 * premise: n2) and n1 -| n3 (conclusion: n1, premise: n3), where node n2
	 * has only inference n2 -| (conclusion: n2, no premises), and n3 has only
	 * inference n3 -| n2, n3 (conclusion: n3, premises n2, n3), then the
	 * resulting node n1' will have only inference n1' -| n2' where n2' has only
	 * inference n2' -|, and n1' and n2' has the same member as n1 and n2
	 * respectively.
	 *
	 * 
	 * @param node
	 * @return {@code null} if the given {@link ProofNode} is not derivable or,
	 *         otherwise, a {@link ProofNode} that has the same member as the
	 *         original {@link ProofNode} and only inferences of the original
	 *         {@link ProofNode} whose all premises {@link ProofNode}s are
	 *         derivable. These premises are transformed in the same way by
	 *         eliminating non-derivable {@link ProofNode}s.
	 * 
	 * @see #isDerivable(ProofNode)
	 * @see ProofNode#getMember()
	 * @see ProofNode#getInferences()
	 */
	public static <C> ProofNode<C> eliminateNotDerivable(ProofNode<C> node) {
		if (isDerivable(node)) {
			return new DerivableProofNode<C>(node);
		}
		// else
		return null;
	}

	/**
	 * Recursively removes {@link ProofNode}s reachable from the given
	 * {@link ProofNode} that are not derivable. In addition, recursively
	 * removes inferences that are involved in cycles. I.e., when the
	 * {@link ProofNode} is derivable using a sequence of inferences from
	 * itself, then the last inference in this sequence is removed. The
	 * arguments {@link ProofNode} and {@link Set} are not modified.
	 * Essentially, the given {@link ProofNode} is transformed into a DAG
	 * structure preserving derivability.
	 * 
	 * For example, if an input node n1 has inferences n1 -| n2 (conclusion: n1,
	 * premise: n2), node n2 has only inferences n2 -| n3 (conclusion: n2,
	 * premise n3), and n3 has only inferences n3 -| n1, n4 (conclusion: n3,
	 * premise n1) and n3 -| (conclusion: n3, no premise), then the resulting
	 * node n1' will have only inference n1' -| n2' where n2' has only inference
	 * n2' -| n3', where n3' has only inference n3' -|, n1', n2', and n2' has
	 * the same member as n1, n2, and n3 respectively. That is, the inference
	 * corresponding to n3 -| n1, n4 was removed because it is cyclic (uses n1
	 * as a premise).
	 * 
	 * @param node
	 * @return {@code null} if the given {@link ProofNode} is not derivable or
	 *         has only inferences that contain this {@link ProofNode} as one of
	 *         the premises; otherwise returns a {@link ProofNode} that has the
	 *         same member and inferences as the original {@link ProofNode}
	 *         except that all inferences that have either non-derivable premise
	 *         or the given {@link ProofNode} as the premise, are removed. The
	 *         premises {@link ProofNode}s of the remaining inferences are
	 *         transformed in the similar way, except that the inferences of
	 *         these {@link ProofNode}s should not use both any of the
	 *         {@link ProofNode}s on the "path" to the given {@link ProofNode}.
	 * 
	 * @see #isDerivable(ProofNode)
	 * @see ProofNode#getMember()
	 * @see ProofNode#getInferences()
	 * 
	 */
	public static <C> ProofNode<C> eliminateNotDerivableAndCycles(
			ProofNode<C> node) {
		if (isDerivable(node)) {
			return new AcyclicDerivableProofNode<C>(node);
		}
		// else
		return null;
	}

}
