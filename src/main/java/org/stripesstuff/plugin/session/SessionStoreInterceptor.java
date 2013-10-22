/*
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Institut de recherches cliniques de Montr&eacute;al (http://www.ircm.qc.ca)</p>
 */
package org.stripesstuff.plugin.session;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.stripesstuff.plugin.session.Session;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.util.ReflectUtil;

/**
 * Interceptor that stores or restores session objects.
 * 
 * @author Christian Poitras
 */
@Intercepts(value={LifecycleStage.ActionBeanResolution, LifecycleStage.EventHandling})
public class SessionStoreInterceptor implements Interceptor {
    
    /** Lazily filled in map of Class to fields annotated with Session. */
    private Map<Class<?>, Collection<Field>> fieldMap = new ConcurrentHashMap<Class<?>, Collection<Field>>();
    
    /**
     * Session attribute where map linking MaxTimeSaverThreads to keys is stored.
     */
    private static final String MAPPER_ATTRIBUTE = SessionStoreInterceptor.class + "#mapper";
    
    /* (non-Javadoc)
     * @see net.sourceforge.stripes.controller.Interceptor#intercept(net.sourceforge.stripes.controller.ExecutionContext)
     */
    public Resolution intercept(ExecutionContext context) throws Exception {
        // Continue on and execute other filters and the lifecycle code.
        Resolution resolution = context.proceed();
        
        // Get all fields with session.
        Collection<Field> fields = getSessionFields(context.getActionBean().getClass());
        
        // Restores values from session.
        if (LifecycleStage.ActionBeanResolution.equals(context.getLifecycleStage())) {
            this.restoreFields(fields, context.getActionBean(), context.getActionBeanContext());
        }
        // Store values in session.
        if (LifecycleStage.EventHandling.equals(context.getLifecycleStage())) {
            // Dont't update values in session if a validation error occured.
            if (context.getActionBeanContext().getValidationErrors().isEmpty()) {
            	if (fields.size() > 0)
            		this.saveFields(fields, context.getActionBean(), context.getActionBeanContext().getRequest().getSession());
            }
        }
        
        return resolution;
    }
    
    /**
     * Saves all fields in session.
     * @param fields Fields to save in session.
     * @param actionBean ActionBean.
     * @param session HttpSession.
     * @throws IllegalAccessException Cannot get access to some fields.
     */
    protected void saveFields(Collection<Field> fields, ActionBean actionBean, HttpSession session) throws IllegalAccessException {
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            setAttribute(session, getFieldKey(field, actionBean.getClass()), field.get(actionBean), ((Session)field.getAnnotation(Session.class)).serializable(), ((Session)field.getAnnotation(Session.class)).maxTime());
        }
    }
    /**
     * Restore all fields from value stored in session except if they.
     * @param fields Fields to restore from session.
     * @param actionBean ActionBean.
     * @param context ActionBeanContext.
     * @throws IllegalAccessException Cannot get access to some fields.
     */
    protected void restoreFields(Collection<Field> fields, ActionBean actionBean, ActionBeanContext context) throws IllegalAccessException {
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
	        Set<String> parameters = this.getParameters(context.getRequest());
	        for (Field field : fields) {
	            if (!field.isAccessible()) {
	                field.setAccessible(true);
	            }
	            if (!parameters.contains(field.getName())) {
	                // Replace value.
	                Object value = session.getAttribute(getFieldKey(field, actionBean.getClass()));
	                // If value is null and field is primitive, don't set value.
	                if (!(value == null && field.getType().isPrimitive())) {
	                    field.set(actionBean, value);
	                }
	            }
	        }
        }
    }
    /**
     * Returns all property that stripes will replace for request.
     * @param request Request.
     * @return All property that stripes will replace for request.
     */
    protected Set<String> getParameters(HttpServletRequest request) {
        Set<String> parameters = new HashSet<String>();
        Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String parameter = (String) paramNames.nextElement();
            // Keep only first property.
            while (parameter.contains(".") || parameter.contains("[")) {
                if (parameter.contains(".")) {
                    parameter = parameter.substring(0, parameter.indexOf("."));
                }
                if (parameter.contains("[")) {
                    parameter = parameter.substring(0, parameter.indexOf("["));
                }
            }
            parameters.add(parameter);
        }
        return parameters;
    }
    /**
     * Returns session key under which field should be saved or read.
     * @param field Field.
     * @param actionBeanClass Action bean class.
     * @return Session key under which field should be saved or read.
     */
    protected String getFieldKey(Field field, Class<? extends ActionBean> actionBeanClass) {
        // Use key attribute if it is defined.
        String sessionKey = ((Session)field.getAnnotation(Session.class)).key();
        if (sessionKey != null && !"".equals(sessionKey)) {
            return sessionKey;
        }
        else {
            // Use default key since no custom key is defined.
            return actionBeanClass + "#" + field.getName();
        }
    }
    /**
     * Returns all fields with Session annotation for a class.
     * @param clazz Class.
     * @return All fields with Session annotation for a class.
     */
    protected Collection<Field> getSessionFields(Class<?> clazz) {
        Collection<Field> fields = fieldMap.get(clazz);
        if (fields == null) {
            fields = ReflectUtil.getFields(clazz);
            Iterator<Field> iterator = fields.iterator();
            while (iterator.hasNext()) {
                Field field = iterator.next();
                if (!field.isAnnotationPresent(Session.class)) {
                    iterator.remove();
                }
            }
            fieldMap.put(clazz, fields);
        }
        return fields;
    }
    
    
    /**
     * Returns an object in session.
     * @param key Key under which object is saved.
     * @return Object.
     * @deprecated Use {@link HttpSession#getAttribute(String)} instead.
     */
    @Deprecated
    public static Object getAttribute(HttpSession session, String key) {
        return session.getAttribute(key);
    }
    /**
     * Saves an object in session for latter use.
     * @param session Session in which to store object.
     * @param key Key under which object is saved.
     * @param object Object to save.
     * @param serializable True if object is serializable.
     * @param maxTime Maximum time to keep object in session.
     * @return Object previously saved under key.
     */
    protected Object setAttribute(HttpSession session, String key, Object object, boolean serializable, int maxTime) {
        if (object == null) {
            // If object is null, remove attribute.
            Object ret;
            synchronized (session) {
                ret = session.getAttribute(key);
                session.removeAttribute(key);
            }
            return ret;
        }
        else {
            // Set object in session.
            Object ret;
            synchronized (session) {
                ret = session.getAttribute(key);
                session.setAttribute(key, object);
            }
            SessionMapper mapper = (SessionMapper) session.getAttribute(MAPPER_ATTRIBUTE);
            if (mapper == null) {
                // Register mapper for session.
                mapper = new SessionMapper();
                session.setAttribute(MAPPER_ATTRIBUTE, mapper);
            }
            synchronized (mapper) {
                // Update field mapper.
                SessionFieldMapper fieldMapper = mapper.get(key);
                if (fieldMapper == null) {
                    fieldMapper = new SessionFieldMapper(serializable && object instanceof Serializable);
                    mapper.put(key, fieldMapper);
                }
                if (maxTime > 0) {
                    // Register runnable to remove attribute.
                    if (fieldMapper.runnable != null) {
                        // Cancel old runnable because a new one will be created.
                        fieldMapper.runnable.cancel();
                    }
                    // Register runnable.
                    RemoveFieldRunnable runnable = new RemoveFieldRunnable(key, maxTime, session);
                    fieldMapper.runnable = runnable;
                    (new Thread(runnable)).start();
                }
            }
            return ret;
        }
    }
}
