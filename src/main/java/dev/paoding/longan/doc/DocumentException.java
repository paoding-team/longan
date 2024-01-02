package dev.paoding.longan.doc;


public class DocumentException extends RuntimeException {
    private static final long serialVersionUID = 0L;
    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public DocumentException(String message) {
        super(message);
    }

    public DocumentException(DocumentProblem documentProblem) {
        super(documentProblem.toString());
    }

    public DocumentException(DocumentProblem documentProblem, Throwable cause) {
        super(documentProblem.toString(), cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
