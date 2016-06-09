<%@page import="java.util.Set"%>
<%@page import="entities.SelectionInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Добро пожаловать!</title>
        <link rel="stylesheet" type="text/css" href="resources/my-styles.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap-theme.css" media="all" />
    </head>
    

    <body>
        <script src="resources/jquery-2.1.1.min.js"></script>
        <script src="resources/jquery.jqplot.js"></script>
        
        <jsp:include page="/WEB-INF/jsp/navbar.jsp">
            <jsp:param name="pageParam" value="index"/>
        </jsp:include>
    </body>
</html>
