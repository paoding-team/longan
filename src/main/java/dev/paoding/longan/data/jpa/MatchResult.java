package dev.paoding.longan.data.jpa;

import java.util.HashMap;
import java.util.Map;

public class MatchResult {
    private String where;
    private Map<String, Object> paramMap;

    public static MatchResult empty() {
        MatchResult matchResult = new MatchResult();
        matchResult.where = "";
        matchResult.paramMap = new HashMap<>();
        return matchResult;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }
}
