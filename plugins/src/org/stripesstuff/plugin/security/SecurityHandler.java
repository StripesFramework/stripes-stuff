package org.stripesstuff.plugin.security;

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;


/**
 * Optional interface that can be implemented by a SecurityManager to determine what to do when access
 * has been denied.
 *
 * @author <a href="mailto:xf2697@fastmail.fm">Fred Daoud</a>
 * @version $Id:$
 */
public interface SecurityHandler
{
	/**
	 * Determines what to do when access has been denied.
	 *
	 * @param bean    the action bean to which access was denied
	 * @param handler the event handler to which access was denied
	 * @return the Resolution to be executed when access has been denied
	 */
	Resolution handleAccessDenied(ActionBean bean, Method handler);
}
