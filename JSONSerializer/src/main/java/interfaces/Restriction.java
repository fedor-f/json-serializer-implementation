package interfaces;

/**
 * An interface for classes where it is necessary to check objects classes for restrictions.
 */
public interface Restriction {
    /**
     * Checks if class has a constructor with no parameters.
     *
     * @param obj an object to check
     * @return true if class has a constructor with no parameters, otherwise, false
     */
    boolean hasConstructor(Object obj);
}
