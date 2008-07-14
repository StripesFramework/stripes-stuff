package org.stripesstuff.plugin.security;

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;


/**
 * Security manager for the Stripes framework. Determines if handling the event for the current
 * execution context is possible or allowed. This is done as follows:<ul>
 * <li>If the security manager allows access, both validation errors and events are allowed.</li>
 * <li>If the security manager disallows access, both validation errors and events are disallowed.</li>
 * <li>If the security manager is undecided, validation errors are allowed, but events are disallowed.</li>
 * </ul>
 * The result of this is that unless specifically allowed, events are disallowed. But when validation errors prevent a
 * decision being made, access (to the error messages) is allowed so the user can retry (and the security manager
 * can make a more informed decision).
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id: SecurityManager.java 203 2007-04-27 18:42:44Z oscar $
 */
public interface SecurityManager
{
	/**
	 * Determines if access for the given execution context is allowed. The security manager is used to determine if
	 * access is allowed (to handle an event) or if access is not denied (thus allowing the display of error messages
	 * for binding and/or validation errors for a secured event). If the latter would not be checked, a user can (even
	 * if only theoretically) see an error message, correct his input, and then see an &quot;access forbidden&quot;
	 * message. Using this design, such a case is only possible when hacking.
	 * <p>
	 * If required contextual information (like what data is affected) is not available, no decision should be made.
	 * This is to ensure that access is not denied when required data is missing because of a binding and/or validation
	 * error.
	 *
	 * @param bean    the action bean on which to perform the action
	 * @param handler the event handler check authorization for
	 * @return {@link Boolean#TRUE} if access is allowed, {@link Boolean#FALSE} if not, and null if no decision can be made
	 */
	Boolean getAccessAllowed(ActionBean bean, Method handler);
}
