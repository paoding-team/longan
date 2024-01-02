package dev.paoding.longan.doc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Document {
    private Map<String, MetaNode> nodes = new HashMap<>();
    private Map<String, String> roles = new HashMap<>();
    private Map<String, MetaMethod> methods = new HashMap<>();
    private Map<String, String> codes = new LinkedHashMap<>();
    private Map<String, MetaModel> models = new HashMap<>();

    public Map<String, MetaNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, MetaNode> nodes) {
        this.nodes = nodes;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

    public Map<String, MetaMethod> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MetaMethod> methods) {
        this.methods = methods;
    }

    public Map<String, String> getCodes() {
        return codes;
    }

    public void setCodes(Map<String, String> codes) {
        this.codes = codes;
    }

    public Map<String, MetaModel> getModels() {
        return models;
    }

    public void setModels(Map<String, MetaModel> models) {
        this.models = models;
    }
}
