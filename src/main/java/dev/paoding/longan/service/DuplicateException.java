package dev.paoding.longan.service;

public class DuplicateException extends ServiceException {
    public DuplicateException(String message) {
        super(message);
        this.code = "duplicate.entry";
    }

    public DuplicateException(Class<?> type) {
        super("Duplicate entry for " + type.getSimpleName());
        this.code = "duplicate.entry." + type.getSimpleName().toLowerCase();
    }

    public DuplicateException(Class<?> type, String message) {
        super(message);
        this.code = "duplicate.entry." + type.getSimpleName().toLowerCase();
    }
}
