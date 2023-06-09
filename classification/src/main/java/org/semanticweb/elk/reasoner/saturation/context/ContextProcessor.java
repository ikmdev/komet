package org.semanticweb.elk.reasoner.saturation.context;
/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

/**
 * An abstract interfaces for implementing processors of {@link Context}s
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public interface ContextProcessor {

	/**
	 * @param context
	 *            the {@link Context} to be processed
	 * @return {@code true} if the {@link Context} has been modified as the
	 *         result of this operation
	 */
	public boolean process(Context context);

}
