package org.stripesstuff.plugin.session;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * All field mappers registered in session.
 * @author Christian Poitras
 */
public class SessionMapper extends HashMap<String, SessionFieldMapper> implements HttpSessionActivationListener, HttpSessionBindingListener {
    
    static final long serialVersionUID = 5721789951704130560L; 
    
    // HttpSessionBindingListener
    /**
     * Cancel all threads from field mappers.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        for (SessionFieldMapper fieldMapper : this.values()) {
            if (fieldMapper.runnable != null) fieldMapper.runnable.cancel();
        }
    }
    public void valueBound(HttpSessionBindingEvent event) {
    }
    
    // HttpSessionActivationListener
    /**
     * Remove all non-serializable fields from session.
     */
    public void sessionWillPassivate(HttpSessionEvent event) {
        for (Entry<String, SessionFieldMapper> entry : this.entrySet())  {
            if (!entry.getValue().serializable) {
                event.getSession().removeAttribute(entry.getKey());
            }
        }
    }
    public void sessionDidActivate(HttpSessionEvent event) {
    }
}
