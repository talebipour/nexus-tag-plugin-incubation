package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.rest.APIConstants.V1_API_PREFIX;

import ir.sahab.nexus.plugin.tag.api.CreateTagRequest;
import ir.sahab.nexus.plugin.tag.api.Tag;
import ir.sahab.nexus.plugin.tag.api.TagRestResourceDoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.Body;
import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.rest.Resource;
/**
 * Endpoint which provides RESTful API for tagging.
 */
@Named
@Singleton
@Path(V1_API_PREFIX)
public class TagRestResource extends ComponentSupport implements Resource, TagRestResourceDoc {

    private TagStore store;

    @Inject
    public TagRestResource(TagStore store) {
        this.store = store;
    }

    @GET
    @Path("/tag/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getByName(@PathParam("name") String name) {
        log.info("Finding tag with name={}", name);
        Optional<TagEntity> optional = store.findByName(name);
        if (optional.isPresent()) {
            Tag found = optional.get().toDto();
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
        Iterable<TagEntity> entities = store.search(attributeMap);
        log.info("Tag search for attributes={}={}", attributes, entities);
        return StreamSupport.stream(entities.spliterator(), false)
                .map(TagEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Decodes query parameter of attributes to an attribute map.
     * @param attributes comma separated key value pairs of attributes. i.e. key1=value1[,key2=value2,...]
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
    public Tag add(CreateTagRequest request) {
        Tag created = store.addOrUpdate(request).toDto();
        log.info("Tag {} created.", created);
        return created;
    }

    @PUT
    @Path("/tag/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag addOrUpdate(CreateTagRequest request, @PathParam("name") String name) {
        if (!name.equals(request.getName())) {
            throw new BadRequestException("Cannot change name.");
        }
        Tag created = store.addOrUpdate(request).toDto();
        log.info("Tag {} created.", created);
        return created;
    }

    @DELETE
    @Path("/tag/{name}")
    @Override
    public Response delete(@PathParam("name") String name) {
        log.info("Deleting tag with name={}", name);
        Optional<TagEntity> optional = store.delete(name);
        if (optional.isPresent()) {
            log.info("Tag {} removed.", optional.get());
            return Response.ok().build();
        }
        log.info("Unable to delete tag with name={}, tag does not exist.", name);
        return Response.status(Status.NOT_FOUND).build();
    }

}
