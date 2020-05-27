package ir.sahab.nexus.plugin.tag.internal;

import ir.sahab.nexus.plugin.tag.api.Tag;
import ir.sahab.nexus.plugin.tag.api.TagDefinition.AssociatedComponent;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.sonatype.nexus.common.entity.AbstractEntity;
import org.sonatype.nexus.repository.storage.Component;


/**
 * Entity class used to persist tag instances.
 */
class TagEntity extends AbstractEntity {

    private String name;

    private Date firstCreated;

    private Date lastUpdated;

    private Map<String, String> attributes;

    private List<Component> components;

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

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "Tag{name='" + name + ", firstCreated='" + firstCreated + ", lastUpdated='" + lastUpdated
                + "', attributes=" + attributes + ", metadata=" + getEntityMetadata() + '}';
    }
}
