<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="entities.SelectionInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Selections</title>
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
        <script src="resources/my-scripts/selections-init.js"></script>
        <script src="resources/AjaxMethod.js"></script>

        <jsp:include page="/WEB-INF/jsp/navbar.jsp">
            <jsp:param name="pageParam" value="selections"/>
        </jsp:include>

        <%
            Set<SelectionInfo> selectionsInfo = (Set<SelectionInfo>) request.getAttribute("selectionsInfo");
        %>
        <div id="selections-div" class="col-md-12"> 
            <h2>Selections <small>Count: <div id="selections-count-div"><%=selectionsInfo.size()%></div></small></h2>
            <table id="selections-table" class="table <%=((selectionsInfo.isEmpty()) ? "hidden" : "")%>">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Size</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (SelectionInfo info : selectionsInfo) {
                    %>
                    <tr id="sel-<%=info.name%>" class="selection-row">
                        <td><%=info.name%></td>
                        <td class="sel-size col-md-3"><%=info.size%></td>
                        <td class="col-md-1" style="text-align:right"><button id="remove-sel-<%=info.name%>" type="buttun" class="close sel-remove" aria-label="Close"><span area-hidden="true">&times;</span></td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
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

