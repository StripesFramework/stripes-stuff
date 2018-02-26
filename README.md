# Stripes Stuff

A collection of plugins, extensions and integrations for the [Stripes Framework](https://github.com/StripesFramework/stripes).

NOTE:
This a modified version that solves the problem that (Not)Allowed tags do not work with FreeMarker.
There are two problems:

Firstly FreeMarker apparently is more strict about the tag class hierarchy than JSP and throws an Exception when using the Allowed tags with Stripes stuff version 0.5.0 or earlier. 

A second problem is that the doStartTag method returns EVAL_BODY_AGAIN, which is a value specified by the IterationTag class for use by the doAfterBody() method.  A doStartTag method should return EVAL_BODY_INCLUDE instead.
(btw. the Tag interface/class hierarchy is a bit sick actually...)

Both solutions were inpired by/taken from: 
  http://stripes-users.narkive.com/YS1xZX3m/securitymanager-throws-jsptagexception-when-using-security-allowed
(though no reference to FreeMarker problems is made).
