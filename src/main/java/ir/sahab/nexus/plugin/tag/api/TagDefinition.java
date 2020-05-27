package ir.sahab.nexus.plugin.tag.api;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Holds fields used to create a new tag.
 */
public class TagDefinition {

    @NotNull
    protected String name;

    @NotNull
    protected Map<String, String> attributes;

    @NotNull
    protected List<AssociatedComponent> components;

    public TagDefinition() {
    }

    public TagDefinition(String name, Map<String, String> attributes, List<AssociatedComponent> components) {
        this.name = name;
        this.attributes = attributes;
        this.components = components;
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

    public List<AssociatedComponent> getComponents() {
        return components;
    }

    public void setComponents(List<AssociatedComponent> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "CreateTagRequest{name='" + name + "', attributes=" + attributes + '}';
    }

    /**
     * Represent a component (artifact) which a tag can be associated with.
     */
    public static class AssociatedComponent {

        @NotNull
        private String repository;

        private String group;

        @NotNull
        private String name;

        @NotNull
        private String version;


        public AssociatedComponent() {
        }

        public AssociatedComponent(String repository, String group, String name, String version) {
            this.repository = repository;
            this.group = group;
            this.name = name;
            this.version = version;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return group + ":" + name + ":" + version;
        }
    }
}
