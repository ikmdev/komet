/**
 * The reusable per-control settings mechanism for embeddable KL-tier controls.
 * <p>
 * Every card-embeddable control that has user-adjustable preferences (print options, view options,
 * display density, …) follows one convention rather than inventing its own store. The convention is
 * built entirely on the existing preferences rail — nothing new is serialized or persisted:
 * <ol>
 *   <li><b>Model the settings as one whole {@link dev.ikm.tinkar.common.binary.Encodable} value.</b>
 *       A record with a {@code DEFAULT} constant and {@code @Encoder}/{@code @Decoder} methods, in
 *       the {@link dev.ikm.komet.layout.area.AreaGridSettings} / {@link dev.ikm.komet.layout.LayoutOverrides}
 *       style — persisted whole, never field-by-field. Use {@code @RecordBuilder} for the
 *       {@code with…} copies where the module runs that annotation processor, or write them by hand
 *       otherwise.</li>
 *   <li><b>Back every closed choice with a concept-identity enum, never a string constant.</b>
 *       Value vocabularies (page size, orientation, theme, …) are enums implementing
 *       {@link dev.ikm.tinkar.common.bind.EnumConceptBinding} with a per-constant pinned
 *       {@code @PublicIdAnnotation(@UuidAnnotation(...))} + {@code @RegularName}, so each choice
 *       already carries a {@code publicId()} and can become a Tinkar concept later.</li>
 *   <li><b>Key the settings with a {@link dev.ikm.komet.layout.preferences.PropertyWithDefault}
 *       enum.</b> One enum constant is simultaneously the {@link dev.ikm.komet.preferences.KometPreferences}
 *       key, the default-value holder, and (via {@code ClassConceptBinding}) a {@code publicId()}-bearing
 *       concept.</li>
 *   <li><b>Persist and restore in the card lifecycle.</b> Override
 *       {@code AbstractHostCard.subCardSave()} / {@code subCardRestore()} and call
 *       {@link dev.ikm.komet.layout.settings.ControlSettings#save} /
 *       {@link dev.ikm.komet.layout.settings.ControlSettings#load} against the card's
 *       {@code preferences()} node.</li>
 *   <li><b>Host the editor in a {@code KlDrawer}.</b> {@code AbstractHostCard.addDrawer(Side, region,
 *       label)} adds the toggle, persists the open-state, and binds the editor to the card lifecycle.</li>
 * </ol>
 * {@link dev.ikm.komet.layout.settings.ControlSettings} is the thin, type-safe glue for step 3+4: it
 * pairs the key enum (compile-time forced to be both an {@code Enum} and a {@code PropertyWithDefault})
 * with the typed default value, and reads/writes it via {@code getObject}/{@code putObject}. The first
 * consumer is the Claude assistant's paged {@code DocumentSurface} print settings
 * ({@code IKE-Network/ike-issues#838}); the mechanism is general to any future control.
 */
package dev.ikm.komet.layout.settings;
