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
var tw_DragHandler = Class.extend({
    _box: null,
    _listener: null,
    _startX: 0,
    _startY: 0,
    _lastX: 0,
    _lastY: 0,
    _dragBox: null,
    
    construct: function(box, listener) {
        this._mouseDown = this._mouseDown.bind(this)
        this._mouseMove = this._mouseMove.bind(this);        
        this._mouseUp = this._mouseUp.bind(this);
        if (box != null) this.setBox(box);
        if (listener != null) this.setListener(listener);
    },
    
    setBox: function(box) {
        if (this._box != null) tw_removeEventListener(this._box, "mousedown", this._mouseDown); 
        this._box = box;
        if (this._box != null) tw_addEventListener(this._box, "mousedown", this._mouseDown);    
    },
    
    getBox: function() {
        return this._box;
    },
        
    setListener: function(listener) {
        this._listener = listener;
    },
    
    _callListener: function(type, changeInX, changeInY) {
        if (this._listener != null) {
            var ev = {}
            ev.type = type;
            ev.box = this._dragBox;
            ev.changeInX = changeInX;
            ev.changeInY = changeInY;
            this._listener(ev);
        }
    },
    
    _mouseDown: function(event) {
        if (tw_getEventButton(event) != 1);
        this._dragBox = tw_getEventTarget(event);
        this._startX = this._lastX = event.clientX;
        this._startY = this._lastY = event.clientY;
        this._callListener(0, 0, 0);
        tw_addEventListener(document, "mousemove", this._mouseMove);    
        tw_addEventListener(document, "mouseup", this._mouseUp);        
        var frames = document.getElementsByTagName("IFRAME");
                
        for (var i = 0, cnt = frames.length; i < cnt; i++) {
            frames.item(i).style.display = "none";
        }
    },    
        
    _mouseMove: function(event) {
        var newX = event.clientX;
        var newY = event.clientY;
        var changeInX = newX - this._lastX;
        var changeInY = newY - this._lastY;
        this._lastX = newX;
        this._lastY = newY;        
        this._callListener(1, changeInX, changeInY);
    },
    
    _mouseUp: function(event) {
        var newX = event.clientX;
        var newY = event.clientY;
        tw_removeEventListener(document, "mousemove", this._mouseMove);    
        tw_removeEventListener(document, "mouseup", this._mouseUp);
        var frames = document.getElementsByTagName("IFRAME");

        for (var i = 0, cnt = frames.length; i < cnt; i++) {
            frames.item(i).style.display = "block";
        }

        var totalChangeInX = newX - this._startX;
        var totalChangeInY = newY - this._startY;
        this._callListener(2, totalChangeInX, totalChangeInY);
        this._dragBox = null;
    },
    
    releaseDrag: function() {
        this._mouseUp({clientX: this._startX, clientY: this._startY});
    },
    
    destroy: function() {
        this._box = this._dragBox = this._listener = null;
    }
});

