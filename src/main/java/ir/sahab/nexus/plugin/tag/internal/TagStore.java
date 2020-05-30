package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.common.app.ManagedLifecycle.Phase.SCHEMAS;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import ir.sahab.nexus.plugin.tag.internal.dto.Tag;
import ir.sahab.nexus.plugin.tag.internal.dto.TagDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.sonatype.nexus.common.app.ManagedLifecycle;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.orient.DatabaseInstance;

/**
 * Acts as a facade for storing and retrieving tags into database.
 */
@Named("tagStore")
@ManagedLifecycle(phase = SCHEMAS)
@Singleton
public class TagStore extends StateGuardLifecycleSupport {

    private final Provider<DatabaseInstance> dbProvider;
    private final TagEntityAdapter entityAdapter;

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

    public Optional<Tag> findByName(String name) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return entityAdapter.findByName(tx, name).map(TagEntity::toDto);
        }
    }

    public List<Tag> search(Map<String, String> attributes) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return StreamSupport.stream(entityAdapter.search(tx, attributes).spliterator(), false)
                    .map(TagEntity::toDto)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Creates tag with given definition if it does not already exists, otherwise
     * existing one will be update with the definition.
     *
     * @return created/updated tag
     */
    public Tag addOrUpdate(TagDefinition definition) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            Date currentDate = new Date();
            Optional<TagEntity> existing = entityAdapter.findByName(tx, definition.getName());
            TagEntity entity = existing.orElseGet(() -> {
                TagEntity newEntity = entityAdapter.newEntity();
                newEntity.setName(definition.getName());
                newEntity.setFirstCreated(currentDate);
                return newEntity;
            });
            entity.setAttributes(new HashMap<>(definition.getAttributes()));
            entity.setComponents(new ArrayList<>(definition.getComponents()));
            entity.setLastUpdated(currentDate);

            ODocument document;
            if (existing.isPresent()) {
                document = entityAdapter.editEntity(tx, entity);
                log.info("Tag {} updated in database.", entity);
            } else {
                document = entityAdapter.addEntity(tx, entity);
                log.info("Tag {} added to database.", entity);
            }
            return entityAdapter.transform(Collections.singleton(document)).iterator().next().toDto();
        }
    }

    /**
     * @param name name of tag to delete
     * @return removed tag if existed.
     */
    public Optional<Tag> delete(String name) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            Optional<TagEntity> optional = entityAdapter.findByName(tx, name);
            optional.ifPresent(entity -> entityAdapter.deleteEntity(tx, entity));
            return optional.map(TagEntity::toDto);
        }
    }
}
