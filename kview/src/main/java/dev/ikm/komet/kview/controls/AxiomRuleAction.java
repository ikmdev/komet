package dev.ikm.komet.kview.controls;

import javafx.scene.control.Menu;

/**
 * One entry of the axiom structure menu, sourced from the axiom rules engine through
 * {@link KLDiTreeControl#ruleActionsProviderProperty()} — the same Evrete rules
 * ({@code AxiomFocusedRules}) that drive the classic axiom control's context menu. The skin
 * substitutes its inline treatments (templates, chip editing) for the entries it recognizes and
 * mounts the rest as-is, so new rule actions appear in the tree without skin changes.
 */
public sealed interface AxiomRuleAction {

    /**
     * A directly runnable rule action. The {@code id} is the generated action's class simple name
     * (e.g. {@code "AddIsA"}, {@code "RemoveAxiomAction"}), which the skin uses to substitute its
     * inline treatment; running an unmapped command executes the engine action itself.
     */
    record Command(String id, String text, Runnable run) implements AxiomRuleAction {
    }

    /**
     * A prebuilt submenu consequence (e.g. the choose-concept popover menus). The skin maps the
     * known choose menus to inline chip editing and mounts unknown ones unchanged.
     */
    record Submenu(String text, Menu menu) implements AxiomRuleAction {
    }
}