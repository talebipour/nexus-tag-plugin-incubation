package ir.sahab.nexus.plugin.tag.internal.exception;

import javax.ws.rs.NotFoundException;

/**
 * This exception is thrown whenever target tag does not exists.
 */
public class TagNotFoundException extends NotFoundException {

    public TagNotFoundException(String tagName) {
        super(String.format("Tag %s does not exists.", tagName));
    }
}
