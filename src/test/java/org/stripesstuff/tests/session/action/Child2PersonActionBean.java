package org.stripesstuff.tests.session.action;

import org.stripesstuff.tests.session.bean.Person;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Child2PersonActionBean extends AbstractPersonActionBean {
    
    
    public Resolution reset() {
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Smith");
        super.setPerson(person);
        return new ForwardResolution("index.jsp");
    }
}
