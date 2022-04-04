package interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark properties of types LocalTime, LocalDate and LocalDateTime
 * to set a format when serializing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD,
        ElementType.RECORD_COMPONENT
})
public @interface DateFormat {
    String pattern();
}
