package dev.ikm.komet.layout_engine.component.window;

import dev.ikm.komet.layout.*;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.PreferenceProperty;
import dev.ikm.komet.layout.preferences.PreferencePropertyDouble;
import dev.ikm.komet.layout.window.KlRenderView;
import dev.ikm.komet.layout_engine.blueprint.StateAndContextBlueprint;
import dev.ikm.komet.layout_engine.layout.DefaultRenderLayout;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.Timer;
import java.util.TimerTask;

import static dev.ikm.komet.layout.window.KlRenderView.PreferenceKeys.*;
import static dev.ikm.komet.layout.window.KlRenderView.PreferenceKeys.ROTATE;
import static dev.ikm.komet.layout.window.KlRenderView.PreferenceKeys.SCALE_X;
import static dev.ikm.komet.layout.window.KlRenderView.PreferenceKeys.SCALE_Y;
import static dev.ikm.komet.layout.window.KlRenderView.PreferenceKeys.SCALE_Z;

public final class RenderView
        extends StateAndContextBlueprint<Scene> implements KlRenderView, EventHandler<KeyEvent>
{

    //TODO: can we use the widget properties directly instead of creating our own here?
    private final PreferencePropertyDouble translateX = PreferenceProperty.doubleProp(this, TRANSLATE_X);
    private final PreferencePropertyDouble translateY = PreferenceProperty.doubleProp(this, TRANSLATE_Y);
    private final PreferencePropertyDouble translateZ = PreferenceProperty.doubleProp(this, TRANSLATE_Z);
    private final PreferencePropertyDouble scaleX = PreferenceProperty.doubleProp(this, SCALE_X);
    private final PreferencePropertyDouble scaleY = PreferenceProperty.doubleProp(this, SCALE_Y);
    private final PreferencePropertyDouble scaleZ = PreferenceProperty.doubleProp(this, SCALE_Z);
    private final PreferencePropertyDouble rotate = PreferenceProperty.doubleProp(this, ROTATE);

    private KlArea<?> rootArea;
    private KnowledgeLayout masterLayout;

    {
        subscribeToChanges();
        restoreFromPreferencesOrDefaults();
    }
    private RenderView(KometPreferences preferences) {
        super(preferences, new Scene(new Label("Render View Blueprint restored")));
    }

    private RenderView(KlPreferencesFactory preferencesFactory, KlRenderView.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new Scene(new Label("Render View Blueprint")));
    }

    @Override
    public <FX extends Region> void setKlRootArea(KlArea<FX> rootArea) {
        this.rootArea = rootArea;
        this.fxObject().setRoot(rootArea.fxObject());
        this.fxObject().addEventHandler(KeyEvent.KEY_RELEASED, this);
        this.masterLayout = new DefaultRenderLayout(this.klObjectId(), this.rootArea.getLayoutOverrides());
        if (KlScopedEvent.isAltDown()) {
            new ScenicViewProvider().accept(this.fxObject());
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() ->
                            fxObject().removeEventHandler(KeyEvent.KEY_RELEASED, RenderView.this));
                }
            }, 5 * 1000);
        }
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.ALT) {
            fxObject().removeEventHandler(KeyEvent.KEY_RELEASED, this);
            Platform.runLater(() -> new ScenicViewProvider().accept(this.fxObject()));
        }
    }

    @Override
    public <FX extends Region> KlArea<FX> getKlRootArea() {
        return (KlArea<FX>) this.rootArea;
    }

    @Override
    public KlTopView topView() {
        Window window = this.fxObject().getWindow();
        return (KlTopView) KlPeerable.getKlPeer(window);
    }

    @Override
    public KnowledgeLayout getMasterLayout() {
        return this.masterLayout;
    }

    public void restoreFromPreferencesOrDefaults() {
        KlView.LOG.info("Restoring from preferences or defaults for {}", this.getClass().getSimpleName());
        for (KlRenderView.PreferenceKeys key : KlRenderView.PreferenceKeys.values()) {
            switch (key) {
                case TRANSLATE_X -> translateX.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case TRANSLATE_Y -> translateY.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case TRANSLATE_Z -> translateZ.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case SCALE_X -> scaleX.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case SCALE_Y -> scaleY.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case SCALE_Z -> scaleZ.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case ROTATE -> rotate.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
            }
        }
    }
    private void subscribeToChanges() {
        for (KlRenderView.PreferenceKeys key : KlRenderView.PreferenceKeys.values()) {
            if (fxObject().getCamera() != null) {
                addPreferenceSubscription(switch (key)  {
                    case TRANSLATE_X -> translateX.subscribe(num -> fxObject().getCamera().translateXProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().translateXProperty().subscribe(num -> translateX.setValue(num.doubleValue())));
                    case TRANSLATE_Y -> translateY.subscribe(num -> fxObject().getCamera().translateYProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().translateYProperty().subscribe(num -> translateY.setValue(num.doubleValue())));
                    case TRANSLATE_Z -> translateZ.subscribe(num -> fxObject().getCamera().translateZProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().translateZProperty().subscribe(num -> translateZ.setValue(num.doubleValue())));
                    case SCALE_X -> scaleX.subscribe(num -> fxObject().getCamera().scaleXProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().scaleXProperty().subscribe(num -> scaleX.setValue(num.doubleValue())));
                    case SCALE_Y -> scaleY.subscribe(num -> fxObject().getCamera().scaleYProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().scaleYProperty().subscribe(num -> scaleY.setValue(num.doubleValue())));
                    case SCALE_Z -> scaleZ.subscribe(num -> fxObject().getCamera().scaleZProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().scaleZProperty().subscribe(num -> scaleZ.setValue(num.doubleValue())));
                    case ROTATE -> rotate.subscribe(num -> fxObject().getCamera().rotateProperty().set(num.doubleValue()))
                            .and(fxObject().getCamera().rotateProperty().subscribe(num -> rotate.setValue(num.doubleValue())));
                });
            }
        }
    }

    @Override
    public Parent fxRoot() {
        return fxObject().getRoot();
    }

    @Override
    public final void subContextSave() {
        for (KlRenderView.PreferenceKeys key : KlRenderView.PreferenceKeys.values()) {
            switch (key) {
                case TRANSLATE_X -> preferences().putDouble(key, translateX.doubleValue());
                case TRANSLATE_Y -> preferences().putDouble(key, translateY.doubleValue());
                case TRANSLATE_Z -> preferences().putDouble(key, translateZ.doubleValue());
                case SCALE_X -> preferences().putDouble(key, scaleX.doubleValue());
                case SCALE_Y -> preferences().putDouble(key, scaleY.doubleValue());
                case SCALE_Z -> preferences().putDouble(key, scaleZ.doubleValue());
                case ROTATE -> preferences().putDouble(key, rotate.doubleValue());
            }
        }
    }

    @Override
    protected void subContextRevert() {
        restoreFromPreferencesOrDefaults();
    }

    public static RenderView restore(KometPreferences preferences) {
        return new RenderView(preferences);
    }

    public static RenderView create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlRenderView.Factory<RenderView>  {

        @Override
        public RenderView restore(KometPreferences preferences) {
            RenderView renderView = new RenderView(preferences);
            return renderView;
        }

        @Override
        public RenderView create(KlPreferencesFactory preferencesFactory) {
            RenderView view = new RenderView(preferencesFactory, this);
            return view;
        }


        public RenderView createAndAddToParent(FxWindow window) {
            KlPreferencesFactory preferencesFactory =
                    KlPreferencesFactory.create(window.preferences(), this.getClass().getEnclosingClass());

            RenderView view = new RenderView(preferencesFactory, this);
            window.addChild(view);

            return view;
        }


        public RenderView create(KlPreferencesFactory preferencesFactory, KnowledgeLayout masterLayout) {
            return new RenderView(preferencesFactory, this);
        }
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to do here.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

}
