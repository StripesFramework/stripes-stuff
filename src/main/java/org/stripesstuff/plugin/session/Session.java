package org.stripesstuff.plugin.session;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that a field should be stored in session.
 * 
 * @author Christian Poitras
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Session {
    /**
     * Indicate if field is serializable.
     * <p>If the field does not implement {@link Serializable}, field is automatically considered non-serializable.</p>
     * 
     * @return true if field is serializable, false otherwise
     */
    boolean serializable() default true;
    /**
     * <p>Maximum time in minutes the field will stay in session if not accessed.</p>
     * <p>When maxTime is 0 or negative, the field will stay in session forever (unless not serializable).</p>
     * 
     * @return maximum time in minutes the field will stay in session if not accessed
     */
    int maxTime() default -1;
    /**
     * <p>Key under which field will be stored.</p>
     * <p>This allows field sharing between 2 action beans.</p>
     * 
     * @return key under which field will be stored
     */
    String key() default "";
}
