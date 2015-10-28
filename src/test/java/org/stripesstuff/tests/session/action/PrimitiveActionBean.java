package org.stripesstuff.tests.session.action;

import org.stripesstuff.plugin.session.Session;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;

/**
 * Test @Session annotation with primitives.
 * 
 * @author Christian Poitras
 */
public class PrimitiveActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    
    private int number;
    
    
    @Session
    private int result;
    
    
    @DefaultHandler
    @HandlesEvent("addNumber")
    public Resolution addNumber() {
        result += number;
        return new ForwardResolution("index.jsp");
    }
    
    
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public int getResult() {
        return result;
    }
    public void setResult(int result) {
        this.result = result;
    }
}
