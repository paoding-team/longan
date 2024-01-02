package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaResponse {
    private MetaParam param;
    private Object sample;
    private List<MetaFault> faults;
//    @Json(serialize = false)
    private List<MetaFilter> filters = new ArrayList<>();

    public void addMetaFilter(MetaFilter metaFilter){
        filters.add(metaFilter);
    }

    public List<MetaFault> getFaults() {
        return faults;
    }

    public void setFaults(List<MetaFault> faults) {
        this.faults = faults;
    }

    public List<MetaFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MetaFilter> filters) {
        this.filters = filters;
    }

    public MetaParam getParam() {
        return param;
    }

    public void setParam(MetaParam param) {
        this.param = param;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    public void addMetaFault(MetaFault metaFault) {
        if (faults == null) {
            faults = new ArrayList<>();
        }
        faults.add(metaFault);
    }
}
