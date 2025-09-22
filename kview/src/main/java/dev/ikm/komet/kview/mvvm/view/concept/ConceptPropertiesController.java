package dev.ikm.komet.kview.mvvm.view.concept;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.common.StampFormController;
import dev.ikm.komet.kview.mvvm.view.properties.*;
import dev.ikm.komet.kview.mvvm.viewmodel.*;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext.*;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampAddSubmitFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampCreateFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.carlfx.cognitive.loader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static dev.ikm.komet.kview.fxutils.CssHelper.genText;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_STAMP_VALUES_THE_SAME_OR_EMPTY;

public class ConceptPropertiesController {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptPropertiesController.class);

    public static final String CONCEPT_PROPERTIES_FXML_FILE = "concept-properties.fxml";

    // --- add / edit tab ---


    // --- history tab ---

    // --- hierarchy tab ---
    protected static final String HIERARCHY_VIEW_FXML_FILE = "hierarchy-view.fxml";
    // --- description tab ---
    protected static final String EDIT_DESCRIPTIONS_FXML_FILE = "edit-descriptions.fxml";


    // --- Properties Tab / Header ---
    @FXML private FlowPane propertiesTabsPane;
    @FXML private ToggleGroup headerTabToggleButtonGroup; // TODO: rename in FXML
    @FXML private ToggleButton editButton;
    @FXML private ToggleButton historyButton;
    @FXML private ToggleButton hierarchyButton;
    @FXML private ToggleButton commentsButton;

    // --- main view ---
    @FXML private BorderPane contentBorderPane;

    // <ViewModels> All needed state is hold in subsequent ViewModels - DO NOT hold state in this controller
    @InjectViewModel
    ConceptViewModelNext conceptViewModelNext;


    // TODO : why did you do that guys :(
    private StampAddSubmitFormViewModel stampAddSubmitFormViewModel;
    private StampCreateFormViewModel stampCreateFormViewModel;

    private DescrNameViewModelNext descrNameViewModelNext;

    // </ViewModels>

    // <JFXNodes>  child JFXNode's created via MVVMLoader in this controller

    // 0 Stamp
    private JFXNode<Pane, StampFormController> stampFormJFXNode;

    // 1 Add/Edit

    private JFXNode<Pane, ConceptPropertiesMenuController> menuJFXNode;

    private JFXNode<Pane, ConceptPropertiesNameMenuController> nameMenuJFXNode;

    //  fqn / otherName add/edit
    private JFXNode<Pane, ConceptPropertiesNameFormController> nameFormJFXNode;


    // 2 History
    private Pane historyTabsBorderPane;
    private HistoryChangeController historyChangeController;

    // 3 Hierarchy
    private Pane hierarchyTabBorderPane;
    private HierarchyController hierarchyController;

    // 4 Comments
    private Pane commentsPane = new StackPane(genText("Comments Pane")); // TODO: nice missing stuff ...

    // </JFXNodes>


    public ConceptPropertiesController() {
        // we get the Concent_Topic state via ConceptViewModel

        // apperently the StampAddFormViewModel and the StampCreateFormViewModel
    }

    @FXML
    public void initialize() {

        createAllNodes();

        setupStampFormBindings();

        // <Bindings> -------------------------------------------------------------------------------------------------

        // bind tab header

        // bind stampForm

        // bind nameForm

        // bind history

        // bind hirarchy

        // general propertiesController logic


        //  -- MainPain --
        SimpleObjectProperty<SelectedPropertyWindowKind> windowToDisplay = conceptViewModelNext.getProperty(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND);

        // mainPaine <-- ViewModel

        // TODO:  open respective windows - this sets up the correct values for the property ViewModels
        windowToDisplay.subscribe((windowKind) -> {
                    if (windowKind != null) {
                        switch (windowKind) {
                            case STAMP -> {

                                EntityFacade thisConceptFacade = conceptViewModelNext.getValue((ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE));
                                UUID conceptTopic = conceptViewModelNext.getValue(ConceptPropertyKeys.THIS_UNIQUE_CONCEPT_TOPIC);
                                ViewProperties viewProperties = conceptViewModelNext.getViewProperties();

                                boolean isValidStamp = conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.HAS_VALID_STAMP);
                                boolean isNewConcept = conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT);

                                if (isNewConcept) {
                                    if (!isValidStamp) {
                                        stampFormJFXNode.controller().init(stampCreateFormViewModel);
                                        conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.CURRENT_STAMP_FORM, stampCreateFormViewModel);

                                    }  // As long as newConcept we don't need to reiniate the stamp form
                                } else {
                                    stampFormJFXNode.controller().init(stampAddSubmitFormViewModel);
                                    conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.CURRENT_STAMP_FORM, stampAddSubmitFormViewModel);
                                }

                                // NOTICE: call update after setting the controller above so we init the "right" ViewModel
                                stampFormJFXNode.controller().getStampFormViewModel().update(thisConceptFacade, conceptTopic,viewProperties);

                                contentBorderPane.setCenter(stampFormJFXNode.node());

                                untoggleAllTabButtons();

                            }
                            case MENU -> {
                                LOG.info("MENU: ");
                                editButton.setSelected(true);
                                contentBorderPane.setCenter(menuJFXNode.node());
                            }
                            case NAME_MENU -> {
                                LOG.info("NAME_MENU: ");
                                editButton.setSelected(true);
                                contentBorderPane.setCenter(nameMenuJFXNode.node());

                                // updateDescViewModel save() / restore() cycle
                            }
                            case NAME_FORM -> {

                                LOG.info("NAME_FORM: ");
                                SimpleBooleanProperty hasStamp =  conceptViewModelNext.getProperty(ConceptPropertyKeys.HAS_VALID_STAMP);

                                // either we edit a already existing semantic -> work on semantic PublicID
                                // we creating a new semantic -> work on a DescName basis ?
                                // NO! we always work on nid f that

                                // in other words we always update the DescNameViewModelNext to either have a) a sémantic PublicID b) a parent Concept Nid
                                // this is a invariants we can assert

                                EntityFacade conceptEntity =  conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE);
                                boolean isNewConcept = conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT);

                                SelectedDescriptionSemantic semanticNameDescr = conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.SELECTED_DESCRIPTION_SEMANTIC);
                                boolean createNewSemantic = semanticNameDescr == null;

                                if (createNewSemantic) {
                                    LOG.info("create new semantic: ");
                                    this.descrNameViewModelNext.createNewSemantic(conceptViewModelNext.getViewProperties());
                                } else {
                                    if (semanticNameDescr instanceof SemanticPublicId(PublicId id)) {
                                        LOG.info("update semantic with public id: ");
                                        this.descrNameViewModelNext.updateExistingSemantic(id, conceptViewModelNext.getViewProperties());
                                    } else if (semanticNameDescr instanceof UncommittedSemanticNameDescr(
                                            DescrName value
                                    )) {
                                        LOG.info("update semantic with descrname: ");
                                        this.descrNameViewModelNext.updateNonCommitedSemantic(value, conceptViewModelNext.getViewProperties());
                                    }
                                }
                                editButton.setSelected(true);
                                contentBorderPane.setCenter(nameFormJFXNode.node());

                            }

                            case HISTORY -> {
                                historyButton.setSelected(true);
                                contentBorderPane.setCenter(historyTabsBorderPane);
                            }
                            case HIERARCHY -> {
                                hierarchyButton.setSelected(true);
                                contentBorderPane.setCenter(hierarchyTabBorderPane);
                            }
                            case COMMENTS -> {
                                commentsButton.setSelected(true);
                                contentBorderPane.setCenter(commentsPane);
                            }
                            case NONE -> {
                                untoggleAllTabButtons();
                                contentBorderPane.setCenter(null);
                            }
                        }
                    } else {
                        untoggleAllTabButtons();
                        contentBorderPane.setCenter(null);
                        // TODO: do nothing right?
                    }
        });

        // TODO: do we need contentCenterPane -> windowToDisplay direction ?

        // -- HeaderTab --
        // TODO: should already be wired up in the FXML
        //headerTabToggleButtonGroup.getToggles().addAll(editButton, historyButton, hierarchyButton); // TODO: comments
        // headerTab --> ViewModel
        headerTabToggleButtonGroup.selectedToggleProperty().subscribe((newToggle) -> {

            if (editButton.equals(newToggle)) conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND,SelectedPropertyWindowKind.MENU);
            else if (hierarchyButton.equals(newToggle)) conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND,SelectedPropertyWindowKind.HIERARCHY);
            else if (historyButton.equals(newToggle)) conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND,SelectedPropertyWindowKind.HISTORY);
            else if (commentsButton.equals(newToggle)) conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND,SelectedPropertyWindowKind.COMMENTS);
        });


        // TODO: we can/have to remove the onAction handlers in properties-fxml as we have the toggle group now

        // </Bindings>



    }

    private void untoggleAllTabButtons() {
        editButton.setSelected(false);
        hierarchyButton.setSelected(false);
        historyButton.setSelected(false);
        commentsButton.setSelected(false);
    }

    private void createAllNodes() {
        // <JFX Nodes>

        // 0 Stamp

        // does not hold state at this point, e.g it will only get state when something that implements StampFormViewModelBase is
        // provided in the StampController on init() call
        Config stampConfig = new Config(StampFormController.class.getResource(StampFormController.STAMP_FORM_FXML_FILE));
        stampFormJFXNode = FXMLMvvmLoader.make(stampConfig);

        // one of this viewModels will be put into the Node's controller above depending on the creation state of the conceptViewModel
        this.stampAddSubmitFormViewModel = new StampAddSubmitFormViewModel(StampFormViewModelBase.Type.CONCEPT);
        this.stampCreateFormViewModel = new StampCreateFormViewModel(StampFormViewModelBase.Type.CONCEPT);

        // 1 Add / Edit Tab

        // --- menu // TODO: no its not one is description + axiom | edit fqn + add otherName
        Config menuConfig = new Config(ConceptPropertiesMenuController.class.getResource(ConceptPropertiesMenuController.EDIT_MENU_FXML_FILE))
                .addNamedViewModel(new NamedVm("conceptViewModelNext", conceptViewModelNext));
        this.menuJFXNode = FXMLMvvmLoader.make(menuConfig);

        // --- name menu
        Config editDescriptionConfig = new Config(ConceptPropertiesNameMenuController.class.getResource(ConceptPropertiesNameMenuController.EDIT_MENU_FXML_FILE))
                .addNamedViewModel(new NamedVm("conceptViewModelNext", conceptViewModelNext));
        this.nameMenuJFXNode = FXMLMvvmLoader.make(editDescriptionConfig);


        // --- nameForm to Add/Edit FQN or otherName
        // TODO: maybe the same update trick to the viewModel / Controller as in stamp to the descViewModelNext -> it needs
        DescrNameViewModelNext descrNameViewModelNext = new DescrNameViewModelNext(conceptViewModelNext.getViewProperties());

        Config nameConfig = new Config(ConceptPropertiesNameFormController.class.getResource(ConceptPropertiesNameFormController.CONCEPT_PROP_NAMES_FXML_FILE));
        nameConfig.addNamedViewModel(new NamedVm("descrNameViewModelNext", descrNameViewModelNext));
        nameConfig.addNamedViewModel(new NamedVm("conceptViewModelNext", conceptViewModelNext));
//        Config nameConfig = new Config(
//                ConceptPropertiesNameFormController.class.getResource(ConceptPropertiesNameFormController.CONCEPT_PROP_NAMES_FXML_FILE),
//                new NamedVm("descrNameViewModelNext", descrNameViewModelNext))
//                .addNamedViewModel(new NamedVm("conceptViewModelNext", conceptViewModelNext));
        this.nameFormJFXNode = FXMLMvvmLoader.make(nameConfig);


        this.descrNameViewModelNext = descrNameViewModelNext;


        // 2 History Tab TODO: update to MVVM
        FXMLLoader loader = new FXMLLoader( HistoryChangeController.class.getResource(HistoryChangeController.HISTORY_CHANGE_FXML_FILE));
        try { historyTabsBorderPane = loader.load();} catch (IOException e) {
            throw new RuntimeException(e);
        }

        historyChangeController = loader.getController();

        // 3 Hierarchy TabTODO: update to MVVM
        FXMLLoader loader2 = new FXMLLoader(HierarchyController.class.getResource(HIERARCHY_VIEW_FXML_FILE));
        try { hierarchyTabBorderPane = loader2.load(); } catch (IOException e) {
            throw new RuntimeException(e);
        }
        hierarchyController = loader2.getController();

        // 4 Comments  Tab TODO: implement :(

        // </JFX Nodes>
    }

    private void setupStampFormBindings() {
        SimpleBooleanProperty confirmedPressedProperty = this.stampCreateFormViewModel.getProperty(IS_CONFIRMED_OR_SUBMITTED);
        confirmedPressedProperty.subscribe( (wasPressed, isPressed) -> {
           if (isPressed)  {
               LOG.info("Confirmed pressed on stampCreate Form");
               boolean isStampSame = stampCreateFormViewModel.getPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY);
               if (!isStampSame) {
                   conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.HAS_VALID_STAMP, true);
                   conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NONE);
                   confirmedPressedProperty.setValue(false);
               }
           }
        });

        SimpleBooleanProperty submittedPressedProperty = this.stampAddSubmitFormViewModel.getProperty(IS_CONFIRMED_OR_SUBMITTED);
        submittedPressedProperty.subscribe(isSubmitted -> {
            if (isSubmitted) {
                LOG.info("STAMP submit form: sbmit button pressed");
                conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.HAS_VALID_STAMP, true); // TODO is this correct?
            }
        });

    }

    public HistoryChangeController getHistoryChangeController() {
        return historyChangeController;
    }

    public HierarchyController getHierarchyController() {
        return hierarchyController;
    }


    public StampFormViewModelBase getStampFormViewModel() {
        return conceptViewModelNext.getPropertyValue(ConceptPropertyKeys.CURRENT_STAMP_FORM);
    }

    /**
     * Returns the propertiesTabsPane to be used as a draggable region.
     * @return The FlowPane containing the property tabs
     */
    public FlowPane getPropertiesTabsPane() {
        return propertiesTabsPane;
    }
}
