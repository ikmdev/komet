package dev.ikm.komet.kview.mvvm.model;

public enum DescriptionFormType {
    ADD_FQN("Add Description: Fully Qualified Name", "Fully Qualified Name", true),
    ADD_OTHER_NAME("Add New Description: Other Name", "Other Name", true),
    EDIT_FQN("Edit Description: Fully Qualified Name", "Fully Qualified Name", false),
    EDIT_OTHER_NAME("Edit Description: Other Name", "Other Name", false);

    private final String title;
    private final String typePrompt;
    private final boolean isAddMode;

    DescriptionFormType(String title, String typePrompt, boolean isAddMode) {
        this.title = title;
        this.typePrompt = typePrompt;
        this.isAddMode = isAddMode;
    }

    public String getTitle() { return title; }
    public String getTypePrompt() { return typePrompt; }
    public boolean isAddMode() { return isAddMode; }
    public boolean isEditMode() { return !isAddMode; }
    public boolean isFqnType() { return this == ADD_FQN || this == EDIT_FQN; }
}
