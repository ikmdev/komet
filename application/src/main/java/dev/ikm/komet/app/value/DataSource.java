package dev.ikm.komet.app.value;

public enum DataSource {
    FILESYSTEM("File system"),
    TARGET_DATABASE("Target database"),
    WEBSOCKET("WebSocket");

    String nameForInterface;

    DataSource(String nameForInterface) {
        this.nameForInterface = nameForInterface;
    }

    @Override
    public String toString() {
        return nameForInterface;
    }
}
