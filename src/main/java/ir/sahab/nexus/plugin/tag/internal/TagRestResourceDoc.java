package ir.sahab.nexus.plugin.tag.internal;

import ir.sahab.nexus.plugin.tag.internal.dto.Tag;
import ir.sahab.nexus.plugin.tag.internal.dto.TagDefinition;
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

@Api(value = "tag")
public interface TagRestResourceDoc {

    @GET
    @ApiOperation("Get a single tag by name")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response getByName(@ApiParam(value = "name of tag to retrieve", required = true) String name);

    @GET
    @ApiOperation("List tags, results may be filtered by optional attributes")
    List<Tag> list(@ApiParam("Comma separated attribute values to search in format key1=value1[,key2=value2,...]")
            String attributes);

    @POST
    @ApiOperation("Add a new tag")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request")
    })
    Tag add(TagDefinition definition);

    @PUT
    @ApiOperation("Add a new tag or updates existing one")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.")
    })
    Tag addOrUpdate(TagDefinition definition,
            @ApiParam(value = "Name of tag to create or update", required = true) String name);

    @DELETE
    @ApiOperation("Deletes stored tag by name")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Tag does not exists")
    })
    Response delete(@ApiParam(value = "Name of tag to delete", required = true) String name);
}