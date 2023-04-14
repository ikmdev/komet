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

import java.util.Objects;

/**
 * A base implementation for {@link Inference}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operates with
 */
public class BaseInference<C> extends AbstractInference<C> {

	private final String name_;

	private final C conclusion_;

	private final List<? extends C> premises_;

	public BaseInference(String name, C conclusion,
			List<? extends C> premises) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(conclusion);
		Objects.requireNonNull(premises);
		this.name_ = name;
		this.conclusion_ = conclusion;
		this.premises_ = premises;
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public C getConclusion() {
		return conclusion_;
	}

	@Override
	public List<? extends C> getPremises() {
		return premises_;
	}

}
