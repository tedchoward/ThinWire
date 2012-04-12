/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
var tw_DragAndDropHandler = Class.extend({
    _source: null,
    _targets: null,
    _dragBox: null,
    _dragX: 0,
    _dragY: 0,
    _dragInd: null,
    _dragBoxHandler: null,
    _cnt: null,
    
    construct: function(source) {
        this._mouseOut = this._mouseOut.bind(this);
        this._mouseOver = this._mouseOver.bind(this);
        this._mouseDown = this._mouseDown.bind(this);
        this._mouseMove = this._mouseMove.bind(this);
        this._mouseUp = this._mouseUp.bind(this);
        this._source = source;
            
        this._dragInd = document.createElement("img");
        var s = this._dragInd.style;
        var cssText = "position:absolute;";
        tw_Component.setCSSText(cssText, this._dragInd);
        
        this._dragBoxHandler = new tw_DragHandler(this._source.getDragArea(), this._dragBoxListener.bind(this));
        tw_addEventListener(this._source.getDragArea(), "mousedown", this._mouseDown);
        this._targets = {};
    },
    
    addTarget: function(target) {
        this._targets[target._id] = target;
    },
    
    removeTarget: function(id) {
        delete this._targets[id];
        for (t in this._targets) if (tw_Component.instances[t] != undefined) return false;
        return true;
    },
    
    _mouseOut: function(event) {
        this._dragInd.src = tw_IMAGE_DRAGDROP_INVALID;
    },
    
    _mouseOver: function(event) {
        this._dragInd.src = tw_IMAGE_DRAGDROP_VALID;
    },
    
    _mouseDown: function(event) {
        if (this._targets == null || !this._source._enabled) return;
    
        for (t in this._targets) {
            if (tw_Component.instances[t] == undefined) {
                this._source.removeDragTarget(t);
            } else {
                var dropArea = this._targets[t].getDropArea();
                tw_addEventListener(dropArea, "mouseover", this._mouseOver);
                tw_addEventListener(dropArea, "mouseout", this._mouseOut);
            }
        }

        this._cnt = 0;
        this._dragBox = this._source.getDragBox(event);
        
        if (this._dragBox == null) {
            this._dragBoxHandler.releaseDrag();
            return;
        }
        
        this._dragX = tw_getEventOffsetX(event, this._source._box.className);
        this._dragY = tw_getEventOffsetY(event, this._source._box.className);
        
        var s = this._dragBox.style;
        s.position = "absolute";
        s.overflow = "hidden";
        s.display = "none";
        s.border = "1px dotted " + tw_COLOR_WINDOWFRAME;
        tw_setOpacity(this._dragBox, 70);
        s.zIndex = ++tw_Component.zIndex;
        
        if (this._source._fontBox != null) {
            var ss = this._source._fontBox.style;
            s.fontFamily = ss.fontFamily;
            s.fontSize = ss.fontSize;
            s.fontWeight = ss.fontWeight;
            s.fontStyle = ss.fontStyle;
            s.textDecoration = ss.textDecoration;
            s.color = ss.color;
        }

        var s = this._dragInd.style;
        s.zIndex = ++tw_Component.zIndex;
        s.display = "none";
        this._dragInd.src = tw_IMAGE_DRAGDROP_INVALID;
        document.body.appendChild(this._dragBox);
        document.body.appendChild(this._dragInd);

        tw_addEventListener(document, "mousemove", this._mouseMove);
        tw_addEventListener(document, "mouseup", this._mouseUp);
    },
    
    _mouseMove: function(event) {
        if (this._cnt == null) return;
        if (this._cnt < 4) {
            this._cnt++;
        } else if (this._cnt == 4) {
            this._cnt++;
            tw_removeEventListener(document, "mousemove", this._mouseMove);
            this._dragBoxListener({type:1});
            this._dragBox.style.display = "block";
            this._dragInd.style.display = "block";
        }
    },
    
    _mouseUp: function(event) {
        tw_removeEventListener(document, "mousemove", this._mouseMove);
        tw_removeEventListener(document, "mouseup", this._mouseUp);
        
        for (t in this._targets) {
            if (tw_Component.instances[t] == undefined) {
                delete this._targets[t];
            } else {
                var dropArea = this._targets[t].getDropArea();
                tw_removeEventListener(dropArea, "mouseover", this._mouseOver);
                tw_removeEventListener(dropArea, "mouseout", this._mouseOut);
            }
        }
        
        this._cnt = null;
        
        if (this._dragBox != null) {
            if (this._dragBox.style.display == "block") {
               for (target in this._targets) {
                    var curTarget = this._targets[target];
                
                    if (tw_getEventTarget(event, curTarget.getDropArea().className) == curTarget.getDropArea()) {
                        var dragX = this._dragX >= 0 ? this._dragX : 0;
                        var dragY = this._dragY >= 0 ? this._dragY : 0;
                        var srcX = tw_getEventOffsetX(event, curTarget._box.className);
                        var srcY = tw_getEventOffsetY(event, curTarget._box.className);
                        if (srcX < 0) srcX = 0;
                        if (srcY < 0) srcY = 0;
                        curTarget.fireDrop(curTarget.getDropTarget(event), this._source, this._dragBox._dragObject, dragX, dragY, srcX, srcY);                    
                    }
                }
            }
    
            document.body.removeChild(this._dragInd);
            document.body.removeChild(this._dragBox);
            this._dragBox = null;
            tw_Component.zIndex--;
        }
    },
    
    _dragBoxListener: function(ev) {
        if (ev.type == 1) {
            if (this._dragBox != null) {
                var s = this._dragBox.style;
                s.left = this._dragBoxHandler._lastX + 4 + "px";
                s.top = this._dragBoxHandler._lastY + 4 + "px";
                var s = this._dragInd.style;
                s.left = this._dragBoxHandler._lastX + 10 + "px";
                s.top = this._dragBoxHandler._lastY + 20 + "px";
            }
        }
    },
    
    destroy: function() {
        this._dragBoxHandler.destroy();
        if (this._source.getDragArea() != null) {
            tw_removeEventListener(this._source.getDragArea(), "mousedown", this._mouseDown);
        }
        this._source = this._targets = this._dragBox = this._dragInd = null;
    }
});

