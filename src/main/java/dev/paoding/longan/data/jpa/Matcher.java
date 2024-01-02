package dev.paoding.longan.data.jpa;

import dev.paoding.longan.data.Between;

public class Matcher {
    private String comparer;
    private String field;
    private Between<?> between;

    public static Matcher of(String comparer, String field) {
        Matcher matcher = new Matcher();
        matcher.comparer = comparer;
        matcher.field = field;
        return matcher;
    }

    public static Matcher of(Between<?> between) {
        Matcher matcher = new Matcher();
        matcher.comparer = "Between";
        matcher.field = between.getField();
        matcher.between = between;
        return matcher;
    }

    public String getComparer() {
        return comparer;
    }

    public void setComparer(String comparer) {
        this.comparer = comparer;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Between<?> getBetween() {
        return between;
    }

    public void setBetween(Between<?> between) {
        this.between = between;
    }
}
