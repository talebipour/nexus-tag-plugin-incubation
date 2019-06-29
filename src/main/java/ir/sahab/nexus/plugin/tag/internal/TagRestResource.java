package ir.sahab.nexus.plugin.tag.internal;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.rest.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.sonatype.nexus.rest.APIConstants.V1_API_PREFIX;

@Named
@Singleton
@Path(TagRestResource.RESOURCE_URI)
@Produces(MediaType.APPLICATION_JSON)
public class TagRestResource extends ComponentSupport implements Resource {
    static final String RESOURCE_URI = V1_API_PREFIX + "/tag";

    private final TagEntityAdapter entityAdapter;

    @Inject
    public TagRestResource(TagEntityAdapter entityAdapter) {
        this.entityAdapter = entityAdapter;
    }

    @GET
    public Iterable<Tag> list() {
        return entityAdapter.browse(null);
    }
}
