package ir.sahab.nexus.plugin.tag.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tag which can be created using REST API. This class is meant to
 * be serialized by Jackson and to be used in REST API.
 */
public class Tag {

    private String name;

    private Date creationDate;

    private Map<String, String> attributes = new HashMap<>();

    public Tag() {
    }

    public Tag(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Tag{name='" + name + ", creationDate='" + creationDate + "', attributes=" + attributes + '}';
    }
}
