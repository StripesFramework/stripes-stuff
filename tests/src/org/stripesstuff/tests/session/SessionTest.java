/*
 * <p>Title: StripesUtils</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (IRCM)</p>
 */
package org.stripesstuff.tests.session;

import java.util.HashMap;
import java.util.Map;

import org.stripesstuff.plugin.session.SessionStoreInterceptor;
import org.stripesstuff.tests.session.action.Child1PersonActionBean;
import org.stripesstuff.tests.session.action.Child2PersonActionBean;
import org.stripesstuff.tests.session.action.FirstKeyActionBean;
import org.stripesstuff.tests.session.action.ListActionBean;
import org.stripesstuff.tests.session.action.LongMaxTimeActionBean;
import org.stripesstuff.tests.session.action.MaxTimeActionBean;
import org.stripesstuff.tests.session.action.MockHttpSessionWithEvent;
import org.stripesstuff.tests.session.action.NegativeMaxTimeActionBean;
import org.stripesstuff.tests.session.action.NoNegativeActionBean;
import org.stripesstuff.tests.session.action.NoResolutionActionBean;
import org.stripesstuff.tests.session.action.NonSerializeActionBean;
import org.stripesstuff.tests.session.action.PersonActionBean;
import org.stripesstuff.tests.session.action.PrimitiveActionBean;
import org.stripesstuff.tests.session.action.SecondKeyActionBean;
import org.stripesstuff.tests.session.action.SerializeActionBean;
import org.stripesstuff.tests.session.action.SessionActionBean;
import org.stripesstuff.tests.session.action.ZeroMaxTimeActionBean;
import org.stripesstuff.tests.session.bean.Person;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

/**
 * Test session interceptor.
 * 
 * @author Christian Poitras
 */
public class SessionTest {
    
    MockServletContext context;
    
    @BeforeGroups("session")
    public void setupContext() {
        context = new MockServletContext("test");
        
        // Add the Stripes Filter
        Map<String,String> filterParams = new HashMap<String,String>();
        filterParams.put("Interceptor.Classes",
                "org.stripesstuff.plugin.session.SessionStoreInterceptor");
        filterParams.put("ActionResolver.Packages",
                "org.stripesstuff.tests.session");
        context.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        
        // Add the Stripes Dispatcher
        context.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
    }
    @AfterGroups("session")
    public void releaseContext() {
        context = null;
    }
    
    
    /**
     * Attributes should not be shared among action beans inheriting abstract classes defining session fields.
     * Key attribute should be used instead.
     * @throws Exception
     */
    @Test(groups="session")
    public void abstractSharing() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Get first child.
        {
            MockRoundtrip trip = new MockRoundtrip(context, Child1PersonActionBean.class, session);
            trip.execute("reset");
            
            Child1PersonActionBean bean = trip.getActionBean(Child1PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        // Get second child.
        {
            MockRoundtrip trip = new MockRoundtrip(context, Child2PersonActionBean.class, session);
            trip.execute("reset");
            
            Child2PersonActionBean bean = trip.getActionBean(Child2PersonActionBean.class);
            Assert.assertEquals("John", bean.getPerson().getFirstName());
            Assert.assertEquals("Smith", bean.getPerson().getLastName());
        }
        
        // Second child should have a different person then first child.
        {
            MockRoundtrip trip1 = new MockRoundtrip(context, Child1PersonActionBean.class, session);
            trip1.execute();
            MockRoundtrip trip2 = new MockRoundtrip(context, Child2PersonActionBean.class, session);
            trip2.execute();
            
            Child1PersonActionBean bean1 = trip1.getActionBean(Child1PersonActionBean.class);
            Child2PersonActionBean bean2 = trip2.getActionBean(Child2PersonActionBean.class);
            Assert.assertEquals("Christian", bean1.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean1.getPerson().getLastName());
            Assert.assertEquals("John", bean2.getPerson().getFirstName());
            Assert.assertEquals("Smith", bean2.getPerson().getLastName());
        }
    }
    /**
     * When serializable attribute is true, value should stay in session when session passivate.
     * @throws Exception
     */
    @Test(groups="session")
    public void sessionSerialization() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SerializeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            SerializeActionBean bean = trip.getActionBean(SerializeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Simulate serialization.
        session.passivate();
        // Simulate deserialization.
        session.activate();
        
        // Add 2 (total 3).
        {
            MockRoundtrip trip = new MockRoundtrip(context, SerializeActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            SerializeActionBean bean = trip.getActionBean(SerializeActionBean.class);
            Assert.assertEquals(new Integer(3), bean.getResult());
        }
        
        
        // Serializable is true by default.
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Simulate serialization.
        session.passivate();
        // Simulate deserialization.
        session.activate();
        
        // Add 2 (total 3).
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(3), bean.getResult());
        }
    }
    /**
     * If serializable attribute is false, value in session should be automatically removed when session passivate.
     * @throws Exception
     */
    @Test(groups="session")
    public void sessionSerializationNonSerializable() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NonSerializeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            NonSerializeActionBean bean = trip.getActionBean(NonSerializeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Simulate serialization.
        session.passivate();
        // Simulate deserialization.
        session.activate();
        
        // Add 2.
        // Total should be 2 since value is removed from session on serialization.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NonSerializeActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            NonSerializeActionBean bean = trip.getActionBean(NonSerializeActionBean.class);
            // Serialization will remove object from session, so result will be reset.
            Assert.assertEquals(new Integer(2), bean.getResult());
        }
        
        
        // Even if serializable is true, field is not serialized if it does not implement java.io.Serializable.
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        
        // Simulate serialization.
        session.passivate();
        // Simulate deserialization.
        session.activate();
        
        // Add 2 (total 3).
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertNull(bean.getPerson());
        }
    }
    
    
    /**
     * Test @Session annotation for list of simple objects.
     * @throws Exception
     */
    @Test(groups="session")
    public void list() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Set 2 numbers.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.setParameter("numbers[0]", "1");
            trip.setParameter("numbers[1]", "2");
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getNumbers().get(0));
            Assert.assertEquals(new Integer(2), bean.getNumbers().get(1));
        }
        
        // Check if numbers are in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getNumbers().get(0));
            Assert.assertEquals(new Integer(2), bean.getNumbers().get(1));
        }
        
        // Replace first number.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.setParameter("numbers[0]", "77");
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals(new Integer(77), bean.getNumbers().get(0));
            // There shouldn't be a second number.
            Assert.assertEquals(1, bean.getNumbers().size());
        }
    }
    /**
     * Test @Session annotation with complex objects.
     * @throws Exception
     */
    @Test(groups="session")
    public void complexObject() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Set first name and last name.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        
        // Check if first name and last name are in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        
        // Properties can be overwritten.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.setParameter("person.firstName", "John");
            trip.setParameter("person.lastName", "Smith");
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("John", bean.getPerson().getFirstName());
            Assert.assertEquals("Smith", bean.getPerson().getLastName());
        }
        
        // If only one property is set, object should be replaced.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals(null, bean.getPerson().getLastName());
        }
    }
    /**
     * Test @Session annotation for complex object with a default value.
     * @throws Exception
     */
    @Test(groups="session")
    public void complexDefault() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Set person to default.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.execute("defaultPerson");
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        // Check if first name and last name are in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        
        
        // Set new person values.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.setParameter("person.firstName", "John");
            trip.setParameter("person.lastName", "Smith");
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("John", bean.getPerson().getFirstName());
            Assert.assertEquals("Smith", bean.getPerson().getLastName());
        }
        // Check if first name and last name are still correct.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PersonActionBean.class, session);
            trip.execute();
            
            PersonActionBean bean = trip.getActionBean(PersonActionBean.class);
            Assert.assertEquals("John", bean.getPerson().getFirstName());
            Assert.assertEquals("Smith", bean.getPerson().getLastName());
        }
    }
    /**
     * Test @Session annotation for list of complex objects.
     * @throws Exception
     */
    @Test(groups="session")
    public void complexList() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Set 2 persons.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.setParameter("persons[0].firstName", "Christian");
            trip.setParameter("persons[0].lastName", "Poitras");
            trip.setParameter("persons[1].firstName", "John");
            trip.setParameter("persons[1].lastName", "Smith");
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals("Christian", bean.getPersons().get(0).getFirstName());
            Assert.assertEquals("Poitras", bean.getPersons().get(0).getLastName());
            Assert.assertEquals("John", bean.getPersons().get(1).getFirstName());
            Assert.assertEquals("Smith", bean.getPersons().get(1).getLastName());
        }
        
        // Check if first name and last name are in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals("Christian", bean.getPersons().get(0).getFirstName());
            Assert.assertEquals("Poitras", bean.getPersons().get(0).getLastName());
            Assert.assertEquals("John", bean.getPersons().get(1).getFirstName());
            Assert.assertEquals("Smith", bean.getPersons().get(1).getLastName());
        }
        
        // Replace first person.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ListActionBean.class, session);
            trip.setParameter("persons[0].firstName", "Tim");
            trip.setParameter("persons[0].lastName", "Robbins");
            trip.execute();
            
            ListActionBean bean = trip.getActionBean(ListActionBean.class);
            Assert.assertEquals("Tim", bean.getPersons().get(0).getFirstName());
            Assert.assertEquals("Robbins", bean.getPersons().get(0).getLastName());
            // There shouldn't be a second person.
            Assert.assertEquals(1, bean.getPersons().size());
        }
    }
    
    
    /**
     * Test @Session annotation.
     * @throws Exception
     */
    @Test(groups="session")
    public void noAttributes() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Add number to result.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "4");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
    }
    /**
     * Session annotation should keep separate values for each sessions.<br>
     * @throws Exception
     */
    @Test(groups="session")
    public void twoSessionsNoAttributes() throws Exception {
        MockHttpSessionWithEvent session1 = new MockHttpSessionWithEvent(context);
        MockHttpSessionWithEvent session2 = new MockHttpSessionWithEvent(context);
        
        // Set session 1 result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session1);
            trip.setParameter("number", "1");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        // Set session 2 result to 3.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session2);
            trip.setParameter("number", "3");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(3), bean.getResult());
        }
        
        // Add 4 to session 1 (total 5).
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session1);
            trip.setParameter("number", "4");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
        // Add 8 to session 2 (total 11).
        // This will test is addition in session 1 conflicted with value in session 2.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session2);
            trip.setParameter("number", "8");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(11), bean.getResult());
        }
        
        // Add 2 to session 1 (total 7).
        // This will test is addition in session 2 conflicted with value in session 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session1);
            trip.setParameter("number", "2");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(7), bean.getResult());
        }
    }
    
    
    /**
     * Test maxTime attribute of @Session.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void shortMaxTime() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Wait over 1 minutes.
        Thread.sleep(1000 * 60 + 2000);
        
        // Add 4.
        // Total should be 4 since result in session is removed.
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "4");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
    }
    /**
     * MaxTime value should be ignored as long as close requests are done to value in session.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void shortMaxTimeMultipleRequests() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Wait less then 1 minutes.
        Thread.sleep(700 * 60);
        
        // Add 4 (total 5).
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "4");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
        
        // Wait less then 1 minutes, but both waits will exceed 1 minute.
        Thread.sleep(700 * 60);
        
        // Add 3 (total 8).
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(8), bean.getResult());
        }
        
        // Wait more then 1 minutes.
        // Here the result value should be removed from session since we exceeded maxTime.
        Thread.sleep(1100 * 60);
        
        // Add 5 (total 5 since result is resetted).
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "5");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
    }
    /**
     * Test longer maxTime attributes.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void longMaxTime() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, LongMaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            LongMaxTimeActionBean bean = trip.getActionBean(LongMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Wait over 1 minutes.
        Thread.sleep(1000 * 60 + 2000);
        
        // Add 4 (total 5).
        {
            MockRoundtrip trip = new MockRoundtrip(context, LongMaxTimeActionBean.class, session);
            trip.setParameter("number", "4");
            trip.execute();
            
            LongMaxTimeActionBean bean = trip.getActionBean(LongMaxTimeActionBean.class);
            // result will still be in session, so it should equal 1 + 4.
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
    }
    /**
     * Validate that long and short maxTime in the same session does not conflict.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void longShortMaxTime() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set short time result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        // Set long time result to 2.
        {
            MockRoundtrip trip = new MockRoundtrip(context, LongMaxTimeActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            LongMaxTimeActionBean bean = trip.getActionBean(LongMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(2), bean.getResult());
        }
        
        // Wait over 1 minutes.
        Thread.sleep(1000 * 60 + 2000);
        
        // Add 3 to short time result.
        // Total should be 3 since value is normally resetted.
        {
            MockRoundtrip trip = new MockRoundtrip(context, MaxTimeActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            MaxTimeActionBean bean = trip.getActionBean(MaxTimeActionBean.class);
            Assert.assertEquals(new Integer(3), bean.getResult());
        }
        // Add 4 to long time result.
        // Value should be 6 since long time result is supposed to be in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, LongMaxTimeActionBean.class, session);
            trip.setParameter("number", "4");
            trip.execute();
            
            LongMaxTimeActionBean bean = trip.getActionBean(LongMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(6), bean.getResult());
        }
    }
    
    
    /**
     * Interceptor should work properly even when no resolution is returned.
     * @throws Exception
     */
    @Test(groups="session")
    public void noResolution() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoResolutionActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            NoResolutionActionBean bean = trip.getActionBean(NoResolutionActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Add 2 (total 3).
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoResolutionActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            NoResolutionActionBean bean = trip.getActionBean(NoResolutionActionBean.class);
            Assert.assertEquals(new Integer(3), bean.getResult());
        }
    }
    
    /**
     * Test if primitive field works.
     * @throws Exception
     */
    @Test(groups="session")
    public void primitive() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, PrimitiveActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            PrimitiveActionBean bean = trip.getActionBean(PrimitiveActionBean.class);
            Assert.assertEquals(1, bean.getResult());
        }
        
        // Add 2 (total 3).
        {
            MockRoundtrip trip = new MockRoundtrip(context, PrimitiveActionBean.class, session);
            trip.setParameter("number", "2");
            trip.execute();
            
            PrimitiveActionBean bean = trip.getActionBean(PrimitiveActionBean.class);
            Assert.assertEquals(3, bean.getResult());
        }
    }
    
    
    /**
     * Parameters should replace values of annotated fields.
     * @throws Exception
     */
    @Test(groups="session")
    public void overwrite() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Replace result (and add 3 to prevent validation errors).
        // Total should be 5.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("result", "2");
            trip.setParameter("number", "3");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            // result is replaced by 2.
            Assert.assertEquals(new Integer(5), bean.getResult());
        }
        
        // Add 3 (total 8).
        {
            MockRoundtrip trip = new MockRoundtrip(context, SessionActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            SessionActionBean bean = trip.getActionBean(SessionActionBean.class);
            Assert.assertEquals(new Integer(8), bean.getResult());
        }
    }
    
    
    /**
     * Negative maxTime values should be considered as infinite.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void negativeMaxTime() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NegativeMaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            NegativeMaxTimeActionBean bean = trip.getActionBean(NegativeMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Wait over 1 minutes.
        Thread.sleep(1000 * 60 + 2000);
        
        // Add 3 (total 4).
        {
            MockRoundtrip trip = new MockRoundtrip(context, NegativeMaxTimeActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            NegativeMaxTimeActionBean bean = trip.getActionBean(NegativeMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
    }
    
    
    /**
     * 0 maxTime value should stay in session forever.
     * @throws Exception
     */
    @Test(groups={"session", "long"})
    public void zeroMaxTime() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, ZeroMaxTimeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            ZeroMaxTimeActionBean bean = trip.getActionBean(ZeroMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Wait over 1 minutes.
        Thread.sleep(1000 * 60 + 2000);
        
        // Add 3 (total 4).
        {
            MockRoundtrip trip = new MockRoundtrip(context, ZeroMaxTimeActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            ZeroMaxTimeActionBean bean = trip.getActionBean(ZeroMaxTimeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
    }
    
    /**
     * When validation errors occurs, value in session should not be updated.
     * @throws Exception
     */
    @Test(groups="session")
    public void validationError() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        // Set result to 1.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.execute();
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(new Integer(1), bean.getResult());
        }
        
        // Cause validation error in binding setting result to a non-number value.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "1");
            trip.setParameter("result", "a");
            trip.execute();
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(null, bean.getResult());
        }
        
        // Add 3.
        // Total should be 4 since result was not previously updated.
        // Note that validation errors should not cause field to be saved.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "3");
            trip.execute();
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
        
        // Cause validation error in binding by adding non-number value.
        // Since result is not updated, result should be 4.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "a");
            trip.execute();
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
        
        // Cause validation error in custom validation by adding a big negative result.
        // Since result is not updated, result should be 4.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "-100");
            trip.execute("customAddNumber");
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
        
        // Cause validation error in event handling by adding a big negative result.
        // Since result is not updated, result should be 4.
        {
            MockRoundtrip trip = new MockRoundtrip(context, NoNegativeActionBean.class, session);
            trip.setParameter("number", "-90");
            trip.execute();
            
            NoNegativeActionBean bean = trip.getActionBean(NoNegativeActionBean.class);
            Assert.assertEquals(new Integer(4), bean.getResult());
        }
    }
    
    
    /**
     * Test @Session key attribute.
     * @throws Exception
     */
    @Test(groups="session")
    public void keyAttibute() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        String personKey = FirstKeyActionBean.class.getDeclaredField("person").getAnnotation(org.stripesstuff.plugin.session.Session.class).key();
        
        // Set person.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            FirstKeyActionBean bean = trip.getActionBean(FirstKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
            // Person should be in session.
            Assert.assertNotNull(session.getAttribute(personKey));
        }
        // Check is person is still in session.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.execute();
            
            FirstKeyActionBean bean = trip.getActionBean(FirstKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
            // Person should be in session.
            Assert.assertNotNull(session.getAttribute(personKey));
        }
    }
    /**
     * Test removal of object from session when event set it to null.
     * @throws Exception
     */
    @Test(groups="session")
    public void removeNull() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        String personKey = FirstKeyActionBean.class.getDeclaredField("person").getAnnotation(org.stripesstuff.plugin.session.Session.class).key();
        
        // Set person.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            FirstKeyActionBean bean = trip.getActionBean(FirstKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
            // Person should be in session.
            Assert.assertNotNull(session.getAttribute(personKey));
        }
        // Remove person from session using action bean.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.execute("remove");
            // Person should not be in session.
            Assert.assertNull(session.getAttribute(personKey));
        }
    }
    /**
     * Test {@link SessionStoreInterceptor#getAttribute(javax.servlet.http.HttpSession, String) SessionStoreInterceptor.get} to get object from session.
     * @throws Exception
     */
    @Test(groups="session")
    public void getAttibute() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        String personKey = FirstKeyActionBean.class.getDeclaredField("person").getAnnotation(org.stripesstuff.plugin.session.Session.class).key();
        
        // Set person.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            FirstKeyActionBean bean = trip.getActionBean(FirstKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        
        // Get person from Interceptor.
        // Test kept for compatibility reasons.
        @SuppressWarnings("deprecation")
        Object person = SessionStoreInterceptor.getAttribute(session, personKey);
        Assert.assertTrue(Person.class.isAssignableFrom(person.getClass()));
        Assert.assertEquals("Christian", ((Person)person).getFirstName());
        Assert.assertEquals("Poitras", ((Person)person).getLastName());
        
        // Compare with session content.
        Assert.assertTrue(Person.class.isAssignableFrom(session.getAttribute(personKey).getClass()));
        Assert.assertEquals("Christian", ((Person)session.getAttribute(personKey)).getFirstName());
        Assert.assertEquals("Poitras", ((Person)session.getAttribute(personKey)).getLastName());
    }
    /**
     * When key attribute is used, fields can be shared between objects.
     * @throws Exception
     */
    @Test(groups="session")
    public void sharedKeyAttibute() throws Exception {
        MockHttpSessionWithEvent session = new MockHttpSessionWithEvent(context);
        
        // Set person.
        {
            MockRoundtrip trip = new MockRoundtrip(context, FirstKeyActionBean.class, session);
            trip.setParameter("person.firstName", "Christian");
            trip.setParameter("person.lastName", "Poitras");
            trip.execute();
            
            FirstKeyActionBean bean = trip.getActionBean(FirstKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
        // Check is person is accessible from second action bean.
        {
            MockRoundtrip trip = new MockRoundtrip(context, SecondKeyActionBean.class, session);
            trip.execute();
            
            SecondKeyActionBean bean = trip.getActionBean(SecondKeyActionBean.class);
            Assert.assertEquals("Christian", bean.getPerson().getFirstName());
            Assert.assertEquals("Poitras", bean.getPerson().getLastName());
        }
    }
    
    
    /**
     * TODO Mix maxTime and serializable attribute in a test.
     */
}
