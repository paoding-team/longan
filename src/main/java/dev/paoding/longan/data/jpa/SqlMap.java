package dev.paoding.longan.data.jpa;

import java.util.HashMap;
import java.util.Map;

public class SqlMap {
    private Map<String, Object> map = new HashMap<>();

    public static SqlMap of() {
        SqlMap sqlMap = new SqlMap();
        return sqlMap;
    }

    public static SqlMap of(String key, Object value) {
        SqlMap sqlMap = new SqlMap();
        sqlMap.put(key, value);
        return sqlMap;
    }

    public SqlMap put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    protected Map<String, Object> build() {
        return map;
    }
}
