<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String pageParam = request.getParameter("pageParam");
    String activeClass = "class='active'";
    Cookie[] cookies = request.getCookies();
    boolean isAuth = false;
    String name = "";
    if(cookies != null) {
        for(Cookie c : cookies){
            if(c.getName().equals("user")){
                isAuth = true;
                name = c.getValue();
                break;
            }
        }
    }
    
    boolean isLogin = (pageParam != null && pageParam.equals("login"));
    boolean isSignup = (pageParam != null && pageParam.equals("sugnup"));
    boolean isSelections = (pageParam != null && pageParam.equals("selections"));
    boolean isTests = (pageParam != null && pageParam.equals("tests"));
%>

<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <% if (isAuth) { %>
                <ul class="nav navbar-nav">
                    <li id='Selections-li' <%=((isSelections) ? activeClass : "")%> ><a 
                            href="/ExpTestService/selections" >Выборки<% 
                        if (isSelections) { 
                            %><span class="sr-only">(current)</span><%
                        }%></a></li>
                    <li id='Tests-li' <%=((isTests) ? activeClass : "")%> ><a 
                            href="/ExpTestService/tests" >Статистики<% 
                        if (isTests) { 
                            %><span class="sr-only">(current)</span><%
                        }%></a></li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" role="button" data-toggle="dropdown" aria-haspopup="true" 
                               contenteditable=""accesskey=""aria-expanded="false">Виктор<span class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <li id="logoff-li" type="button"><a href="/ExpTestService/logoff">Выйти</a></li>
                            <% if (isSelections) { %>
                            <li id="add-new-sel-li" type="button" data-toggle="modal" data-target="#modal-div" data-type="add"><a href="#">Загрузка</a></li>
                            <li id="modulate-sel-li" type="button" data-toggle="modal" data-target="#modal-div" data-type="modulate"><a href="#">Моделирование</a></li>
                            <% } %>
                        </ul>
                    </li>
                </ul>
                
            <% } else { %>
                <ul class="nav navbar-nav">
                    <li id='Log-in-li' <%=((isSelections) ? activeClass : "")%> ><a 
                            href="/ExpTestService/login" >Вход<% 
                        if (isLogin) { 
                            %><span class="sr-only">(current)</span><%
                        }%></a></li>
                    <li id='Sign-up-li' <%=((isTests) ? activeClass : "")%> ><a 
                            href="/ExpTestService/signup" >Регистрация<% 
                        if (isSignup) { 
                            %><span class="sr-only">(current)</span><%
                        }%></a></li>
                </ul>
            <% } %>
        </div>
        
    </div>
    
</nav>