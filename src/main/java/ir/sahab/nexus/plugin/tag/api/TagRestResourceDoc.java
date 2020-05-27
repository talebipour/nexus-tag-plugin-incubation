package ir.sahab.nexus.plugin.tag.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "tag", description = "Provides creating and retrieving tags.")
public interface TagRestResourceDoc {

    @GET
    @ApiOperation("Get a single tag with ID")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response getById(@ApiParam(value = "ID of tag to retrieve", required = true) String id);

    @GET
    @ApiOperation("List tags. Result may be filtered by optional attributes")
    List<Tag> list(@ApiParam("Comma separated attribute values to search in format key1=value1[,key2=value2,...]")
            String attributes);

    @POST
    @ApiOperation("Creates a new tag")
    Tag add(CreateTagRequest request);

    @DELETE
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response delete(@ApiParam(value = "ID of tag to retrieve", required = true) String id);
}