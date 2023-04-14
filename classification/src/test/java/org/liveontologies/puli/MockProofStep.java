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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operate with
 */
public class MockProofStep<C> implements ProofStep<C> {

	private final String name_;

	private final ProofNode<C> conclusion_;

	private final List<ProofNode<C>> premises_;

	public static <C> MockProofStep<C> create(String name,
			ProofNode<C> conclusion, List<ProofNode<C>> premises) {
		return new MockProofStep<C>(name, conclusion, premises);
	}

	public static <C> MockProofStep<C> create(String name,
			ProofNode<C> conclusion) {
		return new MockProofStep<C>(name, conclusion,
				new ArrayList<ProofNode<C>>());
	}

	private MockProofStep(String name, ProofNode<C> conclusion,
			List<ProofNode<C>> premises) {
		name_ = name;
		conclusion_ = conclusion;
		premises_ = premises;
	}

	@Override
	public ProofNode<C> getConclusion() {
		return conclusion_;
	}

	public MockProofStep<C> addPremise(ProofNode<C> premise) {
		premises_.add(premise);
		return this;
	}

	@Override
	public List<? extends ProofNode<C>> getPremises() {
		return premises_;
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String toString() {
		return name_ + premises_ + " |- " + conclusion_;
	}

	@Override
	public Inference<C> getInference() {
		return MockInference.create(name_, conclusion_.getMember(),
				new AbstractList<C>() {

					@Override
					public C get(int index) {
						return premises_.get(index).getMember();
					}

					@Override
					public int size() {
						return premises_.size();
					}
				});
	}

}
