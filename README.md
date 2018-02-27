# Stripes Stuff

A collection of plugins, extensions and integrations for the [Stripes Framework](https://github.com/StripesFramework/stripes).

NOTE:
This a modified version that solves the problem that (Not)Allowed tags do not work with FreeMarker.
When used the following message appears:
Message: "Can't buffer body since org.stripesstuff.plugin.security.AllowedTag does not implement BodyTag."

This is the result of doStartTag() method of the AllowedTag returning EVAL_BODY_AGAIN if the tag "is allowed".
This EVAL_BODY_AGAIN value is defined in the IterationTag interface and is meant only to be returned from the doAfterBody() method to indicate that a next iteration is wanted.

Unfortunately EVAL_BODY_AGAIN has the same actual value as EVAL_BODY_BUFFERED, a valid return value for the doStartTag() of a BodyTag. So returning this value leads to the confusing FreeMarker error message. 

Solution is to have doStartTag() return EVAL_BODY_INCLUDE, the proper return value to have the body evaluated.

NOTE that this error is undoubtedly the result of the completely confusing way JSP has set up its class/interface hierarchy and return value definitions.
