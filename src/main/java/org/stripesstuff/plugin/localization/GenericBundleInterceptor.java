package org.stripesstuff.plugin.localization;

import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Generic bundle interceptor implementation that simply puts the resolved message
 * {@link ResourceBundle} object as a request-scope attribute under the
 * "{@code Localization.MessageResourceBundle}" key. This is useful when a JSTL-independent solution
 * is desired. Developers using the JSTL will find the
 * {@link org.stripesstuff.plugin.jstl.JstlBundleInterceptor} implementation more useful.
 *
 * @author <a href="mailto:xf2697@fastmail.fm">Freddy Daoud</a>
 * @version $Id$
 * @see org.stripesstuff.plugin.jstl.JstlBundleInterceptor
 */
@Intercepts(LifecycleStage.ResolutionExecution)
public class GenericBundleInterceptor extends AbstractBundleInterceptor implements Interceptor {
    /** Constant used to put the message resource bundle as a request-scope attribute. */
    public static final String REQ_ATTR_MESSAGE_RESOURCE_BUNDLE = "Localization.MessageResourceBundle";

    /** Puts the resource bundle as a request-scope attribute. */
    @Override
    protected void setMessageResourceBundle(HttpServletRequest request, ResourceBundle bundle) {
        request.setAttribute(REQ_ATTR_MESSAGE_RESOURCE_BUNDLE, bundle);
    }
}
