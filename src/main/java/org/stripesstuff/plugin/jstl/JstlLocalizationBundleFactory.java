package org.stripesstuff.plugin.jstl;

import java.util.*;
import javax.servlet.jsp.jstl.core.Config;

import net.sourceforge.stripes.config.*;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;


/**
 * Simple LocalizationBundleFactory that uses the JSTL localization context configuration to load ResourceBundles with.
 * Specifically, it uses the default ResourceBundle as defined in {@code web.xml}.
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id:$
 */
@DontAutoLoad
public class JstlLocalizationBundleFactory
		implements LocalizationBundleFactory, ConfigurableComponent
{
	/**
	 * The name of the default resource bundle configured for JSTL.
	 */
	private String bundleName;


	/**
	 * Invoked directly after instantiation to allow the configured component to perform
	 * one time initialization.  Components are expected to fail loudly if they are not
	 * going to be in a valid state after initialization.
	 *
	 * @param configuration the Configuration object being used by Stripes
	 * @throws Exception should be thrown if the component cannot be configured well enough to use.
	 */
	public void init(Configuration configuration)
			throws Exception
	{
		bundleName = configuration.getServletContext().getInitParameter(Config.FMT_LOCALIZATION_CONTEXT);
		if (bundleName == null)
		{
			throw new StripesRuntimeException("JSTL has no resource bundle configured. Please set the context " +
			                                  "parameter " + Config.FMT_LOCALIZATION_CONTEXT + " to the name of the " +
			                                  "ResourceBundle to use.");
		}
	}


	/**
	 * Returns the ResourceBundle from which to draw error messages for the specified locale.
	 *
	 * @param locale the locale that is in use for the current request
	 * @throws java.util.MissingResourceException
	 *          when a bundle that is expected to be present cannot
	 *          be located.
	 */
	public ResourceBundle getErrorMessageBundle(Locale locale)
			throws MissingResourceException
	{
		return ResourceBundle.getBundle(bundleName, locale);
	}


	/**
	 * Returns the ResourceBundle from which to draw the names of form fields for the
	 * specified locale.
	 *
	 * @param locale the locale that is in use for the current request
	 * @throws java.util.MissingResourceException
	 *          when a bundle that is expected to be present cannot
	 *          be located.
	 */
	public ResourceBundle getFormFieldBundle(Locale locale)
			throws MissingResourceException
	{
		return ResourceBundle.getBundle(bundleName, locale);
	}
}
