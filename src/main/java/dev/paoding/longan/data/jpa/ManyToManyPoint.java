package dev.paoding.longan.data.jpa;

import java.util.ArrayList;
import java.util.List;

public class ManyToManyPoint {
    private Class<?> master;
    private Class<?> slaver;
    private List<String> roleList = new ArrayList<>();
    private List<String> tableList = new ArrayList<>();

    public ManyToManyPoint(Class<?> master, Class<?> slaver) {
        this.master = master;
        this.slaver = slaver;
    }

    public Class<?> getMaster() {
        return master;
    }

    public void setMaster(Class<?> master) {
        this.master = master;
    }

    public Class<?> getSlaver() {
        return slaver;
    }

    public void setSlaver(Class<?> slaver) {
        this.slaver = slaver;
    }

    public void addRole(String role) {
        roleList.add(role);
        String masterTableName = SqlParser.toDatabaseName(master.getSimpleName());
        String slaverTableName = SqlParser.toDatabaseName(slaver.getSimpleName());
        String table = SqlParser.getLinkTable(masterTableName, slaverTableName, role);
        tableList.add(table);
    }

    public boolean hasRole(){
        return roleList.size() > 0;
    }

    public List<String> getTableList() {
        return tableList;
    }
}
