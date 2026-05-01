package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import java.util.List;

public class SemanticRow {
    private final List<SemanticField> fields;

    public SemanticRow(List<SemanticField> fields) {
        this.fields = fields;
    }

    public List<SemanticField> getFields() { return fields; }
}