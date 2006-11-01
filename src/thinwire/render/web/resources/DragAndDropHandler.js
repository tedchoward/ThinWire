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
var tw_DragAndDropHandler = Class.extend({
    _source: null,
    _targets: null,
    _dragBox: null,
    _dragBoxHandler: null,
    
    construct: function(source) {
        this._mouseDown = this._mouseDown.bind(this);
        this._mouseUp = this._mouseUp.bind(this);
        this._source = source;
        this._dragBoxHandler = new tw_DragHandler(this._source.getDragArea(), this._dragBoxListener.bind(this));
        tw_addEventListener(this._source.getDragArea(), "mousedown", this._mouseDown);
        this._targets = {};
    },
    
    addTarget: function(target) {
        this._targets[target.getId()] = target;
    },
    
    removeTarget: function(id) {
        delete this._targets[id];
        for (t in this._targets) if (tw_Component.instances[t] != undefined) return false;
        return true;
    },
    
    _mouseDown: function(event) {
        this._dragBox = this._source.getDragBox(event);
        if (this._dragBox == null) {
            this._dragBoxHandler.releaseDrag();
            return;
        }
        
        tw_addEventListener(document, "mouseup", this._mouseUp);
        var s = this._dragBox.style;
        s.left = this._dragBoxHandler._startX + 4 + "px";
        s.top = this._dragBoxHandler._startY + 4 + "px";
        s.zIndex = ++tw_Component.zIndex;
        s.display = "block";
        s.opacity = .5;
        if (tw_isIE) s.filter = "alpha(opacity=50)";
        document.body.appendChild(this._dragBox);
    },
    
    _mouseUp: function(event) {
        tw_removeEventListener(document, "mouseup", this._mouseUp);
        
        for (target in this._targets) {
            var curTarget = this._targets[target];
            if (tw_getEventTarget(event, curTarget.getDropTarget().className) == curTarget.getDropTarget()) {
                alert(typeof(curTarget) + ".fireAction(\"drop\", " + typeof(this._source) + ");");
            }
        }
    },
    
    _dragBoxListener: function(ev) {
        if (ev.type == 1) {
            var s = this._dragBox.style;
            s.left = this._dragBoxHandler._lastX + 4 + "px";
            s.top = this._dragBoxHandler._lastY + 4 + "px";
        } else if (ev.type == 2) {
            if (this._dragBox != null) {
                document.body.removeChild(this._dragBox);
                this._dragBox = null;
                tw_Component.zIndex--;
            }
        }
    },
    
    destroy: function() {
        this._dragBoxHandler.destroy();
        tw_removeEventListener(this._source.getDragArea(), "mousedown", this._mouseDown);
        this._source = this._targets = this._dragBox = null;
    }
});
