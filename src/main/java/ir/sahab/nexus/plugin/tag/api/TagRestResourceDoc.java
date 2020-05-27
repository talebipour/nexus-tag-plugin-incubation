package ir.sahab.nexus.plugin.tag.api;

import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "tag", description = "Provides creating and retrieving tags.")
public interface TagRestResourceDoc {

    @GET
    @ApiOperation("Get a single tag with given name")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response getByName(@ApiParam(value = "name of tag to retrieve", required = true) String name);

    @GET
    @ApiOperation("List tags. Result may be filtered by optional attributes")
    List<Tag> list(@ApiParam("Comma separated attribute values to search in format key1=value1[,key2=value2,...]")
            String attributes);

    @POST
    @ApiOperation("Creates a new tag")
    Tag add(TagDefinition definition);

    @PUT
    @ApiOperation("Creates a new tag or updates existing one")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.")
    })
    Tag addOrUpdate(TagDefinition definition,
            @ApiParam(value = "Name of tag to create or update", required = true) String name);

    @DELETE
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response delete(@ApiParam(value = "Name of tag to retrieve", required = true) String name);
}