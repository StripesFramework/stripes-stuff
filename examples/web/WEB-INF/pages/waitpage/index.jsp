<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<html>
<head>
</head>

<body>
    
    <c:if test="${actionBean.complete}">
        <div>
            Event completed.
        </div>
    </c:if>
    
    <s:form beanclass="org.stripesstuff.examples.waitpage.SlowAction" method="POST">
        <s:submit name="slowEvent">Slow event...</s:submit>
    </s:form>
    
</body>
</html>