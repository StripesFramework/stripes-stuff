package org.stripesstuff.tests.waitpage;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.stripesstuff.plugin.waitpage.Context;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.Log;

/**
 * Wait context that can be used for unit testing.
 * 
 * @author Christian Poitras
 */
public class TestableContext extends Context {
    
    private static final Log log = Log.getInstance(TestableContext.class);
    
    private HttpServletRequest request;
    
    TestableContext(HttpServletRequest request) {
        this.request = request;
    }
    
    /**
     * Invoke event in a background request.
     */
    public void run()
    {
        try
        {
            // Since we are testing interceptor, a MockRoundtrip will be used instead of an HttpURLConnection.
            String testUrl = this.getActionBeanUrl(url, request.getSession().getServletContext());
            Map<String, String> parameters = this.getParameters(url);
            MockRoundtrip trip = new MockRoundtrip((MockServletContext)request.getSession().getServletContext(), testUrl, (MockHttpSession)request.getSession());
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                if ("_sourcePage".equals(entry.getKey())) {
                    trip.setSourcePage(CryptoUtil.decrypt(entry.getValue()));
                }
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            
            // Pass in the session id cookie to maintain the same session.
            log.trace("passing in cookies: ", cookies);
            trip.getRequest().addHeader("Cookie", cookies);
            trip.getRequest().setCookies(request.getCookies());
            
            // Execute round trip.
            trip.execute(eventHandler.getName());
        }
        catch (Exception e)
        {
            // Log any exception that could have occurred.
            log.error(e);
        }
    }
    /**
     * Returns action bean URL part of URL.
     * @param url URL
     * @return action bean URL part of URL
     * @throws UnsupportedEncodingException could not decode URL
     */
    private String getActionBeanUrl(URL url, ServletContext context) throws UnsupportedEncodingException {
        String urlString = URLDecoder.decode(url.toString(), "UTF-8");
        String query = urlString.substring(0, urlString.indexOf("?"));
        return query.substring(query.indexOf(context.getContextPath()) + context.getContextPath().length());
    }
    /**
     * Returns URL parameters.
     * @param url URL
     * @return URL parameters
     * @throws UnsupportedEncodingException could not decode URL
     */
    private Map<String, String> getParameters(URL url) throws UnsupportedEncodingException {
        String urlString = URLDecoder.decode(url.toString(), "UTF-8");
        Map<String, String> parameters = new HashMap<String, String>();
        String query = urlString.substring(urlString.indexOf("?") + 1);
        String[] parameterStrings = query.split("\\&");
        for (String parameter: parameterStrings) {
            String name = parameter.substring(0, parameter.indexOf("="));
            String value = parameter.substring(parameter.indexOf("=") + 1, parameter.length());
            parameters.put(name, value);
        }
        return parameters;
    }
}
