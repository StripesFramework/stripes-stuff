package org.stripesstuff.plugin.jstl;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.controller.*;
import net.sourceforge.stripes.localization.DefaultLocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.util.Log;


/**
 * Interceptor that makes localization bundles available for the JSTL
 * formatting tags. It is configured thus:<table>
 * <tr valign="top"><th>Property</th><th>Value</th><th>Default</th></tr>
 * <tr valign="top">
 * <td>JstlBundleInterceptor.DefaultBundle</td>
 * <td>The name of the resource bundle to load as the default bundle.</td>
 * <td>{@link DefaultLocalizationBundleFactory#BUNDLE_NAME} (equals &quot;StripesResources&quot;)</td>
 * </tr>
 * <tr valign="top">
 * <td>JstlBundleInterceptor.ErrorBundleName</td>
 * <td>If present, the Stripes error message bundle can be accessed with this bundle name</td>
 * <td><em>none</em></td>
 * </tr>
 * <tr valign="top">
 * <td>JstlBundleInterceptor.FieldBundleName</td>
 * <td>If present, the Stripes error message bundle can be accessed with this bundle name</td>
 * <td><em>none</em></td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id$
 */
@Intercepts(LifecycleStage.ResolutionExecution)
public class JstlBundleInterceptor
		implements Interceptor
{
	/**
	 * The configuration property used to identify the default bundle.
	 */
	public static final String DEFAULT_BUNDLE_CONFIG = "JstlBundleInterceptor.DefaultBundle";
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
	 * The name of the resource bundle to use.
	 */
	private String bundleName;
	/**
	 * The name to load the error message bundle with, if any.
	 */
	private String errorBundleName;
	/**
	 * The name to load the field name bundle with, if any.
	 */
	private String fieldBundleName;
	/**
	 * The localization bundle factory used by Stripes.
	 */
	private LocalizationBundleFactory localizationBundleFactory;


	/**
	 * Ensure this object is initialized.
	 */
	protected void ensureInitialization()
	{
		if (localizationBundleFactory == null)
		{
			LOGGER.debug("Initializing JstlBundleInterceptor");
			BootstrapPropertyResolver propertyResolver =
					StripesFilter.getConfiguration().getBootstrapPropertyResolver();
			bundleName = propertyResolver.getProperty(DEFAULT_BUNDLE_CONFIG);
			if (bundleName == null)
			{
				bundleName = DefaultLocalizationBundleFactory.BUNDLE_NAME;
			}
			LOGGER.debug("Using default bundle name: ", bundleName);
			errorBundleName = propertyResolver.getProperty(ERROR_BUNDLE_NAME_CONFIG);
			LOGGER.debug("errorBundleName = ", errorBundleName == null ? "null" : '"' + errorBundleName + '"');
			fieldBundleName = propertyResolver.getProperty(FIELD_BUNDLE_NAME_CONFIG);
			LOGGER.debug("fieldBundleName = ", fieldBundleName == null ? "null" : '"' + fieldBundleName + '"');

			localizationBundleFactory = StripesFilter.getConfiguration().getLocalizationBundleFactory();
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
		ensureInitialization();

		HttpServletRequest request = context.getActionBeanContext().getRequest();
		Locale locale = request.getLocale();

		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
		Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(bundle, locale));
		LOGGER.debug("Enabled JSTL localization using: " + Arrays.asList(request,
		                                                                 Config.FMT_LOCALIZATION_CONTEXT,
		                                                                 Config.get(request,
		                                                                            Config.FMT_LOCALIZATION_CONTEXT)));

		if (localizationBundleFactory != null)
		{
			LOGGER.debug("Determining if Stripes bundles should be loaded.");
			if (errorBundleName != null)
			{
				ResourceBundle errorBundle = localizationBundleFactory.getErrorMessageBundle(locale);
				Config.set(request, errorBundleName, new LocalizationContext(errorBundle, locale));
				LOGGER.debug("Loaded Stripes error message bundle " + errorBundle + " as " + errorBundleName);
			}
			if (fieldBundleName != null)
			{
				ResourceBundle fieldBundle = localizationBundleFactory.getFormFieldBundle(locale);
				Config.set(request, fieldBundleName, new LocalizationContext(fieldBundle, locale));
				LOGGER.debug("Loaded Stripes field name bundle " + fieldBundle + " as " + fieldBundleName);
			}
		}

		return context.proceed();
	}
}
