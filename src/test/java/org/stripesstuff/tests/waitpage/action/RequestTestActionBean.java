/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.waitpage.action;

import org.stripesstuff.plugin.waitpage.WaitPage;
import org.testng.Assert;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;

/**
 * Test {@link WaitPage} annotation.
 * 
 * @author Christian Poitras
 */
@UrlBinding("/RequestTest.action")
public class RequestTestActionBean implements ActionBean {
    
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
     * Add an attribute that name matches stage.
     */
    @Before(stages=LifecycleStage.HandlerResolution)
    public void addHandlerResolutionAttribute() {
        context.getRequest().setAttribute("HandlerResolution", "HandlerResolutionAttribute");
        context.getRequest().getSession().setAttribute("HandlerResolution", "HandlerResolutionAttribute");
    }
    /**
     * Add an attribute that name matches stage.
     */
    @Before(stages=LifecycleStage.BindingAndValidation)
    public void addBindingAndValidationAttribute() {
        context.getRequest().setAttribute("BindingAndValidation", "BindingAndValidationAttribute");
        context.getRequest().getSession().setAttribute("BindingAndValidation", "BindingAndValidationAttribute");
    }
    /**
     * Add an attribute that name matches stage.
     */
    @Before(stages=LifecycleStage.CustomValidation)
    public void addCustomValidationAttribute() {
        context.getRequest().setAttribute("CustomValidation", "CustomValidationAttribute");
        context.getRequest().getSession().setAttribute("CustomValidation", "CustomValidationAttribute");
    }
    /**
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("requestTest")
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution requestTest() throws InterruptedException {
        Thread.sleep(200);
        Assert.assertNull(context.getRequest().getHeader("TestHeader"));
        Assert.assertNotNull(context.getRequest().getParameter("first"));
        Assert.assertNotNull(context.getRequest().getParameter("second"));
        Assert.assertEquals(context.getRequest().getAttribute("HandlerResolution"), null);
        Assert.assertEquals(context.getRequest().getAttribute("BindingAndValidation"), null);
        Assert.assertEquals(context.getRequest().getAttribute("CustomValidation"), null);
        Assert.assertEquals(context.getRequest().getSession().getAttribute("HandlerResolution"), "HandlerResolutionAttribute");
        Assert.assertEquals(context.getRequest().getSession().getAttribute("BindingAndValidation"), "BindingAndValidationAttribute");
        Assert.assertEquals(context.getRequest().getSession().getAttribute("CustomValidation"), "CustomValidationAttribute");
        context.getRequest().setAttribute("EventHandling", "EventHandlingAttribute");
        context.getRequest().getSession().setAttribute("EventHandling", "EventHandlingAttribute");
        result = first + second;
        this.complete = true;
        return new ForwardResolution("index.jsp");
    }
    /**
     * @return index.jsp
     * @throws InterruptedException
     */
    @HandlesEvent("requestTestAjax")
    @WaitPage(path="wait.jsp", refresh=1000, ajax="ajax.jsp")
    public Resolution requestTestAjax() throws InterruptedException {
        Thread.sleep(200);
        Assert.assertNull(context.getRequest().getHeader("TestHeader"), "test");
        Assert.assertNotNull(context.getRequest().getParameter("first"), String.valueOf(1));
        Assert.assertNotNull(context.getRequest().getParameter("second"), String.valueOf(2));
        Assert.assertEquals(context.getRequest().getAttribute("HandlerResolution"), null);
        Assert.assertEquals(context.getRequest().getAttribute("BindingAndValidation"), null);
        Assert.assertEquals(context.getRequest().getAttribute("CustomValidation"), null);
        Assert.assertEquals(context.getRequest().getSession().getAttribute("HandlerResolution"), "HandlerResolutionAttribute");
        Assert.assertEquals(context.getRequest().getSession().getAttribute("BindingAndValidation"), "BindingAndValidationAttribute");
        Assert.assertEquals(context.getRequest().getSession().getAttribute("CustomValidation"), "CustomValidationAttribute");
        context.getRequest().setAttribute("EventHandling", "EventHandlingAttribute");
        context.getRequest().getSession().setAttribute("EventHandling", "EventHandlingAttribute");
        result = first + second;
        this.complete = true;
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
