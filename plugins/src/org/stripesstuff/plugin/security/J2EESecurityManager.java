package org.stripesstuff.plugin.security;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.DontAutoLoad;
import net.sourceforge.stripes.util.Log;


/**
 * Security manager that implements the J2EE security annotations.
 * In this, method level annotations override class level annotations. Apart from that:<ol>
 * <li>@{@link DenyAll} denies access,</li>
 * <li>otherwise, @{@link PermitAll} allows access for all roles,</li>
 * <li>otherwise, @{@link RolesAllowed} lists the roles that allow access</li>
 * </ol>
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id$
 */
@DontAutoLoad
public class J2EESecurityManager
		implements SecurityManager
{
	/**
	 * Logger for this class.
	 */
	private static final Log LOG = Log.getInstance(J2EESecurityManager.class);


	/**
	 * Determines if access for the given execution context is allowed. The security manager is used to determine if
	 * access is allowed (to handle an event) or if access is not denied (thus allowing the display of error messages
	 * for binding and/or validation errors for a secured event). If the latter would not be checked, a user can (even
	 * if only theoretically) see an error message, correct his input, and then see an &quot;access forbidden&quot;
	 * message.
	 * <p>
	 * If required contextual information (like what data is affected) is not available, no decision should be made.
	 * This is to ensure that access is not denied when required data is missing because of a binding and/or validation
	 * error.
	 *
	 * @param bean    the action bean on which to perform the action
	 * @param handler the event handler to check authorization for
	 * @return {@link Boolean#TRUE} if access is allowed, {@link Boolean#FALSE} if not, and null if no decision can be made
	 * @see SecurityManager#getAccessAllowed(ActionBean,Method)
	 */
	public Boolean getAccessAllowed(ActionBean bean, Method handler)
	{
		// Determine if the event handler allows access.

		LOG.debug("Determining if access is allowed for " + handler.getName() + " on " + bean.toString());
		Boolean allowed = determineAccessOnElement(bean, handler, handler);

		// If the event handler didn't decide, determine if the action bean class allows access.
		// Rinse and repeat for all superclasses.

		Class<?> beanClass = bean.getClass();
		while (allowed == null && beanClass != null)
		{
			LOG.debug("Determining if access is allowed for " + beanClass.getName() + " on " + bean.toString());
			allowed = determineAccessOnElement(bean, handler, beanClass);
			beanClass = beanClass.getSuperclass();
		}

		// If the event handler nor the action bean class decided, allow access.
		// This default allows access if no security annotations are used.

		if (allowed == null)
		{
			allowed = true;
		}

		// Return the decision.

		return allowed;
	}


	/**
	 * Decide if the annotated element allows access to the current user.
	 *
	 * @param bean    the action bean; used for security decisions
	 * @param handler the event handler; used for security decisions
	 * @param element the element to check authorization for
	 * @return {@link Boolean#TRUE TRUE} if access is allowed, {@link Boolean#FALSE FALSE} if access is denied, and {@code null} if undecided
	 * @see DenyAll
	 * @see PermitAll
	 * @see RolesAllowed
	 */
	protected Boolean determineAccessOnElement(ActionBean bean, Method handler, AnnotatedElement element)
	{
		// Default decision: none.

		Boolean allowed = null;

		if (element.isAnnotationPresent(DenyAll.class))
		{
			// The element denies access.

			allowed = false;
		}
		else if (element.isAnnotationPresent(PermitAll.class))
		{
			// The element allows access to all security roles (i.e. any authenticated user).

			allowed = isUserAuthenticated(bean, handler);
		}
		else
		{
			RolesAllowed rolesAllowed = element.getAnnotation(RolesAllowed.class);
			if (rolesAllowed != null)
			{
				// The element allows access if the user has one of the specified roles.

				allowed = false;

				for (String role : rolesAllowed.value())
				{
					Boolean hasRole = hasRole(bean, handler, role);
					if (hasRole != null && hasRole)
					{
						allowed = true;
						break;
					}
				}
			}
		}
		return allowed;
	}


	/**
	 * Determine if the user is authenticated. The default implementation is to use {@code getUserPrincipal() != null}
	 * on the HttpServletRequest in the ActionBeanContext.
	 *
	 * @param bean    the current action bean; used for security decisions
	 * @param handler the current event handler; used for security decisions
	 * @return {@link Boolean#TRUE TRUE} if the user is authenticated, {@link Boolean#FALSE FALSE} if not, and {@code null} if undecided
	 */
	protected Boolean isUserAuthenticated(ActionBean bean, Method handler)
	{
		return bean.getContext().getRequest().getUserPrincipal() != null;
	}


	/**
	 * Determine if the current user has the specified role.
	 * Note that '*' is a special role that resolves to any role (see the servlet spec. v2.4, section 12.8).
	 *
	 * @param bean    the current action bean
	 * @param handler the current event handler
	 * @param role    the role to check
	 * @return {@code true} if the user has the role, and {@code false} otherwise
	 */
	protected Boolean hasRole(ActionBean bean, Method handler, String role)
	{
		return bean.getContext().getRequest().isUserInRole(role);
	}
}
