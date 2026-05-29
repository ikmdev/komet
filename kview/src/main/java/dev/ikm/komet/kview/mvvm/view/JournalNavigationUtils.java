package dev.ikm.komet.kview.mvvm.view;

import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.events.ShowNavigationalPanelEvent;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.Node;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;

public final class JournalNavigationUtils {

    private JournalNavigationUtils() {
        // Utility class
    }

    public static void openConceptInNavigatorForContainingJournal(Object source, Node nodeInJournal, PublicId conceptPublicId) {
        if (nodeInJournal == null || conceptPublicId == null) {
            return;
        }

        EntityHandle.get(conceptPublicId).ifConcept(conceptEntity -> {
            openConceptInNavigatorForContainingJournal(
                    source,
                    conceptEntity,
                    nodeInJournal
            );
        });
    }

    public static void openConceptInNavigatorForContainingJournal(Object source, ConceptFacade concept, Node nodeInJournal) {
        if (concept == null || nodeInJournal == null) {
            return;
        }

        UUID journalTopic = findContainingJournalTopic(nodeInJournal);
        if (journalTopic == null) {
            return;
        }

        EvtBusFactory.getDefaultEvtBus().publish(
                journalTopic,
                new ShowNavigationalPanelEvent(
                        source,
                        ShowNavigationalPanelEvent.SHOW_CONCEPT_NAVIGATIONAL_FROM_CONCEPT,
                        concept
                )
        );
    }

    public static void openEntityInJournalForContainingJournal(Object source, Node nodeInJournal, PublicId publicId) {
        if (nodeInJournal == null || publicId == null) {
            return;
        }

        UUID journalTopic = findContainingJournalTopic(nodeInJournal);
        if (journalTopic == null) {
            return;
        }

        EntityHandle.get(publicId).ifEntity(entity ->
                openEntityInJournal(source, journalTopic, entity)
        );
    }

    public static void openEntityInJournalForContainingJournal(Object source, Node nodeInJournal, EntityFacade entity) {
        if (nodeInJournal == null || entity == null) {
            return;
        }

        UUID journalTopic = findContainingJournalTopic(nodeInJournal);
        if (journalTopic == null) {
            return;
        }

        openEntityInJournal(source, journalTopic, entity);
    }

    private static void openEntityInJournal(Object source, UUID journalTopic, EntityFacade entity) {
        EvtBusFactory.getDefaultEvtBus().publish(
                journalTopic,
                new MakeConceptWindowEvent(
                        source,
                        MakeConceptWindowEvent.OPEN_ENTITY_COMPONENT,
                        entity
                )
        );
    }

    private static UUID findContainingJournalTopic(Node node) {
        Node current = node;

        while (current != null) {
            Object journalTopic = current.getProperties().get(CURRENT_JOURNAL_WINDOW_TOPIC);

            if (journalTopic instanceof UUID uuid) {
                return uuid;
            }

            current = current.getParent();
        }

        if (node.getScene() != null && node.getScene().getRoot() != null) {
            Object journalTopic = node.getScene().getRoot().getProperties().get(CURRENT_JOURNAL_WINDOW_TOPIC);

            if (journalTopic instanceof UUID uuid) {
                return uuid;
            }
        }

        return null;
    }
}