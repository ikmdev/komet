package dev.ikm.komet.collection;

public enum CollectionType {
    SET(".tink.set.txt", "set", "Set"),
    LIST(".tink.list.txt", "list", "List");

    public String extension;
    public String userSuffix;
    public String userPrefix;

    CollectionType(String extension, String userSuffix, String userPrefix) {
        this.extension = extension;
        this.userSuffix = userSuffix;
        this.userPrefix = userPrefix;
    }
}
