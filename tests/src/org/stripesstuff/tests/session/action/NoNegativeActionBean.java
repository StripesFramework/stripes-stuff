/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (IRCM)</p>
 */
package org.stripesstuff.tests.session.action;

import org.stripesstuff.plugin.session.Session;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * Same as {@link SessionActionBean}, but will not allow result to be negative.
 * 
 * @author Christian Poitras
 */
public class NoNegativeActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    
    private Integer number;
    
    
    @Session
    private Integer result;
    
    @Before(stages=LifecycleStage.EventHandling)
    public void startup() {
        if (result == null) result = 0;
    }
    
    
    @DefaultHandler
    @HandlesEvent("addNumber")
    public Resolution addNumber() {
        if (result + number < 0) {
            context.getValidationErrors().add("result", new SimpleError("Result must never negative."));
            return new ForwardResolution("index.jsp");
        }
        result += number;
        return new ForwardResolution("index.jsp");
    }
    /**
     * This is the same as {@link #addNumber()}, but this event has a custom validation.
     */
    @HandlesEvent("customAddNumber")
    public Resolution customAddNumber() {
        result += number;
        return new ForwardResolution("index.jsp");
    }
    @ValidationMethod(on="customAddNumber")
    public void validateCustomAddNumber(ValidationErrors errors) {
        if (result + number < 0) {
            errors.add("result", new SimpleError("Result must never negative."));
        }
    }
    
    
    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public Integer getResult() {
        return result;
    }
    public void setResult(Integer result) {
        this.result = result;
    }
}
