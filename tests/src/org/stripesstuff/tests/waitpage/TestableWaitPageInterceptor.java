package org.stripesstuff.tests.waitpage;

import org.stripesstuff.plugin.waitpage.Context;
import org.stripesstuff.plugin.waitpage.WaitPageInterceptor;

import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * {@link TestableWaitPageInterceptor} that creates {@link TestableContext} instead of {@link Context}. 
 * 
 * @author Christian Poitras
 */
@Intercepts({LifecycleStage.ActionBeanResolution,
                LifecycleStage.HandlerResolution,
                LifecycleStage.BindingAndValidation,
                LifecycleStage.CustomValidation,
                LifecycleStage.EventHandling,
                LifecycleStage.ResolutionExecution})
public class TestableWaitPageInterceptor extends WaitPageInterceptor {
    
    @Override
    protected Context createContext(ExecutionContext executionContext) {
        return new TestableContext(executionContext.getActionBeanContext().getRequest());
    }
}