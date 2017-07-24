<%-- 
    Document   : index
    Created on : Jul 24, 2017, 6:35:10 AM
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
        <h1>Cari Hadis!</h1>

        <div class="main">
            <form action="Search" method="post">
                <input type="hidden" name="method" value="bim">
                <input id="searchbar" class="text" name="kueri" type="text"><br>
                <div class="search">
                    <input id="searchbutton" type="submit" value="Cari">
                </div>
            </form>
        </div>
    </body>
</html>
