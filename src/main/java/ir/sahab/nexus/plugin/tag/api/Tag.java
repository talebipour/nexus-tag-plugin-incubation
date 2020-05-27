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

    private Date firstCreated;

    private Date lastUpdated;

    private Map<String, String> attributes = new HashMap<>();

    public Tag() {
    }

    public Tag(String name, Map<String, String> attributes, Date firstCreated, Date lastUpdated) {
        this.name = name;
        this.firstCreated = firstCreated;
        this.lastUpdated = lastUpdated;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFirstCreated() {
        return firstCreated;
    }

    public void setFirstCreated(Date firstCreated) {
        this.firstCreated = firstCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Tag{name='" + name + ", firstCreated='" + firstCreated + ", lastUpdated='" + lastUpdated
                + "', attributes=" + attributes + '}';
    }
}
