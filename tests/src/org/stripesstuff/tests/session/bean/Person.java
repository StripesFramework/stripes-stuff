/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (IRCM)</p>
 */
package org.stripesstuff.tests.session.bean;

/**
 * A person.
 * @author Christian Poitras
 */
public class Person {
    
    /**
     * First name.
     */
    private String firstName;
    /**
     * Last name.
     */
    private String lastName;
    
    
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
