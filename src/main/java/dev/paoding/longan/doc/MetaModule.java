package dev.paoding.longan.doc;

import java.util.ArrayList;
import java.util.List;

public class MetaModule {
    private String name;
    private String model;
    private List<String> methods = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public void addMetaMethod(String method){
        this.methods.add(method);
    }
}
