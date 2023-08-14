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
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.*;

/**
 * Animates an object when its position is changed. For instance, when
 * additional items are added to a Region, and the layout has changed, then the
 * layout animator makes the transition by sliding each item into its final
 * place.
 *
 * Sourced from: https://gist.github.com/jewelsea/5683558
 */
public class LayoutAnimator implements ChangeListener<Number>, ListChangeListener<Node> {

    private final Map<Node, MoveXTransition> nodeXTransitions = new HashMap<>();
    private final Map<Node, MoveYTransition> nodeYTransitions = new HashMap<>();

    /**
     * Animates all the children of a Region.
     * <code>
     *   VBox myVbox = new VBox();
     *   LayoutAnimator animator = new LayoutAnimator();
     *   animator.observe(myVbox.getChildren());
     * </code>
     *
     * @param nodes
     */
    public void observe(ObservableList<Node> nodes) {
        for (Node node : nodes) {
            this.observe(node);
        }
        nodes.addListener(this);
    }

    public void unobserve(ObservableList<Node> nodes) {
        nodes.removeListener(this);
    }

    public void observe(Node n) {
        n.layoutXProperty().addListener(this);
        n.layoutYProperty().addListener(this);

    }

    public void unobserve(Node n) {
        n.layoutXProperty().removeListener(this);
        n.layoutYProperty().removeListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
        final double delta = newValue.doubleValue() - oldValue.doubleValue();
        final DoubleProperty doubleProperty = (DoubleProperty) ov;
        final Node node = (Node) doubleProperty.getBean();

        switch (doubleProperty.getName()) {
            case "layoutX":
                MoveXTransition tx = nodeXTransitions.get(node);
                if (tx == null) {
                    tx = new MoveXTransition(node);
                    nodeXTransitions.put(node, tx);
                }
                tx.setFromX(tx.getTranslateX() - delta);

                tx.playFromStart();
                break;

            default: // "layoutY"
                MoveYTransition ty = nodeYTransitions.get(node);
                if (ty == null) {
                    ty = new MoveYTransition(node);
                    nodeYTransitions.put(node, ty);
                }
                ty.setFromY(ty.getTranslateY() - delta);

                ty.playFromStart();
        }
    }

    private abstract class MoveTransition extends Transition {
        private final Duration MOVEMENT_ANIMATION_DURATION = new Duration(500);

        protected final Translate translate;

        public MoveTransition(final Node node) {
            setCycleDuration(MOVEMENT_ANIMATION_DURATION);
            translate = new Translate();

            node.getTransforms().add(translate);
        }

        public double getTranslateX() {
            return translate.getX();
        }

        public double getTranslateY() {
            return translate.getY();
        }
    }

    private class MoveXTransition extends MoveTransition {
        private double fromX;

        public MoveXTransition(final Node node) {
            super(node);
        }

        @Override protected void interpolate(double frac) {
            translate.setX(fromX * (1 - frac));
        }

        public void setFromX(double fromX) {
            translate.setX(fromX);
            this.fromX = fromX;
        }
    }

    private class MoveYTransition extends MoveTransition {
        private double fromY;

        public MoveYTransition(final Node node) {
            super(node);
        }

        @Override protected void interpolate(double frac) {
            translate.setY(fromY * (1 - frac));
        }

        public void setFromY(double fromY) {
            translate.setY(fromY);
            this.fromY = fromY;
        }
    }

    @Override
    public void onChanged(Change change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Node node : (List<Node>) change.getAddedSubList()) {
                    this.observe(node);
                }
            } else if (change.wasRemoved()) {
                for (Node node : (List<Node>) change.getRemoved()) {
                    this.unobserve(node);
                }
            }
        }
    }

    // todo unobserving nodes should cleanup any intermediate transitions they may have and ensure they are removed from transition cache to prevent memory leaks.
}