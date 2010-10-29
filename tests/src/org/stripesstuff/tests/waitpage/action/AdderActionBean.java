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
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * Test {@link WaitPage} annotation.
 * 
 * @author Christian Poitras
 */
@UrlBinding("/Adder.action")
public class AdderActionBean implements ActionBean {
    
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
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000)
    public Resolution shortAdd() {
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should take less than delay.
     * @return index.jsp
     */
    @HandlesEvent("shortAddNoDelay")
    @WaitPage(path="wait.jsp", refresh=3000)
    public Resolution shortAddNoDelay() {
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 1 refresh.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAdd")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000)
    public Resolution oneRefreshAdd() throws InterruptedException {
        Thread.sleep(1200);
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 2 refresh.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("twoRefreshAdd")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000)
    public Resolution twoRefreshAdd() throws InterruptedException {
        Thread.sleep(2200);
        result = first + second;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition that throws an exception before delay ends.
     * @throws InterruptedException
     */
    @HandlesEvent("shortException")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000)
    public Resolution shortException() throws InterruptedException {
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception before delay ends.
     * @throws InterruptedException
     */
    @HandlesEvent("shortExceptionNoDelay")
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution shortExceptionNoDelay() throws InterruptedException {
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception after 1 refresh.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshException")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000)
    public Resolution oneRefreshException() throws InterruptedException {
        Thread.sleep(1200);
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception before delay ends.
     * WaitPage annotation also declares an error page.
     * @throws InterruptedException
     */
    @HandlesEvent("shortExceptionWithErrorPage")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000, error="error.jsp")
    public Resolution shortExceptionWithErrorPage() throws InterruptedException {
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that throws an exception after 1 refresh.
     * WaitPage annotation also declares an error page.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshExceptionWithErrorPage")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000, error="error.jsp")
    public Resolution oneRefreshExceptionWithErrorPage() throws InterruptedException {
        Thread.sleep(1200);
        throw new RuntimeException("this method always fail");
    }
    /**
     * Addition that completes before delay.
     * WaitPage annotation also declares an AJAX page to support AJAX updater.
     * @throws InterruptedException
     */
    @HandlesEvent("shortAjaxUpdater")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000, ajax="ajax.jsp")
    public Resolution shortAjaxUpdater() throws InterruptedException {
        result = first + second;
        this.complete = true;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition that completes after 1 refresh.
     * WaitPage annotation also declares an AJAX page to support AJAX updater.
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAjaxUpdater")
    @WaitPage(path="wait.jsp", delay=1000, refresh=1000, ajax="ajax.jsp")
    public Resolution oneRefreshAjaxUpdater() throws InterruptedException {
        Thread.sleep(2200);
        result = first + second;
        this.complete = true;
        return new ForwardResolution("index.jsp");
    }
    /**
     * Addition should demand exactly 1 refresh.
     * WaitPage annotation does not declare any delay.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("oneRefreshAddNoDelay")
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution oneRefreshAddNoDelay() throws InterruptedException {
        Thread.sleep(200);
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
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution sourcePage() throws InterruptedException {
        Thread.sleep(200);
        result = first + second;
        return context.getSourcePageResolution();
    }
    /**
     * An error is saved during custom validation if first > 100.
     */
    @ValidationMethod(on={"oneRefreshAdd", "oneRefreshAjaxUpdater"})
    public void customValidationError(ValidationErrors errors) throws InterruptedException {
        if (this.first > 100) {
            errors.addGlobalError(new SimpleError("Event could not complete normally."));
        }
    }
    /**
     * An error is saved during event execution.
     * Source page resolution is returned.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("validationError")
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution validationError() throws InterruptedException {
        Thread.sleep(200);
        context.getValidationErrors().addGlobalError(new SimpleError("Event could not complete normally."));
        return context.getSourcePageResolution();
    }
    /**
     * An error is saved during event execution.
     * Source page resolution is returned.
     * This event is intended to work with an AJAX updater.
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("validationErrorAjax")
    @WaitPage(path="wait.jsp", refresh=1000, ajax="ajax.jsp")
    public Resolution validationErrorAjax() throws InterruptedException {
        Thread.sleep(200);
        context.getValidationErrors().addGlobalError(new SimpleError("Event could not complete normally."));
        this.complete = true;
        return context.getSourcePageResolution();
    }
    /**
     * Add 2 numbers with no {@link WaitPage} annotation.
     * @return index.jsp
     */
    @HandlesEvent("addNoWait")
    public Resolution addNoWait() {
        result = first + second;
        return new ForwardResolution("index.jsp");
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
