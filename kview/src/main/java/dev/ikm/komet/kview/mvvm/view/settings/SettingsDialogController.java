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
package dev.ikm.komet.kview.mvvm.view.settings;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.KonceptSigils;
import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.komet.framework.settings.DisplayLanguage;
import dev.ikm.komet.framework.settings.KometSettings;
import dev.ikm.komet.framework.settings.TextSize;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import network.ike.docs.konceptcore.KonceptKind;

import java.util.List;
import java.util.UUID;

/**
 * Controller for {@code settings-dialog.fxml}. Edits a working copy of the {@link KometSettings}
 * it is constructed with; Apply writes the copy back and keeps the dialog open, OK writes it back
 * and closes, Cancel discards whatever has not been applied. The previews on each page follow the
 * working copy, so the user sees the effect of a choice before committing to it.
 */
public class SettingsDialogController {

    /** Font size of a sample letter sigil, matching the badge's ratio to 13px body text. */
    private static final double SAMPLE_SIGIL_SIZE = 11;
    /** Edge of a sample stamp pentagon, in px. */
    private static final double SAMPLE_STAMP_SIZE = 10;
    /** Edge of a sample identicon, in px. */
    private static final int SAMPLE_IDENTICON_SIZE = 14;
    /** Width reserved for the glyph column in a preview row, so names stay aligned as glyphs come and go. */
    private static final double GLYPH_COLUMN_WIDTH = 22;

    private static final double PREVIEW_TITLE_SIZE = 18;
    private static final double PREVIEW_BODY_SIZE = 14;
    private static final double PREVIEW_META_SIZE = 12;

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton appearanceNav;
    @FXML private ToggleButton componentsNav;
    @FXML private ToggleButton languageNav;
    @FXML private VBox appearancePage;
    @FXML private VBox componentsPage;
    @FXML private VBox languagePage;

    @FXML private ToggleGroup textSizeGroup;
    @FXML private ToggleButton smallPill;
    @FXML private ToggleButton defaultPill;
    @FXML private ToggleButton largePill;
    @FXML private ToggleButton extraLargePill;
    @FXML private Label textSizePercent;
    @FXML private Label previewTitle;
    @FXML private Label previewBody;
    @FXML private Label previewMeta;

    @FXML private CheckBox kindSigilCheck;
    @FXML private CheckBox definitionStatusCheck;
    @FXML private CheckBox multipleParentsCheck;
    @FXML private HBox kindSigilSamples;
    @FXML private HBox definitionStatusSamples;
    @FXML private HBox multipleParentsSamples;
    @FXML private HBox multipleParentsRow;
    @FXML private VBox glyphPreview;

    @FXML private ComboBox<DisplayLanguage> displayLanguageCombo;

    @FXML private Button applyButton;
    @FXML private Button okButton;

    private final ObjectProperty<TextSize> textSize = new SimpleObjectProperty<>(TextSize.DEFAULT);
    private final BooleanProperty showKindSigil = new SimpleBooleanProperty(true);
    private final BooleanProperty showDefinitionStatus = new SimpleBooleanProperty(true);
    private final BooleanProperty showMultipleParents = new SimpleBooleanProperty(true);
    private final ObjectProperty<DisplayLanguage> displayLanguage = new SimpleObjectProperty<>(DisplayLanguage.SYSTEM_DEFAULT);

    private final KometSettings settings;

    /**
     * @param settings the settings the dialog edits; supplied by the loader's controller factory
     */
    public SettingsDialogController(KometSettings settings) {
        this.settings = settings;
    }

    @FXML
    private void initialize() {
        loadWorkingCopy();
        initNavigation();
        initAppearancePage();
        initComponentsPage();
        initLanguagePage();
        initButtons();
    }

    private void loadWorkingCopy() {
        textSize.set(settings.getTextSize());
        showKindSigil.set(settings.isShowKindSigil());
        showDefinitionStatus.set(settings.isShowDefinitionStatus());
        showMultipleParents.set(settings.isShowMultipleParents());
        displayLanguage.set(settings.getDisplayLanguage());
    }

    private void initNavigation() {
        appearanceNav.setUserData(appearancePage);
        componentsNav.setUserData(componentsPage);
        languageNav.setUserData(languagePage);
        navGroup.selectedToggleProperty().subscribe((previous, selected) -> {
            if (selected == null) {
                // Clicking the selected page again must not leave the dialog with no page.
                previous.setSelected(true);
                return;
            }
            for (VBox page : List.of(appearancePage, componentsPage, languagePage)) {
                boolean show = page == selected.getUserData();
                page.setVisible(show);
                page.setManaged(show);
            }
        });
    }

    private void initAppearancePage() {
        smallPill.setUserData(TextSize.SMALL);
        defaultPill.setUserData(TextSize.DEFAULT);
        largePill.setUserData(TextSize.LARGE);
        extraLargePill.setUserData(TextSize.EXTRA_LARGE);
        textSizeGroup.selectedToggleProperty().subscribe((previous, selected) -> {
            if (selected == null) {
                previous.setSelected(true);
                return;
            }
            textSize.set((TextSize) selected.getUserData());
        });
        textSize.subscribe(size -> {
            for (ToggleButton pill : List.of(smallPill, defaultPill, largePill, extraLargePill)) {
                pill.setSelected(pill.getUserData() == size);
            }
            textSizePercent.setText(size.percent() + "%");
            previewTitle.setStyle(fontSizeStyle(PREVIEW_TITLE_SIZE * size.scale()));
            previewBody.setStyle(fontSizeStyle(PREVIEW_BODY_SIZE * size.scale()));
            previewMeta.setStyle(fontSizeStyle(PREVIEW_META_SIZE * size.scale()));
        });
    }

    private static String fontSizeStyle(double px) {
        return "-fx-font-size: " + (Math.round(px * 10) / 10.0) + "px;";
    }

    private void initComponentsPage() {
        kindSigilCheck.selectedProperty().bindBidirectional(showKindSigil);
        definitionStatusCheck.selectedProperty().bindBidirectional(showDefinitionStatus);
        multipleParentsCheck.selectedProperty().bindBidirectional(showMultipleParents);
        // The fork only exists on top of the status glyph.
        multipleParentsRow.disableProperty().bind(showDefinitionStatus.not());
        BooleanBinding showFork = showDefinitionStatus.and(showMultipleParents);

        for (KonceptKind kind : List.of(KonceptKind.DESCRIPTION, KonceptKind.SEMANTIC, KonceptKind.PATTERN,
                KonceptKind.STAMP, KonceptKind.UNKNOWN)) {
            kindSigilSamples.getChildren().add(kindSigil(kind));
        }
        for (KonceptStatus status : List.of(KonceptStatus.DEFINED, KonceptStatus.PRIMITIVE, KonceptStatus.ROOT)) {
            definitionStatusSamples.getChildren().add(statusGlyph(status));
        }
        multipleParentsSamples.getChildren().add(forkGlyph());

        glyphPreview.getChildren().addAll(
                previewRow(statusGlyphs(KonceptStatus.DEFINED, true, showFork),
                        "6a3f0b7e-4a52-4e6e-9a6d-1c2a5e7f9b01", "Myocardial infarction (disorder)"),
                previewRow(statusGlyphs(KonceptStatus.PRIMITIVE, false, showFork),
                        "0d9c2b4a-7e61-4f8d-b3a2-5e6f7a8b9c02", "Chest pain (finding)"),
                previewRow(kindGlyph(KonceptKind.DESCRIPTION),
                        "b2e4d6f8-1a3c-4e5f-8b7d-9c0a1b2c3d03", "Heart attack"));
    }

    private static Node kindSigil(KonceptKind kind) {
        return KonceptSigils.create(kind, SAMPLE_STAMP_SIZE, SAMPLE_SIGIL_SIZE).orElseThrow();
    }

    private static Text statusGlyph(KonceptStatus status) {
        return glyphText(status.glyph(), status.core().colorHex());
    }

    private static Text forkGlyph() {
        return glyphText(KonceptStatus.MULTI_PARENT_GLYPH,
                network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_COLOR_HEX);
    }

    private static Text glyphText(String glyph, String colorHex) {
        Text text = new Text(glyph);
        text.setFill(Color.web(colorHex));
        text.setFont(Font.font(text.getFont().getFamily(), FontWeight.BOLD, SAMPLE_SIGIL_SIZE));
        return text;
    }

    /** The glyph column of a concept row: the status glyph, plus the fork for a multi-parent concept. */
    private Node statusGlyphs(KonceptStatus status, boolean multiParent, ObservableBooleanValue showFork) {
        TextFlow flow = new TextFlow(statusGlyph(status));
        if (multiParent) {
            Text fork = forkGlyph();
            bindShown(fork, showFork);
            flow.getChildren().add(fork);
        }
        bindShown(flow, showDefinitionStatus);
        return flow;
    }

    /** The glyph column of a non-concept row: its kind sigil. */
    private Node kindGlyph(KonceptKind kind) {
        Node sigil = kindSigil(kind);
        bindShown(sigil, showKindSigil);
        return sigil;
    }

    private static void bindShown(Node node, ObservableBooleanValue shown) {
        node.visibleProperty().bind(shown);
        node.managedProperty().bind(shown);
    }

    private static HBox previewRow(Node glyphs, String uuid, String name) {
        HBox glyphColumn = new HBox(glyphs);
        glyphColumn.setAlignment(Pos.CENTER_RIGHT);
        glyphColumn.setMinWidth(GLYPH_COLUMN_WIDTH);
        glyphColumn.setPrefWidth(GLYPH_COLUMN_WIDTH);
        glyphColumn.setMaxWidth(GLYPH_COLUMN_WIDTH);

        ImageView identicon = Identicon.generateIdenticonSync(PublicIds.of(UUID.fromString(uuid)),
                SAMPLE_IDENTICON_SIZE, SAMPLE_IDENTICON_SIZE);
        identicon.setSmooth(false);

        Label nameLabel = new Label(name);
        HBox row = new HBox(6, glyphColumn, identicon, nameLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("settings-preview-row");
        return row;
    }

    private void initLanguagePage() {
        displayLanguageCombo.getItems().setAll(DisplayLanguage.values());
        displayLanguageCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(DisplayLanguage language) {
                return language == null ? "" : language.displayName();
            }

            @Override
            public DisplayLanguage fromString(String string) {
                throw new UnsupportedOperationException("The display language combo box is not editable");
            }
        });
        displayLanguageCombo.valueProperty().bindBidirectional(displayLanguage);
    }

    /** Apply only has something to do while the working copy differs from the saved settings. */
    private void initButtons() {
        BooleanBinding unchanged = Bindings.createBooleanBinding(() ->
                        textSize.get() == settings.getTextSize()
                                && showKindSigil.get() == settings.isShowKindSigil()
                                && showDefinitionStatus.get() == settings.isShowDefinitionStatus()
                                && showMultipleParents.get() == settings.isShowMultipleParents()
                                && displayLanguage.get() == settings.getDisplayLanguage(),
                textSize, showKindSigil, showDefinitionStatus, showMultipleParents, displayLanguage,
                settings.textSizeProperty(), settings.showKindSigilProperty(),
                settings.showDefinitionStatusProperty(), settings.showMultipleParentsProperty(),
                settings.displayLanguageProperty());
        applyButton.disableProperty().bind(unchanged);
    }

    /** Resets the settings on the visible page to their defaults; the other pages are left alone. */
    @FXML
    private void resetToDefault() {
        if (appearancePage.isVisible()) {
            textSize.set(TextSize.DEFAULT);
        } else if (componentsPage.isVisible()) {
            showKindSigil.set(true);
            showDefinitionStatus.set(true);
            showMultipleParents.set(true);
        } else {
            displayLanguage.set(DisplayLanguage.SYSTEM_DEFAULT);
        }
    }

    /** Writes the working copy back to the settings; the dialog stays open. */
    @FXML
    private void apply() {
        settings.setTextSize(textSize.get());
        settings.setShowKindSigil(showKindSigil.get());
        settings.setShowDefinitionStatus(showDefinitionStatus.get());
        settings.setShowMultipleParents(showMultipleParents.get());
        settings.setDisplayLanguage(displayLanguage.get());
    }

    @FXML
    private void ok() {
        apply();
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) okButton.getScene().getWindow()).close();
    }
}
