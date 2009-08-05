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
import org.stripesstuff.tests.waitpage.action.RequestTestActionBean;
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
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
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        
        // In next request result should be available.
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    
    /**
     * Test to validate that error are present when binding fails.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void bindingError() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", "a");
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("oneRefreshAdd");
        
        // Source page should be returned.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // A validation error should be saved.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    /**
     * Test to validate that error are present when custom validation fails.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void customValidationError() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(200));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("oneRefreshAdd");
        
        // Source page should be returned.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // A validation error should be saved.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    /**
     * Test to validate that error are present when event fails.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void eventValidationError() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(200));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("validationError");
        
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Event should have failed and saved an error.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    /**
     * Test to validate that error are present when binding fails with an AJAX updater.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void bindingErrorAjax() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", "a");
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("oneRefreshAjaxUpdater");
        
        // Source page should be returned.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // A validation error should be saved.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    /**
     * Test to validate that error are present when custom validation fails with an AJAX updater.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void customValidationErrorAjax() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(200));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("oneRefreshAjaxUpdater");
        
        // Source page should be returned.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        
        // A validation error should be saved.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    /**
     * Test to validate that error are present when event fails with an AJAX updater.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void eventValidationErrorAjax() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("validationErrorAjax");
        
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.addParameter("ajax", "true");
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "ajax.jsp");
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.isComplete(), true);
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        
        // Event should have failed and saved an error.
        Assert.assertEquals(trip.getForwardUrl(), "sourcePage.jsp");
        bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertFalse(bean.getContext().getValidationErrors().isEmpty(), "validation errors should contain an error");
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    
    /**
     * Test to see if action bean contains field values for form population.
     * Since request coming from wait page is not the one used to run event, wait page request cannot contain field values.
     * They will be lost if no action bean fields matches them.
     * See WaitPageInterceptor documentation.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void requestContainsFields() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        
        // Fields should be in request and action bean.
        AdderActionBean bean = trip.getActionBean(AdderActionBean.class);
        Assert.assertEquals(bean.getFirst(), 1);
        Assert.assertEquals(bean.getSecond(), 2);
        Assert.assertEquals(bean.getResult(), 3);
        Assert.assertEquals(bean.getContext().getRequest().getParameter("first"), String.valueOf(1));
        Assert.assertEquals(bean.getContext().getRequest().getParameter("second"), String.valueOf(2));
        // Since request in trip is not the request used before wait page, it is impossible to test parameters in trip's request
        
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(AdderActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Adder.action"), "Action bean must be save under it's URL");
        Assert.assertEquals(trip.getRequest().getAttribute("/Adder.action"), trip.getActionBean(AdderActionBean.class));
    }
    
    /**
     * Test that request header, parameter and attributes are available in event.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void requestTest() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, RequestTestActionBean.class, session);
        trip.getRequest().addHeader("TestHeader", "test");
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("requestTest");
        
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
    }
    /**
     * Test that request header, parameter and attributes are available in event.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void requestTestAjax() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, RequestTestActionBean.class, session);
        trip.getRequest().addHeader("TestHeader", "test");
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("requestTestAjax");
        
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
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.addParameter("ajax", "true");
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "ajax.jsp");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
        RequestTestActionBean bean = trip.getActionBean(RequestTestActionBean.class);
        Assert.assertEquals(bean.isComplete(), true);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
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
