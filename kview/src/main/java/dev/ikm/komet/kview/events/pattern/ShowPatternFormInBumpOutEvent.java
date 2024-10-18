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
package dev.ikm.komet.kview.events.pattern;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.kview.mvvm.model.PatternField;

/**
 * Events related to displaying properties bump outs (right-side) of pattern details window.
 * <pre>
 *     - Panel to add/edit a definition
 *     - Panel to add/edit a fqn description name
 *     - Panel to add/edit a other description name
 * </pre>
 */
public class ShowPatternFormInBumpOutEvent extends Evt {

    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_ADD_DEFINITION = new EvtType<>(Evt.ANY, "SHOW_ADD_DEFINITION");

    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_EDIT_FIELDS = new EvtType<>(Evt.ANY, "SHOW_EDIT_FIELD");
    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_ADD_FIELDS = new EvtType<>(Evt.ANY, "SHOW_ADD_FIELDS");

    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_CONTINUE_ADD_FIELDS = new EvtType<>(Evt.ANY, "SHOW_CONTINUE_ADD_FIELDS");

    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_CONTINUE_EDIT_FIELDS = new EvtType<>(Evt.ANY, "SHOW_CONTINUE_EDIT_FIELDS");

    ////////// Description Name semantic events
    public static final EvtType<ShowPatternFormInBumpOutEvent> DESCRIPTION_NAME = new EvtType<>(Evt.ANY, "DESCRIPTION_NAME");
    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_ADD_FQN = new EvtType<>(DESCRIPTION_NAME, "SHOW_ADD_FQN");
    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_ADD_OTHER_NAME = new EvtType<>(DESCRIPTION_NAME, "SHOW_ADD_OTHER_NAME_DESCRIPTION");

    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_EDIT_FQN = new EvtType<>(DESCRIPTION_NAME, "SHOW_EDIT_FQN_DESCRIPTION");
    public static final EvtType<ShowPatternFormInBumpOutEvent> SHOW_EDIT_OTHER_NAME = new EvtType<>(DESCRIPTION_NAME, "SHOW_EDIT_OTHER_NAME_DESCRIPTION");
    private final int totalFields;
    private final PatternField patternField;
    private final int fieldOrder;
    //TODO future: other EvtTypes like show History, show Timeline etc

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public ShowPatternFormInBumpOutEvent(Object source, EvtType eventType) {
        super(source, eventType);
        this.totalFields =0;
        patternField = null;
        this.fieldOrder = 1;
    }

    /**
     * Constructs a prototypical Event.
     * This is a add even and the default value for the patttern Field is null and Field order is 1.
     * @param source    the object on which the Event initially occurred
     * @param eventType in this case the event type is SHOW_ADD_FIELDS event type.
     * @param totalFields the total number of fields added in the current pattern.
     * @throws IllegalArgumentException if source is null
     */
    public ShowPatternFormInBumpOutEvent(Object source, EvtType eventType, int totalFields) {
        super(source, eventType);
        this.totalFields = totalFields;
        this.patternField = null;
        this.fieldOrder = 1;
    }

    /**
     * This method is used when we have to edit the Pattern Fields.
     * The input form is populated using existing field order and the pattern fields information.
     * @param source the object on which the event initially occurred.
     * @param eventType the eventType this should be SHOW_EDIT_FIELDS event type.
     * @param totalFields the total number of fields added in the current pattern.
     * @param patternField the pattern field that is to be edited.
     * @param fieldOrder the order of the selected pattern field.
     */
    public ShowPatternFormInBumpOutEvent(Object source, EvtType eventType, int totalFields, PatternField patternField, int fieldOrder) {
        super(source, eventType);
        this.totalFields = totalFields;
        this.patternField = patternField;
        this.fieldOrder = fieldOrder;
    }

    public int getTotalFields() {
        return totalFields;
    }

    public PatternField getPatternField(){
        return patternField;
    }

    public int getFieldOrder(){
        return fieldOrder;
    }

}
