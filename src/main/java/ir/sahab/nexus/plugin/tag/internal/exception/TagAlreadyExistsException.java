package ir.sahab.nexus.plugin.tag.internal.exception;

import javax.ws.rs.BadRequestException;

/**
 * This exception is thrown whenever target tag already exists.
 */
public class TagAlreadyExistsException extends BadRequestException {

    public TagAlreadyExistsException(String tagName) {
        super(String.format("Tag %s already exists.", tagName));
    }
}
