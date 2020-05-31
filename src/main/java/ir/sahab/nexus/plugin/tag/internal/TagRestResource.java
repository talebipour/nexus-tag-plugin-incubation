package ir.sahab.nexus.plugin.tag.internal;

import static org.sonatype.nexus.rest.APIConstants.V1_API_PREFIX;

import ir.sahab.nexus.plugin.tag.internal.dto.Tag;
import ir.sahab.nexus.plugin.tag.internal.dto.TagCloneRequest;
import ir.sahab.nexus.plugin.tag.internal.dto.TagDefinition;
import ir.sahab.nexus.plugin.tag.internal.exception.TagAlreadyExistsException;
import ir.sahab.nexus.plugin.tag.internal.exception.TagNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.rest.Resource;

/**
 * Endpoint which provides RESTful API for tagging.
 */
@Named
@Singleton
@Path(V1_API_PREFIX)
public class TagRestResource extends ComponentSupport implements Resource, TagRestResourceDoc {

    private final TagStore tagStore;
    private final Validator validator;

    @Inject
    public TagRestResource(TagStore tagStore, Validator validator) {
        this.tagStore = tagStore;
        this.validator = validator;
    }

    @GET
    @Path("/tags/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag getByName(@PathParam("name") String name) {
        return tagStore.getByName(name);
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
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag add(TagDefinition definition) {
        validate(definition);
        return tagStore.addOrUpdate(definition);
    }

    @PUT
    @Path("/tags/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag addOrUpdate(TagDefinition definition, @PathParam("name") String name) {
        validate(definition);
        if (!name.equals(definition.getName())) {
            throw new BadRequestException("Cannot change name.");
        }
        return tagStore.addOrUpdate(definition);
    }

    @DELETE
    @Path("/tags/{name}")
    @Override
    public void delete(@PathParam("name") String name) {
        tagStore.delete(name);
    }

    @POST
    @Path("/tags/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Tag clone(@PathParam("name") String name, TagCloneRequest request) {
        validate(request);
        return tagStore.cloneExisting(request.getSourceName(), name, request.getAppendingAttributes());
    }

    /**
     * We haven't managed to use javax validation yet, as if we add @Valid for any parameter, all requests would fail
     * with 400 code. The exact reason is not clear for now, but it seems it's related to internal validators of
     * nexus. So we do validation manually for now.
     * TODO: Fix javax validation problem
     */
    private void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Invalid tag.", violations);
        }
    }

}
