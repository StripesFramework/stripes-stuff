package org.stripesstuff.plugin.mailer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class PlainTextMessageBody implements MessageBody
{
	private String body;
	
	public PlainTextMessageBody(String body)
	{
		this.body = body;
	}

	public void appendTo(MimeMessage message) throws MessagingException
	{
		message.setContent(body, "text/plain");
	}
}
