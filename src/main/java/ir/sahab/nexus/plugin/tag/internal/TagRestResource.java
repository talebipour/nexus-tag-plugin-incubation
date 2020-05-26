package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.rest.APIConstants.V1_API_PREFIX;

import ir.sahab.nexus.plugin.tag.api.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.rest.Resource;

@Named
@Singleton
@Path(V1_API_PREFIX)
public class TagRestResource extends ComponentSupport implements Resource {

    private static final String ATTRIBUTES_PARAM_PREFIX = "attributes.";
    private TagStore store;

    @Inject
    public TagRestResource(TagStore store) {
        this.store = store;
    }

    @GET
    @Path("/tag/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") String id) {
        log.info("Finding tag with id={}", id);
        Optional<TagEntity> optional = store.findById(id);
        if (optional.isPresent()) {
            Tag found = optional.get().toDto();
            log.info("Tag {} found.", found);
            return Response.ok(found, MediaType.APPLICATION_JSON_TYPE).build();
        }
        log.info("Tag with id={} not found.", id);
        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tag> list(@QueryParam("project") String project, @QueryParam("name") String name,
            @Context UriInfo uriInfo) {
        Map<String, String> attributes = extractAttributeParams(uriInfo);
        Iterable<TagEntity> entities = store.search(project, name, attributes);
        log.info("Tag search for (project={}, name={}, attributes={})={}", project, name, attributes, entities);
        return StreamSupport.stream(entities.spliterator(), false)
                .map(TagEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Extracts all query parameters that are used for searching tag attributes. Attribute query parameters starts
     * with {@link #ATTRIBUTES_PARAM_PREFIX} followed by name of attribute to search.
     * If multiple values are assigned to a single attribute, only the first one is used.
     *
     * @return map to attribute name to search value
     */
    private Map<String, String> extractAttributeParams(UriInfo uriInfo) {
        return uriInfo.getQueryParameters(true).entrySet()
                .stream().filter(entry -> entry.getKey().startsWith(ATTRIBUTES_PARAM_PREFIX))
                .collect(Collectors.toMap(entry -> entry.getKey().substring(ATTRIBUTES_PARAM_PREFIX.length()),
                        entry -> entry.getValue().get(0)));
    }

    @POST
    @Path("/tag")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tag add(Tag tag) {
        if (tag.getId() != null) {
            throw new BadRequestException("id must be null.");
        }
        Tag created = store.add(TagEntity.fromDto(tag)).toDto();
        log.info("Tag {} created.", created);
        return created;
    }

    @DELETE
    @Path("/tag/{id}")
    public Response delete(@PathParam("id") String id) {
        log.info("Finding tag with id={}", id);
        Optional<TagEntity> optional = store.delete(id);
        if (optional.isPresent()) {
            log.info("Tag {} removed.", optional.get());
            return Response.ok().build();
        }
        log.info("Unable to delete tag with id={}, tag does not exist.", id);
        return Response.status(Status.NOT_FOUND).build();
    }

}
