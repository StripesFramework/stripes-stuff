package org.stripesstuff.plugin.mailer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.util.Log;

public class UrlMessageBody implements MessageBody
{
	private static final Log								log							= Log.getInstance(UrlMessageBody.class);

	private static final String								STRIPES_STUFF_MAILER_KEY	= "__SSMK";

	private URL												url;
	private Map<String, Object>								parameters;
	private static final Map<String, Map<String, Object>>	instanceParameters			= new ConcurrentHashMap<String, Map<String, Object>>();
	private String											contentType;
	private String											content;

	private static int										keyCounter					= 0;

	private static Pattern									TITLE_REGEX					= Pattern.compile("(?is)<title(?=\\s|>)[^>]*>(.*?)</title");

	public UrlMessageBody(URL url) throws IOException
	{
		this(url, null);
	}

	public UrlMessageBody(URL url, Map<String, Object> parameters) throws IOException
	{
		this.url = url;
		this.parameters = parameters;
		
		loadUrl();
	}
	
	@SuppressWarnings("unchecked")
	private void loadUrl() throws IOException
	{
		String key = null;

		synchronized (this)
		{
			key = Integer.toHexString(keyCounter++);
		}
		try
		{
			if (parameters != null)
			{
				instanceParameters.put(key, parameters);
				
				StringBuilder query = new StringBuilder();
				
				if (url.getQuery() != null)
				{
					query.append('?');
					query.append(url.getQuery());
				}
				
				query.append(query.length() == 0 ? '?' : '&')
						.append(URLEncoder.encode(STRIPES_STUFF_MAILER_KEY, "UTF-8"))
						.append('=')
						.append(key);
				
				for (Map.Entry<String, Object> parameter : parameters.entrySet())
				{
					Object o = parameter.getValue();
					
					Formatter formatter = StripesFilter.getConfiguration().getFormatterFactory().getFormatter(o.getClass(), Locale.getDefault(), null, null);
					String value = null;
					if (formatter != null)
						value = formatter.format(o);
					else
						value = parameter.getValue().toString();

					log.trace("Adding parameter ", parameter.getKey(), " with value ", value);
					
					query.append('&')
							.append(URLEncoder.encode(parameter.getKey(), "UTF-8"))
							.append('=')
							.append(URLEncoder.encode(value, "UTF-8"));
				}
				
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + query);
			}
			
			log.debug("Retrieving data from ", url);
			
			URLConnection connection = url.openConnection();
			
			if (connection instanceof HttpURLConnection)
				contentType = ((HttpURLConnection) connection).getContentType();
			
			byte[] data = new byte[4096];
			int count;
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			StringBuilder content = new StringBuilder();
			
			while ((count = in.read(data)) != -1)
				content.append(new String(data, 0, count));
			
			in.close();
			
			this.content = content.toString();
		}
		catch (UnsupportedEncodingException e)
		{
			log.error(e, "This should NEVER happen!");
		}
		catch (MalformedURLException e)
		{
			log.error(e);
		}
		finally
		{
			// Memory leaks are not good!
			instanceParameters.remove(key);
		}
	}

	public String getTitle()
	{
		if (contentType != null && contentType.startsWith("text/html"))
		{
			Matcher matcher = TITLE_REGEX.matcher(content);

			if (matcher.find())
				return matcher.group(1).trim().replaceAll("\\s+", " ");
		}

		return null;
	}

	public void appendTo(MimeMessage message) throws MessagingException
	{
		if (contentType.startsWith("text/html") && (message.getSubject() == null))
		{
			String title = getTitle();

			if (title != null)
			{
				log.debug("setting subject: ", title);
				message.setSubject(title);
			} else
				log.warn("Couldn't find title. This email is going out without a subject!");
		}
		
		message.setContent(getHtml(), contentType);
	}
	
	private String getHtml()
	{
		String html = content;
		
		if (!html.matches("(?is).*<base\\s.*"))
		{
			String baseHref = url.toString().replaceAll("/[^/]*\\?.*", "/");
			html = html.replaceAll("(?is)(<head(?=\\s|>)[^>]*>)", "$1<base href=\"" + baseHref + "\"/>");
		}
		
		return html;
	}

	public URL getUrl()
	{
		return url;
	}

	public void setUrl(URL url)
	{
		this.url = url;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public static void setRequestAttributes(HttpServletRequest request)
	{
		String key = request.getParameter(STRIPES_STUFF_MAILER_KEY);

		if (key == null)
			return;

		Map<String, Object> parameters = instanceParameters.get(key);
		
		if (parameters == null)
			return;
		
		for (Map.Entry<String, Object> parameter : parameters.entrySet())
			request.setAttribute(parameter.getKey(), parameter.getValue());
	}

	public void setParameters(Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
}
