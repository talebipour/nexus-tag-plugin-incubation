package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.common.app.ManagedLifecycle.Phase.SCHEMAS;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.sonatype.nexus.common.app.ManagedLifecycle;
import org.sonatype.nexus.common.entity.DetachedEntityId;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.orient.DatabaseInstance;

/**
 * Acts as a facade for storing and retrieving tags into database.
 */
@Named("tagStore")
@ManagedLifecycle(phase = SCHEMAS)
@Singleton
public class TagStore extends StateGuardLifecycleSupport {

    private Provider<DatabaseInstance> dbProvider;
    private TagEntityAdapter entityAdapter;

    @Inject
    public TagStore(Provider<DatabaseInstance> dbProvider, TagEntityAdapter entityAdapter) {
        this.dbProvider = dbProvider;
        this.entityAdapter = entityAdapter;
    }

    @Override
    protected void doStart() {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            entityAdapter.register(tx);
            log.info("Tag entity adapter registered.");
        }
    }

    public Optional<TagEntity> findByName(String name) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return entityAdapter.findByName(tx, name);
        }
    }
    public Iterable<TagEntity> search(Map<String, String> attributes) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return entityAdapter.search(tx, attributes);
        }
    }

    public TagEntity add(TagEntity tag) {
        Date currentDate = new Date();
        tag.setFirstCreated(currentDate);
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            ODocument document = entityAdapter.addEntity(tx, tag);
            log.info("Tag {} added to database", tag);
            return entityAdapter.transform(Collections.singleton(document)).iterator().next();
        }
    }

    /**
     * @param name name of tag to delete
     * @return remove entity if exists, otherwise null.
     */
    public Optional<TagEntity> delete(String name) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            Optional<TagEntity> optional = entityAdapter.findByName(tx, name);
            optional.ifPresent(entity -> entityAdapter.deleteEntity(tx, entity));
            return optional;
        }
    }
}
