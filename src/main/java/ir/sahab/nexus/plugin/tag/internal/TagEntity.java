package ir.sahab.nexus.plugin.tag.internal;

import ir.sahab.nexus.plugin.tag.api.CreateTagRequest;
import ir.sahab.nexus.plugin.tag.api.Tag;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.sonatype.nexus.common.entity.AbstractEntity;


/**
 * Entity class used to persist tag instances.
 */
class TagEntity extends AbstractEntity {

    private String name;

    private Date firstCreated;

    private Date lastUpdated;

    private Map<String, String> attributes = new HashMap<>();

    TagEntity() {
    }

    TagEntity(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstCreated(Date firstCreated) {
        this.firstCreated = firstCreated;
    }

    public Date getFirstCreated() {
        return firstCreated;
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

    public Tag toDto() {
        return new Tag(name, attributes, firstCreated, lastUpdated);
    }

    @Override
    public String toString() {
        return "Tag{name='" + name + ", firstCreated='" + firstCreated + ", lastUpdated='" + lastUpdated
                + "', attributes=" + attributes + ", metadata=" + getEntityMetadata() + '}';
    }
}
