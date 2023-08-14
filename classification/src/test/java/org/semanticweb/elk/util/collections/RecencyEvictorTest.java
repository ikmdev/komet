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
package org.semanticweb.elk.util.collections;

import org.junit.Test;

public class RecencyEvictorTest {

	@Test
	public void testEviction() {

		EvictorTestUtils.testRecencyEviction(
				new EvictorTestUtils.TestEvictorFactory<Integer>() {
					@Override
					public Evictor<Integer> newEvictor(final int capacity,
							final double loadFactor) {
						final RecencyEvictor.Builder b = new RecencyEvictor.Builder();
						return b.capacity(capacity).loadFactor(loadFactor)
								.build();
					}
				});

	}

	@Test
	public void testRetainment() {

		EvictorTestUtils.testRecencyRetainment(
				new EvictorTestUtils.TestEvictorFactory<Integer>() {
					@Override
					public Evictor<Integer> newEvictor(final int capacity,
							final double loadFactor) {
						final RecencyEvictor.Builder b = new RecencyEvictor.Builder();
						return b.capacity(capacity).loadFactor(loadFactor)
								.build();
					}
				});

	}

}
