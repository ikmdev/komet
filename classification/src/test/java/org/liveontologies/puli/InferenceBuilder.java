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

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

public class InferenceBuilder<C, B extends Builder<B>>
		extends AbstractBuilder<B> {

	static final String INF_NAME = "inf";

	private final String name_;

	private C conclusion_;

	private final List<C> premises_ = new ArrayList<C>();

	protected InferenceBuilder(String name) {
		this.name_ = name;
	}

	protected InferenceBuilder() {
		this(INF_NAME);
	}

	String getName() {
		return name_;
	}

	C getConclusion() {
		return conclusion_;
	}

	List<C> getPremises() {
		return premises_;
	}

	public B conclusion(C conclusion) {
		Objects.requireNonNull(conclusion);
		if (conclusion_ != null) {
			throw new RuntimeException(
					"Conclusion already assigned: " + conclusion);
		}
		this.conclusion_ = conclusion;
		return getBuilder();
	}

	public B premise(C premise) {
		Objects.requireNonNull(premise);
		premises_.add(premise);
		return getBuilder();
	}

	Inference<C> build() {
		return new BaseInference<C>(name_, conclusion_, premises_);
	}

}
