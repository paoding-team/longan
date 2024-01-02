package dev.paoding.longan.doc;

public class MetaAttribute extends MetaField {
    private long[] size;
    private String regexp;
    private Boolean notBlank;
    private Boolean notEmpty;
    private int validator;

    public int getValidator() {
        return validator;
    }

    public void setValidator(int validator) {
        this.validator = validator;
    }

    public long[] getSize() {
        return size;
    }

    public void setSize(long[] size) {
        this.size = size;
    }

    public Boolean getNotBlank() {
        return notBlank;
    }

    public void setNotBlank(Boolean notBlank) {
        this.notBlank = notBlank;
    }

    public Boolean getNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(Boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }
}
