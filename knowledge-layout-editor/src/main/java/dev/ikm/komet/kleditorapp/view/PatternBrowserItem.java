package dev.ikm.komet.kleditorapp.view;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.PatternFacade;

import java.util.Optional;

/**
 * Represents an Item in the Pattern Browser List.
 */
public class PatternBrowserItem {
    private final String title;
    private final PublicId publicId;
    private final int nid;

    private final ViewCalculator viewCalculator;

    public PatternBrowserItem(Entity<EntityVersion> entity, ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        this.title = retrieveDisplayName(entity.toProxy());
        this.publicId = entity.publicId();
        this.nid = entity.nid();
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }

    public String getTitle() { return title; }
    public PublicId getPublicId() { return publicId; }
    public int getNid() { return nid; }
}
