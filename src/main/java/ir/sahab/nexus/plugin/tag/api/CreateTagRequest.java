package ir.sahab.nexus.plugin.tag.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds fields used to create a new tag.
 */
public class CreateTagRequest {

    private String project;

    private String name;

    private Map<String, String> attributes = new HashMap<>();

    public CreateTagRequest() {
    }

    public CreateTagRequest(String project, String name, Map<String, String> attributes) {
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "CreateTagRequest{project='" + project + "', name='" + name + "', attributes=" + attributes + '}';
    }
}
