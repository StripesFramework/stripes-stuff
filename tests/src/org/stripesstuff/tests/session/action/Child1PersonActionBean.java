/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (IRCM)</p>
 */
package org.stripesstuff.tests.session.action;

import org.stripesstuff.tests.session.bean.Person;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Child1PersonActionBean extends AbstractPersonActionBean {
    
    
    public Resolution reset() {
        Person person = new Person();
        person.setFirstName("Christian");
        person.setLastName("Poitras");
        super.setPerson(person);
        return new ForwardResolution("index.jsp");
    }
}
