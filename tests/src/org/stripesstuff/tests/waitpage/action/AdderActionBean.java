/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.waitpage.action;

import org.stripesstuff.plugin.waitpage.WaitPage;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

/**
 * Test {@link WaitPage} annotation.
 * 
 * @author Christian Poitras
 */
@UrlBinding("/Adder.action")
public class AdderActionBean implements ActionBean {
    
    private static final String COMPLETE_KEY = AdderActionBean.class.getName() + "#complete";
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    /**
     * First number to add.
     */
    @Validate
    private int first;
    /**
     * Second number to add.
     */
    @Validate
    private int second;
    /**
     * Result of addition of first and second numbers.
     */
    private int result;
    /**
     * True if event completed.
     */
    private boolean complete;
    
    
    /**
     * Addition should take less than delay.
     * @return index.jsp
     */
    @DefaultHandler
    @HandlesEvent("shortAdd")
    @WaitPage(path="wait.jsp", delay=100, refresh=100)
    public Resolution shortAdd() {
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 1 refresh.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAdd")
    @WaitPage(path="wait.jsp", delay=100, refresh=100)
    public Resolution oneRefreshAdd() throws InterruptedException {
        Thread.sleep(110);
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 2 refresh.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("twoRefreshAdd")
    @WaitPage(path="wait.jsp", delay=100, refresh=100)
    public Resolution twoRefreshAdd() throws InterruptedException {
        Thread.sleep(210);
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition that throws an exception before delay ends.
     * @throws InterruptedException
     */
    @HandlesEvent("shortException")
    @WaitPage(path="wait.jsp", delay=100, refresh=100)
    public Resolution shortException() throws InterruptedException {
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception after 1 refresh.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshException")
    @WaitPage(path="wait.jsp", delay=100, refresh=100)
    public Resolution oneRefreshException() throws InterruptedException {
        Thread.sleep(110);
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception before delay ends.
     * WaitPage annotation also declares an error page.
     * @throws InterruptedException
     */
    @HandlesEvent("shortExceptionWithErrorPage")
    @WaitPage(path="wait.jsp", delay=100, refresh=100, error="error.jsp")
    public Resolution shortExceptionWithErrorPage() throws InterruptedException {
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception after 1 refresh.
     * WaitPage annotation also declares an error page.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshExceptionWithErrorPage")
    @WaitPage(path="wait.jsp", delay=100, refresh=100, error="error.jsp")
    public Resolution oneRefreshExceptionWithErrorPage() throws InterruptedException {
        Thread.sleep(110);
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that completes before delay.
     * WaitPage annotation also declares an AJAX page to support AJAX updater.
     * @throws InterruptedException
     */
    @HandlesEvent("shortAjaxUpdater")
    @WaitPage(path="wait.jsp", delay=100, refresh=100, ajax="ajax.jsp")
    public Resolution shortAjaxUpdater() throws InterruptedException {
        result = first + second;
        context.getRequest().getSession().setAttribute(COMPLETE_KEY, "true");
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition that completes after 1 refresh.
     * WaitPage annotation also declares an AJAX page to support AJAX updater.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAjaxUpdater")
    @WaitPage(path="wait.jsp", delay=100, refresh=100, ajax="ajax.jsp")
    public Resolution oneRefreshAjaxUpdater() throws InterruptedException {
        Thread.sleep(220);
        result = first + second;
        this.complete = true;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Returns if method completed for AJAX.
     * @return if method completed for AJAX.
     * @throws InterruptedException
     */
    @HandlesEvent("hasCompleted")
    public Resolution hasCompleted() throws InterruptedException {
        if (context.getRequest().getSession().getAttribute(COMPLETE_KEY) == null) {
            return new StreamingResolution("text/plain", "false");
        } else {
            return new StreamingResolution("text/plain", (String)context.getRequest().getSession().getAttribute(COMPLETE_KEY));
        }
    }
    /**
     * Addition should demand exactly 1 refresh.
     * WaitPage annotation does not declare any delay.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAddNoDelay")
    @WaitPage(path="wait.jsp", refresh=100)
    public Resolution oneRefreshAddNoDelay() throws InterruptedException {
        Thread.sleep(10);
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 1 refresh.
     * Source page resolution is returned.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("sourcePage")
    @WaitPage(path="wait.jsp", refresh=100)
    public Resolution sourcePage() throws InterruptedException {
        Thread.sleep(10);
        result = first + second;
        return context.getSourcePageResolution();
    }
    
    
    public int getResult() {
        return result;
    }
    public int getFirst() {
        return first;
    }
    public void setFirst(int first) {
        this.first = first;
    }
    public int getSecond() {
        return second;
    }
    public void setSecond(int second) {
        this.second = second;
    }
    public boolean isComplete() {
        return complete;
    }
}
