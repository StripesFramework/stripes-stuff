package org.stripesstuff.plugin.jstl;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import org.stripesstuff.plugin.localization.AbstractBundleInterceptor;

import net.sourceforge.stripes.config.*;
import net.sourceforge.stripes.controller.*;
import net.sourceforge.stripes.util.Log;


/**
 * <p>Interceptor that makes localization bundles available for the JSTL formatting tags. By default, it provides the
 * same functionality as the existing Stripes localization facilities, but for the default JSTL resource bundle. Without
 * configuration, this means that the resource bundle &quot;StripesResources&quot; will be used as the default JSTL
 * resource bundle.
 * </p>
 * <p>Your options for configuration are the ones listed in {@link AbstractBundleInterceptor}, as well as the following:</p>
 * <dl><dt>Set the context parameter {@code javax.servlet.jsp.jstl.fmt.LocalizationContext} in {@code web.xml}</dt><dd>
 * Has the same effect as the previous option, but the interceptor disables this part so JSTL can handle it.
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
public class JstlBundleInterceptor extends AbstractBundleInterceptor
		implements Interceptor, ConfigurableComponent
{
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
	 * Initialize the interceptor.
	 *
	 * @param configuration the configuration
	 * @throws Exception when initialization fails
	 */
	@Override
	public void init(Configuration configuration)
	        throws Exception
	{
	    super.init(configuration);

        if (!(getLocalizationBundleFactory() instanceof JstlLocalizationBundleFactory))
        {
            BootstrapPropertyResolver propertyResolver = configuration.getBootstrapPropertyResolver();

            errorBundleName = propertyResolver.getProperty(ERROR_BUNDLE_NAME_CONFIG);
            LOGGER.debug("errorBundleName = ", errorBundleName == null ? "null" : '"' + errorBundleName + '"');

            fieldBundleName = propertyResolver.getProperty(FIELD_BUNDLE_NAME_CONFIG);
            LOGGER.debug("fieldBundleName = ", fieldBundleName == null ? "null" : '"' + fieldBundleName + '"');
        }
	}

	@Override
	protected void findResourceBundleFactory(Configuration configuration)
			throws Exception
	{
		if (configuration.getServletContext().getInitParameter(Config.FMT_LOCALIZATION_CONTEXT) == null)
		{
		    super.findResourceBundleFactory(configuration);
		}
		else
		{
			LOGGER.debug("Skipping ResourceBundleFactory initialization, as there is already a default JSTL resource " +
			             "bundle configured via the config parameter \"{}\".", Config.FMT_LOCALIZATION_CONTEXT);
		}
	}

	/**
     * Sets the message resource bundle in the request using the JSTL's mechanism.
     */
	@Override
    protected void setMessageResourceBundle(HttpServletRequest request, ResourceBundle bundle)
	{
		Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(bundle, request.getLocale()));
        LOGGER.debug("Enabled JSTL localization using: ", bundle);
        LOGGER.debug("Loaded resource bundle ", bundle, " as default bundle");
    }

    /**
     * Sets the Stripes error and field resource bundles in the request, if their names have been configured.
     */
	@Override
	protected void setOtherResourceBundles(HttpServletRequest request)
	{
		Locale locale = request.getLocale();

        if (errorBundleName != null)
        {
            ResourceBundle errorBundle = getLocalizationBundleFactory().getErrorMessageBundle(locale);
            Config.set(request, errorBundleName, new LocalizationContext(errorBundle, locale));
            LOGGER.debug("Loaded Stripes error message bundle ", errorBundle, " as ", errorBundleName);
        }

        if (fieldBundleName != null)
        {
            ResourceBundle fieldBundle = getLocalizationBundleFactory().getFormFieldBundle(locale);
            Config.set(request, fieldBundleName, new LocalizationContext(fieldBundle, locale));
            LOGGER.debug("Loaded Stripes field name bundle ", fieldBundle, " as ", fieldBundleName);
        }
	}
}
