package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.AxiomRuleAction;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLDiTreeControl;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;
import static dev.ikm.komet.framework.dnd.KometClipboard.decodePublicId;
import static dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;

/**
 * <p>Skin for {@link KLDiTreeControl}: renders the axiom tree exactly like
 * {@link KLReadOnlyDiTreeControlSkin} and mounts the editing interactions on top of it.</p>
 *
 * <ul>
 *     <li><b>Inline concept edit</b> — clicking a chip swaps it in place for a
 *     {@link KLComponentControl} search slot; picking a concept applies the corresponding
 *     replacement ({@code updateConceptReference} / {@code updateRoleType}); Esc restores the
 *     chip. Is-a chips additionally contribute a "Remove axiom" item to their component context
 *     menu.</li>
 *     <li><b>Structure edits</b> — clause headers reveal a + button on hover (add items for that
 *     node type) and offer the full structure menu on right-click, including changing the set type
 *     and removing the axiom; role rows offer "Remove axiom" on right-click.</li>
 *     <li><b>New content</b> — add actions mount template rows of empty search slots into the
 *     clause; the definition is only transformed once the template's required concepts are picked,
 *     mirroring {@code AxiomFocusedRules}' constraints (a single necessary set). An add-set
 *     affordance below the tree starts a definition from scratch.</li>
 * </ul>
 *
 * <p>Every applied edit rebuilds a new {@link DiTreeEntity} through
 * {@link LogicalExpressionBuilder} — the same transformation API the classic axiom rule actions
 * use — and sets it as the control's value, which re-renders the tree and propagates to the bound
 * editable field.</p>
 */
public class KLDiTreeControlSkin extends KLReadOnlyDiTreeControlSkin {

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLDiTreeControlSkin(KLDiTreeControl control) {
        super(control);
        control.componentSlotFactoryProperty().subscribe(() -> rebuildTree(control.getValue()));
    }

    private KLDiTreeControl control() {
        return (KLDiTreeControl) getSkinnable();
    }

    private boolean isEditable() {
        return control().getComponentSlotFactory() != null && getSkinnable().getComponentItemResolver() != null;
    }

    /*=========================================================================*
     *                                                                         *
     * Rendering hooks                                                         *
     *                                                                         *
     *=========================================================================*/

    /** Chip-node property keys carrying the vertex and editing role, for the leaf row menus. */
    private static final String CHIP_VERTEX = "ditree-chip-vertex";
    private static final String CHIP_KIND = "ditree-chip-kind";

    @Override
    protected Node createChipNode(EntityVertex vertex, int conceptNid, ChipKind kind) {
        Node chip = super.createChipNode(vertex, conceptNid, kind);
        if (!isEditable()) {
            return chip;
        }
        chip.getProperties().put(CHIP_VERTEX, vertex);
        chip.getProperties().put(CHIP_KIND, kind);
        if (chip instanceof ComponentItemNode itemNode) {
            itemNode.setContextMenuItemsSupplier(() -> chipMenuItems(itemNode, vertex, kind));
            itemNode.setShowDiscardButtonOnHover(true);
            itemNode.setOnDiscardAction(() -> startChipEdit(itemNode, vertex, kind));
        }
        chip.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && e.isStillSincePress()) {
                startChipEdit(chip, vertex, kind);
                e.consume();
            }
        });
        setupChipDrop(chip, vertex, kind);
        return chip;
    }

    /*=========================================================================*
     *                                                                         *
     * Drop-to-replace on chips                                                *
     *                                                                         *
     *=========================================================================*/

    private static final PseudoClass DROP_TARGET_PSEUDO_CLASS = PseudoClass.getPseudoClass("drop-target");

    /** The same message KLComponentControl's drop area shows in the properties bump-out. */
    private static final String DRAG_AND_DROP_TEXT = ResourceBundle
            .getBundle("dev.ikm.komet.kview.controls.component-control").getString("textfield.drag.text");

    /**
     * Makes a chip a drop target: dropping a concept from the navigators, search, or any other
     * component chip replaces the chip's concept — the same edit picking it in the inline search
     * slot applies.
     */
    private void setupChipDrop(Node chip, EntityVertex vertex, ChipKind kind) {
        chip.setOnDragOver(event -> {
            if (event.getGestureSource() != chip && hasDroppableComponent(event)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        chip.setOnDragEntered(event -> {
            if (event.getGestureSource() != chip && hasDroppableComponent(event)) {
                chip.pseudoClassStateChanged(DROP_TARGET_PSEUDO_CLASS, true);
                if (chip instanceof ComponentItemNode itemNode) {
                    itemNode.setDropHintText(DRAG_AND_DROP_TEXT);
                }
            }
            event.consume();
        });
        chip.setOnDragExited(event -> {
            chip.pseudoClassStateChanged(DROP_TARGET_PSEUDO_CLASS, false);
            if (chip instanceof ComponentItemNode itemNode) {
                itemNode.setDropHintText(null);
            }
            event.consume();
        });
        chip.setOnDragDropped(event -> {
            int nid = extractDroppedNid(event);
            if (nid != Integer.MIN_VALUE) {
                applyChipEdit(vertex, kind, nid);
                event.setDropCompleted(true);
            }
            event.consume();
        });
    }

    private boolean hasDroppableComponent(DragEvent event) {
        return event.getDragboard().hasContent(COMPONENT_DRAG_FORMAT)
                || event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)
                || (event.getGestureSource() instanceof Node source && source.getUserData() instanceof DragAndDropInfo)
                || event.getGestureSource() instanceof TreeCell
                || event.getGestureSource() instanceof SearchResultCell;
    }

    /**
     * Extracts the dropped component's nid from the drag sources the component controls accept
     * (see {@code KLComponentControlSkin}): the component drag format, the next-gen concept
     * navigator format, next-gen search/pattern navigator {@link DragAndDropInfo}, and the
     * classic navigator and search cells. Returns {@link Integer#MIN_VALUE} when the drag carries
     * no usable component.
     */
    private int extractDroppedNid(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasContent(COMPONENT_DRAG_FORMAT)) {
            PublicId publicId = decodePublicId((String) dragboard.getContent(COMPONENT_DRAG_FORMAT));
            return EntityService.get().nidForPublicId(publicId);
        }
        if (dragboard.hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
            List<?> list = (List<?>) dragboard.getContent(CONCEPT_NAVIGATOR_DRAG_FORMAT);
            Object first = list.isEmpty() ? null : list.getFirst();
            if (first instanceof List<?> firstList && !firstList.isEmpty()) {
                // A multi-concept drag replaces with its first concept.
                first = firstList.getFirst();
            }
            if (first instanceof UUID[] uuids) {
                return EntityService.get().nidForPublicId(PublicIds.of(uuids));
            }
        }
        if (event.getGestureSource() instanceof Node source
                && source.getUserData() instanceof DragAndDropInfo dropInfo
                && dropInfo.publicId() != null) {
            return EntityService.get().nidForPublicId(dropInfo.publicId());
        }
        if (event.getGestureSource() instanceof TreeCell<?> treeCell
                && treeCell.getTreeItem() != null
                && treeCell.getTreeItem().getValue() instanceof EntityFacade entityFacade) {
            return entityFacade.nid();
        }
        if (event.getGestureSource() instanceof SearchResultCell searchResultCell
                && searchResultCell.getTreeItem() != null) {
            Object value = searchResultCell.getTreeItem().getValue();
            if (value instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                return nidTextRecord.nid();
            }
            if (value instanceof LatestVersionSearchResult latestVersionSearchResult) {
                return latestVersionSearchResult.latestVersion().get().nid();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * The chip's contribution to its component context menu — the classic axiom control's actions
     * for the leaf the chip represents. A restriction chip offers no removal: removing its vertex
     * alone would leave the role without a restriction; the whole role is removed instead (row
     * menu, or the role type chip whose vertex is the role itself).
     */
    private List<MenuItem> chipMenuItems(Node chip, EntityVertex vertex, ChipKind kind) {
        return switch (kind) {
            case CONCEPT_REFERENCE -> List.of(
                    createMenuItem("Choose replacement is-a", KometIcon.IconValue.PENCIL,
                            e -> startChipEdit(chip, vertex, kind)),
                    createMenuItem("Remove axiom", KometIcon.IconValue.TRASH, e -> removeVertex(vertex)));
            case ROLE_TYPE -> List.of(
                    createMenuItem("Choose role type", KometIcon.IconValue.PENCIL,
                            e -> startChipEdit(chip, vertex, kind)),
                    createMenuItem("Remove axiom", KometIcon.IconValue.TRASH, e -> removeVertex(vertex)));
            case ROLE_RESTRICTION -> List.of(
                    createMenuItem("Choose role restriction", KometIcon.IconValue.PENCIL,
                            e -> startChipEdit(chip, vertex, kind)));
        };
    }

    @Override
    protected void decorateClauseHeader(HBox header, VBox childrenBox, EntityVertex vertex) {
        if (!isEditable()) {
            return;
        }
        int meaning = vertex.getMeaningNid();
        boolean isSet = meaning == TinkarTerm.NECESSARY_SET.nid()
                || meaning == TinkarTerm.SUFFICIENT_SET.nid()
                || meaning == TinkarTerm.INCLUSION_SET.nid();
        boolean isRoleGroup = meaning == TinkarTerm.ROLE.nid();
        if (!isSet && !isRoleGroup) {
            // Property sets, features, interval roles, … have no add items of their own; the
            // rules engine supplies their actions (set value, choose interval type, …) plus
            // removal — mirroring AxiomFocusedRules' per-node-type rules.
            header.setOnContextMenuRequested(e -> {
                if (e.getTarget() instanceof Node target && isInsideChip(target, header)) {
                    return;
                }
                buildFallbackClauseMenu(vertex).show(header, e.getScreenX(), e.getScreenY());
                e.consume();
            });
            return;
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        StackPane addButton = createAddButton();
        header.getChildren().addAll(spacer, addButton);

        addButton.setOnMouseClicked(e -> {
            buildClauseMenu(vertex, childrenBox).show(addButton, Side.BOTTOM, 0, 2);
            e.consume();
        });
        header.setOnContextMenuRequested(e -> {
            if (e.getTarget() instanceof Node target && isInsideChip(target, header)) {
                return;
            }
            buildClauseMenu(vertex, childrenBox).show(header, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    @Override
    protected void decorateRootHeader(HBox header, VBox childrenBox) {
        if (!isEditable()) {
            return;
        }
        StackPane addButton = createAddButton();
        header.getChildren().add(addButton);

        addButton.setOnMouseClicked(e -> {
            buildRootMenu(childrenBox).show(addButton, Side.BOTTOM, 0, 2);
            e.consume();
        });
        header.setOnContextMenuRequested(e -> {
            if (e.getTarget() instanceof Node target && isInsideChip(target, header)) {
                return;
            }
            buildRootMenu(childrenBox).show(header, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    @Override
    protected boolean renderEmptyDefinition() {
        // An empty definition still shows the root row, so the user can build from scratch.
        return isEditable() && getSkinnable().getRootConceptNid() != 0;
    }

    /**
     * Builds the structure menu of the root row: which sets can be added to the definition. The
     * catalog comes from the rules engine when the provider is wired
     * ({@code AxiomFocusedRules.axiomIsDefinitionRoot} decides the legal items); the built-in
     * catalog below is the fallback.
     */
    private ContextMenu buildRootMenu(VBox childrenBox) {
        List<AxiomRuleAction> ruleActions = getRuleActions(null);
        if (ruleActions != null) {
            ContextMenu menu = newMenu();
            for (AxiomRuleAction ruleAction : ruleActions) {
                menu.getItems().add(rootMenuItem(ruleAction, childrenBox));
            }
            return menu;
        }
        ContextMenu menu = newMenu();
        MenuItem necessary = createMenuItem("Add necessary set", KometIcon.IconValue.PLUS,
                e -> addSetTemplate(childrenBox, true));
        DiTreeEntity current = getSkinnable().getValue();
        // Mirrors AxiomFocusedRules: only one necessary set is allowed per definition.
        necessary.setDisable(current != null && current.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET));
        menu.getItems().add(necessary);
        menu.getItems().add(createMenuItem("Add sufficient set", KometIcon.IconValue.PLUS,
                e -> addSetTemplate(childrenBox, false)));

        MenuItem propertySet = createMenuItem("Add property set", KometIcon.IconValue.PLUS,
                e -> transform(builder -> builder.PropertySet(builder.And(
                        builder.ConceptAxiom(control().getPropertySetSeedNid())))));
        propertySet.setDisable(control().getPropertySetSeedNid() == 0);
        menu.getItems().add(propertySet);
        MenuItem dataPropertySet = createMenuItem("Add data property set", KometIcon.IconValue.PLUS,
                e -> transform(builder -> builder.DataPropertySet(builder.And(
                        builder.ConceptAxiom(control().getDataPropertySetSeedNid())))));
        dataPropertySet.setDisable(control().getDataPropertySetSeedNid() == 0);
        menu.getItems().add(dataPropertySet);
        MenuItem intervalPropertySet = createMenuItem("Add interval property set", KometIcon.IconValue.PLUS,
                e -> transform(builder -> builder.IntervalPropertySet(builder.And(
                        builder.ConceptAxiom(control().getDataPropertySetSeedNid())))));
        intervalPropertySet.setDisable(control().getDataPropertySetSeedNid() == 0);
        menu.getItems().add(intervalPropertySet);
        return menu;
    }

    /**
     * Maps one engine-generated root action to its menu item: the set adds use the inline
     * templates, the property-set adds use the seeded value-flow transforms, anything else runs
     * the engine action itself.
     */
    private MenuItem rootMenuItem(AxiomRuleAction ruleAction, VBox childrenBox) {
        if (ruleAction instanceof AxiomRuleAction.Command command) {
            return switch (command.id()) {
                case "AddNecessarySet" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addSetTemplate(childrenBox, true));
                case "AddSufficientSet" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addSetTemplate(childrenBox, false));
                case "AddPropertySet" -> control().getPropertySetSeedNid() == 0
                        ? engineMenuItem(command)
                        : createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                                e -> transform(builder -> builder.PropertySet(builder.And(
                                        builder.ConceptAxiom(control().getPropertySetSeedNid())))));
                case "AddDataPropertySet" -> control().getDataPropertySetSeedNid() == 0
                        ? engineMenuItem(command)
                        : createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                                e -> transform(builder -> builder.DataPropertySet(builder.And(
                                        builder.ConceptAxiom(control().getDataPropertySetSeedNid())))));
                case "AddIntervalPropertySet" -> control().getDataPropertySetSeedNid() == 0
                        ? engineMenuItem(command)
                        : createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                                e -> transform(builder -> builder.IntervalPropertySet(builder.And(
                                        builder.ConceptAxiom(control().getDataPropertySetSeedNid())))));
                default -> engineMenuItem(command);
            };
        }
        return ((AxiomRuleAction.Submenu) ruleAction).menu();
    }

    /**
     * Maps one engine-generated clause action (sets, role groups) to its menu item: known adds and
     * the set-type change use the inline templates and value-flow transforms, anything else runs
     * the engine action itself.
     */
    private MenuItem clauseMenuItem(AxiomRuleAction ruleAction, EntityVertex vertex, VBox childrenBox) {
        if (ruleAction instanceof AxiomRuleAction.Command command) {
            return switch (command.id()) {
                case "AddIsA" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addIsATemplate(childrenBox, vertex));
                case "AddSomeRole" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addRoleTemplate(childrenBox, vertex));
                case "AddRoleGroup" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addRoleGroupTemplate(childrenBox, vertex));
                case "AddIntervalRole" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addIntervalRole(vertex));
                case "AddFeature" -> createMenuItem(command.text(), KometIcon.IconValue.PLUS,
                        e -> addConcreteRole(vertex));
                case "ChangeSetType" -> createMenuItem(command.text(), KometIcon.IconValue.PENCIL,
                        e -> changeSetType(vertex, command.text().contains("necessary")
                                ? TinkarTerm.NECESSARY_SET : TinkarTerm.SUFFICIENT_SET));
                default -> defaultMenuItem(ruleAction, vertex);
            };
        }
        return ((AxiomRuleAction.Submenu) ruleAction).menu();
    }

    /**
     * The engine-sourced fallback-clause menu (property sets, features, interval roles, …):
     * removal maps to the value-flow transform, everything else runs as the engine generated it.
     */
    private ContextMenu buildFallbackClauseMenu(EntityVertex vertex) {
        ContextMenu menu = newMenu();
        List<AxiomRuleAction> ruleActions = getRuleActions(vertex);
        if (ruleActions == null) {
            menu.getItems().add(createMenuItem("Remove axiom", KometIcon.IconValue.TRASH,
                    e -> removeVertex(vertex)));
            return menu;
        }
        for (AxiomRuleAction ruleAction : ruleActions) {
            menu.getItems().add(defaultMenuItem(ruleAction, vertex));
        }
        return menu;
    }

    /**
     * The identity mapping every menu falls back to: removal becomes the value-flow transform,
     * unknown commands run the engine action, submenus mount unchanged.
     */
    private MenuItem defaultMenuItem(AxiomRuleAction ruleAction, EntityVertex vertex) {
        if (ruleAction instanceof AxiomRuleAction.Command command) {
            if ("RemoveAxiomAction".equals(command.id())) {
                return createMenuItem(command.text(), KometIcon.IconValue.TRASH, e -> removeVertex(vertex));
            }
            return engineMenuItem(command);
        }
        return ((AxiomRuleAction.Submenu) ruleAction).menu();
    }

    private MenuItem engineMenuItem(AxiomRuleAction.Command command) {
        return createMenuItem(command.text(), iconFor(command.id()), e -> command.run());
    }

    private KometIcon.IconValue iconFor(String actionId) {
        if (actionId.startsWith("Add")) {
            return KometIcon.IconValue.PLUS;
        }
        if (actionId.startsWith("Remove")) {
            return KometIcon.IconValue.TRASH;
        }
        return KometIcon.IconValue.PENCIL;
    }

    /**
     * The rules-engine-sourced actions for a vertex, obtained through the control's
     * {@link KLDiTreeControl#ruleActionsProviderProperty() rule actions provider}. Every menu
     * builder starts here: a non-null result drives the menu (the engine decides which actions
     * are present and their order), a null result means no provider is wired and the builder
     * falls back to its built-in action catalog.
     *
     * @param vertex the clause vertex the menu is for, or null for the definition root
     * @return the engine's actions for the vertex, or null when no provider is wired
     */
    private List<AxiomRuleAction> getRuleActions(EntityVertex vertex) {
        Function<EntityVertex, List<AxiomRuleAction>> provider = control().getRuleActionsProvider();
        return provider == null ? null : provider.apply(vertex);
    }

    /**
     * The hover-revealed + button of a header row. The plus glyph is a self-contained shape
     * (styled in ditree-control.css); space is always reserved and the button fades in on header
     * hover.
     */
    private StackPane createAddButton() {
        Region plusIcon = new Region();
        plusIcon.getStyleClass().add("ditree-plus-icon");
        StackPane addButton = new StackPane(plusIcon);
        addButton.getStyleClass().add("ditree-add-button");
        return addButton;
    }

    @Override
    protected void decorateConceptRow(HBox row, EntityVertex vertex) {
        decorateLeafRow(row, vertex);
    }

    @Override
    protected void decorateRoleRow(HBox row, EntityVertex vertex) {
        if (!isEditable()) {
            return;
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().add(spacer);
        decorateLeafRow(row, vertex);
    }

    /**
     * Mounts the leaf affordances on an is-a or role row: a hover-revealed + button and a
     * right-click menu with the classic axiom control's leaf actions (choose replacement /
     * role type / role restriction, remove axiom).
     */
    private void decorateLeafRow(HBox row, EntityVertex rowVertex) {
        if (!isEditable()) {
            return;
        }
        StackPane addButton = createAddButton();
        row.getChildren().add(addButton);

        addButton.setOnMouseClicked(e -> {
            buildLeafMenu(row, rowVertex).show(addButton, Side.BOTTOM, 0, 2);
            e.consume();
        });
        row.setOnContextMenuRequested(e -> {
            if (e.getTarget() instanceof Node target && isInsideChip(target, row)) {
                return;
            }
            buildLeafMenu(row, rowVertex).show(row, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    /**
     * Builds the leaf row menu from the row's chips: one choose action per chip (replacement
     * is-a, role type, role restriction) plus removal of the row's axiom. The catalog comes from
     * the rules engine when the provider is wired — its choose-concept submenus map to the inline
     * chip editing; the built-in catalog below is the fallback.
     */
    private ContextMenu buildLeafMenu(HBox row, EntityVertex rowVertex) {
        List<AxiomRuleAction> ruleActions = getRuleActions(rowVertex);
        if (ruleActions != null) {
            ContextMenu ruleMenu = newMenu();
            for (AxiomRuleAction ruleAction : ruleActions) {
                ruleMenu.getItems().add(leafMenuItem(ruleAction, row, rowVertex));
            }
            return ruleMenu;
        }
        ContextMenu menu = newMenu();
        for (Node child : row.getChildren()) {
            if (child instanceof ComponentItemNode chip
                    && chip.getProperties().get(CHIP_VERTEX) instanceof EntityVertex chipVertex
                    && chip.getProperties().get(CHIP_KIND) instanceof ChipKind kind) {
                String label = switch (kind) {
                    case CONCEPT_REFERENCE -> "Choose replacement is-a";
                    case ROLE_TYPE -> "Choose role type";
                    case ROLE_RESTRICTION -> "Choose role restriction";
                };
                menu.getItems().add(createMenuItem(label, KometIcon.IconValue.PENCIL,
                        e -> startChipEdit(chip, chipVertex, kind)));
            }
        }
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(createMenuItem("Remove axiom", KometIcon.IconValue.TRASH,
                e -> removeVertex(rowVertex)));
        return menu;
    }

    /**
     * Maps one engine-generated leaf action to its menu item: the choose-concept submenus become
     * inline chip edits on the row's matching chip, removal becomes the value-flow transform,
     * anything else runs as the engine generated it.
     */
    private MenuItem leafMenuItem(AxiomRuleAction ruleAction, HBox row, EntityVertex rowVertex) {
        if (ruleAction instanceof AxiomRuleAction.Submenu submenu) {
            ChipKind kind = switch (submenu.text()) {
                case "Choose replacement is-a" -> ChipKind.CONCEPT_REFERENCE;
                case "Choose role type" -> ChipKind.ROLE_TYPE;
                case "Choose role restriction" -> ChipKind.ROLE_RESTRICTION;
                default -> null;
            };
            if (kind != null) {
                Node chip = findChip(row, kind);
                if (chip != null && chip.getProperties().get(CHIP_VERTEX) instanceof EntityVertex chipVertex) {
                    ChipKind chipKind = kind;
                    return createMenuItem(submenu.text(), KometIcon.IconValue.PENCIL,
                            e -> startChipEdit(chip, chipVertex, chipKind));
                }
            }
            return submenu.menu();
        }
        return defaultMenuItem(ruleAction, rowVertex);
    }

    private Node findChip(HBox row, ChipKind kind) {
        for (Node child : row.getChildren()) {
            if (child instanceof ComponentItemNode && child.getProperties().get(CHIP_KIND) == kind) {
                return child;
            }
        }
        return null;
    }

    /*=========================================================================*
     *                                                                         *
     * Inline concept edit                                                     *
     *                                                                         *
     *=========================================================================*/

    private void startChipEdit(Node chip, EntityVertex vertex, ChipKind kind) {
        Pane parent = (Pane) chip.getParent();
        int index = parent.getChildren().indexOf(chip);

        KLComponentControl slot = newSlot();
        if (kind == ChipKind.CONCEPT_REFERENCE) {
            slot.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(slot, Priority.ALWAYS);
        }
        parent.getChildren().set(index, slot);

        slot.entityProperty().subscribe((oldEntity, newEntity) -> {
            if (!KLComponentControl.isEmpty(newEntity)) {
                applyChipEdit(vertex, kind, newEntity.nid());
            }
        });
        onEscape(slot, () -> parent.getChildren().set(parent.getChildren().indexOf(slot), chip));
        focusSlot(slot);
    }

    private void applyChipEdit(EntityVertex vertex, ChipKind kind, int newConceptNid) {
        transform(builder -> {
            LogicalAxiom axiom = builder.get(vertex.vertexIndex());
            switch (kind) {
                case CONCEPT_REFERENCE, ROLE_RESTRICTION ->
                        builder.updateConceptReference((LogicalAxiom.Atom.ConceptAxiom) axiom, newConceptNid);
                case ROLE_TYPE ->
                        builder.updateRoleType((LogicalAxiom.Atom.TypedAtom.Role) axiom, ConceptFacade.make(newConceptNid));
            }
        });
    }

    /*=========================================================================*
     *                                                                         *
     * Structure menus                                                         *
     *                                                                         *
     *=========================================================================*/

    /**
     * Builds the structure menu of a clause: the same actions the classic axiom control offers for
     * the node type through {@code AxiomFocusedRules} — the legal add items, set-type change, and
     * removal. The catalog comes from the rules engine when the provider is wired; the built-in
     * catalog below is the fallback.
     */
    private ContextMenu buildClauseMenu(EntityVertex vertex, VBox childrenBox) {
        List<AxiomRuleAction> ruleActions = getRuleActions(vertex);
        if (ruleActions != null) {
            ContextMenu ruleMenu = newMenu();
            for (AxiomRuleAction ruleAction : ruleActions) {
                ruleMenu.getItems().add(clauseMenuItem(ruleAction, vertex, childrenBox));
            }
            return ruleMenu;
        }
        ContextMenu menu = newMenu();
        int meaning = vertex.getMeaningNid();
        if (meaning == TinkarTerm.ROLE.nid()) {
            // role group: mirrors AxiomFocusedRules.axiomIsRoleGroup
            menu.getItems().add(createMenuItem("Add role", KometIcon.IconValue.PLUS,
                    e -> addRoleTemplate(childrenBox, vertex)));
            menu.getItems().add(createMenuItem("Add interval role", KometIcon.IconValue.PLUS,
                    e -> addIntervalRole(vertex)));
            menu.getItems().add(createMenuItem("Add concrete role", KometIcon.IconValue.PLUS,
                    e -> addConcreteRole(vertex)));
        } else {
            // set: mirrors AxiomFocusedRules.axiomIsSet
            menu.getItems().add(createMenuItem("Add is-a", KometIcon.IconValue.PLUS,
                    e -> addIsATemplate(childrenBox, vertex)));
            menu.getItems().add(createMenuItem("Add role", KometIcon.IconValue.PLUS,
                    e -> addRoleTemplate(childrenBox, vertex)));
            menu.getItems().add(createMenuItem("Add interval role", KometIcon.IconValue.PLUS,
                    e -> addIntervalRole(vertex)));
            menu.getItems().add(createMenuItem("Add role group", KometIcon.IconValue.PLUS,
                    e -> addRoleGroupTemplate(childrenBox, vertex)));
            menu.getItems().add(createMenuItem("Add concrete role", KometIcon.IconValue.PLUS,
                    e -> addConcreteRole(vertex)));
        }
        if (meaning == TinkarTerm.SUFFICIENT_SET.nid()) {
            menu.getItems().add(new SeparatorMenuItem());
            MenuItem toNecessary = createMenuItem("Change to necessary set", KometIcon.IconValue.PENCIL,
                    e -> changeSetType(vertex, TinkarTerm.NECESSARY_SET));
            // Mirrors AxiomFocusedRules: only one necessary set is allowed per definition.
            toNecessary.setDisable(getSkinnable().getValue().containsVertexWithMeaning(TinkarTerm.NECESSARY_SET));
            menu.getItems().add(toNecessary);
        } else if (meaning == TinkarTerm.NECESSARY_SET.nid()) {
            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(createMenuItem("Change to sufficient set", KometIcon.IconValue.PENCIL,
                    e -> changeSetType(vertex, TinkarTerm.SUFFICIENT_SET)));
        }
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(createMenuItem("Remove axiom", KometIcon.IconValue.TRASH,
                e -> removeVertex(vertex)));
        return menu;
    }

    private ContextMenu newMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("klcontext-menu");
        return menu;
    }

    private boolean isInsideChip(Node target, Node stop) {
        Node node = target;
        while (node != null && node != stop) {
            if (node instanceof ComponentItemNode) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    /*=========================================================================*
     *                                                                         *
     * Templates for new content                                               *
     *                                                                         *
     *=========================================================================*/

    private void addIsATemplate(VBox childrenBox, EntityVertex setVertex) {
        KLComponentControl slot = newSlot();
        slot.setMaxWidth(Double.MAX_VALUE);
        HBox row = new HBox(slot);
        row.getStyleClass().add("ditree-concept-row");
        HBox.setHgrow(slot, Priority.ALWAYS);
        slot.entityProperty().subscribe((oldEntity, newEntity) -> {
            if (!KLComponentControl.isEmpty(newEntity)) {
                transform(builder -> builder.addToFirstAnd(setVertex.vertexIndex(),
                        builder.ConceptAxiom(newEntity.nid())));
            }
        });
        onEscape(row, () -> childrenBox.getChildren().remove(row));
        focusSlot(slot);
        childrenBox.getChildren().add(row);
        NodeUtils.setShowing(childrenBox, true);
    }

    /**
     * Adds an interval role with the classic axiom control's initial values
     * ({@code AddIntervalRole}: unmodeled role type and unit, interval (0..10)); the user then
     * refines it. Applies immediately — there is no dedicated template row for intervals yet.
     */
    private void addIntervalRole(EntityVertex parentVertex) {
        transform(builder -> {
            LogicalAxiom.Atom.TypedAtom.IntervalRole role = builder.IntervalRole(
                    TinkarTerm.UNMODELED_ROLE_CONCEPT,
                    BigDecimal.valueOf(0), true, BigDecimal.valueOf(10), true,
                    TinkarTerm.UNMODELED_ROLE_CONCEPT);
            builder.addToFirstAnd(parentVertex.vertexIndex(), role);
        });
    }

    /**
     * Adds a concrete role (feature) with the classic axiom control's initial values
     * ({@code AddFeature}: anonymous feature type, {@code = 1}); the user then refines it.
     * Applies immediately — there is no dedicated template row for features yet.
     */
    private void addConcreteRole(EntityVertex parentVertex) {
        transform(builder -> builder.addToFirstAnd(parentVertex.vertexIndex(),
                builder.FeatureAxiom(TinkarTerm.ANONYMOUS_CONCEPT, TinkarTerm.EQUAL_TO, Integer.valueOf(1))));
    }

    private void addRoleTemplate(VBox childrenBox, EntityVertex parentVertex) {
        HBox row = roleTemplateRow((type, restriction) ->
                transform(builder -> builder.addToFirstAnd(parentVertex.vertexIndex(),
                        builder.SomeRole(ConceptFacade.make(type.nid()),
                                builder.ConceptAxiom(restriction.nid())))));
        onEscape(row, () -> childrenBox.getChildren().remove(row));
        childrenBox.getChildren().add(row);
        NodeUtils.setShowing(childrenBox, true);
    }

    private void addRoleGroupTemplate(VBox childrenBox, EntityVertex setVertex) {
        VBox groupChildren = new VBox();
        groupChildren.getStyleClass().add("ditree-children");
        HBox header = new HBox();
        header.getStyleClass().add("ditree-clause-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(clauseBar("role-group"), clauseLabel("Role group"));
        VBox groupNode = new VBox(header, groupChildren);
        groupNode.getStyleClass().add("ditree-node");

        HBox roleRow = roleTemplateRow((type, restriction) ->
                transform(builder -> builder.addToFirstAnd(setVertex.vertexIndex(),
                        builder.SomeRole(TinkarTerm.ROLE_GROUP, builder.And(
                                builder.SomeRole(ConceptFacade.make(type.nid()),
                                        builder.ConceptAxiom(restriction.nid())))))));
        groupChildren.getChildren().add(roleRow);
        onEscape(groupNode, () -> childrenBox.getChildren().remove(groupNode));
        childrenBox.getChildren().add(groupNode);
        NodeUtils.setShowing(childrenBox, true);
    }

    private void addSetTemplate(VBox rootChildrenBox, boolean necessary) {
        VBox setChildren = new VBox();
        setChildren.getStyleClass().add("ditree-children");
        HBox header = new HBox();
        header.getStyleClass().add("ditree-clause-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(clauseBar(necessary ? "necessary-set" : "sufficient-set"),
                clauseLabel(necessary ? "Necessary set" : "Sufficient set"));
        VBox setNode = new VBox(header, setChildren);
        setNode.getStyleClass().add("ditree-node");

        KLComponentControl slot = newSlot();
        slot.setMaxWidth(Double.MAX_VALUE);
        HBox isARow = new HBox(slot);
        isARow.getStyleClass().add("ditree-concept-row");
        HBox.setHgrow(slot, Priority.ALWAYS);
        slot.entityProperty().subscribe((oldEntity, newEntity) -> {
            if (!KLComponentControl.isEmpty(newEntity)) {
                transform(builder -> {
                    LogicalAxiom.Atom.ConceptAxiom isA = builder.ConceptAxiom(newEntity.nid());
                    if (necessary) {
                        builder.NecessarySet(builder.And(isA));
                    } else {
                        builder.SufficientSet(builder.And(isA));
                    }
                });
            }
        });
        setChildren.getChildren().add(isARow);
        onEscape(setNode, () -> rootChildrenBox.getChildren().remove(setNode));
        focusSlot(slot);
        rootChildrenBox.getChildren().add(setNode);
        NodeUtils.setShowing(rootChildrenBox, true);
    }

    /**
     * Builds a {@code ∃ ( [type slot] ) → [ [restriction slot] ]} template row. The completion
     * action runs once both concepts are picked.
     */
    private HBox roleTemplateRow(BiConsumer<EntityProxy, EntityProxy> onComplete) {
        KLComponentControl typeSlot = newSlot();
        KLComponentControl restrictionSlot = newSlot();

        HBox row = new HBox(clauseBar("role"), operator("∃ ("), typeSlot,
                operator(") → ["), restrictionSlot, operator("]"));
        row.getStyleClass().add("ditree-role-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Runnable maybeComplete = () -> {
            if (!KLComponentControl.isEmpty(typeSlot.getEntity())
                    && !KLComponentControl.isEmpty(restrictionSlot.getEntity())) {
                onComplete.accept(typeSlot.getEntity(), restrictionSlot.getEntity());
            }
        };
        showPendingChipWhenPicked(row, typeSlot, maybeComplete);
        showPendingChipWhenPicked(row, restrictionSlot, maybeComplete);
        focusSlot(typeSlot);
        return row;
    }

    /**
     * While a template waits for its other concept, a picked slot renders as a regular chip —
     * clicking it or its ✕ reopens the slot to change the pick. The slot keeps the picked entity
     * (the template commits from it); the chip is purely presentation.
     */
    private void showPendingChipWhenPicked(HBox row, KLComponentControl slot, Runnable maybeComplete) {
        slot.entityProperty().subscribe((oldEntity, newEntity) -> {
            if (KLComponentControl.isEmpty(newEntity)) {
                return;
            }
            ComponentItemNode chip = ComponentItemNodeFactory.create(
                    getSkinnable().getComponentItemResolver().apply(newEntity.nid()));
            chip.setShowDragHandleOnHover(true);
            chip.setShowDiscardButtonOnHover(true);
            Runnable reopenSlot = () -> {
                slot.setEntity(BLANK_CONCEPT);
                row.getChildren().set(row.getChildren().indexOf(chip), slot);
                focusSlot(slot);
            };
            chip.setOnDiscardAction(reopenSlot);
            chip.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && e.isStillSincePress()) {
                    reopenSlot.run();
                    e.consume();
                }
            });
            row.getChildren().set(row.getChildren().indexOf(slot), chip);
            maybeComplete.run();
        });
    }

    /*=========================================================================*
     *                                                                         *
     * Slots and tree transformations                                          *
     *                                                                         *
     *=========================================================================*/

    private KLComponentControl newSlot() {
        KLComponentControl slot = control().getComponentSlotFactory().get();
        slot.getStyleClass().add("ditree-slot");
        return slot;
    }

    private void onEscape(Node node, Runnable action) {
        node.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                action.run();
                e.consume();
            }
        });
    }

    private void focusSlot(KLComponentControl slot) {
        Platform.runLater(() -> {
            // The skin only exists after a CSS pass; force it so the inner search text field is
            // there to receive focus, letting the user type the search right away.
            slot.applyCss();
            Node searchTextField = slot.lookup(".concept-text-field");
            if (searchTextField != null) {
                searchTextField.requestFocus();
            } else {
                slot.requestFocus();
            }
        });
    }

    /**
     * Applies an edit to the current definition through a {@link LogicalExpressionBuilder} — the
     * same transformation API the classic axiom rule actions use — and sets the resulting tree as
     * the control's value. A null current value starts a fresh definition (root vertex only).
     */
    private void transform(Consumer<LogicalExpressionBuilder> edit) {
        DiTreeEntity tree = getSkinnable().getValue();
        LogicalExpressionBuilder builder = tree == null
                ? new LogicalExpressionBuilder()
                : new LogicalExpressionBuilder(tree);
        edit.accept(builder);
        getSkinnable().setValue(toTree(builder.build()));
    }

    private void changeSetType(EntityVertex setVertex, ConceptFacade newSetMeaning) {
        transform(builder -> builder.changeSetType(
                (LogicalAxiom.LogicalSet) builder.get(setVertex.vertexIndex()), newSetMeaning));
    }

    private void removeVertex(EntityVertex vertex) {
        DiTreeEntity tree = getSkinnable().getValue();
        getSkinnable().setValue(tree.removeVertex(vertex.vertexIndex()).build());
    }

    private static DiTreeEntity toTree(LogicalExpression expression) {
        return switch (expression.sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder builder -> builder.build();
            default -> throw new IllegalStateException("Unexpected source graph: " + expression.sourceGraph());
        };
    }
}