package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.layout.LayoutKey;

public interface GridStepper {

    void setStep(GridStep step);

    void reset();

    int row();

    int column();

    AreaGridSettings nextForFeature(LayoutKey.ForArea forAreaLayoutKey, FeatureKey locator, String factoryClassName);

    AreaGridSettings nextForSupplemental(LayoutKey.ForArea forAreaLayoutKey, String factoryClassName);

    AreaGridSettings nextForFeature(LayoutKey.ForArea forAreaLayoutKey, FeatureKey locator, Class factoryClass);

    AreaGridSettings nextForSupplemental(LayoutKey.ForArea forAreaLayoutKey, Class factoryClass);

}
