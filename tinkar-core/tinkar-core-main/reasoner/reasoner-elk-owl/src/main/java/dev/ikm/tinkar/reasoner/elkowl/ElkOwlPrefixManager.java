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
package dev.ikm.tinkar.reasoner.elkowl;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * @deprecated
 * No longer maintained.
 * 
 * Use dev.ikm.tinkar.reasoner.elksnomed
 */
@Deprecated
public class ElkOwlPrefixManager {

	private ElkOwlPrefixManager() {
	}

	public static final String PREFIX = "http://dev.tinkar/id/";

	public static final String PREFIX_PATTERN = "<" + PREFIX + "(-?[0-9]+)>";

	public static String removePrefix(OWLAxiom ax) {
		return ax.toString().replaceAll(PREFIX_PATTERN, ":$1")
		// .replace(") )", "))")
		;
	}

	private static PrefixManager prefixManager;

	public static PrefixManager getPrefixManager() {
		if (prefixManager == null)
			prefixManager = new DefaultPrefixManager(null, null, PREFIX);
		return prefixManager;
	}

}
