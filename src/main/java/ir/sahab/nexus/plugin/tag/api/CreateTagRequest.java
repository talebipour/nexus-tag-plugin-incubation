package ir.sahab.nexus.plugin.tag.api;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * Holds fields used to create a new tag.
 */
public class CreateTagRequest {

    @NotNull
    private String name;

    @NotNull
    private Map<String, String> attributes = new HashMap<>();

    public CreateTagRequest() {
    }

    public CreateTagRequest(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "CreateTagRequest{name='" + name + "', attributes=" + attributes + '}';
    }
}
