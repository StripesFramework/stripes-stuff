package org.stripesstuff.plugin.waitpage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to send user to a wait page when event handling takes a long time to complete.
 * 
 * @author Aaron Porter
 * @author Christian Poitras
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface WaitPage {
    /**
     * Wait page location.
     */
    String path();
    /**
     * Delay allowed for event handler to complete before sending user to wait page.
     * If event handler completes before delay is expired, the result of event handler will be displayed directly.
     */
    int delay() default 0;
    /**
     * Time between each wait page refresh.
     */
    int refresh() default 0;
    /**
     * Redirect user to this page if event handler throws an exception.
     */
    String error() default "";
    /**
     * Page location for AJAX updater (usually used to display progression).
     */
    String ajax() default "";
}
