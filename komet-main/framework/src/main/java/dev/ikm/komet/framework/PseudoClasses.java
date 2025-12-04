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
package dev.ikm.komet.framework;

import javafx.css.PseudoClass;

/**
 * 
 */
public class PseudoClasses {
    public static final PseudoClass INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("inactive");
    public static final PseudoClass REFERENCED_COMPONENT_INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("referenced-component-inactive");
    /**
     * may be active, but superceded by a different component, so not current in the display
     */
    public static final PseudoClass SUPERSEDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("superseded");
    public static final PseudoClass CONTRADICTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("contradicted");

    public static final PseudoClass UNCOMMITTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("uncommitted");
    public static final PseudoClass UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("uncommitted-with-error");

    public static final PseudoClass LOGICAL_DEFINITION_PSEUDO_CLASS = PseudoClass.getPseudoClass("logical-definition");
    public static final PseudoClass DESCRIPTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("description");
    public static final PseudoClass CONCEPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("concept");
    public static final PseudoClass SEMANTIC_PSEUDO_CLASS = PseudoClass.getPseudoClass("semantic");
    public static final PseudoClass PATTERN_PSEUDO_CLASS = PseudoClass.getPseudoClass("pattern");

    public static final PseudoClass ALERT_INFO_PSEUDO_CLASS = PseudoClass.getPseudoClass("alert-info");
    public static final PseudoClass ALERT_CONFIRM_PSEUDO_CLASS = PseudoClass.getPseudoClass("alert-confirm");
    public static final PseudoClass ALERT_WARN_PSEUDO_CLASS = PseudoClass.getPseudoClass("alert-warn");
    public static final PseudoClass ALERT_ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("alert-error");
    public static final PseudoClass ALERT_SUCCESS_PSEUDO_CLASS = PseudoClass.getPseudoClass("alert-success");

    public static final PseudoClass STATED_PSEUDO_CLASS = PseudoClass.getPseudoClass("stated");
    public static final PseudoClass INFERRED_PSEUDO_CLASS = PseudoClass.getPseudoClass("inferred");

    public static final PseudoClass DROP_POSSIBLE = PseudoClass.getPseudoClass("drop-possible");
    public static final PseudoClass DROP_READY = PseudoClass.getPseudoClass("drop-ready");


}
