package dev.paoding.longan.data;

import dev.paoding.longan.data.jpa.Data;
import dev.paoding.longan.data.jpa.Database;
import dev.paoding.longan.data.jpa.SqlParser;

@Data(alias = "分页对象")
public class Pageable {
    /**
     * 第几页，从1开始，默认为第1页
     */
    private int page;
    /**
     * 每一页的大小，默认为20
     */
    private int size;
    /**
     * 排序属性，默认为 id
     */
    private String sort;
    /**
     * 是否倒序排列，默认为 true
     */
    private boolean desc;

    public Pageable(int page) {
        this(page, 20, "id", true);
    }

    public Pageable(int page, int size) {
        this(page, size, "id", true);
    }

    public Pageable(int page, int size, String sort) {
        this(page, size, sort, true);
    }

    public Pageable(int page, int size, String sort, boolean desc) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.desc = desc;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    private int offset() {
        return (page - 1) * size;
    }

    private int limit() {
        return size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String toSql() {
        StringBuilder sb = new StringBuilder();
        if (sort != null) {
            if (sort.contains(" ")) {
                throw new SecurityException("An error in SQL syntax " + sort);
            }
            sb.append(" order by ").append(SqlParser.toColumnName(sort));
            sb.append(desc ? " desc" : " asc");
        }

        if (Database.isPostgresql()) {
            sb.append(" offset ").append(offset()).append(" limit ").append(limit());
        } else if (Database.isMySQL()) {
            sb.append(" limit ").append(offset()).append(", ").append(limit());
        }
        return sb.toString();
    }

}
