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
var tw_Animation = Class.extend({
    _obj: null,
    _getter: "",
    _setter: "",
    _neg: false,
    _dist: 0,
    _unitSize: 0,
    _unitTime: 0,
    _lastTime: 0,
    _calcUnitTime: 0,
    
    construct: function(obj, getter, setter, dist, unitSize, time) {        
        if (obj == null) obj = window;
        this._obj = obj;
        this._getter = getter;
        this._setter = setter;        
        this._run = this._run.bind(this);
        
        if (dist < 0) {
            this._neg = true;
            dist = ~dist + 1;
        }
        
        this._dist = dist;
        this._unitSize = unitSize;        
        this._calcUnitTime = Math.floor(time / Math.floor(dist / this._unitSize + .5) + .5);
        this._unitTime = Math.floor(this._calcUnitTime / 2);
    },
        
    start: function() {
        setTimeout(this._run, 0);
    },    
    
    _run: function() { 
        if (this._unitSize > this._dist) {
            this._unitSize = this._dist;                        
        } else {
            var thisTime = new Date().getTime();

            if (this._lastTime > 0) {
                var actTime = thisTime - this._lastTime;                
                if (actTime > 0) this._unitTime = Math.floor(this._unitTime * (this._calcUnitTime / actTime) + .5);
            }
            
            this._lastTime = thisTime;
        }
        
        if (this._obj instanceof tw_Component && this._obj._inited) {
            this._obj[this._setter](this._obj[this._getter]() + (this._neg ? -this._unitSize : this._unitSize));
            this._dist -= this._unitSize;
            if (this._dist > 0) setTimeout(this._run, this._unitTime);
        }
    }
});

