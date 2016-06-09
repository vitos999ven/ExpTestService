<%@page import="java.util.List"%>
<%@page import="hibernate.logic.TestType"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Статистики</title>
        <link rel="stylesheet" type="text/css" href="resources/my-styles.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap.css" media="all" />
        <link rel="stylesheet" type="text/css" href="resources/bootstrap-theme.css" media="all" />
    </head>

    <body>
        <script src="resources/jquery-2.1.1.min.js"></script>
        <script src="resources/bootstrap.js"></script>
        <script src="resources/highcharts/highcharts.js"></script>
        <script src="resources/highcharts/exporting.js"></script>
        <script src="resources/my-scripts/my-functions.js"></script>
        <script src="resources/my-scripts/tests-init.js"></script>
        <script src="resources/AjaxMethod.js"></script>

        <jsp:include page="/WEB-INF/jsp/navbar.jsp">
            <jsp:param name="pageParam" value="tests"/>
        </jsp:include>

        <%
            List<TestType> testTypes = (List<TestType>) request.getAttribute("testTypes");
        %>
        <div id="tests-div" class="col-md-12"> 
            <h2>Критерии для проверки гипотезы экспоненциальности</h2>
            <hr>
            <div id="tests-buttons" class="btn-group btn-group-justified <%=((testTypes.isEmpty()) ? "hidden" : "")%>" data-toggle="buttons">
                
                <%
                    for (TestType type : testTypes) {
                %>
                <label id="test-type-<%=type.getType()%>" class="btn btn-primary" test-type="<%=type.getType()%>">
                    <input type="radio" name="options" id="test-type-radio-<%=type.getType()%>" autocomplete="off"></input>
                    <h5><%=type.getName()%></h5>
                </label>
                <%
                    }
                %>
            </div>
        </div>
        <div id="modal-div" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal-div-label"> 
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h3 class="modal-title" id="modal-div-label"></h3>
                    </div>
                    <div class="modal-body">
                    </div>
                    <div class="modal-footer">
                        <button id="modal-event-butto" type="button" class="btn btn-primary"></button>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
