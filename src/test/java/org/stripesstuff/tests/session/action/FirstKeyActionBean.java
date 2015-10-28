package org.stripesstuff.tests.session.action;

import org.stripesstuff.plugin.session.Session;
import org.stripesstuff.tests.session.bean.Person;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;

/**
 * Test key attribute of {@link Session}
 * 
 * @author Christian Poitras
 */
public class FirstKeyActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    /**
     * A person.
     */
    @Session(key="FirstKeyActionBean#person")
    private Person person;
    
    
    @DefaultHandler
    public Resolution input() {
        return new ForwardResolution("index.jsp");
    }
    
    
    /**
     * Removes person object from session.
     * @return Index page.
     */
    @HandlesEvent("remove")
    public Resolution remove() {
        person = null;
        return new ForwardResolution("index.jsp");
    }
    
    
    public Person getPerson() {
        return person;
    }
    public void setPerson(Person person) {
        this.person = person;
    }
}
