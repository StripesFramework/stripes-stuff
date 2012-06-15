package org.stripesstuff.plugin.mailer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class FakeRequest implements HttpServletRequest
{
//	private static final Log log = Log.getInstance(FakeRequest.class);
	
	final private HttpServletRequest request;
	final private Map<String,Object> attributes = new HashMap<String,Object>();
	
	public FakeRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	public String getAuthType()
	{
		return request.getAuthType();
	}

	public String getContextPath()
	{
		return request.getContextPath();
	}

	public Cookie[] getCookies()
	{
		return request.getCookies();
	}

	public long getDateHeader(String arg0)
	{
		return request.getDateHeader(arg0);
	}

	public String getHeader(String arg0)
	{
		return request.getHeader(arg0);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames()
	{
		return request.getHeaderNames();
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String arg0)
	{
		return request.getHeaders(arg0);
	}

	public int getIntHeader(String arg0)
	{
		return request.getIntHeader(arg0);
	}

	public String getMethod()
	{
		return request.getMethod();
	}

	public String getPathInfo()
	{
		return request.getPathInfo();
	}

	public String getPathTranslated()
	{
		return request.getPathTranslated();
	}

	public String getQueryString()
	{
		return request.getQueryString();
	}

	public String getRemoteUser()
	{
		return request.getRemoteUser();
	}

	public String getRequestURI()
	{
		return request.getRequestURI();
	}

	public StringBuffer getRequestURL()
	{
		return request.getRequestURL();
	}

	public String getRequestedSessionId()
	{
		return request.getRequestedSessionId();
	}

	public String getServletPath()
	{
		return request.getServletPath();
	}

	public HttpSession getSession()
	{
		return request.getSession();
	}

	public HttpSession getSession(boolean create)
	{
		return request.getSession(create);
	}

	public Principal getUserPrincipal()
	{
		return request.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie()
	{
		return request.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL()
	{
		return request.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl()
	{
		return request.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdValid()
	{
		return request.isRequestedSessionIdValid();
	}

	public boolean isUserInRole(String arg0)
	{
		return request.isUserInRole(arg0);
	}

	public Object getAttribute(String arg0)
	{
		return request.getAttribute(arg0);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames()
	{
		return request.getAttributeNames();
	}

	public String getCharacterEncoding()
	{
		return request.getCharacterEncoding();
	}

	public int getContentLength()
	{
		return request.getContentLength();
	}

	public String getContentType()
	{
		return request.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException
	{
		return request.getInputStream();
	}

	public String getLocalAddr()
	{
		return request.getLocalAddr();
	}

	public String getLocalName()
	{
		return request.getLocalName();
	}

	public int getLocalPort()
	{
		return request.getLocalPort();
	}

	public Locale getLocale()
	{
		return request.getLocale();
	}

	@SuppressWarnings("unchecked")
	public Enumeration getLocales()
	{
		return request.getLocales();
	}

	public String getParameter(String arg0)
	{
		return request.getParameter(arg0);
	}

	@SuppressWarnings("unchecked")
	public Map getParameterMap()
	{
		return request.getParameterMap();
	}

	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames()
	{
		return request.getParameterNames();
	}

	public String[] getParameterValues(String arg0)
	{
		return request.getParameterValues(arg0);
	}

	public String getProtocol()
	{
		return request.getProtocol();
	}

	public BufferedReader getReader() throws IOException
	{
		return request.getReader();
	}

	@SuppressWarnings("deprecation")
	public String getRealPath(String arg0)
	{
		return request.getRealPath(arg0);
	}

	public String getRemoteAddr()
	{
		return request.getRemoteAddr();
	}

	public String getRemoteHost()
	{
		return request.getRemoteHost();
	}

	public int getRemotePort()
	{
		return request.getRemotePort();
	}

	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		return request.getRequestDispatcher(arg0);
	}

	public String getScheme()
	{
		return request.getScheme();
	}

	public String getServerName()
	{
		return request.getServerName();
	}

	public int getServerPort()
	{
		return request.getServerPort();
	}

	public boolean isSecure()
	{
		return request.isSecure();
	}

	public void removeAttribute(String arg0)
	{
		attributes.remove(arg0);
	}

	public void setAttribute(String arg0, Object arg1)
	{
		attributes.put(arg0, arg1);
	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
	{
	}
}
