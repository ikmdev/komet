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
package dev.ikm.komet.kview.mvvm.view.timeline;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.fxutils.FXUtils;
import dev.ikm.komet.kview.mvvm.model.ChangeCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.ikm.komet.kview.fxutils.FXUtils.localToParent;
import static dev.ikm.komet.kview.fxutils.FXUtils.textFontMetricsBounds;
import static javafx.collections.FXCollections.observableSet;

/**
 * The timeline (time travel) view is associated with the kview-timeline.fxml view file.
 * This control is responsible for updating the containing all change chronology dates from one path and multiple
 * extensions (modules) selected. When user clicks on a date point, the left history panel will highlight the change.
 * Also, provided are range buttons to filter changes to be displayed based.
 * TODO refactor (to decouple) pathMap structure and ChangeCoordinate. Make this component reusable in any context.
 */
public class TimelineController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(TimelineController.class);

    private final ObjectProperty<Circle> datePointSelected = new SimpleObjectProperty<>();
    private Consumer<ChangeCoordinate> datePointSelectedConsumer;
    private BiConsumer<Boolean, Set<ChangeCoordinate>> datePointsInDateRangeConsumer;
    //private List<BiConsumer<Boolean, Set<ChangeCoordinate>>> datePointsInDateRangeConsumers;
    private final ObjectProperty<Rectangle2D> rangeViewRectangleProp = new SimpleObjectProperty<>();

    /**
     * A map of key - ChangeCoordinate record containing (path name, moduleNid, and VersionChangeRecord).
     * When the range sliders are used the data points visible within the range (detect box - between top and bottom sliders).
     */
    private ObservableSet<ChangeCoordinate> slideControlDatePointsInRangeSet;
    /**
     * All circles (date points) created. This helps us determine date points in range.
     */
    private ObservableSet<Circle> allCircleDatePointsSet;

    @FXML
    private ToggleButton rangeToggleButton;

    /**
     * The timeline top range slider body.
     */
    @FXML
    private Region timelineTopRangeSliderBody;

    /**
     * The area where all time slider and components go on top.
     */
    @FXML
    private AnchorPane timelineSurfaceAnchorPane;

    /**
     * The button allowing user to slide range of latest dates. (today)
     */
    @FXML
    private Group timelineTopRangeSliderButton;

    /**
     * The timeline bottom range slider body.
     */
    @FXML
    private Region timelineBottomRangeSliderBody;

    /**
     * The button allowing user to slide range of past dates (2020)
     */
    @FXML
    private Group timelineBottomRangeSliderButton;


    /**
     * Contains rows of HBoxes (each HBox will contain circles and a line).
     */
    @FXML
    private VBox timelineYearContainerVBox;

    /**
     * When user selects a date point a line is used like a callout.
     */
    @FXML
    private Line selectedDatePointLine;

    /**
     * When user selects a date point text is shown like a callout.
     */
    @FXML
    private Text selectedDatePointText;

//    private Dialog<Pane>
    protected static final String FILTER_MENU_FXML_FILE = "filter-menu.fxml";
    private Pane filterMenuPopupContent;
    private FilterMenuController filterMenuController;

    private String configPath;
    private List<Integer> configModuleIds;

    @FXML
    private Button filterMenuButton;

    public FilterMenuController getFilterMenuController() {
        return filterMenuController;
    }

    @FXML
    void toggleDisplayConfig(ActionEvent event) {
        if (getViewProperties() != null && getMainConcept() != null) {

            // Populate filter menu
            Pane filterMenuPane = getFilterMenuPopupContent();
            getFilterMenuController().updateModel(getViewProperties(), pathMap);
            getFilterMenuController().updateView();
            PopOver filterMenuPopup = new PopOver(filterMenuPane);
            filterMenuPopup.getStyleClass().add("filter-menu-popup");

            BiConsumer<String, List<Integer>> pathModuleIdsConsumer = (pathName, moduleIds) -> {
                updateConfigPathAndModules(pathName, moduleIds);
                updateModel(getViewProperties(), getMainConcept());
                updateView();
                filterMenuPopup.hide();
            };
            // apply a callback
            filterMenuController.onSaveAction(pathModuleIdsConsumer);
            // Build a default filter menu
            //filterMenuPopup.getStyleClass().add("filterMenuPopup");

            filterMenuPopup.setContentNode(filterMenuPane);
            filterMenuPopup.setAutoHide(true);
            filterMenuPopup.setAutoFix(true);
            filterMenuPopup.setHideOnEscape(true);
            filterMenuPopup.setDetachable(true);
            filterMenuPopup.setDetached(false);
            filterMenuPopup.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
            filterMenuPopup.show(filterMenuButton);
        }
        LOG.info("toggleDisplayConfig() called");
    }

    /**
     * Range control and VBox containing most Timeline controls sit on this pane.
     */
    @FXML
    private AnchorPane timelineAnchorPane;

    /**
     * Used only for Scene builder this is set to be non-visible.
     */
    @FXML
    private ToggleButton prototypeCollapsibleButton;

    // Range Sliders
    private RangeSliderSupport topRangeSlider;
    private RangeSliderSupport bottomRangeSlider;

    //// housekeeping objects
    private ViewProperties viewProperties;
    private EntityFacade mainConcept;
    private final DateTimeFormatter DATE_POINT_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    /**
     * <pre>
     *     Paths:
     *
     *      ( ) Master
     *      ( ) Developmental
     *
     *      Extensions:
     *      [ ] Snomed CT International
     *      [ ] Snomed CT US
     *      [ ] Primordial
     * </pre>
     * path -> module -> year -> verChangeRecords
     * Map of path as a key, the value is a Map of module nid (key) and value is a map of year (key) and value is a set of all change chronologies.
     */
    private final TimelinePathMap pathMap = new TimelinePathMap();

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public EntityFacade getMainConcept() {
        return mainConcept;
    }

    @FXML
    void toggleRangeSliders(ActionEvent event) {
        boolean visible = rangeToggleButton.isSelected();
        timelineTopRangeSliderBody.setVisible(visible);
        timelineTopRangeSliderButton.setVisible(visible);
        timelineBottomRangeSliderBody.setVisible(visible);
        timelineBottomRangeSliderButton.setVisible(visible);
        if (visible) {
            Rectangle2D detectBox = currentRangeDetectBox();
            // Generate a bounding box
            rangeViewRectangleProp.set(detectBox);
            updateRangeSelectedDatePoints(detectBox);
            dataDump();
        } else {
            // Refresh the callers items.
            getDatePointsInDateRangeConsumer().accept(false, slideControlDatePointsInRangeSet);
        }
    }

    /**
     * Returns the current rectangle or box to detect date points within range.
     * @return A rectangle region
     */
    private Rectangle2D currentRangeDetectBox() {
        double topY  = topRangeSlider.currentRangeYValueProperty().get();
        double width = timelineSurfaceAnchorPane.getWidth();
        double height = bottomRangeSlider.currentRangeYValueProperty().doubleValue() - topY;
        // Generate a bounding box
        return new Rectangle2D(0, topY, width, height);
    }
    @FXML
    void selectDateOnTimeline(MouseEvent event) {
        datePointSelected.set((Circle) event.getSource());
    }
    public void hideDatePointCallout() {
        selectedDatePointText.setVisible(false);
        selectedDatePointLine.setVisible(false);
    }
    public void showDatePointCallout() {
        selectedDatePointText.setVisible(true);
        selectedDatePointLine.setVisible(true);
        selectedDatePointLine.toFront();
    }
    public void updateDatePointCallout(String dateStr, Circle datePoint) {

        // find x,y center left based on radius, (end point), start point will be about 20 px long.
        Bounds circleBounds = localToParent(datePoint, 4);
        Point2D endPt = new Point2D(circleBounds.getCenterX() - datePoint.getRadius() - 2, circleBounds.getMaxY() - datePoint.getRadius());
        //Point2D startPt = new Point2D(endPt.getX() - 20 - (25 * moduleIndex) , endPt.getY());

        selectedDatePointText.setText(dateStr);

        Bounds textBounds = textFontMetricsBounds(selectedDatePointText);
        selectedDatePointText.setTextOrigin(VPos.TOP);
        // Anchor pane is bound to the text on the left side
        //selectedDatePointText.setX(startPt.getX() - textBounds.getWidth() - 4);

        selectedDatePointText.setY(endPt.getY() - (textBounds.getHeight()/2));

        selectedDatePointLine.setStartX(textBounds.getMaxX() + 8);
        selectedDatePointLine.setStartY(endPt.getY());
        selectedDatePointLine.setEndX(endPt.getX());
        selectedDatePointLine.setEndY(endPt.getY());
    }
    @Override
    public void initialize() {
        // Clear out any prototype controls used in SceneBuilder
        hidePrototypeControls();

        // hide selected date point and line
        selectedDatePointText.setLayoutX(0);
        selectedDatePointText.setLayoutY(0);
        selectedDatePointLine.setLayoutX(0);
        selectedDatePointLine.setLayoutY(0);
        hideDatePointCallout();

        // Init date point collections (contains a quick lookup)
        initDatePointCollections();

        // Init visible buttons.
        toggleRangeSliders(null);

        // Bind range slider buttons
        bindRangeSliderControls();

        // Bind date point selection ability (user clicks on date points on the timeline - circle)
        initDatePointSelection();

    }

    /**
     * Creates a collection of all change points as JavaFX circles. Userdata contains the VersionChangeRecord.
     * Sorted set in descending order by date.
     */
    private void initDatePointCollections() {
        // ChangeCoordinates The selected items when range selector is active and items in view.
        slideControlDatePointsInRangeSet = observableSet(new HashSet<>()); // using nids so there is no sort order
        slideControlDatePointsInRangeSet.addListener((InvalidationListener) observable ->
            getDatePointsInDateRangeConsumer().accept(rangeToggleButton.isSelected(), slideControlDatePointsInRangeSet));

        // All Circle object of a Path
        allCircleDatePointsSet = observableSet(new HashSet<>());
    }

    /**
     * User mouse clicks on a date point on a timeline.
     * TODO refactor all style classes to be more type safe (not as strings).
     */
    private void initDatePointSelection() {
        datePointSelected.addListener((observableValue, prevCircle, newCircle) -> {
            if (prevCircle != null){
                prevCircle.getStyleClass().remove("selected");
            }
            if (newCircle != null) {
                ChangeCoordinate changeCoordinate = (ChangeCoordinate) newCircle.getUserData();
                VersionChangeRecord versionChangeRecord = changeCoordinate.versionChangeRecord();
                StampEntity<? extends StampEntityVersion> stampForChange = Entity.getStamp(versionChangeRecord.stampNid());
                String dateStr = DateTimeUtil.format(stampForChange.time(), DATE_POINT_FORMATTER);

                newCircle.getStyleClass().add("selected");
                updateDatePointCallout(dateStr, newCircle);
                showDatePointCallout();

                Platform.runLater(()-> {
                    if (getDatePointSelectedConsumer() != null) {
                        getDatePointSelectedConsumer().accept(changeCoordinate);
                    }
                });
            } else {
                hideDatePointCallout();
            }

            // debug selected
            debugSelectedDatePoint(newCircle);
        });
    }
    private void debugSelectedDatePoint(Circle selectedDatePoint) {
        if (selectedDatePoint == null || selectedDatePoint.getUserData() == null) return;

        ChangeCoordinate changeCoordinate = (ChangeCoordinate) selectedDatePoint.getUserData();
        StampEntity<? extends StampEntityVersion> stamp = Entity.getStamp(changeCoordinate.versionChangeRecord().stampNid());
        LOG.info("           Date Point: " + DateTimeUtil.format(stamp.time(), DATE_POINT_FORMATTER));
        LOG.info("   Top Range Slider Y: " + topRangeSlider.currentRangeYValueProperty().get());
        LOG.info("Bottom Range Slider Y: " + bottomRangeSlider.currentRangeYValueProperty().get());
        LOG.info("            Rectangle: " + rangeViewRectangleProp.get());
        LOG.info("Selected: Center Pt2d: " + FXUtils.centerPointRelativeToParent(selectedDatePoint, timelineSurfaceAnchorPane));
        if (rangeViewRectangleProp.isNotNull().get()) {
            Rectangle2D detectionBox = rangeViewRectangleProp.get();
            allCircleDatePointsSet.forEach((circle) -> {
                ChangeCoordinate findCC = (ChangeCoordinate) circle.getUserData();
                StampEntity<? extends StampEntityVersion> foundStamp = Entity.getStamp(findCC.versionChangeRecord().stampNid());
                String foundDate = DateTimeUtil.format(foundStamp.time(), DATE_POINT_FORMATTER);
                Point2D centerPt = FXUtils.centerPointRelativeToParent(circle, timelineSurfaceAnchorPane);
                if (detectionBox.contains(centerPt)) {
                    LOG.info("Found: " + centerPt + " with " + foundDate);
                } else {
                    LOG.atDebug().log("Not Found: " + centerPt + " with " + foundDate);
                }
            });
        }
    }
    private void updateRangeSelectedDatePoints(Rectangle2D detectBox) {
        slideControlDatePointsInRangeSet.clear();
        // TODO this could be expensive so we can use virtual threads
        // output selected circles.
        allCircleDatePointsSet.forEach((circle) -> {
            Point2D centerPt = FXUtils.centerPointRelativeToParent(circle, getTimelineAnchorPane());
            if (detectBox.contains(centerPt)) {
                slideControlDatePointsInRangeSet.add((ChangeCoordinate) circle.getUserData());
            }
        });
    }

    public AnchorPane getTimelineAnchorPane() {
        return timelineAnchorPane;
    }

    /**
     * Configures the timeline UI range slider controls.
     * <p>
     * Initializes the top and bottom range sliders, binds listeners to update the range view rectangle
     * and selected date points, and sets layout bindings and debugging callbacks.
     */
    private void bindRangeSliderControls() {
        // Initialize range sliders
        topRangeSlider = new RangeSliderSupport(timelineTopRangeSliderButton, timelineBottomRangeSliderButton);
        bottomRangeSlider = new RangeSliderSupport(timelineBottomRangeSliderButton, timelineTopRangeSliderButton);

        // Update range view rectangle based on slider Y-values
        BiConsumer<Number, Number> updateRangeRectangle = (topY, bottomY) -> {
            final double rectWidth = timelineSurfaceAnchorPane.getWidth();
            final double rectTopY = topY.doubleValue();
            final double rectHeight = bottomY.doubleValue() - rectTopY;

            if (rectWidth > 1 && rectHeight > 1) {
                rangeViewRectangleProp.set(new Rectangle2D(0, rectTopY, rectWidth, rectHeight));
            }
        };

        // Add range slider listeners
        topRangeSlider.currentRangeYValueProperty().addListener((observableValue, oldY, newY) ->
                updateRangeRectangle.accept(newY, bottomRangeSlider.currentRangeYValueProperty().get()));

        bottomRangeSlider.currentRangeYValueProperty().addListener((observableValue, oldY, newY) ->
                updateRangeRectangle.accept(topRangeSlider.currentRangeYValueProperty().get(), newY));

        // Debugging callbacks on mouse release
        topRangeSlider.onMouseReleased((mouseEvent -> dataDump()));
        bottomRangeSlider.onMouseReleased((mouseEvent -> dataDump()));

        // Message listeners about what date points were selected.
        rangeViewRectangleProp.addListener((o, prevRect, newRect) -> {
            updateRangeSelectedDatePoints(newRect); // updates the slideRangeSelectedDatePointsSet
        });

        // Bind slider range body to buttons
        timelineTopRangeSliderBody.prefHeightProperty().bind(timelineTopRangeSliderButton.layoutYProperty().add(8)); // midpoint of the button

        // height = height of parent (anchor pane) - y layout of button
        timelineBottomRangeSliderBody.prefHeightProperty().bind(timelineSurfaceAnchorPane.heightProperty()
                .subtract(timelineBottomRangeSliderButton.layoutYProperty())
                .subtract(8)); // mid-point of the button

        // Generate an initial bounding box
        updateRangeRectangle.accept(topRangeSlider.currentRangeYValueProperty().get(), bottomRangeSlider.currentRangeYValueProperty().get());
    }

    /**
     * When prototyping controls or shapes in Scene Builder we clear them during runtime.
     */
    private void hidePrototypeControls() {
        timelineAnchorPane.getChildren().remove(prototypeCollapsibleButton);
        timelineYearContainerVBox.getChildren().clear();
    }

    /**
     * Clears up pathMap and other ui fields.
     * This does not clear the Config info such as configPath and configModuleIds.
     */
    @Override
    public void clearView() {
        // clean up global storage of initial values.
        cleanup();

        // Clear VBox of rows. TODO Make sure to avoid memory leaks.
        timelineYearContainerVBox.getChildren().clear();
        hideDatePointCallout();
        selectedDatePointText.setText("");
    }

    /**
     * Cleanup pathMap and other global variables.
     * This does not clear the Config info such as configPath and configModuleIds.
     */
    @Override
    public void cleanup() {
        pathMap.clear();
        allCircleDatePointsSet.clear();
        slideControlDatePointsInRangeSet.clear();
        datePointSelected.set(null);
    }
    public void updateConfigPathAndModules(String configPath, List<Integer> configModuleIds) {
        this.configPath = configPath;
        this.configModuleIds = configModuleIds;
    }
    public void updateModel(ViewProperties viewProperties, EntityFacade mainConcept) {
        this.viewProperties = viewProperties;
        this.mainConcept = mainConcept;
        clearView();
        buildAllTimeLineMaps();
        if (configPath == null || configModuleIds == null) {
            resetConfigPathAndModules();
            if (pathMap.keySet().size() > 0){
                String configPath = pathMap.keySet().stream().findFirst().get();
                List<Integer> configModuleIds = pathMap.getModuleNids(configPath);
                updateConfigPathAndModules(configPath, configModuleIds);
            }
        }
    }
    public void resetConfigPathAndModules() {
        configPath = null;
        configModuleIds = null;
    }

    public Pane getFilterMenuPopupContent() {
        // Build a default filter menu
        if (filterMenuPopupContent == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FILTER_MENU_FXML_FILE));
            try {
                filterMenuPopupContent = loader.load();
                filterMenuController = loader.getController();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return filterMenuPopupContent;
    }

    /**
     * Refreshes view and populates controls with data.
     */
    public void updateView() {
        if (getViewProperties() == null || getMainConcept() == null) return;

        // Draw timelines as expanded
        drawAllTimelines(configPath, configModuleIds);

        // update to show only date points in range
        updateRangeSelectedDatePoints(currentRangeDetectBox());

        // debug data
        dataDump();
    }

    /**
     * <code>
     *   TreeMap<String, TreeMap<String, TreeMap<Integer, TreeSet<VersionChangeRecord>>>> pathMap = new TreeMap<>();
     * </code>
     *  path -> module -> year -> verChangeRecords
     * Creates a master treemap structure
     *
     */
    private void buildAllTimeLineMaps(){
        // Populate concept versions
        buildTimelineMap(getMainConcept().nid());
        // View Calculator
        ViewCalculator viewCalculator = getViewProperties().calculator();

        // Populate all description semantics of this entity.
        var descrSemanticEntities = viewCalculator.getDescriptionsForComponent(getMainConcept().nid());
        // For each populate a row based on changes
        descrSemanticEntities.forEach(semanticEntity -> buildTimelineMap(semanticEntity.nid()));

        // TODO Axioms (displaying things correctly?)
        // Inferred Axioms
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(getMainConcept().nid());
        inferredSemanticVersion.ifPresent(semanticEntityVersion -> buildTimelineMap(semanticEntityVersion.nid()));

        // Stated Axioms
        Latest<SemanticEntityVersion> statedSemanticVersion = viewCalculator.getStatedAxiomSemanticForEntity(getMainConcept().nid());
        statedSemanticVersion.ifPresent(semanticEntityVersion -> buildTimelineMap(semanticEntityVersion.nid()));

        // show all data for timeline
        dataDump();
    }

    /**
     * <code>
     *   TreeMap<String, TreeMap<String, TreeMap<Integer, TreeSet<VersionChangeRecord>>>> pathMap = new TreeMap<>();
     * </code>
     *  path -> module -> year -> verChangeRecords
     * Creates a master treemap structure
     * @param nid The main concept entity. The map will contain paths, each path contains modules, each module contains years, each year contains a change chronology or date point.
     */
    private void buildTimelineMap(int nid) {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        ChangeChronology changeChronology = viewCalculator.changeChronology(nid);
        Comparator<VersionChangeRecord> comparator = (o1, o2) -> viewCalculator.stampCalculator().comparePositions(o1.stampNid(), o2.stampNid());

        for (VersionChangeRecord changeRecord:changeChronology.changeRecords()) {
            StampEntity<? extends StampEntityVersion> stampForChange = Entity.getStamp(changeRecord.stampNid());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(stampForChange.time());
            int moduleNid = stampForChange.moduleNid();
            String pathName = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampForChange.pathNid());
            int mYear = calendar.get(Calendar.YEAR);

            if (!pathMap.containsKey(pathName)) {
                TreeMap<Integer, TreeMap<Integer, TreeSet<VersionChangeRecord>>> moduleMap = new TreeMap<>();
                this.pathMap.put(pathName, moduleMap);
            }
            if (!pathMap.get(pathName).containsKey(moduleNid)) {
                // add a comparator
                TreeMap<Integer, TreeSet<VersionChangeRecord>> yearMap = new TreeMap<>();
                pathMap.get(pathName).put(moduleNid, yearMap);
            }
            if (!pathMap.get(pathName).get(moduleNid).containsKey(mYear)) {
                // add a comparator
                TreeSet<VersionChangeRecord> changeRecords = new TreeSet<>(comparator);
                pathMap.get(pathName).get(moduleNid).put(mYear, changeRecords);
            }
            pathMap.get(pathName).get(moduleNid).get(mYear).add(changeRecord);
        }
    }

    private long truncateTime(long timeMillis) {
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date(timeMillis));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    private int parseYear(long timeMillis) {
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date(timeMillis));
        return cal.get(Calendar.YEAR);
    }
    /**
     * Latest date is first.
     * 1. unique points IMPORTANT TO KNOW: Algorithm is only using date precision.
     * 2. positions of points order
     * Query all dates of all extensions distinct number of
     * @param pathSelected - A selected path to traverse
     * @param modulesSelected selected modules (extensions).
     * @return A List of ordered long unique values representing truncated date milliseconds values (epoch).
     */
    private TreeSet<Long> determineNumOfUniqueDatePoints(String pathSelected, List<Integer> modulesSelected){

        // A map of epoch millis (truncated time precision) value of boolean denotes true when a change has a date point,
        // otherwise all extensions don't have changes. We need to create a space to denote nothing happened that year.
        TreeSet<Long> datePoints = new TreeSet<>();
        TreeSet<Integer> availableYears = new TreeSet<>();

        for (Integer moduleId : modulesSelected) {
            if (pathMap.get(pathSelected).get(moduleId) == null) {
                String errorMsg = "Path: %s and Module: %s not found. Module id = %s".formatted(pathSelected,
                        getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleId),
                        moduleId);
                throw new RuntimeException(errorMsg);
            }

            pathMap.get(pathSelected)
                    .get(moduleId)
                    .values().forEach(versionChangeRecords -> versionChangeRecords.forEach(versionChangeRecord -> {
                        StampEntity<? extends StampEntityVersion> stampForChange = Entity.getStamp(versionChangeRecord.stampNid());
                        long time = truncateTime(stampForChange.time());
                        datePoints.add(time);
                        availableYears.add(parseYear(time));
                    }));
        }

        int firstYear = parseYear(datePoints.first());
        int endYear = parseYear(datePoints.last());

        int nowYear = parseYear(System.currentTimeMillis());
        if (endYear > nowYear) {
            endYear = nowYear;
        }
        if (firstYear > 1970 && endYear > firstYear) {
            LOG.info("====> IntStream.range(%s, %s);".formatted(firstYear, endYear));
            IntStream yearRange = IntStream.range(firstYear, endYear);
            Set<Integer> allYears = yearRange.boxed().collect(Collectors.toSet());
            // Fill in missing years for extensions (modules) not having date points.
            TreeSet<Integer> missingYears = new TreeSet<>(allYears.stream().filter(year -> !availableYears.contains(year)).toList());
            missingYears.forEach(year -> {
                String dateInString = year + "0101";
                LocalDate localDate = LocalDate.parse(dateInString, DateTimeFormatter.BASIC_ISO_DATE);
                Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                long timeInMillis = truncateTime(instant.toEpochMilli());
                datePoints.add(timeInMillis);
            });
        }

        return datePoints;
    }
    private void drawAllTimelines(String pathSelected, List<Integer> modulesSelected) {
        if (pathSelected == null || modulesSelected == null || modulesSelected.size() ==0) return;
        ViewCalculator viewCalculator = getViewProperties().calculator();
        TreeSet<Long> uniqueDatePoints = determineNumOfUniqueDatePoints(pathSelected, modulesSelected);
        List<Long> dateYearList = uniqueDatePoints.descendingSet().stream().toList();

        int numOfUniqueDatePoints = uniqueDatePoints.size();
        int startY = 50;
        int padding = 4;
        int endSpace = 10;
        int circleHeight = 12; // one circle approx 12 pixels
        int timelineSegmentLength = 3 * circleHeight; // one circle will have space on the timeline.
        int wholeLineLength = numOfUniqueDatePoints * (circleHeight + padding) + endSpace + endSpace;

        final EventHandler<MouseEvent> mouseClick = this::selectDateOnTimeline;

        // HBox each cell contains a timeline.
        HBox timelineColumns = new HBox();
        timelineColumns.getStyleClass().add("timeline-row-container");
        String [] colors = new String[]{ "first", "second", "third"};
        timelineYearContainerVBox.getChildren().add(timelineColumns);
        // Grouping of shapes for the timeline control
        // Line body - length will increase according to how many points in the year.
        // add end points
        // Add line, end, start points
        // each years worth of changes
        IntStream.range(0, modulesSelected.size()).forEachOrdered(i -> {
            Group timelineGroup = new Group(); // one timeline for all years represented
            timelineColumns.getChildren().add(timelineGroup);
            int indexColumn = pathMap.getModuleNids(pathSelected).indexOf(modulesSelected.get(i));
            String columnColor = indexColumn > colors.length - 1 ? colors[0] : colors[indexColumn];
            Line timeLine = new Line(0, startY, 0, startY + wholeLineLength); //
            timeLine.getStyleClass().addAll("timeline-year-line", columnColor);
            Circle endDatePoint = new Circle(0, startY + padding, 4);
            endDatePoint.getStyleClass().addAll("timeline-year-end-point", columnColor);
            Circle startDatePoint = new Circle(0, startY + wholeLineLength - padding, 4);
            startDatePoint.getStyleClass().addAll("timeline-year-end-point", columnColor);
            timelineGroup.getChildren().addAll(timeLine, startDatePoint, endDatePoint);

            LOG.info("Path: %s Module: %s%n".formatted(pathSelected,
                    viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(modulesSelected.get(i))));
            final int moduleNid = modulesSelected.get(i);
            pathMap.get(pathSelected)
                    .get(modulesSelected.get(i))
                    .forEach((year, changeSet) -> {
                        // TODO when a date is the same on another module we need them to align across all points.
                        int length = changeSet.size() * timelineSegmentLength;
                        timeLine.endYProperty().add(length);
                        VersionChangeRecord[] versionChangeRecords = changeSet.toArray(new VersionChangeRecord[0]);
                        for (VersionChangeRecord change : versionChangeRecords) {
                            StampEntity<StampEntityVersion> stampForChange = Entity.getStamp(change.stampNid());
                            String dateStr = ("   Date: " + DateTimeUtil.format(stampForChange.time(), DATE_POINT_FORMATTER));
                            LOG.info(dateStr + " nid of changeChron -> " + change.stampNid());
                            Circle datePoint = new Circle();
                            datePoint.setUserData(new ChangeCoordinate(pathSelected, moduleNid, change));
                            datePoint.setRadius(6);

                            // start + endSpace + endSpace + ((circleHeight + padding ) * find index (date)
                            datePoint.setCenterY(startY + endSpace + endSpace + ((circleHeight + padding) * dateYearList.indexOf(truncateTime(stampForChange.time()))));
                            datePoint.getStyleClass().addAll("timeline-year-date-point", columnColor);
                            timelineGroup.getChildren().add(datePoint);
                            datePoint.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClick);

                            // add to the main Set of Circle's as a lookup.
                            allCircleDatePointsSet.add(datePoint);
                        }
                    });
        });
        }

        public void dataDump() {
            /*
             * <code>
             *   TreeMap<String, TreeMap<String, TreeMap<Integer, TreeSet<VersionChangeRecord>>>> pathMap = new TreeMap<>();
             * </code>
             *  path -> module -> year -> verChangeRecords
             */
            // give me a path and modules of all changes for all years.
            if (getViewProperties() == null) {
                LOG.info("No Concept has been selected.");
                return;
            }
            ViewCalculator viewCalculator = getViewProperties().calculator();

            pathMap.forEach((path, moduleTree) -> {
                LOG.info("Path: " + path);
                moduleTree.forEach((module, yearTree) -> {
                    String moduleName = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(module);
                    LOG.info(" Module: " + moduleName);
                    yearTree.descendingMap().forEach((year, listChanges) -> {
                        LOG.info("  Year: " + year);
                        for (VersionChangeRecord change : listChanges.descendingSet()) {
                            StampEntity<StampEntityVersion> stampForChange = Entity.getStamp(change.stampNid());
                            String dataStr = "   Date: " + DateTimeUtil.format(stampForChange.time(), DATE_POINT_FORMATTER);
                            LOG.info(dataStr + " nid of changeChron -> " + change.stampNid());
                        }
                    });
                });
            });

            // Show viewable rectangular region
            LOG.info("Detect box (Range): " + rangeViewRectangleProp.get());

            // show all change records in range (view port)
            LOG.info("Date Points in Range [%s]".formatted(rangeToggleButton.isSelected() ? "On" : "Off"));
            slideControlDatePointsInRangeSet.forEach(changeCoordinate -> {
                StampEntity<StampEntityVersion> stampForChange = Entity.getStamp(changeCoordinate.versionChangeRecord().stampNid());
                LOG.info(" Date Point: %s Path: %s Module: %s %n".formatted(
                        DateTimeUtil.format(stampForChange.time(), DATE_POINT_FORMATTER),
                        changeCoordinate.pathName(),
                        viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(changeCoordinate.moduleNid())));
            });

            // Show selected Date Point
            if (datePointSelected.isNull().get()) {
                LOG.info(" No Date Point selected.");
            } else {
                ChangeCoordinate changeCoordinate = (ChangeCoordinate) datePointSelected.get().getUserData();
                StampEntity<StampEntityVersion> stampForChange = Entity.getStamp(changeCoordinate.versionChangeRecord().stampNid());
                LOG.info(" Date Point selected: Path: %s Module: %s Date Point: %s".formatted(
                        changeCoordinate.pathName(),
                        viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(changeCoordinate.moduleNid()),
                        DateTimeUtil.format(stampForChange.time(), DATE_POINT_FORMATTER)));
            }
        }

    public Consumer<ChangeCoordinate> getDatePointSelectedConsumer() {
        return datePointSelectedConsumer;
    }

    public BiConsumer<Boolean, Set<ChangeCoordinate>> getDatePointsInDateRangeConsumer() {
        if (datePointSelectedConsumer == null) {
            return (rangeOn, changeCoordinates) -> { }; // empty consumer
        }
        return datePointsInDateRangeConsumer;
    }

    public void onDatePointSelected(Consumer<ChangeCoordinate> datePointSelectedConsumer) {
        this.datePointSelectedConsumer = datePointSelectedConsumer;
    }
    public void onDatePointInRange(BiConsumer<Boolean, Set<ChangeCoordinate>> datePointsInDateRangeConsumer) {
        this.datePointsInDateRangeConsumer = datePointsInDateRangeConsumer;
    }

}
