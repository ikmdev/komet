/*
 * Copyright © 2026 Integrated Knowledge Management (support@ikm.dev)
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

/**
 * Shared CommonMark → incubator-RichTextArea renderer with injectable render-dispatch hooks.
 * Generic and tinkar-free; consumers inject inline nodes (e.g. concept chips) through
 * {@link dev.ikm.komet.markdown.richtext.InlineDecorator}, and block-level node renderers keyed
 * on a fenced block's info string (e.g. a concept tree) through
 * {@link dev.ikm.komet.markdown.richtext.BlockRenderer}.
 */
module dev.ikm.komet.markdown.richtext {
    requires transitive jfx.incubator.richtext;
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;

    exports dev.ikm.komet.markdown.richtext;
}
