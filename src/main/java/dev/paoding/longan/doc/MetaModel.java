package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaModel {
    private String name;
    private String simpleName;
    private String alias;
    private String description;
    private List<MetaField> fields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MetaField> getFields() {
        return fields;
    }

    public void setFields(List<MetaField> fields) {
        this.fields = fields;
    }

    public void addMetaField(MetaField metaField){
        if(this.fields == null){
            this.fields = new ArrayList<>();
        }
        this.fields.add(metaField);
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
}
