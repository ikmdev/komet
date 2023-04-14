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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operate with
 *
 */
public class MockInference<C> extends BaseInference<C> {

	/**
	 * use {@link #create(String, Object)} or
	 * {@link #create(String, Object, List)}
	 * 
	 * @param name
	 * @param conclusion
	 * @param premises
	 */
	private MockInference(String name, C conclusion, List<C> premises) {
		super(name, conclusion, premises);
	}

	public static <C> MockInference<C> create(String name, C conclusion,
			List<C> premises) {
		return new MockInference<C>(name, conclusion, premises);
	}

	public static <C> MockInference<C> create(String name, C conclusion) {
		return new MockInference<C>(name, conclusion, new ArrayList<C>());
	}

}
