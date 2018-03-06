package org.stripesstuff.plugin.security;

import javax.servlet.jsp.JspException;


/**
 * Variant of {@link AllowedTag} that does exactly the opposite.
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id$
 */
public class NotAllowedTag
		extends AllowedTag
{
	private static final long	serialVersionUID	= 1L;

	/**
	 * Do the opposite of the parent tag.
	 *
	 * @return EVAL_BODY_INCLUDE if the body should be included, or SKIP_BODY of not
	 * @throws JspException when the tag cannot (decide if to) write the body content
	 */
	@Override
	public int doStartTag()
			throws JspException
	{
		return super.doStartTag() == SKIP_BODY ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}
