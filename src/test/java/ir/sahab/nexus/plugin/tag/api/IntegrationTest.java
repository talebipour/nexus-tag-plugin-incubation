package ir.sahab.nexus.plugin.tag.api;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ir.sahab.dockercomposer.DockerCompose;
import ir.sahab.dockercomposer.WaitFor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonatype.nexus.rest.APIConstants;

public class IntegrationTest {

    private static final String CHANGE_ID = "Change-Id";
    private static final String STATUS = "Status";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin123";

    @ClassRule
    public static DockerCompose compose = DockerCompose.builder()
            .file("/nexus.yml")
            .projectName("nexus-tag-plugin-test")
            .forceRecreate()
            .afterStart(WaitFor.portOpen("nexus", 8081, 1_200_000))
            .build();

    private Client client;
    private WebTarget target;

    private AssociatedComponent component1;
    private AssociatedComponent component2;

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
        target = client.target("http://nexus:8081/service/rest" + APIConstants.V1_API_PREFIX);
        component1 = new AssociatedComponent("maven-releases", "gr1", "comp1", randomAlphanumeric(5));
        uploadComponent(component1);
        component2 = new AssociatedComponent("maven-releases", "gr2", "comp2", randomAlphanumeric(5));
        uploadComponent(component2);
    }


    @After
    public void tearDown() {
        client.close();
    }

    private void uploadComponent(AssociatedComponent component) {
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("maven2.groupId", component.getGroup(), MediaType.TEXT_PLAIN_TYPE);
        output.addFormData("maven2.artifactId", component.getName(), MediaType.TEXT_PLAIN_TYPE);
        output.addFormData("maven2.version", component.getVersion(), MediaType.TEXT_PLAIN_TYPE);
        output.addFormData("maven2.asset1.extension", "jar", MediaType.TEXT_PLAIN_TYPE);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(randomAlphanumeric(100).getBytes());
        output.addFormData("maven2.asset1", inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE, "test-artifact.jar");

        Response response = target.path("components").queryParam("repository", component.getRepository())
                .request()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .post(Entity.entity(output, MediaType.MULTIPART_FORM_DATA_TYPE));
        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
    }

    /**
     * @return authorization header of basic authentication
     */
    private static String authorizationHeader() {
        String auth = USERNAME + ":" + PASSWORD;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }

    @Test
    public void testCrud() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(CHANGE_ID, randomAlphanumeric(20));
        attributes.put(STATUS, "failed");
        attributes.put("Commit-Id", randomAlphanumeric(20));

        TagDefinition tag = new TagDefinition(randomAlphanumeric(5), attributes, Arrays.asList(component1));

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
        tag.setComponents(Arrays.asList(component1, component2));
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

    private void assertTagEquals(TagDefinition expected, Tag actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAttributes(), actual.getAttributes());
        assertEquals(expected.getComponents(), actual.getComponents());
    }
}
