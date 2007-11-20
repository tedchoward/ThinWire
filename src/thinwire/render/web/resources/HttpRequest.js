/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
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
        if (method == "POST") this._comm.setRequestHeader("Content-Type", "text/plain; charset=utf-8");
        
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

