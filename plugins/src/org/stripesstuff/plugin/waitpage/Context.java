package org.stripesstuff.plugin.waitpage;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.util.Log;

/**
 * Wait context.
 * 
 * @author Aaron Porter
 * @author Christian Poitras
 */
public class Context implements Runnable {
    
    /**
     * Status of event invocation.
     * @author Aaron Porter
     * @author Christian Poitras
     */
    public enum Status {
        /**
         * Event has not been invoked yet or we never went to wait page.
         */
        INIT,
        /**
         * Event has been invoke and we are waiting for it to complete.
         */
        WAITING,
        /**
         * Event has completed and either gave us a resolution (possibly null) or an exception.
         */
        COMPLETE
    }
    
    
    private static final Log log = Log.getInstance(Context.class);
    
    
    /**
     * URL used to invoke event.
     */
    public URL url;
    /**
     * Status of event invocation.
     */
    public Status status = Status.INIT;
    /**
     * Action bean on which event is invoked.
     */
    public ActionBean actionBean;
    /**
     * Flash scope coming from Binding/Validation stages in case some information was saved in it.
     */
    public FlashScope bindingFlashScope;
    /**
     * Flash scope coming from EventHandling stage in case some information was saved in it.
     */
    public FlashScope eventFlashScope;
    /**
     * Event to invoke.
     */
    public Method eventHandler;
    /**
     * Default resolution to execute on current request.
     * If we are waiting, it will be wait page.
     * If event completed, it will be the resolution returned by event.
     */
    public Resolution resolution;
    /**
     * Exception thrown by event, if any.
     */
    public Throwable throwable;
    /**
     * Wait page annotation of event.
     */
    public WaitPage annotation;
    /**
     * Thread used to invoke event in background.
     */
    public Thread thread;
    /**
     * Cookies identifying user.
     */
    public String cookies;
    
    /**
     * Invoke event in a background request.
     */
    public void run()
    {
        try
        {
            // Open connection to URL calling this interceptor.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Pass in the session id cookie to maintain the same session.
            log.trace("passing in cookies: ", cookies);
            connection.setRequestProperty("Cookie", cookies);
            // Invoke event in background.
            connection.getContent();
        }
        catch (Exception e)
        {
            // Log any exception that could have occurred.
            log.error(e);
        }
    }
}
