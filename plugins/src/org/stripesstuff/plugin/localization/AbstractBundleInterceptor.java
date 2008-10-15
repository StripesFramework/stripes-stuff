package org.stripesstuff.plugin.localization;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.ConfigurableComponent;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.DontAutoLoad;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.util.Log;

/**
 * <p>Interceptor that makes localization bundles available for messages in the view layer. By default, it provides the
 * same functionality as the existing Stripes localization facilities, but for the messages resource bundle. Without
 * configuration, this means that the resource bundle &quot;StripesResources&quot; will be used as the default message
 * resource bundle.
 * </p>
 * <p>These are your options for configuration:</p>
 * <dl><dt>Do nothing</dt><dd>
 * {@link DefaultResourceBundleFactory} is used as a ResourceBundleFactory, which uses the resource bundle named
 * "StripesResources" by default.
 * </dd><dt>Set the Stripes configuration parameter {@code ResourceBundleFactory.Bundle}</dt><dd>
 * This configures {@link DefaultResourceBundleFactory} to use your specified resource bundle instead.
 * </dd><dt>Have your implementation of {@link LocalizationBundleFactory} also implement {@link ResourceBundleFactory}</dt><dd>
 * Your {@link LocalizationBundleFactory} is also installed as {@link ResourceBundleFactory}, and will provide the
 * resource bundle being used.
 * </dd><dt>Specify your implementation of {@link ResourceBundleFactory}</dt><dd>
 * If for whatever reason you want to have a separate {@link ResourceBundleFactory} implementation, you can set the
 * Stripes configuration parameter {@code BundleInterceptor.ResourceBundleFactory} to the name of the class that
 * should provide the resource bundle, or you can simply put the class in a package that you have configured with the
 * Stripes {@code Extension.Packages} parameter.
 * </dd></dl>
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id$
 * @see GenericBundleInterceptor
 * @see org.stripesstuff.plugin.jstl.JstlBundleInterceptor
 */
@Intercepts(LifecycleStage.ResolutionExecution)
@DontAutoLoad
public abstract class AbstractBundleInterceptor
		implements Interceptor, ConfigurableComponent
{
    /**
     * The configuration property used to identify the default bundle.
     */
    public static final String DEFAULT_BUNDLE_FACTORY = "BundleInterceptor.ResourceBundleFactory";
	/**
	 * Logger for this class.
	 */
	private static final Log LOGGER = Log.getInstance(AbstractBundleInterceptor.class);
	/**
	 * The resource bundle factory for the default resource bundle.
	 */
	private ResourceBundleFactory resourceBundleFactory;
	/**
	 * The localization bundle factory used by Stripes.
	 */
	private LocalizationBundleFactory localizationBundleFactory;


	/**
	 * Initialize the interceptor.
	 *
	 * @param configuration the configuration
	 * @throws Exception when initialization fails
	 */
	public void init(Configuration configuration)
			throws Exception
	{
		LOGGER.debug("Initializing AbstractBundleInterceptor");

		localizationBundleFactory = configuration.getLocalizationBundleFactory();

		findResourceBundleFactory(configuration);
	}

	/**
	 * Determines the {@link ResourceBundleFactory} implementation to use, and if one is found, calls
	 * its {@code init(Configuration)} method if it implements {@link ConfigurableComponent}.
	 */
	protected void findResourceBundleFactory(Configuration configuration)
	        throws Exception
	{
        BootstrapPropertyResolver propertyResolver = configuration.getBootstrapPropertyResolver();

        Class<? extends ResourceBundleFactory> resourceBundleFactoryClass =
            propertyResolver.getClassProperty(DEFAULT_BUNDLE_FACTORY, ResourceBundleFactory.class);

        boolean newInstance = false;
		if (resourceBundleFactoryClass == null)
		{
			if (localizationBundleFactory instanceof ResourceBundleFactory)
			{
				resourceBundleFactory = (ResourceBundleFactory)localizationBundleFactory;
			}
			else
			{
				resourceBundleFactory = new DefaultResourceBundleFactory();
				newInstance = true;
			}
		}
		else
		{
			try
			{
				resourceBundleFactory = resourceBundleFactoryClass.newInstance();
				newInstance = true;
			}
			catch (Exception e)
			{
				throw new StripesRuntimeException("You specified a class name for a ResourceBundleFactory, " +
				                                  "but I cannot instantiate it. Is the class name correct? " +
				                                  "Does it have a no-arg constructor? For configuration you " +
				                                  "can implement ConfigurableComponent.", e);
			}
		}
		if (newInstance && resourceBundleFactory instanceof ConfigurableComponent)
		{
			((ConfigurableComponent)resourceBundleFactory).init(configuration);
		}
		LOGGER.debug("Using resource bundle factory: ", resourceBundleFactory);
	}

	/**
	 * Invoked when intercepting the flow of execution.
	 *
	 * @param context the ExecutionContext of the request currently being processed
	 * @return the result of calling context.proceed(), or if the interceptor wishes to change
	 *         the flow of execution, a Resolution
	 * @throws Exception if any non-recoverable errors occur
	 */
	public Resolution intercept(ExecutionContext context)
			throws Exception
	{
        HttpServletRequest request = context.getActionBeanContext().getRequest();
        Locale locale = request.getLocale();

        if (resourceBundleFactory != null)
		{
			ResourceBundle bundle = resourceBundleFactory.getDefaultBundle(locale);
			setMessageResourceBundle(request, bundle, locale);
		}

		setOtherResourceBundles(request, locale);

		return context.proceed();
	}

	/**
	 * Returns the configured {@link LocalizationBundleFactory} implementation.
	 */
	protected LocalizationBundleFactory getLocalizationBundleFactory()
	{
	    return localizationBundleFactory;
	}

	/**
	 * Gives subclasses an opportunity to set the resolved message resource bundle in the request, if desired.
	 * The implementation in this class does nothing.
	 */
	protected void setMessageResourceBundle(HttpServletRequest request, ResourceBundle bundle, Locale locale)
	{
	}

    /**
     * Gives subclasses an opportunity to set other resource bundles in the request, if desired.
     * The implementation in this class does nothing.
     */
	protected void setOtherResourceBundles(HttpServletRequest request, Locale locale)
	{
	}
}
