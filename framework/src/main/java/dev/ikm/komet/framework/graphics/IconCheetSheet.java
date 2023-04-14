package dev.ikm.komet.framework.graphics;

public enum IconCheetSheet {
    Fontawesome("https://kordamp.org/ikonli/cheat-sheet-fontawesome.html"),
    Fontawesome5("https://kordamp.org/ikonli/cheat-sheet-fontawesome5.html"),
    RunestroIcons("https://kordamp.org/ikonli/cheat-sheet-runestroicons.html"),
    OctIcons("https://kordamp.org/ikonli/cheat-sheet-octicons.html"),
    MaterialDesign2("https://kordamp.org/ikonli/cheat-sheet-materialdesign2.html"),
    ControlsFx("-fx-graphic: url(\"/org/controlsfx/dialog/dialog-confirm.png\"); in style sheet"),
    
    ;
    String url;

    IconCheetSheet(String url) {
        this.url = url;
    }
}
