package ir.sahab.nexus.plugin.tag.internal;

import ir.sahab.nexus.plugin.tag.api.CreateTagRequest;
import ir.sahab.nexus.plugin.tag.api.Tag;
import java.util.Date;
import org.sonatype.nexus.common.entity.AbstractEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity class used to persist tag instances.
 */
class TagEntity extends AbstractEntity {

    private String project;

    private String name;

    private Date creationDate;

    private Map<String, String> attributes = new HashMap<>();

    TagEntity() {
    }

    TagEntity(String project, String name, Map<String, String> attributes) {
        this.project = project;
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Tag toDto() {
        Tag dto = new Tag(project, name, attributes);
        if (getEntityMetadata() != null) {
            dto.setId(getEntityMetadata().getId().getValue());
        }
        dto.setCreationDate(creationDate);
        return dto;
    }

    public static TagEntity forCreateRequest(CreateTagRequest tag) {
        return new TagEntity(tag.getProject(), tag.getName(), tag.getAttributes());
    }

    @Override
    public String toString() {
        return "Tag{project='" + project + "', name='" + name + ", creationDate='" + creationDate +
               "', attributes=" + attributes + ", metadata=" + getEntityMetadata() + '}';
    }
}
