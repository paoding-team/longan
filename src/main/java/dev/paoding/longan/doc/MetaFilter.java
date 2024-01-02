package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Json;

import java.util.ArrayList;
import java.util.List;

public class MetaFilter {
    private String type;
    private List<MetaField> fields = new ArrayList<>();
    @Json(serialize = false)
    private List<String> includes = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<MetaField> getFields() {
        return fields;
    }

    public void setFields(List<MetaField> fields) {
        this.fields = fields;
    }

    public void addMetaField(MetaField metaField){
        fields.add(metaField);
    }

}
