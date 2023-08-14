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
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.reasoner.ReasonerResultsNodeFactory;

module dev.ikm.komet.classification {
    requires transitive dev.ikm.komet.framework;
    requires transitive static com.google.auto.service;
    requires dev.ikm.tinkar.collection;
    requires org.roaringbitmap;
    requires org.jgrapht.core;

    opens dev.ikm.komet.reasoner;
    exports dev.ikm.komet.reasoner;
    exports dev.ikm.komet.reasoner.expression;
    opens dev.ikm.komet.reasoner.expression;
    exports dev.ikm.komet.reasoner.elk;
    opens dev.ikm.komet.reasoner.elk;
    exports dev.ikm.komet.reasoner.sorocket;
    opens dev.ikm.komet.reasoner.sorocket;

    provides KometNodeFactory
            with ReasonerResultsNodeFactory;

}
