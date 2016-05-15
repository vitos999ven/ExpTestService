function Ajax(method, adress, params, headers, respFunc){
    
    if  ((method !== "GET") 
      && (method !== "POST") 
      && (method !== "DELETE")){
                    method = "GET";
    } 
    var xhr = new XMLHttpRequest();
    var paramsString = "";
    
    if ( {}.toString.call(params) === '[object Object]' ) {
        var num = 0;
        for(var key in params) {
            if (num === 0){
                paramsString = "?";
            }else{
                paramsString += "&";
            };
            paramsString += key + "=" + params[key];
            num++;
        };
    };
    
    xhr.open(method, adress + paramsString, true);
    if (typeof respFunc === "function") {
        xhr.onreadystatechange = function() {
            if (this.readyState !== 4) return;
            respFunc(xhr.responseText);
        };
    };
    
    if ( {}.toString.call(headers) === '[object Object]' ) {
        for(var key in headers) {
            xhr.setRequestHeader(key, headers[key]);
        };
    };
    xhr.send("");
    return xhr;
};