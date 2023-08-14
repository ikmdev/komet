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
package org.liveontologies.puli;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2021 Live Ontologies Project
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

import java.util.List;

import java.util.Objects;

/**
 * A base implementation for {@link JustifiedInference}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            The type of conclusions and premises this inference operate with
 * @param <J>
 *            The type of justifications of the inferences.
 */
public class BaseJustifiedInference<C, J> extends BaseInference<C>
		implements JustifiedInference<C, J> {

	private final J justification_;

	public BaseJustifiedInference(String name, C conclusion,
			List<? extends C> premises, J justification) {
		super(name, conclusion, premises);
		this.justification_ = Objects.requireNonNull(justification);
	}

	@Override
	public J getJustification() {
		return this.justification_;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof BaseJustifiedInference<?, ?>) {
			return super.equals(o) && justification_
					.equals(((BaseJustifiedInference<?, ?>) o).justification_);
		}
		// else
		return false;
	}

	@Override
	protected int computeHashCode() {
		return super.computeHashCode() + justification_.hashCode();
	}

	@Override
	public String toString() {
		return super.toString() + " justification: " + justification_;
	}

}
