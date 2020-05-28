package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.common.app.ManagedLifecycle.Phase.SCHEMAS;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import ir.sahab.nexus.plugin.tag.api.Tag;
import ir.sahab.nexus.plugin.tag.api.TagDefinition;
import ir.sahab.nexus.plugin.tag.api.AssociatedComponent;

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
import javax.validation.ValidationException;

import org.sonatype.nexus.common.app.ManagedLifecycle;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.BucketStore;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ComponentStore;

/**
 * Acts as a facade for storing and retrieving tags into database.
 */
@Named("tagStore")
@ManagedLifecycle(phase = SCHEMAS)
@Singleton
public class TagStore extends StateGuardLifecycleSupport {

    private final Provider<DatabaseInstance> dbProvider;
    private final TagEntityAdapter entityAdapter;
    private final Provider<RepositoryManager> repositoryManagerProvider;
    private final Provider<ComponentStore> componentStoreProvider;
    private final Provider<BucketStore> bucketStoreProvider;

    @Inject
    public TagStore(Provider<DatabaseInstance> dbProvider, TagEntityAdapter entityAdapter,
            Provider<RepositoryManager> repositoryManagerProvider, Provider<ComponentStore> componentStoreProvider,
            Provider<BucketStore> bucketStoreProvider) {
        this.dbProvider = dbProvider;
        this.entityAdapter = entityAdapter;
        this.repositoryManagerProvider = repositoryManagerProvider;
        this.componentStoreProvider = componentStoreProvider;
        this.bucketStoreProvider = bucketStoreProvider;
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
            return entityAdapter.findByName(tx, name).map(this::toDto);
        }
    }

    public List<Tag> search(Map<String, String> attributes) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            return StreamSupport.stream(entityAdapter.search(tx, attributes).spliterator(), false)
                    .map(this::toDto)
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
            entity.setComponents(findComponents(definition.getComponents()));
            entity.setLastUpdated(currentDate);

            ODocument document;
            if (existing.isPresent()) {
                document = entityAdapter.editEntity(tx, entity);
                log.info("Tag {} updated in database.", entity);
            } else {
                document = entityAdapter.addEntity(tx, entity);
                log.info("Tag {} added to database.", entity);
            }
            return toDto(entityAdapter.transform(Collections.singleton(document)).iterator().next());
        }
    }

    /**
     * @param components list of a associated components to search
     * @return component equivalent component entities of definition list
     * @throws ValidationException if any of the associated components does not exist
     */
    private List<Component> findComponents(List<AssociatedComponent> components) {
        List<Component> result = new ArrayList<>();
        RepositoryManager repositoryManager = repositoryManagerProvider.get();
        ComponentStore componentStore = componentStoreProvider.get();
        for (AssociatedComponent associatedComponent : components) {
            Repository repository = repositoryManager.get(associatedComponent.getName());
            Map<String, String> versionAttribute = Collections.singletonMap("version", associatedComponent.getVersion());
            List<Component> foundComponents = componentStore.getAllMatchingComponents(repository,
                    associatedComponent.getGroup(), associatedComponent.getName(), versionAttribute);
            if (foundComponents.isEmpty()) {
                throw new ValidationException("Component " + associatedComponent + " does not exists.");
            }
            result.addAll(foundComponents);
        }
        return result;
    }

    /**
     * @param name name of tag to delete
     * @return removed tag if existed.
     */
    public Optional<Tag> delete(String name) {
        try (ODatabaseDocumentTx tx = dbProvider.get().acquire()) {
            Optional<TagEntity> optional = entityAdapter.findByName(tx, name);
            optional.ifPresent(entity -> entityAdapter.deleteEntity(tx, entity));
            return optional.map(this::toDto);
        }
    }

    public Tag toDto(TagEntity entity) {
        List<AssociatedComponent> convertedComponents = entity.getComponents().stream()
                .map(component -> new AssociatedComponent(findRepositoryName(component), component.group(),
                        component.name(), component.version()))
                .collect(Collectors.toList());
        return new Tag(entity.getName(), entity.getAttributes(), convertedComponents, entity.getFirstCreated(),
                entity.getLastUpdated());
    }

    private String findRepositoryName(Component component) {
        BucketStore bucketStore = bucketStoreProvider.get();
        Bucket bucket = bucketStore.getById(component.bucketId());
        return bucket.getRepositoryName();
    }
}
