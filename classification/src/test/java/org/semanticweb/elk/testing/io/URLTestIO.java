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
package org.semanticweb.elk.testing.io;

import java.net.URL;

import org.semanticweb.elk.testing.UrlTestInput;

/**
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 * @author Peter Skocovsky
 */
public class URLTestIO implements UrlTestInput {

	private final String name_;
	private final URL url;

	public URLTestIO(final String name, final URL file) {
		this.name_ = name;
		this.url = file;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return name_ + " " + url.toString();
	}

	@Override
	public String getName() {
		return name_;
	}

}