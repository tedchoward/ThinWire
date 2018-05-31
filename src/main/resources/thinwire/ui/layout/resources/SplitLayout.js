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
var tw_SplitLayout = Class.extend({
    _comp: null,
    _compDestroy: null,
    _vertical: false,
    _drag: null,
    _margin: 0,
    
    construct: function(id) {
        this._comp = tw_Component.instances[id];
        this._compDestroy = this._comp.destroy;
        this._comp.destroy = this.destroy.bind(this);
        this._drag = new tw_DragHandler(this._comp._box, this._dragListener.bind(this));
        this._vertical = this._comp._width < this._comp._height;
        this._comp._box.style.cursor = this._vertical ? "W-resize" : "N-resize";
        this._comp._box.style.zIndex = "10";
    },
    
    _dragListener: function(ev) {        
        if (ev.type == 0) {
            this._comp._box.style.backgroundColor = "black";
            this._vertical = this._comp._width < this._comp._height;
        } else if (ev.type == 1) {
            if (this._vertical) {
                this._comp._box.style.cursor = "W-resize";
                var x = this._comp._x + ev.changeInX;
                
                if (x < this._margin) {
                    x = this._margin;
                } else {    
                    var width = this._comp._parent._width - this._margin;
                    if (x > width) x = width;
                }
                
                this._comp.setX(x);
            } else {
                this._comp._box.style.cursor = "N-resize";
                var y = this._comp._y + ev.changeInY;
                
                if (y < this._margin) {
                    y = this._margin;
                } else {
                   var height = this._comp._parent._height - this._margin;
                   if (y > height) y = height;
                }
                
                this._comp.setY(y);
            }
        } else if (ev.type == 2) {
            this._comp._box.style.backgroundColor = "transparent";
            tw_em.sendViewStateChanged(this._comp._id, "position", this._comp._x + "," + this._comp._y);            
        }
    },
    
    setMargin: function(margin) {
        this._margin = margin;
    },
    
    destroy: function() {
        if (this._comp != null) {
            this._drag.destroy();
            this._comp.destroy = this._compDestroy;
            if (this._comp._inited) this._comp.destroy();
            this._compDestroy = this._comp = this._drag = null;
        }
    }
});

tw_SplitLayout.instances = {};

tw_SplitLayout.newInstance = function(id, margin) {
    var sl = tw_SplitLayout.instances[id] = new tw_SplitLayout(id);
    sl.setMargin(margin);
};

tw_SplitLayout.setMargin = function(id, margin) {
    tw_SplitLayout.instances[id].setMargin(margin);
};
