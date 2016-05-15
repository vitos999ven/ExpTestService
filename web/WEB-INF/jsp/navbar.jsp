<%
    String pageParam = request.getParameter("pageParam");
    String activeClass = "class='active'";
    boolean isSelections = (pageParam != null && pageParam.equals("selections"));
    boolean isTests = (pageParam != null && pageParam.equals("tests"));
%>

<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav">
                <li id='Selections-li' <%=((isSelections) ? activeClass : "")%> ><a 
                        href="/ExpTestService/selections" >Selections<% 
                    if (isSelections) { 
                        %><span class="sr-only">(current)</span><%
                    }%></a></li>
                <li id='Tests-li' <%=((isTests) ? activeClass : "")%> ><a 
                        href="/ExpTestService/tests" >Tests<% 
                    if (isTests) { 
                        %><span class="sr-only">(current)</span><%
                    }%></a></li>
            </ul>
        <% if (isSelections) { %>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" role="button" data-toggle="dropdown" aria-haspopup="true" 
                       aria-expanded="false">New <span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <li id="add-new-sel-li" type="button" data-toggle="modal" data-target="#modal-div" data-type="add"><a href="#">Add</a></li>
                        <li id="modulate-sel-li" type="button" data-toggle="modal" data-target="#modal-div" data-type="modulate"><a href="#">Modulate</a></li>
                    </ul>
                </li>
            </ul>
        <% } else if (isTests) { %>
        <% } %>    
        </div>
        
    </div>
    
</nav>