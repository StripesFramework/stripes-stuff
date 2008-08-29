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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.StripesRequestWrapper;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.util.Log;

public class UrlMessageBody implements MessageBody
{
	private static final Log log = Log.getInstance(UrlMessageBody.class);

	private URL url;
	private Map<String, Object> parameters;
	private String contentType;
	private String content;

	private static Pattern TITLE_REGEX = Pattern.compile("(?is)<title(?=\\s|>)[^>]*>(.*?)</title");
	
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
		HttpServletRequest request = Mailer.getRequest();
		
		try
		{
			if (parameters != null)
			{
				StringBuilder query = new StringBuilder();
				
				if (url.getQuery() != null)
				{
					query.append('?');
					query.append(url.getQuery());
				}
				
				FlashScope flash = getFlashScope(request);
				
				if (flash != null)
					query.append(query.length() == 0 ? '?' : '&')
						.append(StripesConstants.URL_KEY_FLASH_SCOPE_ID)
						.append('=')
						.append(flash.key());
				else
					log.warn("Couldn't create FlashScope!");
				
				for (Map.Entry<String, Object> parameter : parameters.entrySet())
				{
					Object o = parameter.getValue();
					
					Formatter formatter = StripesFilter.getConfiguration().getFormatterFactory().getFormatter(o.getClass(), Locale.getDefault(), null, null);
					
					String name = parameter.getKey();
					String value = null;
					
					if (formatter != null)
						value = formatter.format(o);
					else
						value = o.toString();

					log.trace("Adding parameter ", name, " with value ", value);
					
					query.append('&')
							.append(URLEncoder.encode(name, "UTF-8"))
							.append('=')
							.append(URLEncoder.encode(value, "UTF-8"));
					
					if (flash != null)
						flash.put(name, o);
				}
				
				releaseFlashScope(flash);
				
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + query);
			}
			
			log.debug("Retrieving data from ", url);
			
			URLConnection connection = url.openConnection();
			
			HttpURLConnection http = (HttpURLConnection) connection;
			
			String cookie = request == null ? null : request.getHeader("Cookie");
			
			if (cookie != null)
			{
				log.trace("Adding cookies: ", cookie);
				http.addRequestProperty("Cookie", cookie);
			}
			
			contentType = http.getContentType();
			
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
	}
	
	private FlashScope getFlashScope(HttpServletRequest request)
	{
		if (request == null)
		{
			log.debug("Missing request!");
			return null;
		}
		
		log.trace("Attempting to create a FakeRequest");
		
		request = new FakeRequest(request);
		
		try
		{
			request = new StripesRequestWrapper(request);
		} catch (StripesServletException e)
		{
			log.error(e);
		}
		
		return FlashScope.getCurrent(request, true);		
	}
	
	private void releaseFlashScope(FlashScope flashScope)
	{
		if (flashScope != null)
			flashScope.completeRequest();
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
