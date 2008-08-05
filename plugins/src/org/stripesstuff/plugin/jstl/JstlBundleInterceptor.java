package org.stripesstuff.plugin.jstl;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.*;
import net.sourceforge.stripes.controller.*;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.util.Log;


/**
 * <p>Interceptor that makes localization bundles available for the JSTL formatting tags. By default, it provides the
 * same functionality as the existing Stripes localization facilities, but for the efault JSTL resource bundle. Without
 * configuration, this means that the resource bundle &quot;StripesResources&quot; will be used as the default JSTL
 * resource bundle.
 * </p>
 * <p>These are your options for configuration:</p>
 * <dl><dt>Do nothing</dt><dd>
 * {@link DefaultResourceBundleFactory} is used as a ResourceBundleFactory, which uses the resource bundle named
 * "StripesResources" by default.
 * </dd><dt>Set the Stripes configuration parameter {@code ResourceBundleFactory.Bundle}</dt><dd>
 * This configures {@link DefaultResourceBundleFactory} to use your specified resource bundle instead.
 * </dd><dt>Set the context parameter {@code javax.servlet.jsp.jstl.fmt.LocalizationContext} in {@code web.xml}</dt><dd>
 * Has the same effect as the previous option, but the interceptor disables this part so JSTL can handle it.
 * </dd><dt>Have your implementation of {@link LocalizationBundleFactory} also implement {@link ResourceBundleFactory}</dt><dd>
 * Your {@link LocalizationBundleFactory} is also installed as {@link ResourceBundleFactory}, and will provide the
 * resource bundle being used.
 * </dd><dt>Specify your implementation of {@link ResourceBundleFactory}</dt><dd>
 * If for whatever reason you want to have a separate {@link ResourceBundleFactory} implementation, you can set the
 * Stripes configuration parameter {@code JstlBundleInterceptor.ResourceBundleFactory} to the name of the class that
 * should provide the resource bundle.
 * </dd><dt>Set the Stripes configuration parameter {@code JstlBundleInterceptor.ErrorBundleName}</dt><dd>
 * This option works independant of the other options. Using this option, you can have the error message resource bundle
 * provided by Stripes be made available under this name.
 * </dd><dt>Set the Stripes configuration parameter {@code JstlBundleInterceptor.FieldBundleName}</dt><dd>
 * This option works independant of the other options. Using this option, you can have the field name resource bundle
 * provided by Stripes be made available under this name.
 * </dd></dl>
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id$
 */
@Intercepts(LifecycleStage.ResolutionExecution)
public class JstlBundleInterceptor
		implements Interceptor, ConfigurableComponent
{
	/**
	 * The configuration property used to identify the default bundle.
	 */
	public static final String DEFAULT_BUNDLE_FACTORY = "JstlBundleInterceptor.ResourceBundleFactory";
	/**
	 * The configuration property used to name the Stripes error message bundle for JSTL.
	 * If not defined, the Stripes error message bundle is not loaded.
	 */
	public static final String ERROR_BUNDLE_NAME_CONFIG = "JstlBundleInterceptor.ErrorBundleName";
	/**
	 * The configuration property used to name the Stripes field name bundle for JSTL.
	 * If not defined, the Stripes field name bundle is not loaded.
	 */
	public static final String FIELD_BUNDLE_NAME_CONFIG = "JstlBundleInterceptor.FieldBundleName";
	/**
	 * Logger for this class.
	 */
	private static final Log LOGGER = Log.getInstance(JstlBundleInterceptor.class);
	/**
	 * The name to load the error message bundle with, if any.
	 */
	private String errorBundleName;
	/**
	 * The name to load the field name bundle with, if any.
	 */
	private String fieldBundleName;
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
		LOGGER.debug("Initializing JstlBundleInterceptor");
		BootstrapPropertyResolver propertyResolver = configuration.getBootstrapPropertyResolver();

		localizationBundleFactory = configuration.getLocalizationBundleFactory();

		if (!(localizationBundleFactory instanceof JstlLocalizationBundleFactory))
		{
			errorBundleName = propertyResolver.getProperty(ERROR_BUNDLE_NAME_CONFIG);
			LOGGER.debug("errorBundleName = ", errorBundleName == null ? "null" : '"' + errorBundleName + '"');

			fieldBundleName = propertyResolver.getProperty(FIELD_BUNDLE_NAME_CONFIG);
			LOGGER.debug("fieldBundleName = ", fieldBundleName == null ? "null" : '"' + fieldBundleName + '"');
		}

		if (configuration.getServletContext().getInitParameter(Config.FMT_LOCALIZATION_CONTEXT) == null)
		{
			String resourceBundleFactoryClassName = propertyResolver.getProperty(DEFAULT_BUNDLE_FACTORY);
			boolean newInstance = false;
			if (resourceBundleFactoryClassName == null)
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
					Class<?> resourceBundleFactoryClass = Class.forName(resourceBundleFactoryClassName);
					resourceBundleFactory = (ResourceBundleFactory)resourceBundleFactoryClass.newInstance();
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
			LOGGER.debug("Using default bundle factory: ", resourceBundleFactory);
		}
		else
		{
			LOGGER.debug("Skipping ResourceBundleFactory initialization, as there is already a default JSTL resource " +
			             "bundle configured via the config parameter \"{}\".", Config.FMT_LOCALIZATION_CONTEXT);
		}
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
		if (resourceBundleFactory != null || errorBundleName != null || fieldBundleName != null)
		{
			HttpServletRequest request = context.getActionBeanContext().getRequest();
			Locale locale = request.getLocale();

			if (resourceBundleFactory != null)
			{
				ResourceBundle bundle = resourceBundleFactory.getDefaultBundle(locale);
				Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(bundle, locale));
				LOGGER.debug("Enabled JSTL localization using: ", bundle);
				LOGGER.debug("Loaded resource bundle ", bundle, " as default bundle");
			}

			LOGGER.debug("Determining if Stripes bundles should be loaded.");
			if (errorBundleName != null)
			{
				ResourceBundle errorBundle = localizationBundleFactory.getErrorMessageBundle(locale);
				Config.set(request, errorBundleName, new LocalizationContext(errorBundle, locale));
				LOGGER.debug("Loaded Stripes error message bundle ", errorBundle, " as ", errorBundleName);
			}
			if (fieldBundleName != null)
			{
				ResourceBundle fieldBundle = localizationBundleFactory.getFormFieldBundle(locale);
				Config.set(request, fieldBundleName, new LocalizationContext(fieldBundle, locale));
				LOGGER.debug("Loaded Stripes field name bundle ", fieldBundle, " as ", fieldBundleName);
			}
		}

		return context.proceed();
	}
}
