/*
 * INIT
 */

jQuery(document).ready(function ($) {
    init();
});

function init() {
    var body = $("body");
    var procDiv = $("#processing-div");
    if (procDiv.length === 0) {
        procDiv = $("<div/>", {
            id: "processing-div",
            class: "col-md-12"
        }).appendTo(body);
    }
    initTypesButtons();
}

function initTypesButtons() {
    var testsButtons = $("#tests-buttons>label");
    
    testsButtons.click(function () {
        var type = TestTypes.getType($(this).attr("test-type"));
        if (!type) {
            return;
        }

        loadIterationsCounts(type);
    });
}

/*
 *  ITERATIONS COUNTS
 */

function loadIterationsCounts(type) {
    Ajax("GET", "itercounts", {test_type: type.type}, null,
            function (json) {
                var result = JSON.parse(json);
                
                if (!result.succeeded) {
                    var message = handleFailedAjaxResult(result.reason);
                    console.error(message);
                    return;
                }
                
                document.currTestType = result.data;
                onLoadIterationsCounts();
            });
}

var quantilesCountBySignLevel = 20;

function onLoadIterationsCounts() {
    if (!document.currTestType){
        console.error("No test type loaded");
        return;
    }
    
    var typeObj = TestTypes.getType(document.currTestType.test_type);
    
    var procDiv = $("#processing-div");
    
    procDiv.empty();
    
    var testTypeTable = $("<table/>").appendTo(procDiv);
    var tr = $("<tr/>").appendTo($("<tbody/>").appendTo(testTypeTable));
    var nameEl = $("<td/>").appendTo(tr);
    var iterCountsEl =  $("<td/>").appendTo(tr);
    
    $("<h2/>", {
        text: typeObj.name
    }).appendTo(nameEl);
    
    var iterCountsButtomsBorder = $("<div/>", {
        class: "iter-counts-buttons"
    }).appendTo(iterCountsEl);
    
    var iterCountsBtnGrp = $("<div/>", {
        class: "btn-group",
        "data-toggle": "buttons"
    }).appendTo(iterCountsButtomsBorder);

    var defaultCount = document.currTestType.default_iter_count;
    var iterCounts = document.currTestType.iter_counts;
    for (var i = 0; i < iterCounts.length; ++i) {
        var iterCount = iterCounts[i];

        var typeLabel = $("<label/>", {
            class: "btn btn-sm btn-default",
            html: $("<div/>", {class: ((iterCount === defaultCount) ? "my-bold" : ""), text: iterCount}),
            "iter-count": iterCount
        }).appendTo(iterCountsBtnGrp).click(function () {
            var currIterCount = $(this).attr("iter-count");
            if (!currIterCount) {
                return;
            }
            
            document.currTestType.curr_count = currIterCount;
            getQuantilesByIterCount(10, quantilesCountBySignLevel);
        });
              
        if (iterCount === defaultCount) {
            typeLabel.tooltip({
                placement: "bottom",
                title: "Стандартное количество испытаний"
            });
        }
        

        $("<input />", {
            type: "radio",
            name: "options",
            autocomplete: "off"
        }).appendTo(typeLabel);
    }
      
}

/*
 *  QUANTILES
 */

var quantilesIter = 0;
function getQuantilesByIterCount(min, count) {
    var currIter = ++quantilesIter;
    if (!document.currTestType || !document.currTestType.curr_count) {
        return;
    }
    
    Ajax("GET", "getquantiles", {
            test_type: document.currTestType.test_type, 
            iter_count: document.currTestType.curr_count,
            min_sel_size: min, 
            quantiles_count: count
        }, null,
            function (json) {
                var result = JSON.parse(json);

                if (!result.succeeded) {
                    var message = handleFailedAjaxResult(result.reason);
                    console.error(message);
                    return;
                }

                createQuantilesTable(result.data, currIter);
            });
}

function createQuantilesTable(data, currIter) {
    var type = TestTypes.getType(document.currTestType.test_type);
    var procDiv = $("#processing-div");
    
    var quantilesTableDiv = procDiv.children("#quantiles-table-div");
    procDiv.children("#power-div").remove();
    if (quantilesTableDiv.length === 0) {
        quantilesTableDiv = $("<div/>", {
            id: "quantiles-table-div",
            class: "panel panel-default"
        }).appendTo(procDiv);
    }
    
    quantilesTableDiv.empty();
    
    $("<div/>", {
        class: "panel-heading lead",
        text: "Квантили"
    }).appendTo(quantilesTableDiv);
    
    var quantilesTable = $("<table/>", {
            id: "quantiles-table",
            class: "table table-bordered my-table text-center"
    }).appendTo(quantilesTableDiv);
    
    var tableHead = $("<thead/>").appendTo(quantilesTable);
    var firstHeadTr = $("<tr/>").appendTo(tableHead);
    var secondHeadTr = $("<tr/>").appendTo(tableHead);
    
    var headIndexTh = $("<th/>",{
            class: "my-index-row",
            rowspan: 2
        }).appendTo(firstHeadTr);
           
    var tableBody = quantilesTable.children("tbody");
    if (tableBody.length === 0) {
        tableBody = $("<tbody/>").appendTo(quantilesTable);
    }  
    
    var rows = {};
    var minSelSize = data.min_sel_size;
    var maxSelSize = data.min_sel_size + data.quantiles_count - 1;
    for (var selSize = minSelSize; selSize <= maxSelSize; selSize++) {
        var bodyTr = $("<tr/>", {
            id: "quantiles-table-tr-" + selSize,
            "sel-size": selSize
        }).appendTo(tableBody);
        $("<th/>",{
            class: "quantiles-table-index-th row my-index-row text-center clickable-cell",
            "sel-size": selSize,
            text: selSize
        }).appendTo(bodyTr).click(function() {
            var selSize = $(this).attr("sel-size");
            loadPower(selSize);
        });
        rows[selSize] = bodyTr;
    }
    
    var quantiles = data.quantiles;
    
    for (var signLevel in quantiles) {
        var signLevelValue = +signLevel / 1000;
        var signLevelQuantiles = quantiles[signLevel];
        
        $("<th/>",{
            id: "sign-level-th-" + signLevel,
            class: "sign-level-th lead text-center",
            "sign-level": signLevel,
            colspan: 4,
            text: signLevelValue
        }).appendTo(firstHeadTr);
        
        if(!type.onesided){
            $("<th/>",{
                class: "sign-level-side-th side-first small text-center",
                "sign-level": signLevel,
                colspan: 2,
                side: "first",
                text: "0.05"
            }).appendTo(secondHeadTr);
        }
        
        $("<th/>",{
            class: "sign-level-side-th side-second small text-center",
            "sign-level": signLevel,
            colspan: (type.onesided) ? 4 : 2,
            side: "second",
            text: "0.95"
        }).appendTo(secondHeadTr);
        
        for (var selSize = minSelSize; selSize <= maxSelSize; selSize++) {
            var quantile = signLevelQuantiles[selSize];
            var row = rows[selSize];
            if(!row) continue;
            
            var td = $("<td/>", {
                colspan: 4,
                "sign-level": signLevel,
                class: "clickable-cell"
            }).appendTo(row);
            
            var tdDiv = $("<div/>", {
                class: "quantile-td-div"
            }).appendTo(td);
            
            function initTd() {
                var Td = td;
                var TdDiv = tdDiv; 
                var Row = row;
                var size = selSize;
                var level = signLevel;
                var quant = quantile;
                 
                function setContent() {
                    
                    TdDiv.empty();
                    if (!quant) {
                        $("<span/>", {
                            class: "glyphicon glyphicon-minus"
                        }).appendTo(TdDiv);
                    } else if(type.onesided){
                        $("<div/>", {
                            text: quant.second
                        }).appendTo(TdDiv);
                    }else{
                        var qTable = $("<table/>", {
                            class: "col-md-12 text-center"
                        }).appendTo(TdDiv);
                        var qBody = $("<tbody/>").appendTo(qTable);
                        var qTr = $("<tr/>").appendTo(qBody);

                        $("<td/>", {
                            text: quant.first,
                            style: "width: 50%"
                        }).appendTo(qTr);

                        $("<td/>", {
                            text: quant.second,
                            style: "width: 50%"
                        }).appendTo(qTr);
                    }

                }
                
                function onClick() {
                    Td.unbind("click");
                    Td.removeClass("clickable-cell");
                    TdDiv.empty();
                    $("<span/>", {
                        class: "glyphicon glyphicon-hourglass"
                    }).appendTo(TdDiv);
                    modulate();
                }
                
                function modulate() {
                    Ajax("GET", "modulatequantile", {
                            test_type: data.test_type,
                            iter_count: data.iter_count,
                            sign_level: level,
                            sel_size: size,
                            if_not_exists: (!quant)
                        }, null,
                            function (json) {
                                var result = JSON.parse(json);

                                if (!result.succeeded) {
                                    var common = handleFailedAjaxResult(result.reason, {
                                        dataNotReadyYet: function(status) {
                                            status = ((status * 100) | 0);
                                            var progressDiv = TdDiv.children("div.progress");
                                            if (progressDiv.length === 0) {
                                                TdDiv.empty();
                                                progressDiv = $("<div/>", {
                                                    class: "progress",
                                                    style: "margin-bottom: 0px"
                                                }).appendTo(TdDiv);
                                            }
                                            
                                            var progressBar = progressDiv.children("div.progress-bar");
                                            if (progressBar.length === 0) {
                                                progressBar = $("<div/>", {
                                                    class: "progress-bar progress-bar-striped active",
                                                    role: "progressbar",
                                                    "aria-valuenow": status,
                                                    "aria-valuemin": "0",
                                                    "aria-valuemax": "100",
                                                    style: "min-width: 2em; width: " + status + "%",
                                                    text: "" + status + "%"
                                                }).appendTo(progressDiv);
                                            }else{
                                                progressBar.attr("aria-valuenow", status);
                                                progressBar.css("width", "" + status + "%");
                                                progressBar.text("" + status + "%");
                                            }
                                            
                                            setTimeout(function() {
                                                if (currIter !== quantilesIter) {
                                                    return;
                                                }
                                                modulate();
                                            }, 4000);
                                        }
                                    });
                                    
                                    if (!!common) {
                                        Td.addClass("clickable-cell");
                                        TdDiv.text(common).click(onClick);
                                    }
                                    return;
                                }

                                quant = result.data;
                                Td.addClass("clickable-cell");
                                Td.click(onClick);
                                setContent();
                            });
                }

                Td.click(onClick);
                setContent();
            };
            initTd();
        }
    }
    
    var divFooter = $("<div/>", {
        class: "panel-footer"
    }).appendTo(quantilesTableDiv);
    
    var navBtnGrp = $("<div/>", {
        class: "btn-group btn-group-sm",
        role: "group"
    }).appendTo(divFooter);
    
    var prevBtn = $("<button/>", {
        id: "btn-prev-quantiles",
        class: "btn btn-default",
        type: "button",
        text: "Prev"
    }).appendTo(navBtnGrp).click(function() {
        var minsize = minSelSize - data.quantiles_count;
        if (minsize <= 10) {
            minsize = 10;
        }
        getQuantilesByIterCount(minsize, data.quantiles_count);
    });
    if (minSelSize <= 10) {
        prevBtn.prop("disabled", true);
    }
    
    var nextBtn = $("<button/>", {
        id: "btn-next-quantiles",
        class: "btn btn-default",
        type: "button",
        text: "Next"
    }).appendTo(navBtnGrp).click(function() {
        var minsize = minSelSize + data.quantiles_count;
        if (minsize + data.quantiles_count - 1 >= 990) {
            minsize = 970;
        }
        getQuantilesByIterCount(minsize, data.quantiles_count);
    });
    if (maxSelSize >= 990) {
        nextBtn.prop("disabled", true);
    }
}

/*
 * POWERS
 */
      
var powerId = 0;
function loadPower(selSize, currId) {
    if (!selSize) return;
    if (!currId) currId = ++powerId;
            
    var procDiv = $("#processing-div");
    Ajax("GET", "modulateweibullpowers", {
        test_type: document.currTestType.test_type,
        iter_count: document.currTestType.curr_count,
        sel_size: selSize,
        if_not_exists: true
    }, null,
            function (json) {
                var result = JSON.parse(json);
                
                function createPowerDiv() {
                    var powerDiv = procDiv.children("#power-div");
                    if (powerDiv.length === 0) {
                        powerDiv = $("<div/>", {
                            id: "power-div"
                        }).appendTo(procDiv);
                    }
                    powerDiv.empty();
                    $("<label/>", {
                        id: "power-panel-heading",
                        text: "График мощности статистики для объема выборки " + selSize
                    }).appendTo(powerDiv);
                    
                    return powerDiv;
                }
                
                
                if (!result.succeeded) {
                    var common = handleFailedAjaxResult(result.reason, {
                        dataNotReadyYet: function (status) {
                            var powerDiv = createPowerDiv();
                            status = ((status * 100) | 0);
                            var progressDiv = powerDiv.children("div.progress");
                            if (progressDiv.length === 0) {
                                progressDiv = $("<div/>", {
                                    class: "progress",
                                    style: "margin-bottom: 0px"
                                }).appendTo(powerDiv);
                            }

                            var progressBar = progressDiv.children("div.progress-bar");
                            if (progressBar.length === 0) {
                                progressBar = $("<div/>", {
                                    class: "progress-bar progress-bar-striped active",
                                    role: "progressbar",
                                    "aria-valuenow": status,
                                    "aria-valuemin": "0",
                                    "aria-valuemax": "100",
                                    style: "min-width: 2em; width: " + status + "%",
                                    text: "" + status + "%"
                                }).appendTo(progressDiv);
                            } else {
                                progressBar.attr("aria-valuenow", status);
                                progressBar.css("width", "" + status + "%");
                                progressBar.text("" + status + "%");
                            }

                            setTimeout(function () {
                                if (currId !== powerId) {
                                    return;
                                }
                                loadPower(selSize, currId);
                            }, 60000);
                        }
                    });

                    if (!!common) {
                        console.log(common);
                    }
                    return;
                }

                var powerDiv = createPowerDiv();
                plotPowerCharts(powerDiv, result.data, selSize);
            });
}

function plotPowerCharts(powerDiv, data, selSize) {
    var powerPlotDiv = $("#power-plot-div");
    
    if (powerPlotDiv.length === 0) {
        powerPlotDiv = $("<div/>", {
            id: "power-plot-div",
            class: "col-md-12"
        }).appendTo(powerDiv);
    }
    
    var series = [];
    for (var signLevel in data) {
        var values = data[signLevel];
        
        var signLevelStr = +signLevel / 1000;
        series.push({
            name: signLevelStr,
            data: values
        });
    }
    
    powerPlotDiv.highcharts({
        chart: {
            type: "spline"
        },
        title: {
            text: "Графики мощностей статистики против Weibull для n=" + selSize + " и уровнями значимости 0.01, 0.05, 0.1",
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