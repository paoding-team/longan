package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Json;

import java.util.List;

public class MetaMethod {
    private String name;
    private String alias;
    private String type;
    private String description;
//    private boolean anonymous;
    @Json(serialize = false)
//    private Class<?>[] role;
    private String[] clients;
    private MetaRequest request;
    private MetaResponse response;
    private List<MetaFault> faults;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public MetaRequest getRequest() {
        return request;
    }

    public void setRequest(MetaRequest request) {
        this.request = request;
    }

    public MetaResponse getResponse() {
        return response;
    }

    public void setResponse(MetaResponse response) {
        this.response = response;
    }

    public String[] getClients() {
        return clients;
    }

    public void setClients(String[] clients) {
        this.clients = clients;
    }

    //    public Class<?>[] getRole() {
//        return role;
//    }
//
//    public void setRole(Class<?>[] role) {
//        this.role = role;
//    }

    public void setFaults(List<MetaFault> faults) {
        this.faults = faults;
    }
}
