package dev.paoding.longan.doc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MetaNode {
    private String name;
    private String type;
    private String link;
    private Set<MetaNode> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<MetaNode> getChildren() {
        return children;
    }

    public void setChildren(Set<MetaNode> children) {
        this.children = children;
    }

    public void addChild(MetaNode metaNode){
        if(this.children == null){
            this.children = new HashSet<>();
        }
        this.children.add(metaNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaNode)) return false;
        MetaNode metaNode = (MetaNode) o;
        return Objects.equals(link, metaNode.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
