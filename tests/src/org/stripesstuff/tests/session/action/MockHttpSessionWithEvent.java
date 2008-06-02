/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montréal (IRCM)</p>
 */
package org.stripesstuff.tests.session.action;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;

import net.sourceforge.stripes.mock.MockHttpSession;

/**
 * MockHttpSession that handle HttpSession events.
 * @author Christian Poitras
 */
@SuppressWarnings("deprecation")
public class MockHttpSessionWithEvent extends MockHttpSession {
    
    boolean active = true;
    
    public MockHttpSessionWithEvent(ServletContext context) {
        super(context);
    }
    
    @Override
    public void putValue(String key, Object value) {
        setAttribute(key, value);
    }
    @Override
    public void removeAttribute(String key) {
        if (!active) {
            activate();
        }
        Object oldValue = this.getAttribute(key);
        super.removeAttribute(key);
        if (oldValue instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener)oldValue).valueUnbound(new HttpSessionBindingEvent(this, key, oldValue));
        }
    }
    @Override
    public void removeValue(String key) {
        removeAttribute(key);
    }
    @Override
    public void setAttribute(String key, Object value) {
        if (!active) {
            activate();
        }
        Object oldValue = this.getAttribute(key);
        super.setAttribute(key, value);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this, key, oldValue == null ? value : oldValue));
        }
        if (oldValue instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener)oldValue).valueUnbound(new HttpSessionBindingEvent(this, key, oldValue));
        }
    }
    
    /**
     * Simulate a passivate operation.
     */
    public void passivate() {
        Enumeration<String> names = this.getAttributeNames();
        String[] keys;
        synchronized(names) {
            List<String> nameList = Collections.list(names);
            keys = new String[nameList.size()];
            for (int i = 0; i < nameList.size(); i++) {
                keys[i] = nameList.get(i);
            }
        }
        for (String key : keys) {
            Object value = this.getAttribute(key);
            if (value != null && value instanceof HttpSessionActivationListener) {
                ((HttpSessionActivationListener)value).sessionWillPassivate(new HttpSessionEvent(this));
            }
        }
        active = false;
    }
    /**
     * Simulate a activate operation.
     */
    public void activate() {
        Enumeration<?> keys = this.getAttributeNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object value = this.getAttribute(key);
            if (value != null && value instanceof HttpSessionActivationListener) {
                ((HttpSessionActivationListener)value).sessionDidActivate(new HttpSessionEvent(this));
            }
        }
        active = true;
    }
}
