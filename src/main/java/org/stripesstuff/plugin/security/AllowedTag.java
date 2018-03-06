package org.stripesstuff.plugin.security;

import java.lang.reflect.Method;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;


/**
 * JSP tag to secure part of a JSP file. The body is shown (or not) based on whether performing an event on a supplied
 * action bean is allowed. This secures any event on any action bean, while leaving the security decisions to the
 * security manager.
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id:$
 */
public class AllowedTag extends TagSupport
{
	/**
	 * Version number for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Logger for this class.
	 */
	private static final Log LOG = Log.getInstance(AllowedTag.class);
	/**
	 * The name of the bean that is the action bean to secure. If null, the current action bean of the page is used.
	 */
	private String bean;
	/**
	 * The event to secure on the action bean. If null, the default event is assumed.
	 */
	private String event;
	/**
	 * Flag to negate the judgement of the security manager: the body of the tag is shown if the event is not allowed.
	 *
	 * @deprecated Use {@link NotAllowedTag} instead.
	 */
	@Deprecated
	private boolean negate;


	/**
	 * Create the secure tag.
	 */
	public AllowedTag()
	{
		initValues();
	}


	/**
	 * Release the state of this tag.
	 */
	@Override
	public void release()
	{
		super.release();
		initValues();
	}


	/**
	 * Initialize the values to (re)use this tag.
	 */
	private void initValues()
	{
		bean = null;
		event = null;
		//noinspection deprecation
		negate = false;
	}


	/**
	 * Determine if the body should be evaluated or not.
	 *
	 * @return EVAL_BODY_INCLUDE if the body should be included, or SKIP_BODY
	 * @throws JspException when the tag cannot (decide if to) write the body content
	 */
	@Override
	public int doStartTag()
			throws JspException
	{
		// Retrieve the action bean and event handler to secure.

		ActionBean actionBean;
		if (bean == null)
		{
			// Search in page, request, session (if valid) and application scopes, in that order.
			actionBean = (ActionBean)pageContext.findAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
			LOG.debug("Determining access for the action bean of the form: ", actionBean);
		}
		else
		{
			// Search in page, request, session (if valid) and application scopes, in that order.
			actionBean = (ActionBean)pageContext.findAttribute(bean);
			LOG.debug("Determining access for action bean \"", bean, "\": ", actionBean);
		}
		if (actionBean == null)
		{
			throw new StripesJspException(
					"Could not find the action bean. This means that either you specified the name \n" +
					"of a bean that doesn't exist, or this tag is not used inside a Stripes Form.");
		}

		Method handler;
		try
		{
			if (event == null)
			{
				handler = StripesFilter.getConfiguration().getActionResolver().getDefaultHandler(actionBean.getClass());
				LOG.debug("Found a handler for the default event: ", handler);
			}
			else
			{
				handler = StripesFilter.getConfiguration().getActionResolver().getHandler(actionBean.getClass(), event);
				LOG.debug("Found a handler for event \"", event, "\": %s", handler);
			}
		}
		catch (StripesServletException e)
		{
			throw new StripesJspException("Failed to get the handler for the event.", e);
		}

		// Get the judgement of the security manager.

		SecurityManager securityManager = (SecurityManager)pageContext.getAttribute(
				SecurityInterceptor.SECURITY_MANAGER, PageContext.REQUEST_SCOPE);
		boolean haveSecurityManager = securityManager != null;
		boolean eventAllowed;
		if (haveSecurityManager)
		{
			LOG.debug("Determining access using this security manager: ", securityManager);
			eventAllowed = Boolean.TRUE.equals(securityManager.getAccessAllowed(actionBean, handler));
		}
		else
		{
			LOG.debug("There is no security manager; allowing access");
			eventAllowed = true;
		}

		// Show the tag's content (or not) based on this

		//noinspection deprecation
		if (haveSecurityManager && negate)
		{
			LOG.debug("This tag negates the decision of the security manager.");
			eventAllowed = !eventAllowed;
		}

		LOG.debug("Access is ", eventAllowed ? "allowed" : "denied", '.');
		return eventAllowed ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}


	/**
	 * Getter for the field {@link #bean}.
	 *
	 * @return the value of the field
	 * @see #bean
	 */
	public String getBean()
	{
		return bean;
	}


	/**
	 * Setter for the field {@link #bean}.
	 *
	 * @param bean the new value for the field
	 * @see #bean
	 */
	public void setBean(String bean)
	{
		this.bean = bean;
	}


	/**
	 * Getter for the field {@link #event}.
	 *
	 * @return the value of the field
	 * @see #event
	 */
	public String getEvent()
	{
		return event;
	}


	/**
	 * Setter for the field {@link #event}.
	 *
	 * @param event the new value for the field
	 * @see #event
	 */
	public void setEvent(String event)
	{
		this.event = event;
	}


	/**
	 * Getter for the field {@link #negate}.
	 *
	 * @return the value of the field
	 * @see #negate
	 */
	public boolean isNegate()
	{
		//noinspection deprecation
		return negate;
	}


	/**
	 * Setter for the field {@link #negate}.
	 *
	 * @param negate the new value for the field
	 * @see #negate
	 */
	public void setNegate(boolean negate)
	{
		//noinspection deprecation
		this.negate = negate;
	}
}
