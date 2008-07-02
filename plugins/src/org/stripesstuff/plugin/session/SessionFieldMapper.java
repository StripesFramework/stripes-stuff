/*
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (http://www.ircm.qc.ca)</p>
 */
package org.stripesstuff.plugin.session;

import java.io.Serializable;

/**
 * Map a field to current runnable.
 * @author Christian Poitras
 */
public class SessionFieldMapper implements Serializable {
    
    static final long serialVersionUID = 1209561911164455936L;
    
    /**
     * Thread that will remove field from session.
     */
    RemoveFieldRunnable runnable;
    /**
     * Field is serializable.
     */
    boolean serializable;
    SessionFieldMapper(boolean serializable) {
        this.serializable = serializable;
    }
}
