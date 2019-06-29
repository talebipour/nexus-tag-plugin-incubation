package ir.sahab.nexus.plugin.tag.internal;

import org.sonatype.nexus.common.entity.AbstractEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tag which can be created using REST API.
 */
public class Tag extends AbstractEntity {

    private final String project;

    private final String artifact;

    private final String version;

    private Map<String, String> metadata = new HashMap<>();

    Tag(String project, String artifact, String version) {
        this.project = project;
        this.artifact = artifact;
        this.version = version;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
