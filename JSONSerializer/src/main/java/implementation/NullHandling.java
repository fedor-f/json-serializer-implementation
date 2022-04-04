package implementation;

/**
 * Sets handling of null values for reference types.
 */
public enum NullHandling {
    /**
     * Excludes null value from string representation of an object.
     */
    EXCLUDE,

    /**
     * Includes null values to string representation of an object.
     */
    INCLUDE;

    /**
     * Checks if null values are excluded or included.
     *
     * @return true if included, otherwise, false
     */
    boolean isIncluded() {
        return switch (this) {
            case EXCLUDE -> false;
            case INCLUDE -> true;
        };
    }
}
