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
package dev.ikm.komet.amplify.commons;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static dev.ikm.komet.amplify.commons.ResourceHelper.toAbsolutePath;

public class CssHelper {
    private static final Logger LOG = LoggerFactory.getLogger(CssHelper.class);
    public static final String AMPLIFY_OPTION_1A_CSS_FILE = "../amplify-opt-1a.css";
    public static final String AMPLIFY_OPTION_2_CSS_FILE = "../amplify-opt-2.css";
    public static final String AMPLIFY_DEFAULT_CSS_FILE = AMPLIFY_OPTION_2_CSS_FILE;

    public static String retrieveStyleSheet(String path, Class<?> clazz) {
        String cssFile = toAbsolutePath(path, clazz);
        URL cssResource = clazz.getResource(cssFile);
        return cssResource.toString();
    }
    public static String defaultStyleSheet() {
        // find out who called this method.
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement previousCall = stackTraceElements[2];

        Class<?> clazz = CssHelper.class;
        String cssFile = toAbsolutePath(AMPLIFY_DEFAULT_CSS_FILE, clazz);

        // Log what file is loaded.
        LOG.atDebug().log("%s/%s.%s() added stylesheet file: %s".formatted(
                previousCall.getModuleName(), previousCall.getClassName(),
                previousCall.getMethodName(), cssFile));

        URL cssResource = clazz.getResource(cssFile);
        return cssResource.toString();
    }

    public static Text genText(String str) {
        InnerShadow is = new InnerShadow();
        is.setOffsetX(4.0f);
        is.setOffsetY(4.0f);
        Text t = new Text();
        t.setEffect(is);
        t.setText(str);
        t.setFill(Color.web("#94EE5DFF"));
        t.setFont(Font.font("Open Sans", FontWeight.BOLD, 50));
        return t;
    }

    private static String decimalToHex(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public static String toWebColor(Color value) {
        return "#%s%s%s%s".formatted(
                decimalToHex(value.getRed()),                      /* R */
                        decimalToHex(value.getGreen()),            /* G */
                        decimalToHex(value.getBlue()),             /* B */
                        decimalToHex(value.getOpacity())           /* Alpha */
        ).toUpperCase();
    }

    /**
     * Clear any parent nodes associated with a css file resource and refresh with an updated css File(s).
     * @param pane the root node
     * @param cssFile array of css files.
     */
    public static void refreshPanes(Parent pane, String ...cssFile) {
        List<Parent> cssPanes = getAllParentNodes(pane, node -> node instanceof Parent parent && parent.getStylesheets().size() > 0);
        cssPanes.forEach(cssPane -> {
            cssPane.getStylesheets().clear();
            cssPane.getStylesheets().addAll(cssFile);
            for(String cssF : cssFile) {
                LOG.atDebug().log("css updated: %s with %s".formatted(cssPane.getStyleClass().toString(), cssF));
            }
        });
        LOG.info("Number of Panes refreshed: %s".formatted(cssPanes.size()));
    }

    /**
     * Traverse the graph of nested nodes and filter out Parent nodes that have a stylesheets.
     * @param root The root node.
     * @param filter A filter to find Parent nodes you are interested in.
     * @return A list of Parent node objects. e.g. ones having one-to-many CSS files as stylesheets.
     */
    public static List<Parent> getAllParentNodes(Parent root, Predicate<Node> filter) {
        List<Node> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes.stream()
                .filter(filter)
                .map(node -> (Parent) node)
                .toList();
    }

    /**
     * Recursive way to DFS a root JavaFX node.
     * @param parent root node
     * @param nodes accumulated children nodes.
     */
    private static void addAllDescendents(Parent parent, List<Node> nodes) {
        // add parent
        nodes.add(parent);

        // add children that are Parent nodes.
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }

}
