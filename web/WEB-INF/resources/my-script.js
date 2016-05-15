function onLoadSelection() {
        Ajax("GET", "selection", {name : this.id.substring(4)}, null,
                        function(result){
                            console.log(result);
                            document.currSelection = JSON.parse(result);
                            
                            $.jqplot('selection-graph', [document.currSelection.values], {
                                title: "Values",
                                series:[{showMarker:false}],
                                axes:{
                                    xaxis:{
                                        min: 0,
                                        label: "Index",
                                        labelRenderer: $.CanvasAxisLabelRenderer
                                    },
                                    yaxis:{
                                        label: "Value",
                                        labelRenderer: $.CanvasAxisLabelRenderer
                                    }
                                }
                            });
                            plotDensityGraphic();
                        });                    
};

function plotDensityGraphic() {
    var sorted = document.currSelection.sorted = document.currSelection.values.slice().sort();
    var min = sorted[0];
    var max = sorted[sorted.length - 1];
    var densityArray = [];
    var ticks = [];
    var step = (max - min) / 10;
    var j = 0;
    var lower = min;
    
    for (var i = 1; i < 11; i++) {
        var currentBorder =  min + i * step;
        var count = 0;
        while ((j < sorted.length) && 
                (compareWithEpsilon(sorted[j], currentBorder) <= 0)) {
            count++;
            j++;
        }
        densityArray.push(count);
        ticks.push("" + (Math.round(lower *10)/10) + " - " + (Math.round(currentBorder *10)/10));
        lower = currentBorder;
    }
    $.jqplot('selection-density-graph', [densityArray], {
        title: "Probability Density",
        animate: !$.use_excanvas,
        seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            pointsLabels: { show: true }
        },
        axes: {
            xaxis: {
                label: "Interval",
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                tickOptions: {
                    angle: 30
                }
            },
            yaxis: {
                label: "Count",
                labelRenderer: $.CanvasAxisLabelRenderer
            }
        }
    });
}



