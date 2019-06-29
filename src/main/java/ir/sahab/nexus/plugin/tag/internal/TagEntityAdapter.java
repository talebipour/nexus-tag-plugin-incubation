package ir.sahab.nexus.plugin.tag.internal;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.sonatype.nexus.orient.entity.IterableEntityAdapter;

import java.util.Collections;

public class TagEntityAdapter extends IterableEntityAdapter<Tag> {

    private static final String TYPE_NAME = "tag";

    public TagEntityAdapter() {
        super(TYPE_NAME);
    }

    @Override
    protected void defineType(OClass oClass) {

    }

    @Override
    protected Tag newEntity() {
        return null;
    }

    @Override
    protected void readFields(ODocument oDocument, Tag tag) throws Exception {

    }

    @Override
    protected void writeFields(ODocument oDocument, Tag tag) throws Exception {

    }

    //Mock implementation TODO: Remove this
    @Override
    public Iterable<Tag> browse(ODatabaseDocumentTx db) {
        return Collections.singleton(new Tag("testProj", "testArtifact", "1.0"));
    }
}
