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
        <title>Cari Hadis!</title>
    </head>
    <body>
        <h1>Cari Hadis!</h1>

        <div class="main">
            <form action="Search" method="post">
                <input id="searchbar" class="text" name="kueri" type="text"><br>
                <div>
                    Skema pembobotan :
                    <input type="radio" name="skema" value="bim" required> BIM
                    <input type="radio" name="skema" value="okapi"> Okapi
                </div>
                <br>
                <div class="search">
                    <input id="searchbutton" type="submit" value="Cari">
                </div>
            </form>
        </div>
    </body>
</html>