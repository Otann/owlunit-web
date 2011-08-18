<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
	<META http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<title>owls</title>
</head>
<body>
<div class="container">
	<h1>
		Information Items Exposed Service Through JSON Protocol
	</h1>
	<p>
		Locale = ${pageContext.response.locale}
    <p>
		<img src="<c:url value="/resources/now-what.jpg" />" />
	<p>
	<ul>
		<li><a href="movies">Ajax @Controller Movies</a></li>
	</ul>
</div>
</body>
</html>