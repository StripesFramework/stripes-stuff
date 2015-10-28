package org.stripesstuff.tests.session.action;

import org.stripesstuff.plugin.session.Session;
import org.stripesstuff.tests.session.bean.Person;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public abstract class AbstractPersonActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    /**
     * Person.
     */
    @Session
    private Person person;
    
    
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution("index.jsp");
    }
    public abstract Resolution reset();
    
    
    public Person getPerson() {
        return person;
    }
    public void setPerson(Person person) {
        this.person = person;
    }
}
