<%@page import="java.util.Set"%>
<%@page import="entities.SelectionInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Регистрация</title>
        <link rel="stylesheet" type="text/css" href="resources/my-styles.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap-theme.css" media="all" />
    </head>

    <body>
        <script src="resources/jquery-2.1.1.min.js"></script>
        <script src="resources/jquery.jqplot.js"></script>
        
        <jsp:include page="/WEB-INF/jsp/navbar.jsp">
            <jsp:param name="pageParam" value="login"/>
        </jsp:include>
        
        <div class="container" style="width:40%; min-width: 500px;">
            <h2>Регистрация</h2>
            <label for="usernameInput" class="sr-only" >Имя</label>
            <input id="usernameInput" class="form-control" placeholder="Имя" style="margin-bottom: 10px;" >
            <label for="emailInput" class="sr-only" >Email</label>
            <input id="emailInput" class="form-control" placeholder="Email" style="margin-bottom: 10px;" >
            <label for="passInput" class="sr-only">Пароль</label>
            <input id="passInput" type="password" class="form-control" placeholder="Пароль" style="margin-bottom: 10px;">
            <label for="pass2Input" class="sr-only">Повторите пароль</label>
            <input id="pass2Input" type="password" class="form-control" placeholder="Повторите пароль" style="margin-bottom: 10px;">
            <button id="login" class="btn btn-lg btn-primary btn-block" type="submit">Зарегистрироваться</button>
        </div>
        
        <script>
            function createCookie(name, value, days) {
                var expires;

                if (days) {
                var date = new Date();
                date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
                    expires = "; expires=" + date.toGMTString();
                } else {
                    expires = "";
                }
                document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) + expires + "; path=/";
            }
            
            jQuery(document).ready(function ($) {
                var usernameInput = $("#usernameInput");
                var emailInput = $("#emailInput");
                var passInput = $("#passInput");  
                var pass2Input = $("#pass2Input");
                $("#loginbtn").click(function(){
                    var name = usernameInput.val();
                    if (name === "") {
                        usernameInput.css("border-color", "red");
                        return;
                    } else {
                        usernameInput.css("border-color", "");
                    }
                    
                    var email = emailInput.val();
                    if (email === "") {
                        emailInput.css("border-color", "red");
                        return;
                    } else {
                        emailInput.css("border-color", "");
                    }
                    
                    var password = passInput.val();
                    if (password === "") {
                        passInput.css("border-color", "red");
                        return;
                    } else {
                        passInput.css("border-color", "");
                    }
                    
                    var password2 = pass2Input.val();
                    if (password !== password2) {
                        pass2Input.css("border-color", "red");
                        return;
                    } else {
                        pass2Input.css("border-color", "");
                    }
                    
                    createCookie("user", name, 1);
                    document.location.href = "http://localhost:8080/ExpTestService/";
                });
            });
        </script>
        
    </body>
</html>
