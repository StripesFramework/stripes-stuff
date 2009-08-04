/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.waitpage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.stripesstuff.tests.waitpage.action.AdderActionBean;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.Log;

/**
 * Test wait page interceptor.
 * 
 * @author Christian Poitras
 */
public class WaitPageTest {
    
    @SuppressWarnings("unused")
    private static final Log log = Log.getInstance(WaitPageTest.class);
    
    MockServletContext context;
    
    @BeforeClass
    public void setupContext() {
        context = new MockServletContext("waitpage");
        
        // Add the Stripes Filter
        Map<String,String> filterParams = new HashMap<String,String>();
        filterParams.put("Interceptor.Classes",
                "org.stripesstuff.tests.waitpage.TestableWaitPageInterceptor");
        filterParams.put("ActionResolver.Packages",
                "org.stripesstuff.tests.waitpage.action");
        context.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        
        // Add the Stripes Dispatcher
        context.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
    }
    @AfterClass
    public void releaseContext() {
        context = null;
    }
    
    
    /**
     * Result page should be returned just after redirect is called.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void shortAdd() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        // First round trip redirect to wait page.
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortAdd");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    /**
     * Wait page should be returned once.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void oneRefreshAdd() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("oneRefreshAdd");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // In next request result should be available.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    /**
     * Wait page should be returned twice.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void twoRefreshAdd() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("twoRefreshAdd");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        for (int i = 0; i < 2; i++) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
            Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        }
        
        // In next request result should be available.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * An exception should be thrown when redirecting.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void shortException() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortException");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        try {
            // We except an exception to be thrown.
            trip.execute();
            Assert.fail("No exception thrown when it was supposed to");
        } catch (Exception e) {
        }
        
        // Test that action bean is in request.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    /**
     * An exception should be thrown after having waited once.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void oneRefreshException() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("oneRefreshException");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // In next request exception should be thrown.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        try {
            // We except an exception to be thrown.
            trip.execute();
            Assert.fail("No exception thrown when it was supposed to");
        } catch (Exception e) {
        }
        
        // Test that action bean is in request.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * Error page should be returned after redirection.
     * Exception should be saved in request.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void shortExceptionWithErrorPage() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortExceptionWithErrorPage");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "error.jsp");
        Assert.assertNotNull(trip.getRequest().getAttribute("exception"), "Exception no saved as a request attribute");
        Assert.assertTrue(trip.getRequest().getAttribute("exception") instanceof Exception, "Exception no saved as a request attribute");
        
        // Test that action bean is in request.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    /**
     * Error page should be returned after having waited once.
     * Exception should be saved in request.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void oneRefreshExceptionWithErrorPage() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("oneRefreshExceptionWithErrorPage");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // In next request error page should be returned.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "error.jsp");
        Assert.assertNotNull(trip.getRequest().getAttribute("exception"), "Exception no saved as a request attribute");
        Assert.assertTrue(trip.getRequest().getAttribute("exception") instanceof Exception, "Exception no saved as a request attribute");
        
        // Test that action bean is in request.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * Since process completes too fast, result page is expected even with an AJAX updater.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void shortAjaxUpdater() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortAjaxUpdater");
        
        // Since the event executes very quickly, AJAX updater is not used.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    /**
     * AJAX page should be returned on redirect and complete flag should be true.
     * When page is refreshed, result page should be returned.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void oneRefreshAjaxUpdater() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("oneRefreshAjaxUpdater");
        
        // Execute redirection.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // Simulate AJAX updater in wait page.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.addParameter("ajax", "true");
        trip.execute();
        // On first update, event didn't complete.
        Assert.assertEquals(trip.getForwardUrl(), "ajax.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.isComplete(), false);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.addParameter("ajax", "true");
        trip.execute();
        // On second update, event completed.
        Assert.assertEquals(trip.getForwardUrl(), "ajax.jsp");
        bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.isComplete(), true);
        
        // Refresh page to get result.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * Wait page should be returned once.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void oneRefreshAddNoDelay() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        // First round trip redirect to wait page.
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("oneRefreshAddNoDelay");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // Result page should be available on second request.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getResult(), 3);
        
        // Test that action bean is in request.
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * Wait page should be returned once. After that, source page is returned.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void sourcePage() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("sourcePage");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "wait.jsp");
        
        // In next request result should be available.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // Test that action bean is in request.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), bean);
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), bean);
    }
    
    /**
     * Returns action bean URL part of URL.
     * @param url URL
     * @return action bean URL part of URL
     * @throws UnsupportedEncodingException could not decode URL
     */
    private String getActionBeanUrl(String url, ServletContext context) throws UnsupportedEncodingException {
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
    private Map<String, String> getParameters(String url) throws UnsupportedEncodingException {
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
