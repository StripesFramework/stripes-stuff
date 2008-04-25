package org.stripesstuff.plugin.mailer;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

@Intercepts({LifecycleStage.RequestInit, LifecycleStage.RequestComplete})
public class MailerInterceptor implements Interceptor
{
	private static ThreadLocal<ExecutionContext> executionContext = new ThreadLocal<ExecutionContext>();
	
	public Resolution intercept(ExecutionContext executionContext) throws Exception
	{
		switch (executionContext.getLifecycleStage())
		{
			case RequestInit:
				MailerInterceptor.executionContext.set(executionContext);
				
				UrlMessageBody.setRequestAttributes(executionContext.getActionBeanContext().getRequest());
				break;
			case RequestComplete:
				MailerInterceptor.executionContext.remove();
				break;
		}

		return null;
	}

	public static ExecutionContext getExecutionContext()
	{
		return executionContext.get();
	}
}
