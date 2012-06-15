package org.stripesstuff.plugin.mailer;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.util.Log;

public class Mailer
{
	private static final Log		log		= Log.getInstance(Mailer.class);
	private static final Properties	DEFAULT_PROPERTIES;

	static
	{
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.setProperty("mail.transport.protocol", "smtp");
		DEFAULT_PROPERTIES.setProperty("mail.host", "localhost");

		log.debug("static initializer complete");
	}

	private String					subject;
	private MessageBody				body;
	private UrlMessageBody			urlMessageBody;
	private URL						url;
	private String					contentType;
	private InternetAddress			from;
	private Set<InternetAddress>	to		= new HashSet<InternetAddress>();
	private Set<InternetAddress>	CC		= new HashSet<InternetAddress>();
	private Set<InternetAddress>	BCC		= new HashSet<InternetAddress>();

	private Session					session;
	
	private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>(); 

	public Mailer()
	{
		this(null);
	}
	
	public Mailer(HttpServletRequest request)
	{
		this(request, DEFAULT_PROPERTIES);
	}

	public Mailer(HttpServletRequest request, Properties properties)
	{
		this(request, properties, null);
	}

	public Mailer(HttpServletRequest request, Authenticator authenticator)
	{
		this(request, DEFAULT_PROPERTIES, authenticator);
	}

	public Mailer(HttpServletRequest request, Properties properties, Authenticator authenticator)
	{
		Mailer.request.set(request);

		session = Session.getDefaultInstance(properties, authenticator);
	}
	
	static HttpServletRequest getRequest()
	{
		return request.get();
	}
	
	public void send() throws MessagingException
	{
		log.debug("preparing email");
		MimeMessage message = new MimeMessage(session);

		if (to.size() > 0)
		{
			log.debug("setting to: ", to);
			message.setRecipients(MimeMessage.RecipientType.TO, (InternetAddress[]) to.toArray(new InternetAddress[0]));
		}
		if (CC.size() > 0)
		{
			log.debug("setting cc: ", CC);
			message.setRecipients(MimeMessage.RecipientType.CC, (InternetAddress[]) CC.toArray(new InternetAddress[0]));
		}
		if (BCC.size() > 0)
		{
			log.debug("setting bcc: ", BCC);
			message.setRecipients(MimeMessage.RecipientType.BCC, (InternetAddress[]) BCC.toArray(new InternetAddress[0]));
		}

		log.debug("setting from: ", from);
		message.setFrom(from);
		
		if (subject != null)
		{
			log.debug("setting subject: ", subject);
			message.setSubject(subject);
		}

		body.appendTo(message);

		log.debug("sending email");
		Transport.send(message);

		log.debug("email sent to ", to);
	}
	
	protected static Set<InternetAddress> toInternetAddress(String...addresses) throws AddressException
	{
		Set<InternetAddress> set = new HashSet<InternetAddress>();
		
		for (String address : addresses)
			set.add(new InternetAddress(address));
		
		return set;
	}
	
	public Mailer setBody(URL url) throws IOException
	{
		return setBody(url, (Map<String, Object>) new HashMap<String, Object>());
	}
	
	public Mailer setBody(URL url, Map<String, Object> parameters) throws IOException
	{
		body = new UrlMessageBody(url, parameters);
		
		return this;
	}
	
	public Mailer setBody(URL url, Object... parameters) throws IOException
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (int i = 0; i < parameters.length - 1; i+=2)
			map.put((String) parameters[i], parameters[i+1]);

		return setBody(url, map);
	}
	
	public Set<InternetAddress> getBCC()
	{
		return BCC;
	}
	public Mailer setBCC(Set<InternetAddress> bcc)
	{
		this.BCC = bcc;
		return this;
	}
	public Set<InternetAddress> getCC()
	{
		return CC;
	}
	public Mailer setCC(Set<InternetAddress> cc)
	{
		this.CC = cc;
		return this;
	}
	public String getContentType()
	{
		return contentType;
	}
	public Mailer setContentType(String contentType)
	{
		this.contentType = contentType;
		return this;
	}
	public InternetAddress getFrom()
	{
		return from;
	}
	public Mailer setFrom(InternetAddress from)
	{
		this.from = from;
		return this;
	}
	public Mailer setFrom(String from) throws AddressException
	{
		this.from = new InternetAddress(from);
		return this;
	}
	public MessageBody getBody()
	{
		return body;
	}
	public Mailer setBody(String body)
	{
		// if it looks like a URL send it off to a different setBody
		if (body.charAt(0) == '/' || body.startsWith("http"))
		{
			try
			{
				return setBody(body, (Object[]) null);
			}
			catch (Exception e)
			{
				log.error("body looked like a URL but an exception occurred: ", e);
			}
		}

		this.body = new PlainTextMessageBody(body);
		return this;
	}
	public Mailer setBody(String url, Object... parameters) throws IOException
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (parameters != null)
		{
			for (int i = 0; i < parameters.length;)
				map.put(parameters[i++].toString(), parameters[i++]);
		}
		
		if (url.charAt(0) == '/')
		{
			HttpServletRequest request = getRequest();
			
			if (request == null)
				log.error("Mailer needs to be passed an HttpServletRequest in the constructor if you want to email relative URLs");
			else
			{
				url = request.getContextPath() + url;
				
				log.debug("Setting body to ", url);
				
				return setBody(new URL(	request.getScheme(),
										request.getServerName(),
										request.getServerPort(),
										url
									  ), map);
			}
		}
		
		return setBody(new URL(url), map);
	}
	
	public String getSubject()
	{
		return subject;
	}
	public Mailer setSubject(String subject)
	{
		this.subject = subject;
		return this;
	}
	public Set<InternetAddress> getTo()
	{
		return to;
	}
	public Mailer setTo(Set<InternetAddress> to)
	{
		this.to = to;
		return this;
	}
	public Mailer setTo(String to) throws AddressException
	{
		this.to.clear();
		return addTo(to);
	}
	public Mailer addTo(InternetAddress to)
	{
		this.to.add(to);
		return this;
	}
	public Mailer addTo(String...to) throws AddressException
	{
		getTo().addAll(toInternetAddress(to));
		return this;
	}
	public Mailer addCC(InternetAddress cc)
	{
		this.CC.add(cc);
		return this;
	}
	public Mailer addCC(String...cc) throws AddressException
	{
		getCC().addAll(toInternetAddress(cc));
		return this;
	}
	public Mailer addBCC(InternetAddress bcc)
	{
		this.BCC.add(bcc);
		return this;
	}
	public Mailer addBCC(String...bcc) throws AddressException
	{
		getBCC().addAll(toInternetAddress(bcc));
		return this;
	}

	public UrlMessageBody getUrlMessageBody()
	{
		return urlMessageBody;
	}

	public void setUrlMessageBody(UrlMessageBody urlMessageBody)
	{
		this.urlMessageBody = urlMessageBody;
	}

	public URL getUrl()
	{
		return url;
	}

	public void setUrl(URL url)
	{
		this.url = url;
	}

	public Session getSession()
	{
		return session;
	}

	public void setSession(Session session)
	{
		this.session = session;
	}

	public void setBody(MessageBody body)
	{
		this.body = body;
	}	
}
