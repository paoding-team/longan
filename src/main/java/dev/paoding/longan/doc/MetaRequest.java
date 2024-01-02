package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaRequest {
    private List<MetaParam> params = new ArrayList<>();
    private Object sample;
    private List<MetaValidator> validators = new ArrayList<>();

    public void addMetaValidator(MetaValidator metaValidator){
        validators.add(metaValidator);
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    public List<MetaParam> getParams() {
        return params;
    }

    public void setParams(List<MetaParam> params) {
        this.params = params;
    }

    public void addMetaParam(MetaParam metaParam){
        params.add(metaParam);
    }
}
