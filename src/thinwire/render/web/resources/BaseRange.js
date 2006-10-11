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
var tw_BaseRange = tw_Component.extend({
    _selection: null,
    _length: null,
    _multiplier: null,
    _vertical: false,
    _currentIndex: null,
        
    construct: function(tagName, className, id, containerId) {
        arguments.callee.$.construct.call(this, tagName, className, id, containerId);
        this._fontBox = null;

        var selection = document.createElement("div");     
        var s = selection.style;
        s.position = "absolute";
        s.lineHeight = "0px";
        this._box.appendChild(selection);
        this._selection = this._backgroundBox = selection;
    },
    
    _recalc: function() {
        this._vertical = this.getWidth() <= this.getHeight();
        this._updateMultiplier();
    },
    
    getMax: function() {
        return this._vertical ? this.getHeight() : this.getWidth();
    },
    
    setLength: function(length) {
        if (length != null) this._length = length;
        this._updateMultiplier();
    },
    
    setCurrentIndex: function(currentIndex) {
        if (currentIndex != null) this._currentIndex = currentIndex;
        this._updateSelection();
    },
    
    getCurrentIndex: function() {
        return this._currentIndex;
    },
    
    setWidth: function(width) {
        arguments.callee.$.setWidth.call(this, width);
        this._recalc();
    },
    
    setHeight: function(height) {
        arguments.callee.$.setHeight.call(this, height);
        this._recalc();
    },
    
    _updateMultiplier: function() {
        this._multiplier = (this.getMax()) / (this._length - 1);
        this._updateSelection();
    },
    
    _updateSelection: function(hprop) {
        if (this.getCurrentIndex() == null || this._multiplier == null) return;
        var s = this._selection.style;
        if (this._vertical) {
            s.top = Math.floor(this.getMax() - (this.getCurrentIndex() * this._multiplier)) + "px";
        } else {
            s[hprop] = Math.floor(this.getCurrentIndex() * this._multiplier) + "px";
        }
    }
});
