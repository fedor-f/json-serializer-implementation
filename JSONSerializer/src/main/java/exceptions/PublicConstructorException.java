package exceptions;

/**
 * An exception thrown when there is an attempt to serialize
 * an object whose class has no public constructor with no parameters
 */
public class PublicConstructorException extends RuntimeException {
    public PublicConstructorException(String message) {
        super(message);
    }
}
