package interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to set a property's name when serializing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.RECORD_COMPONENT,
        ElementType.FIELD,
})
public @interface PropertyName {
    String value();
}
