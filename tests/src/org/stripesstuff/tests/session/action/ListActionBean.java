/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.session.action;

import java.util.List;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.stripesstuff.plugin.session.Session;
import org.stripesstuff.tests.session.bean.Person;

/**
 * Used to test list of objects.
 * 
 * @author Christian Poitras
 */
public class ListActionBean implements ActionBean {
    
    private ActionBeanContext context;
    public ActionBeanContext getContext() {return context;}
    public void setContext(ActionBeanContext context) {this.context = context;}
    
    /**
     * Number list.
     */
    @Session
    private List<Integer> numbers;
    /**
     * Person list.
     */
    @Session
    private List<Person> persons;
    
    
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution("index.jsp");
    }
    
    
    public List<Person> getPersons() {
        return persons;
    }
    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
    public List<Integer> getNumbers() {
        return numbers;
    }
    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }
}
