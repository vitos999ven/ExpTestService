document.epsilon = 0.00001;

function compareWithEpsilon(first, second){
    var value = first - second;
    if (value < -document.epsilon) return -1;
    if (value > document.epsilon) return 1;
    return 0;
}

var TestTypes = {
    GINI: {type: "gini", name: "Gini test", value: 1},
    GREENWOOD: {type: "greenwood", name: "Greenwood test", value: 2},
    FROCINI:{type: "frocini", name:"Frocini test", value: 3},
    KIMBERMICHEL:{type: "kimbermichel", name:"Kimber-Michel test", value: 4},
    MORAN:{type: "moran", name:"Moran test", value: 5},
    RAO:{type: "rao", name:"Rao test", value: 6},
    SHAPIROWILK:{type: "shapirowilk", name:"Shapiro-Wilk test"}, value: 7,
    SHERMAN:{type: "sherman", name:"Sherman test", value: 8},
    getType: function(type) {
        for (var testTypeName in TestTypes) {
            var testType = TestTypes[testTypeName];
            if (!testType.type){
                continue;
            }
            
            if (type === testType.type) {
                return testType;
            }
        }
        
        return null;
    }
};

var SelectionTypes = {
    EXP: {
        type:"exp", 
        name: "Exponential",
        createSeries: function(min, max, step) {
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.EXP.densityFunc(current, 1);
                if (!!result) {
                    array.push([current, result]);
                }
                current += step;
            }
            return array;
        },
        densityFunc: function(x, l) {
            return (x >= 0) ? (1 / l) * Math.exp(-x / l) : 0;
        },
        value: function(x, l) {
            return -Math.log(1 - x);
        }
    },
    WEIBULL: {
        type:"weibull", 
        name: "Weibull",
        createSeries: function(min, max, step, k) {
            if (!k) k = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.WEIBULL.densityFunc(current, 1, k);
                if (!!result) {
                    array.push([current, result]);
                }
                current += step;
            }
            return array;
        },
        densityFunc: function(x, l, k) {
            var val = (compareWithEpsilon(x, 0.0) >= 0) ? (k / l) * Math.pow(x / l, k - 1) * Math.exp(- Math.pow(x / l, k)) : 0;
            if (val === Infinity) {
                return SelectionTypes.WEIBULL.densityFunc(x + 0.01, l, k);
            }
            return val;
        },
        value: function(x, l, k) {
            return Math.pow(-Math.log(1 - x), 1 / k);
        }
    },
    getType: function(type) {
        for (var selTypeName in SelectionTypes) {
            var selType = SelectionTypes[selTypeName];
            if (!selType.type){
                continue;
            }
            
            if (type === selType.type) {
                return selType;
            }
        }
        
        return null;
    }
};

function getDensityArray(sortedArray, stepsCount, scale) {
     if (!stepsCount)
        stepsCount = 10;
    if (!scale)
        scale = 1;
    var accuracy = Math.pow(10, scale);
    
    var min = ((compareWithEpsilon(sortedArray[0], 0.0) < 0) ? sortedArray[0] : 0);
    var max = sortedArray[sortedArray.length - 1];
    var densityArray = [];
   
    var step = (max - min) / stepsCount;
    var j = 0;
    var lower = min;
    
    for (var i = 1; i < stepsCount + 1; i++) {
        var currentBorder =  min + i * step;
        var count = 0;
        while ((j < sortedArray.length) && 
                (compareWithEpsilon(sortedArray[j], currentBorder) <= 0)) {
            count++;
            j++;
        }
        densityArray.push(
                [
                    "" + (Math.round(lower * accuracy)/accuracy) + " - " + (Math.round(currentBorder *accuracy)/accuracy), 
                    count
                ]);
        lower = currentBorder;
    }
    
    return densityArray;
}

function getPercentageString(doubleVal) {
    return "" + ((doubleVal * 100) | 0) + " %";
}

function handleFailedAjaxResult(reason, handlers) {
    var unknownReasonMessage = "Request failed! Unknown reason";
    if (!reason) {
        console.error("No reason object to handle!");
        alert(unknownReasonMessage);
        return;
    }
    
    var type = reason.type;
    
    if (!type) {
        return unknownReasonMessage;
    }
    
    switch (type) {
        case "already_exists":
            console.log(handlers);
            if (!!handlers && !!handlers.alreadyExists) {
                handlers.alreadyExists();
                return;
            } else {
                return "Data already exists";
            }
        case "data_not_ready_yet":
            var status = reason.status;
            
            if (!!handlers && !!handlers.dataNotReadyYet) {
                handlers.dataNotReadyYet(status);
            } else {
                if (!status) {
                    console.error("No status field in 'data_not_ready_yet' reason");
                    return "Data not ready yet";
                }
                return "Data not ready yet. Status: " + getPercentageString(status);
            }
            return;
        case "db_problems":
            if (!!handlers && !!handlers.dbProblems) {
                handlers.dbProblems();
                return;
            } else {
                return "Problems with database";
            }
            return;   
        case "internal_error":
            var message = reason.message;
            
            if (!!handlers && !!handlers.internalError) {
                handlers.internalError(message);
            } else {
                if (!message) {
                    console.error("No message field in 'internal_error' reason");
                    return "Internal error";
                }
                return "Internal error: " + message;
            }
            return;  
        case "invalid_param":
            var param = reason.parameter;
            
            if (!!handlers && !!handlers.invalidParam) {
                handlers.invalidParam(param);
            } else {
                if (!param) {
                    console.error("No parameter field in 'invalid_param' reason");
                    return "Invalid request parameter";
                }
                return "Invalid request parameter: " + param;
            }
            return;    
        case "no_data":
            if (!!handlers && !!handlers.noData) {
                handlers.noData();
            } else {
                return "No data";
            }
            return;
        case "no_dependent_data":
            var name = reason.name;
            
            if (!!handlers && !!handlers.noDependentData) {
                handlers.noDependentData(name);
            } else {
                if (!name) {
                    console.error("No name field in 'no_dependent_data' reason");
                    return "No dependent data";
                }
                return "No dependent data: " + param;
            }
            return;    
        case "wrong_db_data":
            var name = reason.name;
            
            if (!!handlers && !!handlers.wrongDBData) {
                handlers.wrongDBData(name);
            } else {
                if (!name) {
                    console.error("No name field in 'wrong_db_data' reason");
                    return "Wrong database data";
                }
                return "Wrong database data: " + param;
            }
            return;
        default:
            console.error("Unknown reason type!");
            return unknownReasonMessage;
    }
}

