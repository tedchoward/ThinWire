/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
function tw_Logger() {
    //
    //Private Fields
    //      
	var _logLevel = 3;
    var _DOMObjectIntervalId = 0;
	 
    //
    //Private Methods
    //      
    function _parseLevel(name) {
		name = name.toUpperCase();
	    var level = 0;	    
		if (name == "SEVERE") level = 1;
	    else if (name == "WARNING") level = 2;
	    else if (name == "INFO") level = 3;
	    else if (name == "CONFIG") level = 4;
	    else if (name == "FINE") level = 5;
	    else if (name == "FINER") level = 6;
	    else if (name == "FINEST") level = 7;	
	    return level;
	}
        
    //
    //Public Methods
    //        
	this.setLogLevel = function(levelName) {
		_logLevel = _parseLevel(levelName);
        
        if (this.isLoggable("FINE") && tw_isIE) {
            _DOMObjectIntervalId = setInterval(function() {
                document.title = document.all.length + " DOM Objects - " + new Date();
            }, 1000);
        } else if (_DOMObjectIntervalId != 0) {
            clearInterval(_DOMObjectIntervalId);
        }
	}
    
	this.isLoggable = function(levelName) { 
	    var level = _parseLevel(levelName);
	    return level != 0 && level <= _logLevel;
	}    
	
    this.log = function(levelName, msg) {
	    var level = _parseLevel(levelName);
	    if (level == 0 || level > _logLevel) return;
        tw_em.postLogMessage(levelName, msg);
	}
	
	this.severe = function(msg) { this.log("severe", msg); }
	this.warning = function(msg) { this.log("warning", msg); }
	this.info = function(msg) { this.log("info", msg); }
	this.config = function(msg) { this.log("config", msg); }
	this.fine = function(msg) { this.log("fine", msg); }
	this.finer = function(msg) { this.log("finer", msg); }
	this.finest = function(msg) { this.log("finest", msg); }
}

var tw_log = new tw_Logger();
