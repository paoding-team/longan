package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaValidator {
    private int id;
    private String type;
    private List<MetaAttribute> fields = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addMetaAttribute(MetaAttribute metaAttribute) {
        fields.add(metaAttribute);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MetaAttribute> getFields() {
        return fields;
    }

    public void setFields(List<MetaAttribute> fields) {
        this.fields = fields;
    }
}
