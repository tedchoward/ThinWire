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
var tw_Slider = tw_BaseRange.extend({
    _cursorDrag: null,
    _max: null,
    _line: null,
   
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "slider", id, containerId);
        var s = this._box.style;
        s.fontSize = "1px"; //Hack to work around IE height sizing issue
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        
        var line = this._line = document.createElement("div");
        var s = line.style;
        var ds = tw_Component.defaultStyles["Divider"];
        var cssText = "position:absolute;line-height:0px;border-style:" + ds.borderStyle + ";";
        tw_Component.setCSSText(cssText, line);
        this._box.insertBefore(line, this._selection);
        
        this._borderBox = this._selection;
        this._cursorDrag = new tw_DragHandler(this._selection, this._cursorDragListener.bind(this));
        
        this.init(-1, props);
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        var s = this._line.style;
        
        if (name == "borderColor") {
            s[name] = tw_Component.getIEBorder(value, this._borderType);
        } else if (name == "borderWidth") {
            value = Math.floor(parseInt(value) / 2);
            if (value < 1) value = 1;
            s[name] = value + "px";
        }
    },
    
    _cursorDragListener: function(ev) {
        if (!this._enabled) return;
        var s = this._selection.style;
        if (ev.type == 1) {
            var pos_prop = this._vertical ? "top" : "left";
            var change_prop = this._vertical ? "changeInY" : "changeInX";
            var x = parseInt(s[pos_prop]) + ev[change_prop];
            if (x < 0) x = 0;
            if (x > this.getMax()) x = this.getMax();
            s[pos_prop] = x + "px";
        } else if (ev.type == 2) {
            var newValue = 0;
            if (this._vertical) {
                newValue = Math.round((this.getMax() - parseFloat(s.top)) / this._multiplier);
            } else {
                newValue = Math.round(parseFloat(s.left) / this._multiplier);
            }
            this.setCurrentIndex(newValue);
            this.firePropertyChange("currentIndex", newValue);
        }
    },
    
    _recalc: function() {
        if (this._width != -1 && this._height != -1) {
            
            if (this._width > this._height) {
                var l = this._line.style;
                l.left = "0px";
                l.top = Math.floor(this._height / 2) + "px";
                l.width = this._width + "px";
                l.height = "0px";
                
                var c = this._selection.style;
                c.top = "0px";
                c.width = "5px";
                c.height = this._height > this._borderSize ? this._height - this._borderSizeSub + "px" : "0px";
                
                this._max = this._width - (parseInt(c.width) + this._borderSizeSub);
            } else {
                
                var l = this._line.style;
                l.left = Math.floor(this._width / 2) + "px";
                l.top = "0px";
                l.width = "0px";
                l.height = this._height + "px";
                
                var c = this._selection.style;
                c.left = "0px";
                c.width = this._width > this._borderSize ? this._width - this._borderSizeSub + "px" : "0px";
                c.height = "5px";
                
                this._max = this._height - (parseInt(c.height) + this._borderSizeSub);
            }
            
            arguments.callee.$.call(this);
        }
    },
    
    _updateSelection: function() {
        arguments.callee.$.call(this, "left");
    },
    
    getMax: function() {
        return this._max;
    },
    
    setEnabled: function(enabled) {
       if (this._focusCapable) tw_setFocusCapable(this._box, enabled);
       if (enabled == this._enabled) return;
       arguments.callee.$.call(this, enabled);
    },

    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._box, true);
        } else {
            tw_setFocusCapable(this._box, false);
        }
    },
    
    keyPressNotify: function(keyPressCombo) {
        if ((keyPressCombo == "ArrowDown" && this._vertical) || (keyPressCombo == "ArrowLeft" && !this._vertical)) {
            if (this.getCurrentIndex() > 0) {
                this.setCurrentIndex(this.getCurrentIndex() - 1);
                this.firePropertyChange("currentIndex", this.getCurrentIndex());
            }
            return false;
        } else if ((keyPressCombo == "ArrowUp" && this._vertical) || (keyPressCombo == "ArrowRight" && !this._vertical)) {
            if (this.getCurrentIndex() < this._length - 1) {
                this.setCurrentIndex(this.getCurrentIndex() + 1);
                this.firePropertyChange("currentIndex", this.getCurrentIndex());
            }
            return false;
        } else {
            return arguments.callee.$.call(this, keyPressCombo);
        }
    }
});
