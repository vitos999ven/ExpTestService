function loadSelection(name, testOnInit) {
    Ajax("GET", "selection", {sel_name: name}, null,
            function (json) {
                var result = JSON.parse(json);
                
                if (!result.succeeded) {
                    handleFailedAjaxResult(result.reason, getSelEventHandlers(name));
                    return;
                }
                
                document.currSelection = result.data;
                onLoadSelection(testOnInit);
            });
}
;

function onLoadSelection(testOnInit) {
    if (!document.currSelection) {
        console.error("No selection loaded");
        return;
    }
    
    document.currSelection.sorted = document.currSelection.values.slice();
    document.currSelection.sorted.sort(compareWithEpsilon);
    
    var procDiv = $("#processing-div");
    procDiv.empty();
    
    var selDiv = $("<div/>", {
            id: "selection-div",
            class: "hidden"
        }).appendTo(procDiv);
    
    setSelectionInfo(selDiv);
    plotValuesChart(selDiv);
    plotDensityChart(selDiv);
    initSelectionTestButton(selDiv, testOnInit);
    $("#selection-div").removeClass("hidden");
};

function setSelectionInfo(selDiv) {
    var infoDiv = $("#selection-info-div");
    
    if (infoDiv.length === 0) {
        infoDiv = $("<div/>", {
            id: "selection-info-div",
            class: "col-md-12"
        }).appendTo(selDiv);
    }
    
    var nameH = $("#selection-name-h");
    
    if (nameH.length === 0) {
        nameH = $("<h4/>", {
            id: "selection-name-h",
            class: "col-md-4 text-left title"
        }).appendTo(infoDiv);
    }
    nameH.text(document.currSelection.name);
    
    var sizeH = $("#selection-size-h");
    
    if (sizeH.length === 0) {
        sizeH = $("<h5/>", {
            id: "selection-size-h",
            class: "col-md-4 text-right"
        }).appendTo(infoDiv);
    }
    sizeH.text("Size: ");
    
    var sizeValueDiv = $("#selection-size-value-div");
    
    if (sizeValueDiv.length === 0) {
        sizeValueDiv = $("<div/>", {
            id: "selection-size-value-div",
            class: "inline"
        }).appendTo(sizeH);
    }
    sizeValueDiv.text(document.currSelection.values.length);
    
    var appendDiv = $("#selection-append-div");
    
    if (appendDiv.length === 0) {
        appendDiv = $("<div/>", {
            id: "selection-append-div",
            class: "col-md-4 input-group"
        }).appendTo(infoDiv);
    }
    
    var newValueInput = $("#new-value-input");
    
    if (newValueInput.length === 0) {
        newValueInput = $("<input/>", {
            id: "new-value-input",
            type: "number",
            class: "form-control",
            min: "0.0",
            step: "any"
        }).appendTo(appendDiv);
    }
    
    var buttonSpan = $("#new-value-btn-span");
    
    if (buttonSpan.length === 0) {
        buttonSpan = $("<span/>", {
            id: "new-value-btn-span",
            class: "input-group-btn"
        }).appendTo(appendDiv);
    }
    
    var newValueButton =  $("#new-value-btn");
    
    if (newValueButton.length === 0) {
        newValueButton = $("<button/>", {
            id: "new-value-btn",
            type: "button",
            class: "btn btn-default",
            text: "Append"
        }).appendTo(buttonSpan);
    }
    
    newValueButton.click(function(){
        appendValue(newValueInput);
    });
        
}

function appendValue(newValueInput) {
    if (!newValueInput) {
        newValueInput = $("#new-value-input");
    }
    
    if (newValueInput.length === 0) {
        console.error("Can't append value: no new-value-input element");
        return;
    }
    
    var text = newValueInput.val();
    newValueInput.val("");
    if (text === "") {
        return;
    }
    Ajax("GET", "appendtoselection", {
        sel_name: document.currSelection.name,
        sel_values: JSON.stringify([text])
    }, null,
            function (json) {
                var result = JSON.parse(json);

                if (!result.succeeded) {
                    handleFailedAjaxResult(result.reason, getSelEventHandlers(document.currSelection.name));
                    return;
                }

                var size = result.data.size;
                var hash = result.data.hash;

                if (hash !== 1 + document.currSelection.hash) {
                    loadSelection(document.currSelection.name);
                } else {
                    document.currSelection.hash = hash;
                    document.currSelection.values.push(+text);
                    $("#sel-" + document.currSelection.name + " .sel-size").text(size);
                    onLoadSelection();
                }
            });
};

function plotValuesChart(selDiv) {
    var selPlotDiv = $("#selection-plot-div");
    
    if (selPlotDiv.length === 0) {
        selPlotDiv = $("<div/>", {
            id: "selection-plot-div",
            class: "col-md-12"
        }).appendTo(selDiv);
    }
    
    selPlotDiv.highcharts({
        title: {
            text: 'Selection values chart',
            x: -20 //center
        },
        xAxis: {
            title: {
                text: 'Index'
            },
            plotLines: [{
                value: 0,
                heiht: 1,
                color: '#808080'
            }]
        },
        yAxis: {
            title: {
                text: 'Value'
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        tooltip: {
            pointFormat: '<b>{point.y}</b>'
        },
        series: [{
            name: document.currSelection.name,
            data: document.currSelection.values
        }, {
            name: document.currSelection.name + " sorted",
            data: document.currSelection.sorted
        }]
    });
}

function plotDensityChart(selDiv) {
    var sorted = document.currSelection.sorted;
    var densityArray = getDensityArray(sorted, 10, 1);
    
    var densityPlotDiv = $("#selection-density-plot-div");
    
    if (densityPlotDiv.length === 0) {
        densityPlotDiv = $("<div/>", {
            id: "selection-density-plot-div",
            class: "col-md-12"
        }).appendTo(selDiv);
    }
    
    densityPlotDiv.highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: 'Density chart for selection'
        },
        xAxis: {
            type: 'category',
            labels: {
                rotation: -45,
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Count'
            }
        },
        legend: {
            enabled: false
        },
        tooltip: {
            pointFormat: '<b>{point.y} values</b>'
        },
        series: [{
            name: 'Density',
            data: densityArray,
            dataLabels: {
                enabled: true,
                rotation: 0,
                color: '#090909',
                align: 'right',
                format: '{point.y}', // one decimal
                x: -13, // 10 pixels down from the top
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        }]
    });
};

function initSelectionTestButton(selDiv, testOnInit) {
    var selTestDiv = $("#selection-test-div");
    
    document.currSelection.testResults = {};
    
    if (selTestDiv.length === 0) {
        selTestDiv = $("<div/>", {
            id: "selection-test-div",
            class: "col-md-12"
        }).appendTo(selDiv);
    };
    
    var selTestButton = $("#selection-test-button");
    
    if (selTestButton.length === 0) {
        selTestButton = $("<button/>", {
            id: "selection-test-button",
            class: "btn btn-default col-md-12",
            type: "button",
            text: "Запустить проверку статистиками"
        }).appendTo(selTestDiv);
    };
    var lastCheckId = 0;
    var checkByTests = function(checkId, testTypes, iterCount) {
        if (checkId !== lastCheckId) return;
        if (!iterCount) iterCount = 1;
        if (iterCount > 20) return;
        
        var hasNotReadyYet = false;
        
        Ajax("GET", "checkselection", {
                sel_name: document.currSelection.name, 
                sign_level: 50,
                test_types: (!!testTypes) ? testTypes : "" 
            }, null,
            function (json) {
                var result = JSON.parse(json);
                
                if (!result.succeeded) {
                    handleFailedAjaxResult(result.reason, getSelEventHandlers(document.currSelection.name));
                    return;
                }
                
                if (document.currSelection.name !== result.data.selection.name ||
                        document.currSelection.hash !== result.data.selection.hash) {
                    console.error("Test result of wrong selection received! reload");
                    loadSelection(document.currSelection.name, true);
                    return;
                }
                
                var testResults = result.data.test_results;
                var failedTests = [];
                
                var testResultsTable = $("#test-results-table");
                
                if (testResultsTable.length === 0) {
                    testResultsTable = $("<table/>", {
                        id: "test-results-table",
                        class: "table text-center col-md-12"
                    }).appendTo(selTestDiv);
                };
                
                var testResultsTableHead = testResultsTable.children("thead");
                
                if (testResultsTableHead.length === 0) {
                    testResultsTableHead = $("<thead/>").appendTo(testResultsTable);
                };
                
                var headTr = testResultsTableHead.children("tr");
                
                if (headTr.length === 0) {
                    headTr = $("<tr/>").appendTo(testResultsTableHead);
                };
                
                var testResultsTableBody = testResultsTable.children("tbody");
                
                if (testResultsTableBody.length === 0) {
                    testResultsTableBody = $("<tbody/>").appendTo(testResultsTable);
                };
                
                var bodyTr = testResultsTableBody.children("tr");
                
                if (bodyTr.length === 0) {
                    bodyTr = $("<tr/>").appendTo(testResultsTableBody);
                };
                
                for (var testType in testResults){
                    var testResult = testResults[testType];
                    var testTypeObj = TestTypes.getType(testType);
                    
                    if(!testTypeObj) {
                        console.warn("No test type obj for type " + testType);
                        continue;
                    }
                    
                    var typeTh = headTr.children("th#th-test-type-" + testType);
                    if (typeTh.length === 0) {
                        typeTh = $("<th/>", {
                            id: "th-test-type-" + testType,
                            class: "text-center title",
                            text: testTypeObj.name
                        }).appendTo(headTr);
                    }
                    
                    var typeTd = bodyTr.children("td#td-test-type-" + testType);
                    if (typeTd.length === 0) {
                        typeTd = $("<td/>", {
                            id: "td-test-type-" + testType
                        }).appendTo(bodyTr);
                    }
                    
                    if (!testResult.succeeded) {
                        failedTests.push(testType);
                        
                        if(testResult.reason.type === "data_not_ready_yet") {
                            hasNotReadyYet = true;
                        }
                    }else{
                        document.currSelection.testResults[testType] = testResult.data.result;
                    }
                    
                    createTestResultTable(testResult, testTypeObj, typeTd);
                }
                
                var tds = bodyTr.children("td");
                if (tds.length !== 0) {
                    tds.css("width", "" + ((100 / tds.length) | 0) + "%");
                }
                
                if (failedTests.length === 0) {
                    appendAlternative(selTestDiv, testResults);
                    return;
                }
                
                if (!hasNotReadyYet) iterCount++;
                
                setTimeout(function(){checkByTests(checkId, failedTests, iterCount);}, 2000);
            }
        );
    };
    selTestButton.click(function(){lastCheckId++; checkByTests(lastCheckId);});
    
    if (testOnInit === true) {
        checkByTests(lastCheckId);
    }
};

function appendAlternative(selTestDiv, testResults) {
    var weibullResultsDiv = $("<div/>", {
        id: "alternatives-div"
    }).appendTo(selTestDiv);
    var label = $("<label/>", {text: "Выбрать альтернативное распределениe:"}).appendTo(weibullResultsDiv);
    var select = $("<select/>", {
        id:"alt-select", 
        class: "form-control",
        style: "margin-bottom: 10px;"
    }).appendTo(weibullResultsDiv); 
    
    for (var typeName in SelectionTypes) {
        var selType = SelectionTypes[typeName];
        if (!selType.type || selType.type === "exp") {
            continue;
        }
        $("<option/>", {
            value: selType.type, 
            text: selType.name
        }).appendTo(select);
    }
    
    function loadPowerAgainstAlternative(selType){
        
        var types =[];
        var i = 0;
        for (var testType in testResults){
            types[i] = testType;
            i++;
            
        }
         
        var powerDiv = weibullResultsDiv.children("#power-div");
        if (powerDiv.length === 0) {
            powerDiv = $("<div/>", {
                    id: "power-div"
                }).appendTo(weibullResultsDiv);
        }
        powerDiv.empty();  
       
       var datas ={};
       function plotData(){
            var powerPlotDiv = $("#power-plot-div");
    
            if (powerPlotDiv.length === 0) {
            powerPlotDiv = $("<div/>", {
                    id: "power-plot-div",
                    class: "col-md-12"
                }).appendTo(powerDiv);
            }
            
            function plotSignLevel(levelStr){
                var signLevelStr = +levelStr / 1000;
                
                var series = [];
                for (var data in datas) {
                    var testObj = TestTypes.getType(data);
                    
                    var levels = datas[data];
                    var values = levels[levelStr];
                    series.push({
                        name: testObj.name,
                        data: values
                    });
                }
                var signLevelDiv = $("<div/>", {
                        id: "div-" + signLevelStr
                    }).appendTo(powerPlotDiv);
                signLevelDiv.highcharts({
                    chart: {
                        type: "spline"
                    },
                    title: {
                        text: "Графики мощностей статистик против Weibull для n=" + document.currSelection.values.length + " и уровня значимости " + signLevelStr,
                        x: -20 //center
                    },
                    xAxis: {
                        title: {
                            text: 'Параметр формы'
                        },
                        plotLines: [{
                            value: 0,
                            heiht: 1,
                            color: '#808080'
                        }]
                    },
                    yAxis: {
                        title: {
                            text: 'Мощность'
                        },
                        plotLines: [{
                            value: 0,
                            width: 1,
                            color: '#808080'
                        }],
                        max: 1,
                        min: 0
                    },
                    legend: {
                        layout: 'vertical',
                        align: 'right',
                        verticalAlign: 'middle',
                        borderWidth: 0
                    },
                    plotOptions: {
                        series: {
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    tooltip: {
                        shared: true
                    },
                    series: series
                });
           }
           
           plotSignLevel("10");
           plotSignLevel("50");
           plotSignLevel("100");
       }
       
       function loadPower(k){
           Ajax("GET", "modulateweibullpowers", {
                test_type: types[k],
                iter_count: 10000,
                sel_size: document.currSelection.values.length,
                if_not_exists: true
                }, null,
                function (json) {
                    var result = JSON.parse(json);
                
                    if (!result.succeeded) {
                        handleFailedAjaxResult(result.reason);
                    }
                    else{
                        datas[types[k]] = result.data;
                    }
                    if(k === i){
                        plotData();
                    }
                    else{
                        loadPower(k + 1);
                    }
                });
       }
       
       loadPower(0);
    }
    
    function createAOETable(selType){
        console.log(selType);
        var aoeDiv = weibullResultsDiv.children("#aoe-div");
        if (aoeDiv.length === 0) {
            aoeDiv = $("<div/>", {
                    id: "aoe-div"
                }).appendTo(weibullResultsDiv);
        }
        aoeDiv.empty();
        if(selType !== "weibull" && selType !== "gamma"){
            return;
        }
        $("<label/>", {
                        text: "Значения асимптотически оптимальной эффективности для Weibull",
                        style: "margin-top: 20px;"
                    }).appendTo(selTestDiv);
                    
        var aoeTable = $("<table/>", {
                        id: "aoe-table",
                        class: "table text-center col-md-12"
                    }).appendTo(selTestDiv);
                
        var aoeTableHead = $("<thead/>").appendTo(aoeTable);
                
        var headTr = $("<tr/>").appendTo(aoeTableHead);
                
        var aoeTableBody = $("<tbody/>").appendTo(aoeTable);
                
        var bodyTr = $("<tr/>").appendTo(aoeTableBody);
                
        for (var testType in testResults){
            var testTypeObj = TestTypes.getType(testType);
                    
            if(!testTypeObj) {
                console.warn("No test type obj for type " + testType);
                continue;
            }
            var val = (selType === "weibull") ? 
                    ((!!testTypeObj.aoewe) ? testTypeObj.aoewe : "-") :  
                    ((!!testTypeObj.aoega) ? testTypeObj.aoega : "-");
            var typeTh  = $("<th/>", {
                            class: "text-center title",
                            text: testTypeObj.name
                        }).appendTo(headTr);
                    
            var typeTd = $("<td/>", {
                            class: "text-center",
                            text: val
                        }).appendTo(bodyTr);
        }
        
    }
    
    select.change(function(){
        var type = SelectionTypes[select.val()];
        loadPowerAgainstAlternative("weibull");
        createAOETable("weibull");
    });
    
    loadPowerAgainstAlternative("weibull");
    createAOETable("weibull");
}

function createTestResultTable(testResult, testTypeObj, parentTd) {
    if (!testResult || !parentTd) return;
    parentTd.removeClass();
    parentTd.empty();
    
    var resultTable = $("<table/>", {
        class: "col-md-12"
    }).appendTo(parentTd);
    
    var resultTableBody = $("<tbody/>").appendTo(resultTable);
    
    var resultTr = $("<tr/>").appendTo(resultTableBody);
    $("<td/>",{ text: "Result", class: "bold"}).appendTo(resultTr);
    var resultTd = $("<td/>").appendTo(resultTr);
    
    if (!testResult.succeeded) {
        var reason = testResult.reason;
        var reasonTr = $("<tr/>").appendTo(resultTableBody);
        var reasonTdTitle = $("<td/>", {class: "bold"}).appendTo(reasonTr);
        var reasonTdDesc = $("<td/>").appendTo(reasonTr);
        if (reason.type === "data_not_ready_yet") {
            parentTd.addClass("info");
            resultTd.text("No quantiles. Modulation...");
            reasonTdTitle.text("Status");
            reasonTdDesc.text(getPercentageString(reason.status));
        } else {
            parentTd.addClass("danger");
            resultTd.text("Failed");
            reasonTdTitle.text("Reason");
            var reasonText;
            switch (reason.type) {
                case "already_exists":
                    reasonText = "Data already exists";
                    break;
                case "db_problems":
                    reasonText = "Problems with database";
                    break;
                case "internal_error":
                    var message = reason.message;

                    reasonText = "Internal error";
                    
                    if (!message) {
                        console.error("No message field in 'internal_error' reason");
                        break;
                    }

                    var messageTr = $("<tr/>").appendTo(resultTableBody);
                    $("<td/>", {text: "Message", class: "bold"}).appendTo(messageTr);
                    $("<td/>", {text: message}).appendTo(messageTr);
                    break;
                case "invalid_param":
                    var param = reason.parameter;

                    reasonText = "Invalid request parameter";
                    
                    if (!param) {
                        console.error("No parameter field in 'invalid_param' reason");
                        break;
                    }

                    var paramTr = $("<tr/>").appendTo(resultTableBody);
                    $("<td/>", {text: "Param", class: "bold"}).appendTo(paramTr);
                    $("<td/>", {text: param}).appendTo(paramTr);
                    break;
                case "no_data":
                    reasonText = "No data";
                    break;
                case "no_dependent_data":
                    var name = reason.name;

                    reasonText = "No dependent data";
                    
                    if (!name) {
                        console.error("No name field in 'no_dependent_data' reason");
                        break;
                    }

                    var nameTr = $("<tr/>").appendTo(resultTableBody);
                    $("<td/>", {text: "Name", class: "bold"}).appendTo(nameTr);
                    $("<td/>", {text: name}).appendTo(nameTr);
                    break;
                case "wrong_db_data":
                    var name = reason.name;

                    reasonText = "Wrong database data";
                    
                    if (!name) {
                        console.error("No name field in 'wrong_db_data' reason");
                        break;
                    }

                    var nameTr = $("<tr/>").appendTo(resultTableBody);
                    $("<td/>", {text: "Name", class: "bold"}).appendTo(nameTr);
                    $("<td/>", {text: name}).appendTo(nameTr);
                    break;
                default:
                    console.error("Unknown reason type!");
                    reasonText = "Unknown";
                    break;
            }
            
            reasonTdDesc.text(reasonText);
        }
    } else {
        var data = testResult.data;
        var valueTr = $("<tr/>").appendTo(resultTableBody);
        $("<td/>", {text: "Значение", class: "bold"}).appendTo(valueTr);
        $("<td/>", {text: data.test_value}).appendTo(valueTr);
        
        if(testTypeObj.onesided){
            var secondTr = $("<tr/>").appendTo(resultTableBody);
            $("<td/>", {text: "0.95", class: "bold", rowspan:2}).appendTo(secondTr);
            $("<td/>", {text: data.second_value, rowspan:2}).appendTo(secondTr);
        }else{
            var firstTr = $("<tr/>").appendTo(resultTableBody);
            $("<td/>", {text: "0.05", class: "bold"}).appendTo(firstTr);
            $("<td/>", {text: data.first_value}).appendTo(firstTr);
            var secondTr = $("<tr/>").appendTo(resultTableBody);
            $("<td/>", {text: "0.95", class: "bold"}).appendTo(secondTr);
            $("<td/>", {text: data.second_value}).appendTo(secondTr);
        }
    
        if(!data.result){
            parentTd.addClass("warning");
            resultTd.text("False");
        }else{
            parentTd.addClass("success");
            resultTd.text("True");
        }
    }
}

function removeSelection(name) {
    Ajax("GET", "removeselection", {sel_name: name}, null,
            function (json) {
                var result = JSON.parse(json);
                
                if (!result.succeeded) {
                    handleFailedAjaxResult(result.reason, getSelEventHandlers(name));
                    return;
                }
                
                var selCountDiv = $("#selections-count-div");
                selCountDiv.text(+selCountDiv.text() - 1);
                $("#sel-" + name).remove();
                if (document.currSelection != null && document.currSelection.name === name) {
                    $("#processing-div").empty();
                    document.currSelection = null;
                }
            });
};

var selDivEventHandlers = {
    onClick : {
        selectionRow: function () {
            var name = this.id.substring(4);
            loadSelection(name);
        },
        selRemove: function () {
            var event = event || window.event;
            if (event)
                if (event.stopPropagation) {
                    event.stopPropagation();
                } else
                    event.canselBuddle = true;
            var name = this.id.substring(11);
            removeSelection(name);
        }
    }
};

function initSelectionsDiv() {
    $(".selection-row").click(selDivEventHandlers.onClick.selectionRow);
    $(".sel-remove").click(selDivEventHandlers.onClick.selRemove);

};

function createSelectionTr(result) {
    var selectionstableBody = $("#selections-table tbody");

    var selTr = $("<tr/>", {
        id: "sel-" + result.data.name,
        class: "selection-row"
    }).appendTo(selectionstableBody);

    selTr.click(selDivEventHandlers.onClick.selectionRow);

    $("<td/>", {
        text: result.data.name
    }).appendTo(selTr);

    $("<td/>", {
        class: "sel-size col-md-3",
        text: result.data.size
    }).appendTo(selTr);

    var removeTd = $("<td/>", {
        class: "col-md-1",
        style: "text-align:right",
    }).appendTo(selTr);

    var removeButton = $("<button/>", {
        id: "remove-sel-" + result.data.name,
        type: "close",
        class: "close sel-remove",
        "aria-label": "Close"
    }).appendTo(removeTd);

    $("<span/>", {
        "aria-hidden": "true",
        html: "&times;"
    }).appendTo(removeButton);

    removeButton.click(selDivEventHandlers.onClick.selRemove);
}

function initModalDiv() {
    var modalDiv = $("#modal-div");
    var modalLabel = modalDiv.find("#modal-div-label");
    var modalBody = modalDiv.find(".modal-body");
    var modalFooter = modalDiv.find(".modal-footer");
    
    function createAlertDiv(type, message) {
        modalFooter.find("div.alert").remove();
        modalFooter.prepend($("<div/>", {
            class: "alert alert-" + type,
            role: "alert",
            text: message
        }));
    }
    ;
                                
    var modalTypes = {
        add: {
            label: "Добавить существующие данные",
            create: function() {
                var typeObj = modalTypes.add;
                var addForm = $("<form/>").appendTo(modalBody);
                
                var noName = true;
                var nameForm = $("<div/>",{
                    class: "form-group"
                }).appendTo(addForm);
                $("<label/>",{
                    for: "add-new-sel-name-input",
                    class: "control-label",
                    text: "Идентификатор: "
                }).appendTo(nameForm);
                var nameInput = $("<input/>",{
                    id: "add-new-sel-name-input",
                    type: "text",
                    class: "form-control"
                }).appendTo(nameForm).on("keyup", function() {
                    noName = (!nameInput.val());
                    if (noName) {
                        nameInput.css("border-color", "red");
                        nameInput.tooltip({
                            placement: "bottom",
                            title: "Введите идентификатор"
                        });
                    }else{
                        nameInput.css("border-color", "");
                        nameInput.tooltip("destroy");
                    }
                    checkState();
                });
                
                var valuesForm = $("<div/>",{
                    class: "form-group"
                }).appendTo(addForm);
                $("<label/>",{
                    class: "control-label",
                    text: "Значения: "
                }).appendTo(valuesForm);
                
                var fileLabel = $("<label/>",{
                    class: "btn btn-default btn-file",
                    style: "margin-top: 5px; margin-bottom:5px; display: block;",
                    text: "Загрузить из файла"
                }).appendTo(valuesForm);
                $("<input/>",{
                    type: "file",
                    style: "display: none"
                }).appendTo(valuesForm);
                var valuesTextarea = $("<textarea/>",{
                    id: "add-new-sel-values-textarea",
                    class: "form-control"
                }).appendTo(valuesForm);
                
                
                var addButton = $("<button/>",{
                    type: "button",
                    class: "btn btn-primary",
                    text: "Добалить",
                    "data-loading-text": "Добавление...",
                    autocomplete: "off"
                }).appendTo(modalFooter).prop("disabled", true).click(function() {
                    addButton.button("loading");
                    typeObj.addSelection(nameInput, valuesTextarea, addButton);
                });
                
                
                
                function checkState() {
                    var disable = (noName);
                    addButton.prop("disabled", disable);
                }
            },
            addSelection: function(nameInput, valuesTextarea, button) {
                if (nameInput.length === 0) {
                    console.error("Can't create new selection: no name input");
                    button.button('reset');
                    return;
                }

                if (valuesTextarea.length === 0) {
                    console.error("Can't create new selection: no values textarea");
                    button.button('reset');
                    return;
                }

                var name = nameInput.val();
                var values = valuesTextarea.val();
                if (name === "") {
                    console.error("Can't create new selection: no name");
                    button.button('reset');
                    return;
                }
                Ajax("GET", "addselection", {
                    sel_name: name,
                    sel_values: "[" + values + "]"
                }, null,
                        function (json) {
                            var result = JSON.parse(json);

                            if (!result.succeeded) {
                                var common = handleFailedAjaxResult(result.reason, {
                                        alreadyExists: function() {
                                            createAlertDiv("danger", "Selection with name '" + name + "' already exists!");
                                        },
                                        invalidParam: function(param) {
                                            if (param === "sel_name") {
                                                createAlertDiv("danger", "Invalid selection name!");
                                            }else if (param === "sel_values") {
                                                createAlertDiv("danger", "Invalid selection values!");
                                            }else{
                                                console.error("Unknown parameter!");
                                                createAlertDiv("danger", "Invalid parameter!");
                                            }
                                        }
                                });
                                
                                if (!!common) {
                                    createAlertDiv("danger", common);
                                }  
                                
                                button.button('reset');
                                return;
                            }

                            var selCountDiv = $("#selections-count-div");
                            selCountDiv.text(+selCountDiv.text() + 1);
                            createSelectionTr(result);
                            
                            modalDiv.modal("hide");
                            
                            loadSelection(result.data.name);
                        });
            }
        }, 
        modulate: {
            label: "Смоделировать новую повторную выборку",
            create: function() {
                var typeObj = modalTypes.modulate;
                var modulateForm = $("<form/>").appendTo(modalBody);
                
                var noName = true;
                var wrongSize = false;
                var wrongTypeParams = false;
                
                var nameForm = $("<div/>",{
                    class: "form-group"
                }).appendTo(modulateForm);
                $("<label/>",{
                    for: "modulate-new-sel-name-input",
                    class: "control-label",
                    text: "Идентификатор: "
                }).appendTo(nameForm);
                var nameInput = $("<input/>",{
                    id: "modulate-new-sel-name-input",
                    type: "text",
                    class: "form-control"
                }).appendTo(nameForm).on("keyup", function() {
                    noName = (!nameInput.val());
                    if (noName) {
                        nameInput.css("border-color", "red");
                        nameInput.tooltip({
                            placement: "bottom",
                            title: "Введите идентификатор выборки"
                        });
                    }else{
                        nameInput.css("border-color", "");
                        nameInput.tooltip("destroy");
                    }
                    checkState();
                });
                
                var sizeForm = $("<div/>",{
                    class: "form-group"
                }).appendTo(modulateForm);
                $("<label/>",{
                    for: "modulate-new-sel-size-input",
                    class: "control-label",
                    text: "Объем: "
                }).appendTo(sizeForm);
                var sizeInput = $("<input/>",{
                    id: "modulate-new-sel-size-input",
                    type: "number",
                    min: 10,
                    max: 1000,
                    step: 1,
                    value: 10,
                    class: "form-control"
                }).appendTo(sizeForm).on("keyup", function() {
                    var size = +sizeInput.val();
                    
                    wrongSize = (size < 10 || size > 1000);
                    
                    if (wrongSize) {
                        sizeInput.css("border-color", "red");
                        sizeInput.tooltip({
                            placement: "bottom",
                            title: "Неправильное значение объема"
                        });
                    }else{
                        sizeInput.css("border-color", "");
                        sizeInput.tooltip('destroy');
                    }
                    checkState();
                });
                
                var currType;
                
                var selTypesBtnGrp = $("<div/>",{
                    class: "btn-group btn-group-justified",
                    "data-toggle": "buttons"
                }).appendTo(modulateForm);
                
                var first = true;
                var currentType;
                
                var min;
                var max;
                var highchart;
                
                for (var typeName in SelectionTypes) {
                    var selType = SelectionTypes[typeName];
                    if (!selType.type) {
                        continue;
                    }
                    
                    var typeLabel = $("<label/>", {
                        class: "btn btn-primary",
                        text: selType.name,
                        "sel-type" : selType.type
                    }).appendTo(selTypesBtnGrp).click(function() {
                        var type = SelectionTypes.getType($(this).attr("sel-type"));
                        if (!type){
                            return;
                        }
                        
                        currentType = type;
                        min = (currentType.type === "normal") ? -5 : 0;
                        max = 5;
                        createTypeDiv();
                    });
                    
                    var typeInput = $("<input />",{
                        type: "radio",
                        name: "options",
                        id: "modulate-type-radio-" + selType.type,
                        autocomplete: "off"
                    }).appendTo(typeLabel);
                    
                    if (first) {
                        currentType = selType;
                        typeLabel.addClass("active");
                    }
                    
                    first = false;
                }
                
                var modulateButton = $("<button/>",{
                    type: "button",
                    class: "btn btn-primary",
                    text: "Смоделировать",
                    "data-loading-text": "Моделирование...",
                    autocomplete: "off"
                }).appendTo(modalFooter).prop("disabled", true).click(function() {
                    modulateButton.button("loading");
                    typeObj.modulateSelection(nameInput.val(), sizeInput.val(), currentType, modulateButton);
                });
                
                var selTypeDiv = $("<div/>", {
                    class: "col-md-12 hidden"
                }).appendTo(modulateForm);
                
                min = (currentType.type === "normal") ? -5 : 0;
                max = 5;
                var step = 0.05;
                
                function createTypeDiv() {
                    selTypeDiv.children(":not(#modulate-chart-container)").remove();
                    
                    var chart = selTypeDiv.find("#modulate-chart-container");
                    if (chart.length === 0) {
                        chart = $("<div/>", {
                            id: "modulate-chart-container"
                        }).appendTo(selTypeDiv);
                        chart.highcharts({
                            chart: {
                                type: "spline"
                            },
                            xAxis: {
                                plotLines: [{
                                        value: 0,
                                        height: 1,
                                        color: '#808080'
                                    }]
                            },
                            yAxis: {
                                plotLines: [{
                                        value: 0,
                                        width: 1,
                                        color: '#808080'
                                    }],
                                min: 0
                            },
                            legend: {
                                enabled: false
                            },
                            tooltip: {
                                enabled: false
                            },
                            plotOptions: {
                                series: {
                                    marker: {
                                        enabled: false
                                    }
                                }
                            }
                        });
                        modalDiv.on("shown.bs.modal", function (event) {
                            selTypeDiv.removeClass("hidden");
                            chart.highcharts().reflow();
                        });
                    }
                    
                    highchart = chart.highcharts();
                    
                    highchart.setTitle({
                        text: currentType.name + " плотность"
                    });
                    if (highchart.series.length === 0) {
                        highchart.addSeries({
                            name: currentType.name,
                            data: currentType.createSeries(min, max, step)
                        });
                    }else{
                        highchart.series[0].name = currentType.name;
                        highchart.series[0].setData(currentType.createSeries(min, max, step));
                        highchart.redraw();
                    }
                    
                    
                    switch (currentType.type) {
                        case "exp":
                            var ScaleParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-scale-param-input",
                                class: "control-label",
                                text: "Параметр масштаба: "
                            }).appendTo(ScaleParameterForm);
                            var ScaleParamInput = $("<input/>", {
                                id: "modulate-new-sel-scale-param-input",
                                type: "number",
                                min: 0,
                                max: 1000,
                                step: "any",
                                value: 1,
                                class: "form-control"
                            }).appendTo(ScaleParameterForm).on("keyup", function () {
                                var parameter = +ScaleParamInput.val();
                                
                                wrongTypeParams = (compareWithEpsilon(parameter, 0.1) < 0 || parameter > 1000);
                                if (wrongTypeParams) {
                                    ScaleParamInput.css("border-color", "red");
                                    ScaleParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    ScaleParamInput.css("border-color", "");
                                    ScaleParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, parameter));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            break
                        case "weibull":
                            var wrongScaleParam = false;
                            var wrongShapeParam = false;
                            var ScaleParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-scale-param-input",
                                class: "control-label",
                                text: "Параметр масштаба: "
                            }).appendTo(ScaleParameterForm);
                            var ShapeParamInput;
                            var ScaleParamInput = $("<input/>", {
                                id: "modulate-new-sel-scale-param-input",
                                type: "number",
                                min: 0,
                                max: 1000,
                                step: "any",
                                value: 1,
                                class: "form-control"
                            }).appendTo(ScaleParameterForm).on("keyup", function () {
                                var parameter = +ScaleParamInput.val();
                                
                                wrongScaleParam = (compareWithEpsilon(parameter, 0.1) < 0 || parameter > 1000);
                                wrongTypeParams = wrongScaleParam && wrongShapeParam;
                                if (wrongScaleParam) {
                                    ScaleParamInput.css("border-color", "red");
                                    ScaleParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Направильное значение"
                                    });
                                } else {
                                    ScaleParamInput.css("border-color", "");
                                    ScaleParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, parameter, ShapeParamInput.val()));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            var ShapeParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-shape-param-input",
                                class: "control-label",
                                text: "Параметр формы: "
                            }).appendTo(ShapeParameterForm);
                            ShapeParamInput = $("<input/>", {
                                id: "modulate-new-sel-shape-param-input",
                                type: "number",
                                min: 0,
                                max: 1000,
                                step: "any",
                                value: 1,
                                class: "form-control"
                            }).appendTo(ShapeParameterForm).on("keyup", function () {
                                var parameter = +ShapeParamInput.val();
                                
                                wrongShapeParam = (compareWithEpsilon(parameter, 0.1) < 0 || parameter > 1000);
                                wrongTypeParams = wrongScaleParam && wrongShapeParam;
                                if (wrongShapeParam) {
                                    ShapeParamInput.css("border-color", "red");
                                    ShapeParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    ShapeParamInput.css("border-color", "");
                                    ShapeParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, +ScaleParamInput.val(), parameter));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            break;
                        case "normal":
                            var wrongMParam = false;
                            var wrongDParam = false;
                            var MParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-m-param-input",
                                class: "control-label",
                                text: "Математическое ожидание: "
                            }).appendTo(MParameterForm);
                            var DParamInput;
                            var MParamInput = $("<input/>", {
                                id: "modulate-new-sel-m-param-input",
                                type: "number",
                                min: -1000,
                                max: 1000,
                                step: "any",
                                value: 1,
                                class: "form-control"
                            }).appendTo(MParameterForm).on("keyup", function () {
                                var parameter = +MParamInput.val();
                                
                                wrongMParam = (parameter < -1000 || parameter > 1000);
                                wrongTypeParams = wrongMParam && wrongDParam;
                                if (wrongMParam) {
                                    MParamInput.css("border-color", "red");
                                    MParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    MParamInput.css("border-color", "");
                                    MParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, parameter, DParamInput.val()));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            var DParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-d-param-input",
                                class: "control-label",
                                text: "Среднеквадретическое отклонение: "
                            }).appendTo(DParameterForm);
                            DParamInput = $("<input/>", {
                                id: "modulate-new-sel-d-param-input",
                                type: "number",
                                min: 0,
                                max: 1000,
                                step: "any",
                                value: 1,
                                class: "form-control"
                            }).appendTo(DParameterForm).on("keyup", function () {
                                var parameter = +DParamInput.val();
                                
                                wrongDParam = (compareWithEpsilon(parameter, 0.1) < 0 || parameter > 1000);
                                wrongTypeParams = wrongMParam && wrongDParam;
                                if (wrongShapeParam) {
                                    DParamInput.css("border-color", "red");
                                    DParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    DParamInput.css("border-color", "");
                                    DParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, +MParamInput.val(), parameter));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            break;
                        case "chi-scuared":
                            var KParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-k-param-input",
                                class: "control-label",
                                text: "Количество степеней свободы: "
                            }).appendTo(KParameterForm);
                            var KParamInput = $("<input/>", {
                                id: "modulate-new-sel-k-param-input",
                                type: "number",
                                min: 1,
                                max: 1000,
                                step: 1,
                                value: 1,
                                class: "form-control"
                            }).appendTo(KParameterForm).on("keyup", function () {
                                var parameter = +KParamInput.val();
                                
                                wrongTypeParams = (parameter < 1 || parameter > 1000);
                                if (wrongTypeParams) {
                                    KParamInput.css("border-color", "red");
                                    KParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    KParamInput.css("border-color", "");
                                    KParamInput.tooltip('destroy');
                                    max = 3.5 + parameter + Math.pow(parameter, 1.5) / 2;
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, parameter));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            break;
                        case "fisher":
                            var wrongK1Param = false;
                            var wrongK2Param = false;
                            var K1ParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-k1-param-input",
                                class: "control-label",
                                text: "Количество степеней свободы числителя: "
                            }).appendTo(K1ParameterForm);
                            var K2ParamInput;
                            var K1ParamInput = $("<input/>", {
                                id: "modulate-new-sel-k1-param-input",
                                type: "number",
                                min: 1,
                                max: 1000,
                                step: 1,
                                value: 1,
                                class: "form-control"
                            }).appendTo(K1ParameterForm).on("keyup", function () {
                                var parameter = +K1ParamInput.val();
                                
                                wrongK1Param = (parameter < 1 || parameter > 1000);
                                wrongTypeParams = wrongK1Param && wrongK2Param;
                                if (wrongK1Param) {
                                    K1ParamInput.css("border-color", "red");
                                    K1ParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    K1ParamInput.css("border-color", "");
                                    K1ParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, parameter, +K2ParamInput.val()));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            
                            var K2ParameterForm = $("<div/>", {
                                class: "form-group"
                            }).appendTo(selTypeDiv);
                            $("<label/>", {
                                for : "modulate-new-sel-k2-param-input",
                                class: "control-label",
                                text: "Количество степеней свободы знаменателя: "
                            }).appendTo(K2ParameterForm);
                            K2ParamInput = $("<input/>", {
                                id: "modulate-new-sel-k2-param-input",
                                type: "number",
                                min: 1,
                                max: 1000,
                                step: 1,
                                value: 1,
                                class: "form-control"
                            }).appendTo(K2ParameterForm).on("keyup", function () {
                                var parameter = +K2ParamInput.val();
                                
                                wrongK2Param = (parameter < 1 || parameter > 1000);
                                wrongTypeParams = wrongK1Param && wrongK2Param;
                                if (wrongK2Param) {
                                    K2ParamInput.css("border-color", "red");
                                    K2ParamInput.tooltip({
                                        placement: "bottom",
                                        title: "Неправильное значение"
                                    });
                                } else {
                                    K2ParamInput.css("border-color", "");
                                    K2ParamInput.tooltip('destroy');
                                    highchart.series[0].setData(currentType.createSeries(min, max, step, +K1ParamInput.val(), parameter));
                                    highchart.redraw();
                                }
                                checkState();
                            }); 
                            break;
                    }
                };
                
                createTypeDiv();
                
                function checkState() {
                    var disable = (noName || wrongSize || wrongTypeParams);
                    modulateButton.prop("disabled", disable);
                }
            },
            modulateSelection: function(name, size, type, button) {
                if (!name) {
                    console.error("Can't modulate new selection: no name");
                    button.button('reset');
                    return;
                }

                if (!size) {
                    console.error("Can't modulate new selection: no size");
                    button.button('reset');
                    return;
                }
                
                if (!type) {
                    console.error("Can't modulate new selection: no type");
                    button.button('reset');
                    return;
                }
                var requestParams = {
                    sel_name: name,
                    sel_size: size,
                    sel_type: type.type       
                };
                
                switch(type.type) {
                    case "exp":
                        var scaleParamInput = $("#modulate-new-sel-scale-param-input");
                        if (scaleParamInput.length !== 0) {
                            requestParams.scale_param = paramInput.val();
                        }
                    case "weibull":
                        var paramInput = $("#modulate-new-sel-shape-param-input");
                        if (paramInput.length !== 0) {
                            requestParams.weibull_param = paramInput.val();
                        }
                        break;
                }

                Ajax("GET", "modulateselection", requestParams, null,
                        function (json) {
                            var result = JSON.parse(json);

                            if (!result.succeeded) {
                                var common = handleFailedAjaxResult(result.reason, {
                                        alreadyExists: function() {
                                            createAlertDiv("danger", "Selection with name '" + name + "' already exists!");
                                        },
                                        invalidParam: function(param) {
                                            if (param === "sel_name") {
                                                createAlertDiv("danger", "Invalid selection name!");
                                            }else if (param === "sel_size") {
                                                createAlertDiv("danger", "Invalid selection size!");
                                            }else if (param === "sel_type") {
                                                createAlertDiv("danger", "Invalid selection type!");
                                            }else if (param === "weibull_param") {
                                                createAlertDiv("danger", "Invalid weibull parameter!");
                                            }else{
                                                console.error("Unknown parameter!");
                                                createAlertDiv("danger", "Invalid parameter!");
                                            }
                                        }
                                });
                                
                                if (!!common) {
                                    createAlertDiv("danger", common);
                                }  
                                
                                button.button('reset');
                                return;
                            }

                            var selCountDiv = $("#selections-count-div");
                            selCountDiv.text(+selCountDiv.text() + 1);
                            createSelectionTr(result);
                            
                            modalDiv.modal("hide");
                            
                            loadSelection(result.data.name);
                        });
            }
        }
    };
    
    modalDiv.on("show.bs.modal", function(event) {
        var button = $(event.relatedTarget);
        var type = button.data("type");
        
        modalLabel.text(modalTypes[type].label);
        modalBody.empty();
        modalFooter.empty();
        modalTypes[type].create();
    });
    
    modalDiv.on("hidden.bs.modal", function() {
        modalLabel.empty();
        modalBody.empty();
        modalFooter.empty();
    });
}

function init() {
    var body = $("body");
    var procDiv = $("#processing-div");
    if (procDiv.length === 0) {
        procDiv = $("<div/>", {
            id: "processing-div"
        }).appendTo(body);
    }
    initModalDiv();
    initSelectionsDiv();
}
;

jQuery(document).ready(function ($) {
    init();
});

function getSelEventHandlers(selName) {
    return {
        noData: function () {
            if (!!selName) {
                alert("No selection '" + selName + "'!");
            }

            location.reload(true);
        }
    };
};


