<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<c:url var="javascriptUrl" value="/javascript/jquery-1.3.1.min.js"/>
<script type="text/javascript" src="${javascriptUrl}"></script>
<script type="text/javascript">
<!--
var count = 0;
function updater() {
    var complete = false;
    var progress;
    count++;
	jQuery.get(window.location.href, {ajax: "true"}, function(content){
        jQuery("span.progress").html(jQuery(content).filter("span.progress").html());
        jQuery("span.complete").html(jQuery(content).filter("span.complete").html());
        progress = jQuery(content).filter("span.progress").html();
		complete = jQuery(content).filter("span.complete").html();
        if (complete == "true") {
            window.location.reload();
        } else {
            updater();
        }
	}, "html");
}
jQuery(function(){updater()});
-->
</script>
</head>

<body>
    <div>
        Progression: <span class="progress">0</span>
    </div>
    <div>
        Complete: <span class="complete">false</span>
    </div>
</body>
</html>