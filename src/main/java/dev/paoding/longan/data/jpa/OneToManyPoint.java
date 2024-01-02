package dev.paoding.longan.data.jpa;

public class OneToManyPoint {
    private Class<?> master;
    private Class<?> slaver;
    private String joinField;
    private boolean orphanRemoval;
    private int slaverQuantity = 1;

    public OneToManyPoint(Class<?> master, Class<?> slaver) {
        this.master = master;
        this.slaver = slaver;
    }

    public int getSlaverQuantity() {
        return slaverQuantity;
    }

    public void addSlaver(){
        slaverQuantity ++;
    }

    public Class<?> getSlaver() {
        return slaver;
    }

    public String getJoinField() {
        return joinField;
    }

    public void setJoinField(String joinField) {
        this.joinField = joinField;
    }

    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    public void setOrphanRemoval(boolean orphanRemoval) {
        this.orphanRemoval = orphanRemoval;
    }

    @Override
    public String toString() {
        return "OneToManyPoint{" +
                "master=" + master +
                ", slaver=" + slaver +
                ", joinField='" + joinField + '\'' +
                ", orphanRemoval=" + orphanRemoval +
                '}';
    }
}
