/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
// Add support for creating functions that are tighly bound to Objects.
Function.prototype.bind = function (object) {
    var method = this;
    
    return function () {
        method.apply(object, arguments);
    };
}

function Class() { }
Class.prototype.construct = function() {};
Class.extend = function(def) {
    var classDef = function() {
        if (arguments[0] !== Class) { this.construct.apply(this, arguments); }
    };
    
    var proto = new this(Class);
    var superClass = this.prototype;
    
    for (var n in def) {
        var item = def[n];                        
        if (item instanceof Function) item.$ = superClass;
        proto[n] = item;
    }

    var setters = {};
    
    for (var n in proto) {
        if (n.indexOf("set") == 0 && proto[n] instanceof Function) {            
            setters[n.charAt(3).toLowerCase() + n.substring(4)] = n;
        }
    }
    
    proto.__setters__ = setters;
    
    classDef.prototype = proto;
    
    //Give this new class the same static extend method    
    classDef.extend = this.extend;        
    return classDef;
};
