/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_HttpRequest = Class.extend({
    _comm: null,
    _ready: true,
    _userAsyncFunc: null,
    _usingMSHttpRequest: false,
            
    construct: function(userAsyncFunc) {
        this._userAsyncFunc = userAsyncFunc;
        this._asyncFunc = this._asyncFunc.bind(this);

        try {
            try {        	
                this._comm = new XMLHttpRequest();
                if (tw_isIE) this._usingMSHttpRequest = true;
            } catch (e) {
                this._comm = new ActiveXObject(tw_bVer < 6 ? "Microsoft.XMLHTTP" : "Msxml2.XMLHTTP");
                this._usingMSHttpRequest = true;
            }
        } catch (e) {
            alert("Unable to create tw_HttpRequest :" + e);
        }        
    },
    
    _getResponse: function() {
        var response = this._comm.responseText;
        
        if (response == null) {
            response = "";
        } else {
            response += ""; //Guarantee conversion to string since this could be a XML object;
        }
        
        return response;
    },
    
    _asyncFunc: function() {
        if (this._comm.readyState == 4) {
            this._userAsyncFunc(this._getResponse());
            this._ready = true;
        }
    },
    
    send: function(method, url, data) {
        this._ready = false;
        //XXX: Causes Permission Denied error occasionally in IE, unsure why, arguments looked
        //correct in Visual Studio.  _comm.readyState reflects the value zero, all other fields of
        //XMLHttpRequest are empty.
        //Second error after playing with VS: The data necessary to complete this operation is not yet available.
        this._comm.open(method, url, this._userAsyncFunc != null);

        if (method == "POST") {
            this._comm.setRequestHeader("Content-Type", "text/plain");
            this._comm.setRequestHeader("Content-Length", data.length);
        }
        
        if (this._userAsyncFunc != null) {
            if (this._usingMSHttpRequest) {
                this._comm.onreadystatechange = this._asyncFunc;
            } else {
                this._comm.onload = this._asyncFunc;
            }
            
            this._comm.send(data);            
            return "";
        } else {
            this._comm.send(data);
            var ret = this._getResponse();
            this._ready = true;
            return ret;
        }                        
    },      
    
    abort: function() {
        this._comm.abort();
    },
    
    isReady: function() {
        return this._ready;
    }
});

