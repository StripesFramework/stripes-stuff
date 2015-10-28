package org.stripesstuff.tests.waitpage.action;

import org.stripesstuff.plugin.waitpage.WaitPage;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;

/**
 * Test messages used with {@link WaitPage} annotation.
 * 
 * @author Christian Poitras
 */
@UrlBinding("/Messages.action")
public class MessagesActionBean implements ActionBean {
    
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
     * Add a message in binding stage.
     */
    @Before(stages=LifecycleStage.BindingAndValidation)
    public void addBindingMessage() {
        context.getMessages().add(new SimpleMessage("BindingAndValidation"));
        context.getMessages("BindingAndValidation").add(new SimpleMessage("BindingAndValidation.BindingAndValidation"));
    }
    /**
     * Add a message in custom validation stage.
     */
    @Before(stages=LifecycleStage.CustomValidation)
    public void addValidationMessage() {
        context.getMessages().add(new SimpleMessage("CustomValidation"));
        context.getMessages("CustomValidation").add(new SimpleMessage("CustomValidation.CustomValidation"));
    }
    /**
     * Add a message in event handling stage.
     */
    @Before(stages=LifecycleStage.EventHandling)
    public void addEventMessage() {
        context.getMessages().add(new SimpleMessage("EventHandling"));
        context.getMessages("EventHandling").add(new SimpleMessage("EventHandling.EventHandling"));
    }
    
    
    /**
     * Addition should take one refresh.
     * @return index.jsp
     */
    @DefaultHandler
    @HandlesEvent("add")
    @WaitPage(path="wait.jsp", refresh=1000)
    public Resolution add() throws InterruptedException {
        Thread.sleep(200);
        result = first + second;
        context.getMessages().add(new SimpleMessage("add"));
        context.getMessages("add").add(new SimpleMessage("add.add"));
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
