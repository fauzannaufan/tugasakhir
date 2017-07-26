<%-- 
    Document   : hadis
    Created on : Jul 24, 2017, 4:18:33 PM
    Author     : M. Fauzan Naufan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="main.css">
        <title>JSP Page</title>
    </head>
    <body>
        <a href="index.jsp">
            <h1>Cari Hadis!</h1>
            <c:set var="id" value="${param.id}" scope="request" />
            <c:set var="id" value="${param.kueri}" scope="request" />
            <jsp:include page="/HadisServlet" />
        </a>
    </body>
</html>
