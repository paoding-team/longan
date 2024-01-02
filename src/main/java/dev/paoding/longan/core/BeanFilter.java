package dev.paoding.longan.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BeanFilter {
    private Class<?> type;
    private final Set<String> includes = new HashSet<>();

     public Class<?> getType() {
          return type;
     }

     public void setType(Class<?> type) {
          this.type = type;
     }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        Collections.addAll(this.includes, includes);
    }


}
