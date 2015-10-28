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

/**
 * Test @Session annotation with negative maxTime attribute.
 * 
 * @author Christian Poitras
 */
public class NegativeMaxTimeActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    
    private Integer number;
    
    
    @Session(maxTime=-1)
    private Integer result;
    
    @Before(stages=LifecycleStage.EventHandling)
    public void startup() {
        if (result == null) result = 0;
    }
    
    
    @DefaultHandler
    @HandlesEvent("addNumber")
    public Resolution addNumber() {
        result += number;
        return new ForwardResolution("index.jsp");
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
