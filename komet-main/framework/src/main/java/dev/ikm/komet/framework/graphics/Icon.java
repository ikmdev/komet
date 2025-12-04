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
package dev.ikm.komet.framework.graphics;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static dev.ikm.komet.framework.graphics.IconCheetSheet.*;

public enum Icon {


    VIEW("view", "ri-viewpoint", RunestroIcons),
    TAXONOMY_CLICK_TO_CLOSE("taxonomy-closed-icon", "fas-caret-down", Fontawesome5),
    TAXONOMY_CLICK_TO_OPEN("taxonomy-open-icon", "fas-caret-right", Fontawesome5),
    STATED("stated-form", "ri-chat", IconCheetSheet.RunestroIcons),
    INFERRED("inferred-form", "fa-gears", Fontawesome),
    SOURCE_BRANCH_1("branch-1", "", null),
    LINK_EXTERNAL("link-external", "oct-link-external-16", OctIcons),
    TAXONOMY_ROOT_ICON("taxonomy-root-icon", "mdi2h-hexagon-outline", MaterialDesign2),
    ALERT_CONFIRM("alert-confirm", "url(\"/org/controlsfx/dialog/dialog-confirm.png\"", ControlsFx),
    TAXONOMY_DEFINED_MULTIPARENT_OPEN("taxonomy-defined-multiparent-open-icon", "mdi2a-arrow-up-bold-circle-outline", MaterialDesign2),
    TAXONOMY_DEFINED_MULTIPARENT_CLOSED("taxonomy-defined-multiparent-closed-icon", "mdi2a-arrow-up-bold-circle-outline", MaterialDesign2),
    TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN("taxonomy-primitive-multiparent-open-icon", "mdi2a-arrow-up-bold-hexagon-outline", MaterialDesign2),
    TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED("taxonomy-primitive-multiparent-closed-icon", "mdi2a-arrow-up-bold-hexagon-outline", MaterialDesign2),
    TAXONOMY_DEFINED_SINGLE_PARENT("taxonomy-defined-singleparent-icon", "mdi2c-checkbox-blank-circle-outline", MaterialDesign2),
    TAXONOMY_PRIMITIVE_SINGLE_PARENT("taxonomy-primitive-singleparent-icon", "mdi2h-hexagon", MaterialDesign2),
    ALERT_CONFIRM2("alert-confirm-2", "far-question-circle", Fontawesome5),
    ALERT_INFORM2("alert-info-2", "mdi2i-information-outline", MaterialDesign2),
    ALERT_ERROR2("alert-error-2", "mdi2a-alert-octagon", MaterialDesign2),
    ALERT_WARN2("alert-warn-2", "mdi2a-alert-circle-outline", MaterialDesign2),
    CHECK("check", "far-check-circle", Fontawesome5),
    TEMPORARY_FIX("temporary-fix", "mdi2b-bandage", MaterialDesign2),

    NAVIGATION("navigator-node", "mdi2f-file-tree", MaterialDesign2),

    COORDINATES("coordinate-crosshairs", "mdi2c-crosshairs-gps", MaterialDesign2),

    // Activity streams
    ACTIVITY("activityStream", "oct-pulse-16", OctIcons),
    ANY_ACTIVITY_STREAM("any-activityStream", "mdi2l-link-variant", MaterialDesign2),
    UNLINKED_ACTIVITY_STREAM("unlinked-activityStream", "mdi2l-link-variant-off", MaterialDesign2),
    SEARCH_ACTIVITY_STREAM("search-activityStream", "mdi2m-magnify", MaterialDesign2),
    NAVIGATION_ACTIVITY_STREAM("navigation-activityStream", "mdi2f-file-tree", MaterialDesign2),
    CLASSIFICATION_ACTIVITY_STREAM("classification-activityStream", "fa-gears", Fontawesome),
    CORRELATION_ACTIVITY_STREAM("correlation-activityStream", "mdi2c-compare-horizontal", MaterialDesign2),
    LIST_ACTIVITY_STREAM("list-activityStream", "mdi2s-script-text-outline", MaterialDesign2),
    BUILDER_ACTIVITY_STREAM("builder-activityStream", "mdi2s-shape-circle-plus", MaterialDesign2),
    FLWOR_ACTIVITY_STREAM("flwor-activityStream", "mdi2f-flower", MaterialDesign2),
    PREFERENCES_ACTIVITY_STREAM("preferences-activityStream", "fas-sliders-h", Fontawesome),

    PUBLISH_TO_STREAM("publish-to-activityStream", "mdi2l-location-exit", MaterialDesign2),
    SUBSCRIBE_TO_STREAM("subscribe-to-activityStream", "mdi2l-location-enter", MaterialDesign2),
    SYNCHRONIZE_WITH_STREAM("synchronize-activityStream", "mdi2c-cached", MaterialDesign2),

    EYE("focus", "far-eye", Fontawesome5),
    EYE_SLASH("focus-no", "far-eye-slash", Fontawesome5),

    CANCEL("cancel", "mdi2c-cancel", MaterialDesign2),
    DUPLICATE("duplicate", "mdi2c-content-duplicate", MaterialDesign2),
    PLUS("plus", "mdi2p-plus", MaterialDesign2),

    INDENT_INCREASE("indent-increase", "mdi2f-format-indent-increase", MaterialDesign2),
    INDENT_DECREASE("indent-decrease", "mdi2f-format-indent-decrease", MaterialDesign2),
    BY_NAME("by-name", "mdi2f-format-list-text", MaterialDesign2),

    LITERAL_STRING("literal-string", "mdi2f-format-quote-close", MaterialDesign2),
    LITERAL_NUMERIC("literal-number", "fas-hashtag", Fontawesome5),
    LAMBDA("lambda", "mdi2l-lambda", MaterialDesign2),
    ROLE_GROUP("role-group", "mdi2f-format-list-bulleted-type", MaterialDesign2),

    OPEN("open-disclosure", "mdi2m-menu-down", MaterialDesign2),
    CLOSE("close-disclosure", "mdi2m-menu-right", MaterialDesign2),
    EDIT_PENCIL("edit-pencil", "mdi2l-lead-pencil", MaterialDesign2),

    PAPER_CLIP("paper-clip", "mdi2p-paperclip", MaterialDesign2),

    PATTERN("pattern", "mdi2m-math-compass", MaterialDesign2),

    SEMANTIC_TABLE("semantic-table", "mdi2t-table", MaterialDesign2),

    PANEL_PREFERENCE_SLIDERS("panel-preference-sliders", "fa-sliders", Fontawesome),

    CONCEPT_DETAILS("concept-details", "mdi2v-view-week", MaterialDesign2),

    ;
    String styleId;
    String iconCode;
    IconCheetSheet cheatSheet;


    Icon(String styleId, String iconCode, IconCheetSheet cheatSheet) {
        this.styleId = styleId;
        this.iconCode = iconCode;
        this.cheatSheet = cheatSheet;
    }

    public static HBox makeIconGroup(String... styleIds) {
        HBox titleNode = new HBox(2);
        for (String styleId : styleIds) {
            titleNode.getChildren().add(makeIcon(styleId));
        }
        return titleNode;
    }

    public static Label makeIcon(String styleId) {
        FontIcon icon = new FontIcon();
        //icon.setId(styleId);
        Label iconLabel = new Label("", icon);
        iconLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        iconLabel.setId(styleId);
        return iconLabel;
    }

    public static HBox makeIconGroup(Node... icons) {
        HBox titleNode = new HBox(2);
        for (Node icon : icons) {
            titleNode.getChildren().add(icon);
        }
        return titleNode;
    }

    public String styleId() {
        return styleId;
    }

    public String iconCode() {
        return iconCode;
    }

    public Label makeIconWithStyles(String... styleClasses) {
        Label label = makeIcon();
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    public Label makeIcon() {
        FontIcon icon = new FontIcon();
        //icon.setId(this.styleId);
        Label iconLabel = new Label("", icon);
        iconLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        iconLabel.setId(this.styleId);
        return iconLabel;
    }

}
