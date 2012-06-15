package org.stripesstuff.plugin.waitpage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sourceforge.stripes.action.ActionBeanContext;

/**
 * Used to send user to a wait page when event handling takes a long time to complete.
 * <p>
 * Event will be invoked by a background request and the user will be redirected to a wait page.
 * </p>
 * <p>
 * The wait page must refresh itself to allow the resolution returned by event to be executed.
 * To refresh wait page, it can contain a <code>&lt;meta http-equiv="refresh" content="0"/&gt;</code> tag or it can use an AJAX
 * updater to know when to refresh page.
 * </p>
 * <p>
 * The expected flow when using a simple wait page is:
 * <ol>
 * <li>
 * ActionBeanResolution, HandlerResolution, BindingAndValidation, CustomValidation are executed the same way as if event didn't
 * contain a WaitPage annotation.
 * </li>
 * <li>
 * A resolution is returned to wait for event to complete.
 * </li>
 * <li>
 * If event completes before delay, resolution returned by event is executed. Flow ends immediately.
 * </li>
 * <li>
 * If delay expired and event didn't complete, wait page is returned.
 * </li>
 * <li>
 * Wait page is refreshed until event completes.
 * </li>
 * <li>
 * Event resolution is executed.
 * </li>
 * </ol>
 * </p>
 * <p>
 * If an AJAX updater is used, your page must have a way to known when to refresh itself. It can
 * be done by putting an indicator in your action bean that will be flagged once event completes.<br>
 * Examples :<br>
 * <code>
 * public Resolution myEvent {<br>
 *     Do stuff...<br>
 *     completeIndicator = true;<br>
 * }<br>
 * </code>
 * Then your AJAX page must inform the AJAX updater to refresh wait page.<br>
 * The expected flow when using an AJAX updater is:
 * <ol>
 * <li>
 * ActionBeanResolution, HandlerResolution, BindingAndValidation, CustomValidation are executed the same as if event didn't
 * contain a WaitPage annotation.
 * </li>
 * <li>
 * A resolution is returned to wait for delay.
 * </li>
 * <li>
 * If event completes before delay, resolution returned by event is executed. Flow ends immediately.
 * </li>
 * <li>
 * If delay expired and event didn't complete, wait page is returned.
 * </li>
 * <li>
 * AJAX updater in wait page will now make requests to the same URL as wait page would do for refresh, but a non-empty parameter
 * named "ajax" must be added to request.
 * </li>
 * <li>
 * Once indicator is flagged, AJAX updater must refresh wait page.
 * </li>
 * <li>
 * Event resolution is executed.
 * </li>
 * </ol>
 * </p>
 * <p>
 * How validation errors are handled:
 * <ul>
 * <li>
 * If a validation error occurs during any stages preceding event EventHandling, WaitPage annotation will have no effects.
 * </li>
 * <li>
 * If a validation error occurs during EventHandling, event resolution is execute and errors are available in
 * {@link ActionBeanContext#getValidationErrors()} and Stripes's errors tag can be used normally.
 * Event can return {@link ActionBeanContext#getSourcePageResolution()}.
 * </li>
 * </ul>
 * </p>
 * <p>
 * In case the event throws an exception, you can specify an error page that the user will be forwarded to. If no error page is
 * specified, the exception will be handled by stripes (or any exception handler registered by stripes).<br>
 * Note that if event throws an exception and no error page is specified, the exception will be handled twice by stripes:
 * once for the event invocation in background and once again when wait page refreshes.
 * </p>
 * <p>
 * It is recommended to use session instead of request to store attributes when using WaitPage.<br> 
 * The request present in action bean during event execution is <strong>NOT</strong> the same request as the one in preceding lifecycle stages.
 * So all headers and parameters set before event handling will <strong>NOT</strong> be available in event.<br>
 * <strong>
 * Request attributes set before EventHandling stage will not be available in event.<br>
 * </strong>
 * <strong>
 * Request headers and parameters will not be available in resolution.<br>
 * </strong>
 * <strong>
 * Request attributes set before resolution stage will not be available in resolution.<br>
 * </strong>
 * <strong>
 * Form tags values must be coming from action bean since no request parameter will be available.
 * </strong>
 * </p>
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
     * Delay in milliseconds allowed for event to complete before sending user to wait page.<br>
     * If event completes before delay is expired, the resolution returned by event will be executed directly and wait page
     * will never be shown.
     */
    int delay() default 0;
    /**
     * Time between each wait page refresh in milliseconds.
     */
    int refresh() default 0;
    /**
     * Forward user to this page if event throws an exception.
     */
    String error() default "";
    /**
     * Page location for AJAX updater (usually used to display progression).
     */
    String ajax() default "";
}
