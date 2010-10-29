/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.waitpage;

import static org.testng.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.stripesstuff.plugin.waitpage.Context;
import org.stripesstuff.plugin.waitpage.WaitPageInterceptor;
import org.stripesstuff.tests.waitpage.action.AdderActionBean;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
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
public class WaitPageExpiredTest {
    
    @SuppressWarnings("unused")
    private static final Log log = Log.getInstance(WaitPageExpiredTest.class);
    
    MockServletContext context;
    WaitPageInterceptor waitPageInterceptor;
    Map<Integer, Context> waitContexts;
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public void setupContext() throws Exception {
        context = new MockServletContext("waitpage");
        
        // Add the Stripes Filter
        Map<String,String> filterParams = new HashMap<String,String>();
        filterParams.put("CoreInterceptor.Classes",
                "org.stripesstuff.tests.waitpage.TestableWaitPageInterceptor," +
                "net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor," +
                "net.sourceforge.stripes.controller.HttpCacheInterceptor");
        filterParams.put("ActionResolver.Packages",
                "org.stripesstuff.tests.waitpage.action");
        filterParams.put("WaitPageInterceptor.ContextTimeout",
            "2000");
        context.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        
        // Add the Stripes Dispatcher
        context.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
        
        // Obtain WaitPageInterceptor instance.
        StripesFilter stripesFilter = (StripesFilter)context.getFilters().get(0);
        Iterator<Interceptor> interceptorsIter = stripesFilter.getInstanceConfiguration().getInterceptors(LifecycleStage.ActionBeanResolution).iterator();
        WaitPageInterceptor waitPageInterceptor = null;
        while (waitPageInterceptor == null && interceptorsIter.hasNext()) {
            Interceptor interceptor = interceptorsIter.next();
            if (interceptor instanceof WaitPageInterceptor) {
                waitPageInterceptor = (WaitPageInterceptor)interceptor;
            }
        }
        assertNotNull(waitPageInterceptor);
        Field field = WaitPageInterceptor.class.getDeclaredField("contexts");
        field.setAccessible(true);
        waitContexts = (Map<Integer, Context>)field.get(waitPageInterceptor);
    }
    @AfterClass
    public void releaseContext() {
        context = null;
    }
    
    
    /**
     * Expired contexts are removed automatically when they are expired.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void autoRemoveExpiredContexts() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        // Add a wait context.
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("shortAdd");
        
        assertEquals(waitContexts.size(), 1);
        
        Thread.sleep(3000);
        
        // After 2 seconds, context should be deleted when interceptor is called.
        trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("addNoWait");
        
        assertEquals(waitContexts.size(), 0);
    }
    /**
     * Expired contexts are removed automatically when they are expired.
     * @throws Exception
     */
    @Test(groups="waitpage")
    public void autoRemoveExpiredContextsLong() throws Exception {
        MockHttpSession session = new MockHttpSession(context);
        
        // Add a wait context.
        MockRoundtrip trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("twoRefreshAdd");
        
        assertEquals(waitContexts.size(), 1);
        
        Thread.sleep(3000);
        
        // After 3 seconds, context should still be in memory.
        trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("addNoWait");
        
        assertEquals(waitContexts.size(), 1);
        
        Thread.sleep(3000);
        
        // After 6 seconds, context should be deleted when interceptor is called.
        trip = new MockRoundtrip(context, AdderActionBean.class, session);
        trip.addParameter("first", String.valueOf(1));
        trip.addParameter("second", String.valueOf(2));
        trip.execute("addNoWait");
        
        assertEquals(waitContexts.size(), 0);
    }
}
