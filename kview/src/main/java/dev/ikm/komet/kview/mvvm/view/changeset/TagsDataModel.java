package dev.ikm.komet.kview.mvvm.view.changeset;

public class TagsDataModel {
    public String tagName;
    public String tagNid;
    public boolean tagSelected;
    public String tagDescription;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagNid() {
        return tagNid;
    }

    public void setTagNid(String tagNid) {
        this.tagNid = tagNid;
    }

    public boolean isTagSelected() {
        return tagSelected;
    }

    public void setTagSelected(boolean tagSelected) {
        this.tagSelected = tagSelected;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public void setTagDescription(String tagDescription) {
        this.tagDescription = tagDescription;
    }
}