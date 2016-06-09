document.epsilon = 0.00001;

function compareWithEpsilon(first, second){
    var value = first - second;
    if (value < -document.epsilon) return -1;
    if (value > document.epsilon) return 1;
    return 0;
}

var TestTypes = {
    GINI: { type: "gini", name: "Gini", value: 1, onesided:false, aoewe: 0.8762, aoega: 0.6941 },
    GREENWOOD: { type: "greenwood", name: "Greenwood", value: 2, onesided:false, aoewe: 0.6079, aoega: 0.3876 },
    FROCINI:{ type: "frocini", name:"Frocini test", value: 3, onesided:true },
    KIMBERMICHEL:{ type: "kimbermichel", name:"Kimber-Michel", value: 4, onesided:true },
    MORAN:{ type: "moran", name:"Moran", value: 5, onesided:false, aoewe:0.9426, aoega: 1 },
    RAO:{ type: "rao", name:"Rao", value: 6, onesided:false },
    SHAPIROWILK:{ type: "shapirowilk", name:"Shapiro-Wilk", value: 7, onesided:false },
    SHERMAN:{ type: "sherman", name:"Sherman", value: 8, onesided:false },
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
        createSeries: function(min, max, step, scale) {
            if (!scale) scale = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.EXP.densityFunc(current, scale);
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
        createSeries: function(min, max, step, scale, k) {
            if (!scale) scale = 1;
            if (!k) k = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.WEIBULL.densityFunc(current, scale, k);
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
    NORMAL: {
        type:"normal", 
        name: "Normal",
        createSeries: function(min, max, step, m, d) {
            if (!m) m = 0;
            if (!d) d = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.NORMAL.densityFunc(current, m, d);
                if (!!result) {
                    array.push([current, result]);
                }
                current += step;
            }
            return array;
        },
        densityFunc: function(x, m, d) {
            var val = (1 / d * Math.sqrt(2 * Math.PI)) * Math.exp(- Math.pow(x - m, 2)/(2 * Math.pow(d, 2)));
            if (val === Infinity) {
                return SelectionTypes.NORMAL.densityFunc(x + 0.01, m, d);
            }
            return val;
        },
        value: function(x, l, k) {
            return 0;
        }
    },
    CHISCUARED: {
        type:"chi-scuared", 
        name: "Chi-scuared",
        createSeries: function(min, max, step, k) {
            if (!k) k = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.CHISCUARED.densityFunc(current, k);
                if (!!result) {
                    array.push([current, result]);
                }
                current += step;
            }
            return array;
        },
        densityFunc: function(x, k) {
            var k2 = k / 2;
            var val = (Math.pow(0.5, k2) / gammaFunc(k2)) * Math.pow(x, k2 - 1) * Math.exp(- x / 2);
            if (val === Infinity) {
                return SelectionTypes.CHISCUARED.densityFunc(x + 0.01, k);
            }
            return val;
        },
        value: function(x, l, k) {
            return 0;
        }
    },
    FISHER: {
        type:"fisher", 
        name: "Fisher",
        createSeries: function(min, max, step, k1, k2) {
            if (!k1) k1 = 1;
            if (!k2) k2 = 1;
            var array = [];
            var current = min;
            while(current <= max) {
                var result = SelectionTypes.FISHER.densityFunc(current, k1, k2);
                if (!!result) {
                    array.push([current, result]);
                }
                current += step;
            }
            return array;
        },
        densityFunc: function(x, k1, k2) {
            var k12 = k1 / 2;
            var k22 = k2 / 2;
            var k122 = k12 - k22;
            if(k122 === 0) k122 = 0.0001
            var gamma122 = gammaFunc(k122);
            var gamma12 = (k122 === k12) ? gamma122 : gammaFunc(k12);
            var gamma22 = (k122 === k22) ? gamma122 : (k12 === k22) ? gamma12 : gammaFunc(k22);
            var val = (gamma122 / (gamma12 * gamma22)) * (Math.pow(x, k12 - 1) / Math.pow(x + 1, k12 + k22));
            if (val === Infinity) {
                return SelectionTypes.FISHER.densityFunc(x + 0.01, k1, k2);
            }
            return val;
        },
        value: function(x, l, k) {
            return 0;
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

var g_gamma = 7;
var C_gamma = [0.99999999999980993, 676.5203681218851, -1259.1392167224028,771.32342877765313, -176.61502916214059, 12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7];

function gammaFunc(z) {

    if (z < 0.5) return Math.PI / (Math.sin(Math.PI * z) * gammaFunc(1 - z));
    else {
        z -= 1;

        var x = C_gamma[0];
        for (var i = 1; i < g_gamma + 2; i++)
        x += C_gamma[i] / (z + i);

        var t = z + g_gamma + 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, (z + 0.5)) * Math.exp(-t) * x;
    }
}

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

