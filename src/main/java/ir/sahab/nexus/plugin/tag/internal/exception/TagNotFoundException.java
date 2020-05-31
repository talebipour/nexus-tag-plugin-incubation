package ir.sahab.nexus.plugin.tag.internal.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This exception is thrown whenever target tag does not exists.
 */
public class TagNotFoundException extends NotFoundException {

    public TagNotFoundException(String tagName) {
        super(Response.status(Status.NOT_FOUND).entity("Tag doess not exists.").build());
    }
}
