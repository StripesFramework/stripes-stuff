<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<html>
<head>
</head>

<body>
    
    <s:form beanclass="org.stripesstuff.examples.session.CounterAction" method="POST">
        <p>Count: <c:out value="${actionBean.counter}" default="0"></c:out></p>
        
        <s:submit name="count">Add 1 to counter</s:submit><br><br>
        
        <s:errors field="amount"></s:errors>
        Enter amount to add to counter: <s:text name="amount" value=""></s:text> <s:submit name="countAmount">Add</s:submit>
    </s:form>
    
</body>
</html>