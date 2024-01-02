package dev.paoding.longan.data.jpa;

public class Sorting {
    private String sort;
    private boolean desc;

    public Sorting() {

    }

    public static Sorting asc(String sort) {
        Sorting sorting = new Sorting();
        sorting.sort = sort;
        sorting.desc = false;
        return sorting;
    }

    public static Sorting desc(String sort) {
        Sorting sorting = new Sorting();
        sorting.sort = sort;
        sorting.desc = true;
        return sorting;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String toSql() {
        if (sort == null || sort.contains(" ")) {
            throw new SecurityException("An error in SQL syntax " + sort);
        }
        return SqlParser.toColumnName(sort) + (desc ? " desc" : " asc");
    }
}

