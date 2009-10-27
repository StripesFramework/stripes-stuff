package org.stripesstuff.plugin.waitpage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.UrlBuilder;

/**
 * Interceptor documentation is explained in {@link WaitPage} documentation.
 * 
 * @author Aaron Porter
 * @author Christian Poitras
 * @see WaitPage
 */
@Intercepts({LifecycleStage.ActionBeanResolution,
                LifecycleStage.HandlerResolution,
                LifecycleStage.BindingAndValidation,
                LifecycleStage.CustomValidation,
                LifecycleStage.EventHandling,
                LifecycleStage.ResolutionExecution})
public class WaitPageInterceptor implements Interceptor {
    
    private static final Log log = Log.getInstance(WaitPageInterceptor.class);
    
    /**
     * Parameter used in wait page to find wait context.
     */
    private static final String ID_PARAMETER = "__WPI__";
    /**
     * URL used to invoke event handler (wait page annotation skips the event execution on normal request).
     */
    private static final String THREAD_URL = "/__WPI_THREAD__.wait";
    /**
     * Parameter used to known we are using an AJAX updater.
     */
    private static final String AJAX = "ajax";
    /**
     * Default refresh time if none is specified in wait page annotation.<br>
     * Default will refresh wait page every 60 seconds.
     */
    private static final int DEFAULT_REFRESH_TIMEOUT = 60000;
    /**
     * Saved wait context.
     */
    private static Map<Integer, Context> contexts = new ConcurrentHashMap<Integer, Context>();
    
    
    /**
     * Intercepts execution to handle {@link WaitPage} annotation and invoke the event in a background request.
     */
    public Resolution intercept(ExecutionContext executionContext) throws Exception {
        // Get wait context, if any.
        Context context = getContext(executionContext);
        LifecycleStage stage = executionContext.getLifecycleStage();
        
        if (executionContext.getActionBeanContext().getRequest().getRequestURI().contains(THREAD_URL)) {
            // The request we are processing is the one used to invoke event handler in background and to get resolution to send to user when event completes.
            // Since we are going to invoke the real event, the action bean and event must stay the same.
            // Binding and validation must be skipped since they were done in the first request.
            switch (executionContext.getLifecycleStage()) {
                // Use action bean that will process event.
                case ActionBeanResolution:
                    log.trace("injecting ActionBean");
                    // Since session can be lost from original request (Tomcat is a good example), request must be updated with the background request.
                    context.actionBean.setContext(executionContext.getActionBeanContext());
                    executionContext.setActionBean(context.actionBean);
                    // Skip normal action bean resolution.
                    return null;
                // Select event to call.
                case HandlerResolution:
                    log.trace("injecting event handler");
                    executionContext.setHandler(context.eventHandler);
                    // Skip normal handler resolution.
                    return null;
                // No biding or validation, it was done before a background request was created to handle event.
                case BindingAndValidation:
                case CustomValidation:
                    log.trace("skipping binding and validation");
                    // Skip binding and validation.
                    return null;
                // Execute event since we are in the background request.
                case EventHandling:
                    log.trace("executing event handler");
                    // Execute event and save exception if any.
                    try {
                        return executionContext.proceed();
                    }
                    catch (Exception e) {
                        synchronized(context.actionBean)
                        {
                            log.trace("setting exception in context");
                            context.throwable = e;
                            context.status = Context.Status.COMPLETE;
                            context.actionBean.notifyAll();
                            context.eventFlashScope = FlashScope.getCurrent(context.actionBean.getContext().getRequest(), false);
                        }
                        throw e;
                    }
                // Save resolution to send to user when he will refresh wait page.
                case ResolutionExecution:
                    synchronized(context.actionBean)
                    {
                        log.trace("setting resolution in context");
                        context.resolution = executionContext.getResolution();
                        context.status = Context.Status.COMPLETE;
                        context.actionBean.notifyAll();
                        context.eventFlashScope = FlashScope.getCurrent(context.actionBean.getContext().getRequest(), false);
                    }
                    
                    // Use a default resolution to prevent processing the one event returned.
                    executionContext.setResolution(new StreamingResolution("text/plain", "thread complete"));
                    return executionContext.proceed();
                case RequestInit:
                case RequestComplete:
                    return executionContext.proceed();
            }
        }
        else {
            if (LifecycleStage.ActionBeanResolution.equals(stage)) {
                if (context != null) {
                    // We are using a wait context for this request. Go to wait page or event's resolution depending on status and skip everything else.
                    return checkStatus(executionContext, context);
                }
            }
            else if (LifecycleStage.EventHandling.equals(stage)) {
                WaitPage annotation = executionContext.getHandler().getAnnotation(WaitPage.class);
                if (annotation != null) {
                    // Event has @WaitPage annotation. Create wait context to invoke event in background and redirect user to wait page.
                    return createContextAndRedirect(executionContext, annotation);
                }
            }
        }
        
        return executionContext.proceed();
    }
    
    /**
     * Returns wait context based on context id found in request.
     * @param executionContext execution context
     * @return wait context
     */
    private Context getContext(ExecutionContext executionContext) {
        // Get context id.
        HttpServletRequest request = executionContext.getActionBeanContext().getRequest();
        String parameter = request.getParameter(ID_PARAMETER);
        
        // Return context.
        if (parameter != null) {
            int id = Integer.parseInt(parameter, 16);
            return contexts.get(id);
        }
        
        return null;
    }
    
    /**
     * Create a wait context to execute event in background.
     * @param executionContext execution context
     * @param annotation wait page annotation
     * @return redirect redirect user so that wait page appears
     * @throws IOException could not create background request
     */
    private Resolution createContextAndRedirect(ExecutionContext executionContext, WaitPage annotation) throws IOException {
        // Create context used to call the event in background.
        Context context = this.createContext(executionContext);
        context.actionBean = executionContext.getActionBean();
        context.eventHandler = executionContext.getHandler();
        context.annotation = annotation;
        context.resolution = new ForwardResolution(annotation.path());
        context.bindingFlashScope = FlashScope.getCurrent(context.actionBean.getContext().getRequest(), false);
        int id = context.hashCode();
        
        // Id of context.
        String ids = Integer.toHexString(id);
        
        // Create background request to execute event.
        HttpServletRequest request = executionContext.getActionBeanContext().getRequest();
        UrlBuilder urlBuilder = new UrlBuilder(executionContext.getActionBeanContext().getLocale(), THREAD_URL, false);
        urlBuilder.addParameter(ID_PARAMETER, ids);
        if (context.bindingFlashScope != null) {
            urlBuilder.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, String.valueOf(context.bindingFlashScope.key()));
        }
        urlBuilder.addParameter(StripesConstants.URL_KEY_SOURCE_PAGE, CryptoUtil.encrypt(executionContext.getActionBeanContext().getSourcePage()));
        context.url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath() + urlBuilder.toString());
        context.cookies = request.getHeader("Cookie");
        
        // Save context.
        contexts.put(id, context);
        
        // Execute background request.
        context.thread = new Thread(context);
        context.thread.start();
        
        // Redirect user to wait page.
        return new RedirectResolution(StripesFilter.getConfiguration().getActionResolver().getUrlBinding(context.actionBean.getClass())) {
            @Override
            public RedirectResolution addParameter(String key, Object... value) {
                // Leave flash scope to background request.
                if (!StripesConstants.URL_KEY_FLASH_SCOPE_ID.equals(key)) {
                    return super.addParameter(key, value);
                }
                return this;
            }
        }.addParameter(ID_PARAMETER, ids);
    }
    /**
     * Returns a new instance of context.
     * @param executionContext execution context
     * @return new instance of context
     */
    protected Context createContext(ExecutionContext executionContext) {
        return new Context();
    }
    
    /**
     * Return wait page resolution or event's resolution depending on completion status.
     * @param executionContext execution context
     * @param context wait context
     * @return wait page resolution or event's resolution depending on completion status
     * @throws Exception exception thrown by event if no error page is specified
     */
    private Resolution checkStatus(ExecutionContext executionContext, Context context) throws Exception {
        // Resolution execution must not be executed while we are checking status.
        synchronized (context.actionBean) {
            if (context.status == Context.Status.INIT) {
                // If a delay was specified we'll just wait here. If event completes while we're waiting the wait page won't be displayed.
                if (context.annotation.delay() > 0) context.actionBean.wait(context.annotation.delay());
                
                if (context.status == Context.Status.INIT) {
                    // We've waited long enough, go to wait page.
                    context.status = Context.Status.WAITING;
                }
            }
            else if (context.status == Context.Status.WAITING) {
                // Wait some time to allow event to complete before refreshing wait page.
                log.trace("waiting to be signaled");
                context.actionBean.wait(context.annotation.refresh() > 0 ? context.annotation.refresh() : DEFAULT_REFRESH_TIMEOUT);
            }
            
            // Default is to go to wait page. This will be changed if an AJAX updater is used.
            // If event completed, this will be the resolution returned by event.
            Resolution resolution = context.resolution;
            // Action to use is the action bean on which event is invoked.
            executionContext.setActionBean(context.actionBean);
            // Save action bean in request scope to make it available in JSP.
            executionContext.getActionBeanContext().getRequest().setAttribute("actionBean", context.actionBean);
            // Set action bean in request so form will be populated.
            executionContext.getActionBeanContext().getRequest().setAttribute(StripesFilter.getConfiguration().getActionResolver().getUrlBinding(context.actionBean.getClass()), context.actionBean);
            // Copy flash scope/messages from action bean's context to execution context.
            if (context.bindingFlashScope != null) {
                this.copyFlashScope(context.bindingFlashScope, FlashScope.getCurrent(executionContext.getActionBeanContext().getRequest(), true));
            }
            if (context.eventFlashScope != null) {
                this.copyFlashScope(context.eventFlashScope, FlashScope.getCurrent(executionContext.getActionBeanContext().getRequest(), true));
            }
            
            if (executionContext.getActionBeanContext().getRequest().getParameter(AJAX) != null)
            {
                // We are using an AJAX updater. We need to go to AJAX page to allow javascript to validate if event completed.
                resolution = new ForwardResolution(context.annotation.ajax().length() > 0 ? context.annotation.ajax() : context.annotation.path());
            }
            else if (context.status == Context.Status.COMPLETE)
            {
                log.trace("the processor is finished so we'll remove it from the map");
                // Remove context since event completed and we will show resolution returned by event.
                contexts.remove(context.hashCode());
                // Copy errors from action bean's context to execution context.
                this.copyErrors(context.actionBean.getContext(), executionContext.getActionBeanContext());
                // Replace request in action bean so that session will be valid.
                context.actionBean.setContext(executionContext.getActionBeanContext());
                
                if (context.throwable != null) {
                    // Event did not complete normally, it thrown an exception.
                    if (("".equals(context.annotation.error())) && (context.throwable instanceof Exception)) {
                        // No error page, throw exception.
                        throw (Exception) context.throwable;
                    }
                    else {
                        // An error page is specified, save error and go to error page.
                        executionContext.getActionBeanContext().getRequest().setAttribute("exception", context.throwable);
                        resolution = new ForwardResolution(context.annotation.error());
                    }
                }
                // Stripes or user code may use executionContext.getResolution() to obtain resolution instead of returned resolution. So set resolution in executionContext too.
                executionContext.setResolution(resolution);
            }
            
            // Since context in current execution context is artificial, we should not update context in action bean as it would make action bean in other thread inconsistent.
            //context.actionBean.setContext(executionContext.getActionBeanContext());
            
            // Go to wait page or execute resolution from event, if it completed.
            return resolution;
        }
    }
    
    /**
     * Copy errors from a context to another context.
     * @param source source containing errors to copy
     * @param destination where errors will be copied
     */
    protected void copyErrors(ActionBeanContext source, ActionBeanContext destination) {
        destination.getValidationErrors().putAll(source.getValidationErrors());
    }
    /**
     * Copy source flash scope content (including messages) from to destination flash scope.
     * @param source flash scope to copy
     * @param destination where source flash scope content will be copied
     */
    protected void copyFlashScope(FlashScope source, FlashScope destination) {
        destination.putAll(source);
    }
}