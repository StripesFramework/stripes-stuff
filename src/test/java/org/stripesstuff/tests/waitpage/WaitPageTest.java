package org.stripesstuff.tests.waitpage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.stripesstuff.tests.waitpage.action.AdderActionBean;
import org.stripesstuff.tests.waitpage.action.MessagesActionBean;
import org.stripesstuff.tests.waitpage.action.RequestTestActionBean;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesConstants;
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
        filterParams.put("CoreInterceptor.Classes",
                "org.stripesstuff.tests.waitpage.TestableWaitPageInterceptor," +
                "net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor," +
                "net.sourceforge.stripes.controller.HttpCacheInterceptor");
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
     * Result page should be returned just after redirect is called.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void shortAddNoDelay() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        // First round trip redirect to wait page.
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortAddNoDelay");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        trip = new MockRoundtrip(context, actionBeanUrl, session);
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            trip.addParameter(entry.getKey(), entry.getValue());
        }
        trip.execute();
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
        // Result page.
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
        try {
            while (trip.getForwardUrl() == null || "wait.jsp".equals(trip.getForwardUrl())) {
                trip = new MockRoundtrip(context, actionBeanUrl, session);
                for (Map.Entry<String, String> entry: parameters.entrySet()) {
                    trip.addParameter(entry.getKey(), entry.getValue());
                }
                trip.execute();
            }
            // We except an exception to be thrown.
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
     * An exception should be thrown when redirecting.
     * @throws Exception
     */
    @Test(groups="waitpage", timeOut=3000)
    public void shortExceptionNoDelay() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortExceptionNoDelay");
        
        // Simulate wait page.
        String resolutionUrl = trip.getRedirectUrl();
        String actionBeanUrl = this.getActionBeanUrl(resolutionUrl, context);
        Map<String, String> parameters = this.getParameters(resolutionUrl);
        try {
            while (trip.getForwardUrl() == null || "wait.jsp".equals(trip.getForwardUrl())) {
                trip = new MockRoundtrip(context, actionBeanUrl, session);
                for (Map.Entry<String, String> entry: parameters.entrySet()) {
                    trip.addParameter(entry.getKey(), entry.getValue());
                }
                trip.execute();
            }
            // We except an exception to be thrown.
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
        try {
            while ("wait.jsp".equals(trip.getForwardUrl())) {
                trip = new MockRoundtrip(context, actionBeanUrl, session);
                for (Map.Entry<String, String> entry: parameters.entrySet()) {
                    trip.addParameter(entry.getKey(), entry.getValue());
                }
                trip.execute();
            }
            // We except an exception to be thrown.
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
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
        while ("ajax.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
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
        while ("ajax.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
        while ("ajax.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        
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
     * Test that session attributes are available in event.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void sessionTest() throws Exception {
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
        // Test to see if attributes are in request/session.
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("HandlerResolution"), "HandlerResolutionAttribute");
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("BindingAndValidation"), "BindingAndValidationAttribute");
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("CustomValidation"), "CustomValidationAttribute");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        // Test to see if attributes are in request/session.
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("HandlerResolution"), "HandlerResolutionAttribute");
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("BindingAndValidation"), "BindingAndValidationAttribute");
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("CustomValidation"), "CustomValidationAttribute");
        Assert.assertEquals(trip.getRequest().getSession().getAttribute("EventHandling"), "EventHandlingAttribute");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/RequestTest.action"), trip.getActionBean(RequestTestActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/RequestTest.action"), "Action bean must be save under it's URL");
    }
    
    /**
     * Test that session attributes are available in event.
     * @throws Exception
     */
    @Test(groups="messages")
    public void requestMessages() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        MockRoundtrip trip = new MockRoundtrip(context, MessagesActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.setSourcePage("sourcePage.jsp");
        trip.execute("add");
        
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
        // Test that messages are available.
        Assert.assertNotNull(this.getMessages(trip.getRequest(), StripesConstants.REQ_ATTR_MESSAGES), "No messages found in wait page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "BindingAndValidation"), "No messages found for key BindingAndValidation in wait page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "CustomValidation"), "No messages found for key CustomValidation in wait page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), StripesConstants.REQ_ATTR_MESSAGES).isEmpty(), "No messages found in wait page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "BindingAndValidation").isEmpty(), "No messages found for key BindingAndValidation in wait page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "CustomValidation").isEmpty(), "No messages found for key CustomValidation in wait page");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(MessagesActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/Messages.action"), trip.getActionBean(MessagesActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Messages.action"), "Action bean must be save under it's URL");
        while ("wait.jsp".equals(trip.getForwardUrl())) {
            trip = new MockRoundtrip(context, actionBeanUrl, session);
            for (Map.Entry<String, String> entry: parameters.entrySet()) {
                trip.addParameter(entry.getKey(), entry.getValue());
            }
            trip.execute();
        }
        Assert.assertEquals(trip.getForwardUrl(), "index.jsp");
        // Test that messages are available.
        Assert.assertNotNull(this.getMessages(trip.getRequest(), StripesConstants.REQ_ATTR_MESSAGES), "No messages found in result page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "BindingAndValidation"), "No messages found for key BindingAndValidation in result page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "CustomValidation"), "No messages found for key CustomValidation in result page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "EventHandling"), "No messages found for key EventHandling in result page");
        Assert.assertNotNull(this.getMessages(trip.getRequest(), "add"), "No messages found for key add in result page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), StripesConstants.REQ_ATTR_MESSAGES).isEmpty(), "No messages found in result page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "BindingAndValidation").isEmpty(), "No messages found for key BindingAndValidation in result page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "CustomValidation").isEmpty(), "No messages found for key CustomValidation in result page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "EventHandling").isEmpty(), "No messages found for key EventHandling in result page");
        Assert.assertFalse(this.getMessages(trip.getRequest(), "add").isEmpty(), "No messages found for key add in result page");
        // Test that action bean is in request.
        Assert.assertNotNull(trip.getRequest().getAttribute("actionBean"), "Action bean must be save in request");
        Assert.assertEquals(trip.getRequest().getAttribute("actionBean"), trip.getActionBean(MessagesActionBean.class));
        Assert.assertEquals(trip.getRequest().getAttribute("/Messages.action"), trip.getActionBean(MessagesActionBean.class));
        Assert.assertNotNull(trip.getRequest().getAttribute("/Messages.action"), "Action bean must be save under it's URL");
    }
    /**
     * Gets the list of messages that will be displayed by the tag.  Looks first in the request
     * under the specified key, and if none are found, then looks in session under the same key.
     *
     * @return List<Message> a possibly null list of messages to display
     */
    @SuppressWarnings("unchecked")
    protected List<Message> getMessages(HttpServletRequest request, String key) {
        List<Message> messages = (List<Message>) request.getAttribute(key);

        if (messages == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                messages = (List<Message>) session.getAttribute(key);
                session.removeAttribute(key);
            }
        }

        return messages;
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
