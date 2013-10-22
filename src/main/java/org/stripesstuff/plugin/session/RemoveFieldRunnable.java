/*
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (http://www.ircm.qc.ca)</p>
 */
package org.stripesstuff.plugin.session;

import java.util.Date;

import javax.servlet.http.HttpSession;

/**
 * Remove an object in session after a limited time.
 * @author Christian Poitras
 */
public class RemoveFieldRunnable implements Runnable {
    /**
     * Key under which object is bound.
     */
    String key;
    /**
     * Maximum time in session.
     */
    int maxTime;
    /**
     * Session where object is stored.
     */
    HttpSession session;
    /**
     * Time where attribute is automatically removed form session.
     */
    private Date endTime;
    /**
     * Cancel this thread.
     */
    private boolean cancel;
    
    RemoveFieldRunnable(String key, int maxTime, HttpSession session) {
        this.key = key;
        this.maxTime = maxTime;
        this.session = session;
    }
    
    /**
     * Remove field from session after maxTime is elapsed.
     */
    public void run() {
        if (maxTime < 0) {
            return;
        }
        long timeMillis = (long)maxTime * 60 * 1000;
        endTime = new Date((new Date()).getTime() + timeMillis);
        while (!cancel && endTime.after(new Date())) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                cancel = true;
            }
        }
        if (!cancel && !endTime.after(new Date())) {
            session.removeAttribute(key);
        }
    }
    /**
     * Cancel current thread.
     */
    public void cancel() {
        cancel = true;
    }
}
