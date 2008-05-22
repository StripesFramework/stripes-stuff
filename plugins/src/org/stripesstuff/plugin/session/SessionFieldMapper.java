/*
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (http://www.ircm.qc.ca)</p>
 */
package org.stripesstuff.plugin.session;

/**
 * Map a field to current runnable.
 * @author Christian Poitras
 */
public class SessionFieldMapper {
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
