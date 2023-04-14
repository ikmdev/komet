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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 */
public class ProofTest {

	@Test
	public void proofTest() {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(1).premise(2).add();
		b.conclusion(2).premise(3).premise(4).add();
		b.conclusion(2).premise(5).premise(6).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		assertEquals(1, p.getInferences(1).size());
		assertEquals(2, p.getInferences(2).size());
		assertEquals(0, p.getInferences(3).size());
	}

	@Test
	public void blockCyclicProof() throws Exception {
		BaseProofBuilder<String, ?> b = new BaseProofBuilder<>();
		b.conclusion("A ⊑ B").premise("A ⊑ B ⊓ C").add();
		b.conclusion("A ⊑ B").premise("A ⊑ C").premise("C ⊑ B").add();
		b.conclusion("A ⊑ C").premise("A ⊑ D").premise("D ⊑ C").add();
		b.conclusion("A ⊑ D").premise("A ⊑ B").premise("B ⊑ D").add();
		b.conclusion("A ⊑ B ⊓ C").add();
		b.conclusion("B ⊑ D").add();
		b.conclusion("D ⊑ C").add();
		b.conclusion("C ⊑ B").add();

		Proof<? extends Inference<String>> p = b.getProof();

		assertTrue(ProofNodes.isDerivable(ProofNodes.create(p, "A ⊑ C")));

		ProofNode<String> root = ProofNodes.create(p, "A ⊑ B");

		assertTrue(ProofNodes.isDerivable(root));

		assertEquals(2,
				ProofNodes.eliminateNotDerivable(root).getInferences().size());

		// only one inference remains since the other is cyclic
		assertEquals(1, ProofNodes.eliminateNotDerivableAndCycles(root)
				.getInferences().size());

		// testing the same but using derivability "from" methods

		root = ProofNodes.create(p, "A ⊑ B");
		assertTrue(ProofNodes.isDerivable(root));

		assertEquals(2,
				ProofNodes.eliminateNotDerivable(root).getInferences().size());

		// only one inference remains since the other is cyclic
		assertEquals(1, ProofNodes.eliminateNotDerivableAndCycles(root)
				.getInferences().size());

	}

	@Test
	public void testDerivabilityCheckerWithBlocking0() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).add();
		b.conclusion(1).premise(2).add();
		b.conclusion(11).premise(2).add();
		b.conclusion(2).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		DerivabilityCheckerWithBlocking<Integer, Inference<Integer>> checker = new InferenceDerivabilityChecker<>(
				p);
		checker.block(2);
		assertFalse(checker.isDerivable(0));
		checker.block(1);
		checker.unblock(2);
		assertTrue(checker.isDerivable(11));
		assertFalse(checker.isDerivable(0));
		checker.unblock(1);
		assertTrue(checker.isDerivable(0));
	}

	@Test
	public void testDerivabilityCheckerWithBlocking1() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(11).premise(22).add();
		b.conclusion(11).premise(1).add();
		b.conclusion(22).premise(2).add();
		b.conclusion(1).add();
		b.conclusion(2).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		DerivabilityCheckerWithBlocking<Integer, Inference<Integer>> checker = new InferenceDerivabilityChecker<>(
				p);
		assertTrue(checker.isDerivable(0));
		checker.block(2);
		assertFalse(checker.isDerivable(0));
		checker.block(1);
		checker.unblock(2);
		assertFalse(checker.isDerivable(0));
		checker.unblock(1);
		assertTrue(checker.isDerivable(0));
	}

	@Test
	public void testDerivabilityCheckerWithBlocking2() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).premise(2).add();
		b.conclusion(0).premise(3).premise(4).add();
		b.conclusion(2).premise(0).premise(0).add();
		b.conclusion(1).premise(3).premise(4).add();
		b.conclusion(3).add();
		b.conclusion(4).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		DerivabilityCheckerWithBlocking<Integer, Inference<Integer>> checker = new InferenceDerivabilityChecker<>(
				p);
		assertTrue(checker.isDerivable(0));
		checker.block(1);
		assertTrue(checker.isDerivable(0));
		checker.unblock(1);
		checker.block(3);
		assertFalse(checker.isDerivable(0));
		assertFalse(checker.isDerivable(3));
		assertTrue(checker.isDerivable(4));
		checker.unblock(3);
		assertTrue(checker.isDerivable(0));
		assertTrue(checker.isDerivable(3));
		assertTrue(checker.isDerivable(4));
		checker.block(4);
		assertFalse(checker.isDerivable(0));
		assertTrue(checker.isDerivable(3));
		assertFalse(checker.isDerivable(4));
	}

	@Test
	public void testDerivabilityCheckerWithBlocking3() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).add();
		b.conclusion(0).premise(2).add();
		b.conclusion(1).add();
		b.conclusion(2).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		DerivabilityCheckerWithBlocking<Integer, Inference<Integer>> checker = new InferenceDerivabilityChecker<>(
				p);
		checker.block(1);
		assertTrue(checker.isDerivable(0));
		checker.unblock(1);
		checker.block(2);
		assertTrue(checker.isDerivable(0));
		checker.block(1);
		assertFalse(checker.isDerivable(0));
		checker.unblock(1);
		checker.unblock(2);
		assertTrue(checker.isDerivable(0));
	}
	
	@Test
	public void testDerivabilityCheckerWithBlocking4() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).add();
		b.conclusion(1).premise(0).add();
		b.conclusion(0).add();
		Proof<? extends Inference<Integer>> p = b.getProof();
		DerivabilityCheckerWithBlocking<Integer, Inference<Integer>> checker = new InferenceDerivabilityChecker<>(
				p);
		assertTrue(checker.isDerivable(0));		
	}

	@Test
	public void blockCyclicProof2() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).premise(2).add();
		b.conclusion(0).premise(3).premise(4).add();
		b.conclusion(2).premise(0).premise(0).add();
		b.conclusion(1).add();
		b.conclusion(3).add();
		b.conclusion(4).add();
		Proof<? extends Inference<Integer>> p = b.getProof();

		ProofNode<Integer> root = ProofNodes.create(p, 0);

		assertTrue(ProofNodes.isDerivable(root));

		// everything is derivable
		assertEquals(2,
				ProofNodes.eliminateNotDerivable(root).getInferences().size());

		// only one inference remains since the other is cyclic
		assertEquals(1, ProofNodes.eliminateNotDerivableAndCycles(root)
				.getInferences().size());

		// the same using derivability "from"

		root = ProofNodes.create(p, 0);

		assertTrue(ProofNodes.isDerivable(root));

		assertEquals(2,
				ProofNodes.eliminateNotDerivable(root).getInferences().size());

		// only one inference remains since the other is cyclic
		assertEquals(1, ProofNodes.eliminateNotDerivableAndCycles(root)
				.getInferences().size());

	}

	@Test
	public void recursiveBlocking() throws Exception {
		BaseProofBuilder<Integer, ?> b = new BaseProofBuilder<>();
		b.conclusion(0).premise(1).premise(2).add();
		b.conclusion(1).premise(3).premise(4).premise(5).add();
		b.conclusion(3).premise(6).premise(7).add();
		b.conclusion(4).premise(8).premise(9).add();
		b.conclusion(2).add();
		b.conclusion(5).add();
		b.conclusion(7).add();
		b.conclusion(8).add();
		b.conclusion(9).add();
		Proof<? extends Inference<Integer>> p1 = b.getProof();

		b = new BaseProofBuilder<>();
		b.conclusion(6).add();
		Proof<? extends Inference<Integer>> p2 = b.getProof();

		ProofNode<Integer> root = ProofNodes.create(p1, 0);

		// not derivable
		assertEquals(null, ProofNodes.eliminateNotDerivable(root));

		root = ProofNodes.create(Proofs.union(p1, p2), 0);

		// derivable

		assertEquals(1,
				ProofNodes.eliminateNotDerivable(root).getInferences().size());

	}

}
