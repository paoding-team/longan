package dev.paoding.longan.data.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Operator<T> {
    private StringBuilder whereBuilder;
    private List<Object> whereParams;
    private MetaTable<T> metatable;

    public Operator(MetaTable<T> metatable, String key) {
        this.metatable = metatable;
        this.whereBuilder = new StringBuilder();
        this.whereParams = new ArrayList<>();
        whereBuilder.append(metatable.getColumnName(key));
    }

    public StringBuilder getWhereBuilder(){
        return whereBuilder;
    }

    public List getWhereParams(){
        return whereParams;
    }

    public Operator<T> and(String key) {
        whereBuilder.append(" AND " + metatable.getColumnName(key));
        return this;
    }


    public Operator<T> and(Operator operator) {
        whereBuilder.append(" AND (" + operator.toString() + ")");
        whereParams.addAll(operator.getWhereParams());
        return this;
    }

    public Operator<T> or(String key) {
        whereBuilder.append(" OR " + metatable.getColumnName(key));
        return this;
    }

    public Operator<T> or(Operator operator) {
        whereBuilder.append(" OR (" + operator.toString() + ")");
        whereParams.addAll(operator.getWhereParams());
        return this;
    }

    public Operator<T> is(Object o) {
        whereBuilder.append(" = ?");
        whereParams.add(o);
        return this;
    }

    public Operator ne(Object o) {
        whereBuilder.append(" != ?");
        whereParams.add(o);
        return this;
    }

    public Operator lt(Object o) {
        whereBuilder.append(" < ?");
        whereParams.add(o);
        return this;
    }

    public Operator lte(Object o) {
        whereBuilder.append(" <= ?");
        whereParams.add(o);
        return this;
    }

    public Operator gt(Object o) {
        whereBuilder.append(" > ?");
        whereParams.add(o);
        return this;
    }

    public Operator gte(Object o) {
        whereBuilder.append(" >= ?");
        whereParams.add(o);
        return this;
    }

    public Operator in(Object... o) {
        in(Arrays.asList(o));
        return this;
    }

    public Operator in(Collection<?> c) {
        whereBuilder.append(" IN ?");
        whereParams.add(c);
        return this;
    }

    public Operator nin(Object... o) {
        return nin(Arrays.asList(o));
    }

    public Operator nin(Collection<?> c) {
        whereBuilder.append(" NOT IN ?");
        whereParams.add(c);
        return this;
    }


}
