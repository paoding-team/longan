package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaFault {
    private String name;
    private String description;
    private Object sample;
    private List<MetaVar> vars;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addMetaVar(MetaVar metaVar){
        if(vars == null){
            vars = new ArrayList<>();
        }
        vars.add(metaVar);
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }
}
