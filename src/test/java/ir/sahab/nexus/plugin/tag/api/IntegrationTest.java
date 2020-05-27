package ir.sahab.nexus.plugin.tag.api;


import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import ir.sahab.dockercomposer.DockerCompose;
import ir.sahab.dockercomposer.WaitFor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonatype.nexus.rest.APIConstants;

public class IntegrationTest {

    private static final String CHANGE_ID = "Change-Id";
    private static final String STATUS = "Status";

    @ClassRule
    public static DockerCompose compose = DockerCompose.builder()
            .file("/nexus.yml")
            .projectName("nexus-tag-plugin-test")
            .forceRecreate()
            .afterStart(WaitFor.portOpen("nexus", 8081, 1_200_000))
            .build();

    private Client client;
    private WebTarget target;

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
        target = client.target("http://nexus:8081/service/rest" + APIConstants.V1_API_PREFIX);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testCrud() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(CHANGE_ID, randomAlphanumeric(20));
        attributes.put(STATUS, "failed");
        attributes.put("Commit-Id", randomAlphanumeric(20));
        CreateTagRequest tag = new CreateTagRequest(randomAlphanumeric(5), attributes);

        // Create Tag
        Response response = target.path("tag").request().post(Entity.entity(tag, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        Tag postResponseTag = response.readEntity(Tag.class);
        assertFalse(new Date().before(postResponseTag.getFirstCreated()));
        assertEquals(postResponseTag.getFirstCreated(), postResponseTag.getLastUpdated());
        assertTagEquals(tag, postResponseTag);

        // Test Get by name
        Tag retrieved = target.path("tag/" + tag.getName()).request().get(Tag.class);
        assertTagEquals(tag, retrieved);

        // Test search by attribute
        List<Tag> result = target.path("tags")
                .queryParam("attributes", CHANGE_ID + "=" + tag.getAttributes().get(CHANGE_ID))
                .request()
                .get(new GenericType<List<Tag>>() {});
        assertEquals(1, result.size());
        assertTagEquals(tag, result.get(0));

        // Update tag
        tag.getAttributes().put(STATUS, "successful");
        response = target.path("tag/" + tag.getName())
                .request()
                .put(Entity.entity(tag, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        Tag putResponseTag = response.readEntity(Tag.class);
        assertEquals(postResponseTag.getFirstCreated(), putResponseTag.getFirstCreated());
        assertFalse(new Date().before(putResponseTag.getLastUpdated()));
        assertTagEquals(tag, putResponseTag);

        // Test deleting tag
        response = target.path("tag/" + tag.getName()).request().delete();
        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        response = target.path("tag/" + tag.getName()).request().get();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
    }

    private void assertTagEquals(CreateTagRequest expected, Tag actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAttributes(), actual.getAttributes());
    }
}
