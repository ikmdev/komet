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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A simple pretty printer for proofs using ASCII characters. Due to potential
 * cycles, inferences for every conclusion are printed only once upon their
 * first occurrence in the proof. Every following occurrence of the same
 * conclusion is labeled by {@code *}.
 * 
 * @author Yevgeny Kazakov
 */
public class ProofPrinter {

	/**
	 * the set of inferences from which the proofs are formed
	 */
	private final Proof<? extends AxiomPinpointingInference<?, ?>> proof_;

	/**
	 * the current positions of iterators over inferences for conclusions
	 */
	private final Deque<Iterator<? extends AxiomPinpointingInference<?, ?>>> inferenceStack_ = new LinkedList<>();

	/**
	 * the current positions of iterators over conclusions for inferences
	 */
	private final Deque<Iterator<?>> conclusionStack_ = new LinkedList<>();

	/**
	 * the current positions of iterators over justifications for inferences
	 */
	private final Deque<Iterator<?>> justificationStack_ = new LinkedList<>();

	/**
	 * accumulates the printed conclusions to avoid repetitions
	 */
	private final Set<Object> printed_ = new HashSet<>();

	/**
	 * where the output is written
	 */
	private final BufferedWriter writer_;	
	
	protected ProofPrinter(
			final Proof<? extends AxiomPinpointingInference<?, ?>> proof,
			BufferedWriter writer) {
		this.proof_ = proof;
		this.writer_ = writer;
	}

	protected ProofPrinter(
			final Proof<? extends AxiomPinpointingInference<?, ?>> proof) {
		this(proof, new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	
	public void printProof(Object conclusion) throws IOException {
		process(conclusion);
		process();
		writer_.flush();
	}

	public static void print(final Proof<? extends Inference<?>> proof,
			Object goal, BufferedWriter writer) throws IOException {
		ProofPrinter pp = new ProofPrinter(Proofs.transform(proof,
				input -> input instanceof AxiomPinpointingInference<?, ?>
						? (AxiomPinpointingInference<?, ?>) input
						: new AxiomPinpointingInferenceAdapter<>(input)),
				writer);
		pp.printProof(goal);
	}
	
	public static void print(final Proof<? extends Inference<?>> proof,
			Object goal) throws IOException {
		print(proof, goal, new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	
	public static String toString(final Proof<? extends Inference<?>> proof,
			Object goal) throws IOException {
		StringWriter writer = new StringWriter();
		print(proof, goal, new BufferedWriter(writer));
		return writer.toString();
	}

	protected BufferedWriter getWriter() {
		return writer_;
	}

	protected void writeConclusion(Object conclusion) throws IOException {
		// can be overridden
		writer_.write(conclusion.toString());
	}

	private boolean process(Object conclusion) throws IOException {
		writePrefix();
		writeConclusion(conclusion);
		boolean newConclusion = printed_.add(conclusion);
		if (newConclusion) {
			inferenceStack_.push(proof_.getInferences(conclusion).iterator());
		} else {
			// block conclusions appeared earlier in the proof
			writer_.write(" *");
		}
		writer_.newLine();
		return newConclusion;
	}

	private void print(Object just) throws IOException {
		writePrefix();
		writer_.write(just.toString());
		writer_.newLine();
	}

	private void printTautology() throws IOException {
		writePrefix();
		writer_.newLine();		
	}
	
	private void process() throws IOException {
		for (;;) {
			// processing inferences
			Iterator<? extends AxiomPinpointingInference<?, ?>> infIter = inferenceStack_
					.peek();
			if (infIter == null) {
				return;
			}
			// else
			if (infIter.hasNext()) {
				AxiomPinpointingInference<?, ?> inf = infIter.next();
				List<?> premises = inf.getPremises();
				Set<?> justification = inf.getJustification();
				conclusionStack_.push(premises.iterator());
				justificationStack_.push(justification.iterator());
				if (premises.isEmpty() && justification.isEmpty()) {
					printTautology();
				}				
			} else {
				inferenceStack_.pop();
			}
			// processing conclusions
			Iterator<?> conclIter = conclusionStack_.peek();
			if (conclIter == null) {
				return;
			}
			// else
			for (;;) {
				if (conclIter.hasNext()) {
					if (process(conclIter.next())) {
						break;
					}
					// else
					continue;
				}
				// else
				// processing justifications
				Iterator<?> justIter = justificationStack_.peek();
				if (justIter == null) {
					return;
				}
				// else
				while (justIter.hasNext()) {
					print(justIter.next());
				}
				conclusionStack_.pop();
				justificationStack_.pop();
				break;
			}
		}
	}

	private void writePrefix() throws IOException {
		Iterator<Iterator<? extends AxiomPinpointingInference<?, ?>>> inferStackItr = inferenceStack_
				.descendingIterator();
		Iterator<Iterator<?>> conclStackItr = conclusionStack_
				.descendingIterator();
		Iterator<Iterator<?>> justStackItr = justificationStack_
				.descendingIterator();
		while (inferStackItr.hasNext()) {
			Iterator<? extends AxiomPinpointingInference<?, ?>> inferIter = inferStackItr
					.next();
			Iterator<?> conclIter = conclStackItr.next();
			Iterator<?> justIter = justStackItr.next();
			boolean hasNextPremise = conclIter.hasNext() || justIter.hasNext();
			if (conclStackItr.hasNext() || justStackItr.hasNext()) {
				writer_.write(hasNextPremise ? "|  "
						: inferIter.hasNext() ? ":  " : "   ");
			} else {
				writer_.write(hasNextPremise ? "+- " : "\\- ");
			}
		}
	}

}
