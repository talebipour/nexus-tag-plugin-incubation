package ir.sahab.nexus.plugin.tag.api;

import javax.validation.constraints.NotNull;

/**
 * Represent a component (artifact) which a tag can be associated with.
 */
public class AssociatedComponent {

    @NotNull
    private String repository;

    private String group;

    @NotNull
    private String name;

    @NotNull
    private String version;


    public AssociatedComponent() {
        // Used by Jackson
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