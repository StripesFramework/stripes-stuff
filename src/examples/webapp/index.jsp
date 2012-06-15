<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
</head>

<body>
    
    <div>
        <s:url var="sessionLink" beanclass="org.stripesstuff.examples.session.CounterAction"/>
        <a href="${sessionLink}">Session plugin example</a>
    </div>
    
    <div>
        <s:url var="waitPageLink" beanclass="org.stripesstuff.examples.waitpage.SlowAction"/>
        <a href="${waitPageLink}">Wait page plugin example</a>
    </div>
    
</body>
</html>