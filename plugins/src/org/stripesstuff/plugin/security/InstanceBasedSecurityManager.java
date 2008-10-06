package org.stripesstuff.plugin.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.DontAutoLoad;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.expression.ExpressionValidator;


/**
 * Security manager for the Stripes framework that handles the J2EE security annotations, but also
 * applies instance based security.
 * <p>
 * It does this by restricting roles:<ol>
 * <li>Plain role names are used as-is (the superclass handles this already).</li>
 * <li>Role names can also have this format {@code <rolename> if <EL expression>}.</li>
 * <li>The rolename part of such roles is used to determine if access is possible.</li>
 * <li>Both the rolename and the expression are used to determine if access is allowed.</li>
 * </ol>
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @author <a href="mailto:xf2697@fastmail.fm">Fred Daoud</a>
 * @version $Id: InstanceBasedSecurityManager.java 205 2007-04-27 18:58:49Z oscar $
 */
@DontAutoLoad
public class InstanceBasedSecurityManager
		extends J2EESecurityManager
{
	/**
	 * Logger for this class.
	 */
	private static final Log LOG = Log.getInstance(InstanceBasedSecurityManager.class);


	/**
	 * Determine if the current user has the specified role. Delegates to
	 * {@link #hasRoleName(ActionBean, Method, String)} to determine if the user has a role without
	 * using the expression; this method handles evaluating the expression.
	 *
	 * @param bean the current action bean
	 * @param handler the current event handler
	 * @param role the role to check
	 * @return {@code true} if the user has the role, and {@code false} otherwise
	 */
	@Override
	protected Boolean hasRole(ActionBean bean, Method handler, String role)
	{
		LOG.debug("Checking role " + role + " using " + bean);

		String roleName;
		String roleExpression;

		Matcher ifTrigger = Pattern.compile("\\bif\\b").matcher(role);
		if (ifTrigger.find())
		{
			roleName = role.substring(0, ifTrigger.start()).trim(); // Lose leading and trailing whitespace.
			roleExpression = role.substring(ifTrigger.end()).trim(); // Lose leading and trailing whitespace.
		}
		else // The role does not contain a condition.
		{
			roleName = role;
			roleExpression = null;
		}

		LOG.debug("The role name and its expression are " + roleName + " & " + String.valueOf(roleExpression));

		// Check if the user has the required role.

		Boolean hasRole = hasRoleName(bean, handler, roleName);

		// If there is a limiting expression, restrict the role to cases where the expression evaluates to true.

		if (hasRole != null && hasRole && roleExpression != null)
		{
			LOG.debug("Checking expression " + roleExpression);

			Object value = evaluateRoleExpression(bean, roleExpression);
			hasRole = value == null ? null : Boolean.TRUE.equals(value);
		}

		LOG.debug("Done checking role " + role + ": access is " + (hasRole ? "allowed" : "denied") + '.');
		return hasRole;
	}


	/**
	 * Checks to see if the user has an individual role by name. The default is to use the parent class and call
	 * {@code super.hasRole(bean,roleName)}. When subclassing {@link InstanceBasedSecurityManager}, override
	 * this method instead of {@link #hasRole(ActionBean, Method, String)} to keep using the EL expression logic but
	 * change how to verify if a user has an individual role name.
	 *
	 * @param bean     the current action bean
	 * @param handler the current event handler
	 * @param roleName the name of the role to check
	 * @return {@code true} if the user has the role, and {@code false} otherwise
	 */
	protected Boolean hasRoleName(ActionBean bean, Method handler, String roleName)
	{
		// Let our superclass check if the user has the required role.
		return super.hasRole(bean, handler, roleName);
	}


	/**
	 * Evaluate the given EL expression using the current action bean to resolve variables.
	 *
	 * @param bean       the bean to evaluate the expression on
	 * @param expression the EL expression to evaluate
	 * @return the result of the EL expression
	 */
	private Object evaluateRoleExpression(ActionBean bean, String expression)
	{
		try
		{
			LOG.debug("Evaluating expression: '" + expression + '\'');

			// This is somewhat of a hack until the ExpressionEvaluator becomes more accessible.
			ParameterName name = new ParameterName("security");
			List<Object> values = new ArrayList<Object>();
			values.add(null);
			ValidationMetadata metadata = new ValidationMetadata("security").expression(expression);
			ValidationErrors errors = new ValidationErrors();

			ExpressionValidator.evaluate(bean, name, values, metadata, errors);

			return errors.isEmpty();
		}
		catch (Exception exc)
		{
			throw new StripesRuntimeException(exc);
		}
	}
}
