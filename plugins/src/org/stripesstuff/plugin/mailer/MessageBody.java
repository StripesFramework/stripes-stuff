package org.stripesstuff.plugin.mailer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface MessageBody
{
	public void appendTo(MimeMessage message) throws MessagingException;
}
