/*
 * <p>Title: StripesStuff</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (http://www.ircm.qc.ca)</p>
 */
package org.stripesstuff.plugin.session;

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
     */
    boolean serializable() default true;
    /**
     * <p>Maximum time in minutes the field will stay in session if not accessed.</p>
     * <p>When maxTime is 0 or negative, the field will stay in session forever (unless not serializable).</p>
     */
    int maxTime() default -1;
    /**
     * <p>Key under which field will be stored.</p>
     * <p>This allows field sharing between 2 action beans.</p>
     */
    String key() default "";
}
