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
        }
    }

    public Optional<TagEntity> findById(String id) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return entityAdapter.findById(tx, new DetachedEntityId(id));
        }
    }
    public Iterable<TagEntity> search(String project, String name, Map<String, String> attributes) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return entityAdapter.search(tx, project, name, attributes);
        }
    }

    public TagEntity add(TagEntity tag) {
        tag.setCreationDate(new Date());
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            ODocument document = entityAdapter.addEntity(tx, tag);
            log.info("Tag {} added to database", tag);
            return entityAdapter.transform(Collections.singleton(document)).iterator().next();
        }
    }

    /**
     * @param id id of tag to delete
     * @return remove entity if exists, otherwise null.
     */
    public Optional<TagEntity> delete(String id) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            Optional<TagEntity> optional = entityAdapter.findById(tx, new DetachedEntityId(id));
            optional.ifPresent(entity -> entityAdapter.deleteEntity(tx, entity));
            return optional;
        }
    }
}
