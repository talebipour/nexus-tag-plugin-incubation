package ir.sahab.nexus.plugin.tag.internal;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.nexus.orient.entity.IterableEntityAdapter;

/**
 * Provides persisting/reading tags from database.
 */
public class TagEntityAdapter extends IterableEntityAdapter<TagEntity> {

    private static final String TYPE_NAME = "tag";

    private static final String DB_CLASS = new OClassNameBuilder().type(TYPE_NAME).build();

    private static final String NAME_FIELD = "name";
    private static final String PROJECT_FIELD = "project";
    private static final String ATTRIBUTES_FIELD = "attributes";
    private static final String CREATION_DATE_FIELD = "creationDate";

    private static final String NAME_INDEX = new OIndexNameBuilder().type(DB_CLASS).property("name").build();
    private static final String PROJECT_INDEX = new OIndexNameBuilder().type(DB_CLASS).property(PROJECT_FIELD).build();

    public TagEntityAdapter() {
        super(TYPE_NAME);
    }

    @Override
    protected void defineType(OClass type) {
        type.createProperty(PROJECT_FIELD, OType.STRING) .setMandatory(true).setNotNull(true);
        type.createProperty(NAME_FIELD, OType.STRING).setMandatory(true).setNotNull(true);
        type.createProperty(CREATION_DATE_FIELD, OType.DATETIME).setMandatory(true).setNotNull(true);
        type.createProperty(ATTRIBUTES_FIELD, OType.EMBEDDEDMAP).setMandatory(true).setNotNull(true);

        type.createIndex(NAME_INDEX, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, NAME_FIELD);
        type.createIndex(PROJECT_INDEX, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, PROJECT_FIELD);
        type.createIndex(CREATION_DATE_FIELD, INDEX_TYPE.NOTUNIQUE, CREATION_DATE_FIELD);

    }

    @Override
    protected TagEntity newEntity() {
        return new TagEntity();
    }

    @Override
    protected void readFields(ODocument oDocument, TagEntity tag) {
        tag.setProject(oDocument.field(PROJECT_FIELD));
        tag.setName(oDocument.field(NAME_FIELD));
        tag.setCreationDate(oDocument.field(CREATION_DATE_FIELD));
        tag.setAttributes(oDocument.field(ATTRIBUTES_FIELD));
    }

    @Override
    protected void writeFields(ODocument oDocument, TagEntity tag) {
        oDocument.field(PROJECT_FIELD, tag.getProject());
        oDocument.field(NAME_FIELD, tag.getName());
        oDocument.field(CREATION_DATE_FIELD, tag.getCreationDate());
        oDocument.field(ATTRIBUTES_FIELD, tag.getAttributes());
    }

    public Optional<TagEntity> findById(ODatabaseDocumentTx tx, EntityId id) {
        TagEntity entity = transformEntity(document(tx, id));
        return Optional.ofNullable(entity);
    }

    public Iterable<TagEntity> search(ODatabaseDocumentTx tx, String project, String name,
            Map<String, String> attributes) {
        List<QueryPredicate> predicates = new ArrayList<>();
        if (project != null) {
            predicates.add(new QueryPredicate(PROJECT_FIELD, "=", stringLiteral(project)));
        }
        if (name != null) {
            predicates.add(new QueryPredicate(NAME_FIELD, "=", stringLiteral(name)));
        }

        for (Entry<String, String> entry : attributes.entrySet()) {
            String field = ATTRIBUTES_FIELD + "[" + stringLiteral(entry.getKey()) + "]";
            predicates.add(new QueryPredicate(field, "=", stringLiteral(entry.getValue())));
        }

        String query = buildQuery(predicates);
        log.info("Searching for tags with query={}", query);
        List<ODocument> documents = tx.query(new OSQLSynchQuery<>(query));
        return transform(documents);
    }

    /**
     * @return SQL string literal for given value (enclosed in single quotes)
     */
    private static String stringLiteral(String value) {
        return "'" + value + "'";
    }

    private static String buildQuery(List<QueryPredicate> predicates) {
        StringBuilder query = new StringBuilder("select * from ").append(DB_CLASS);
        Iterator<QueryPredicate> it = predicates.iterator();
        if (it.hasNext()) {
            query.append(" where");
        }
        while (it.hasNext()) {
            QueryPredicate predicate = it.next();
            query.append(' ').append(predicate.field)
                    .append(' ')
                    .append(predicate.operator)
                    .append(' ')
                    .append(predicate.value);
            if (it.hasNext()) {
                query.append(" and");
            }
        }
        query.append(" order by ").append(CREATION_DATE_FIELD).append(" DESC");
        return query.toString();
    }


    private static class QueryPredicate {
        private final String field;
        private final String operator;
        private  final String value;

        public QueryPredicate(String field, String operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }
}
