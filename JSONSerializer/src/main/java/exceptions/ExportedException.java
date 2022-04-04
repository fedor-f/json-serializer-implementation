package exceptions;

/**
 * An exception thrown when there is an attempt to serialize an object
 * but its class is not marked @Exported.
 */
public class ExportedException extends RuntimeException {
    public ExportedException(String message) {
        super(message);
    }
}
