import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.provider.changeset.ChangeSetWriterProvider;

module dev.ikm.tinkar.provider.changeset {
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.schema;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;

    requires org.slf4j;
    requires dev.ikm.tinkar.common;
    exports dev.ikm.tinkar.provider.changeset;

    provides ChangeSetWriterService with ChangeSetWriterProvider;
}