package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.rest.APIConstants.V1_API_PREFIX;

import ir.sahab.nexus.plugin.tag.api.AssociatedComponent;
import ir.sahab.nexus.plugin.tag.api.Tag;
import ir.sahab.nexus.plugin.tag.api.TagDefinition;
import ir.sahab.nexus.plugin.tag.api.TagRestResourceDoc;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ComponentStore;
import org.sonatype.nexus.rest.Resource;

/**
 * Endpoint which provides RESTful API for tagging.
 */
@Named
@Singleton
@Path(V1_API_PREFIX)
public class TagRestResource extends ComponentSupport implements Resource, TagRestResourceDoc {

    private final TagStore tagStore;
    private final RepositoryManager repositoryManager;
    private final ComponentStore componentStore;

    @Inject
    public TagRestResource(TagStore tagStore, RepositoryManager repositoryManager, ComponentStore componentStore) {
        this.tagStore = tagStore;
        this.repositoryManager = repositoryManager;
        this.componentStore = componentStore;
    }

    @GET
    @Path("/tag/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getByName(@PathParam("name") String name) {
        log.info("Finding tag with name={}", name);
        Optional<Tag> optional = tagStore.findByName(name);
        if (optional.isPresent()) {
            Tag found = optional.get();
            log.info("Tag {} found.", found);
            return Response.ok(found, MediaType.APPLICATION_JSON_TYPE).build();
        }
        log.info("Tag with name={} not found.", name);
        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<Tag> list(@QueryParam("attributes") String attributes) {
        Map<String, String> attributeMap = decodeAttributes(attributes);
        List<Tag> tags = tagStore.search(attributeMap);
        log.info("Tag search for attributes={}={}", attributes, tags);
        return tags;
    }

    /**
     * Decodes query parameter of attributes to an attribute map.
     *
     * @param attributes comma separated key value pairs of attributes. i.e.
     *                   key1=value1[,key2=value2,...]
     * @return map of attribute name to search value
     * @throws BadRequestException if key value pair is not in format key=value
     */
    private Map<String, String> decodeAttributes(String attributes) {
        if (attributes == null || attributes.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (String keyValue : attributes.split(",")) {
            String[] splitted = keyValue.split("=");
            if (splitted.length != 2) {
                throw new BadRequestException("Invalid attribute key value pair: " + keyValue);
            }
            map.put(splitted[0].trim(), splitted[1].trim());
        }
        return map;
    }

    @POST
    @Path("/tag")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag add(@Valid TagDefinition definition) {
        validateComponents(definition.getComponents());
        Tag created = tagStore.addOrUpdate(definition);
        log.info("Tag {} created.", created);
        return created;
    }

    @PUT
    @Path("/tag/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag addOrUpdate(@Valid TagDefinition definition, @PathParam("name") String name) {
        if (!name.equals(definition.getName())) {
            throw new BadRequestException("Cannot change name.");
        }
        validateComponents(definition.getComponents());
        Tag created = tagStore.addOrUpdate(definition);
        log.info("Tag {} created.", created);
        return created;
    }

    @DELETE
    @Path("/tag/{name}")
    @Override
    public Response delete(@PathParam("name") String name) {
        log.info("Deleting tag with name={}", name);
        Optional<Tag> optional = tagStore.delete(name);
        if (optional.isPresent()) {
            log.info("Tag {} removed.", optional.get());
            return Response.ok().build();
        }
        log.info("Unable to delete tag with name={}, tag does not exist.", name);
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Checks if is given associated components exists in repository.
     */
    private void validateComponents(List<AssociatedComponent> components) {
        for (AssociatedComponent component : components) {
            Repository repository = repositoryManager.get(component.getRepository());
            if (repository == null) {
                throw new ValidationException("Repository " + component.getRepository() + " does not exists.");
            }
            Map<String, String> versionAttribute = Collections.singletonMap("version", component.getVersion());
            List<Component> founds = componentStore.getAllMatchingComponents(repository,
                    component.getGroup(), component.getName(), versionAttribute);
            if (founds.isEmpty()) {
                throw new ValidationException("Component " + component + " does not exists.");
            }
        }
    }
}
