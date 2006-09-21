/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
var tw_Slider = tw_Component.extend({
    _cursorDrag: null,
    _max: null,
    _length: null,
    _multiplier: null,
    _cursorIndex: null,
    _vertical: false,
    _clickListener: null,
   
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "slider", id, containerId]);
        var s = this._box.style;
        s.backgroundColor = "transparent";
        
        var line = document.createElement("div");
        var s = line.style;
        s.position = "absolute";
        s.border = "1px solid";
        s.borderColor = "buttonshadow buttonhighlight buttonhighlight buttonshadow";
        s.lineHeight = "0px";
        this._box.appendChild(line);
        
        var cursor = document.createElement("div");
        var s = cursor.style;
        s.position = "absolute";    
        s.border = "2px outset";
        s.overflow = "hidden";
        s.backgroundColor = "buttonface";
        this._cursorDrag = new tw_DragHandler(cursor, this._cursorDragListener.bind(this));
        this._box.appendChild(cursor);
        
        this.setLength(props.length);
        this.setCursorIndex(props.cursorIndex);
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        tw_setFocusCapable(this._box, true);
        this.init(-1, props);
    },
    
    _cursorDragListener: function(ev) {
        if (ev.type == 1) {
            var s = this._box.getElementsByTagName("div")[1].style;
            if (this._vertical) {
                var y = parseInt(s.top) + ev.changeInY;
                if (y < 0) y = 0;
                if (y > this._max) y = this._max
                s.top = y + "px";
            } else {
                var x = parseInt(s.left) + ev.changeInX;
                if (x < 0) x = 0;
                if (x > this._max) x = this._max
                s.left = x + "px";
            }
        } else if (ev.type == 2) {
            var s = this._box.getElementsByTagName("div")[1].style;
            var newValue = 0;
            if (this._vertical) {
                newValue = Math.round((this._max - parseFloat(s.top)) / this._multiplier);
            } else {
                newValue = Math.round(parseFloat(s.left) / this._multiplier);
            }
            this.setCursorIndex(newValue);
            this.firePropertyChange("cursorIndex", newValue);
        }
    },
    
    _recalc: function() {
        if (this._width != -1 && this._height != -1) {
            var children = this._box.getElementsByTagName("div");
            
            if (this.getWidth() > this.getHeight()) {
                this._vertical = false;    
                var l = children[0].style;    //line
                l.left = "0px";
                l.top = Math.floor(this.getHeight() / 2) + "px";
                l.width = this._width + "px";
                l.height = "0px";
                
                var c = children[1].style;    //cursor
                c.height = "15px";
                c.width = "5px";
                c.top = (Math.floor(this.getHeight() / 2) - Math.floor((parseInt(c.height) + tw_CALC_BORDER_SUB) / 2)) + "px";
                c.left = "0px";
                
                this._max = this.getWidth() - (parseInt(c.width) + tw_CALC_BORDER_SUB);
            } else {
                this._vertical = true;
                var l = children[0].style;
                l.left = Math.floor(this.getWidth() / 2) + "px";
                l.top = "0px";
                l.width = "0px";
                l.height = this._height + "px";
                
                var c = children[1].style;
                c.top = "0px";
                c.height = "5px";
                c.width = "15px";
                c.left = (Math.floor(this.getWidth() / 2) - Math.floor((parseInt(c.width) + tw_CALC_BORDER_SUB) / 2)) + "px";
                this._max = this.getHeight() - (parseInt(c.height) + tw_CALC_BORDER_SUB);
            }
            this.setLength();
            this.setCursorIndex();
        }
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
       this.setFocus(true);
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this._recalc();
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        this._recalc();
    },
    
    setCursorIndex: function(cursorIndex) {
        if (cursorIndex != null) this._cursorIndex = cursorIndex;
        var s = this._box.getElementsByTagName("div")[1].style;
        if (this._vertical) {
            s.top = Math.floor(this._max - (this._cursorIndex * this._multiplier)) + "px";
        } else {
            s.left = Math.floor(this._cursorIndex * this._multiplier) + "px";
        }
    },
    
    setLength: function(length) {
        if (length != null) this._length = length;
        this._multiplier = this._max / (this._length - 1);
    },
    
    keyPressNotify: function(keyPressCombo) {
        if ((keyPressCombo == "ArrowDown" && this._vertical) || (keyPressCombo == "ArrowLeft" && !this._vertical)) {
            if (this._cursorIndex > 0) {
                this.setCursorIndex(--this._cursorIndex);
                this.firePropertyChange("cursorIndex", this._cursorIndex);
            }
            return false;
        } else if ((keyPressCombo == "ArrowUp" && this._vertical) || (keyPressCombo == "ArrowRight" && !this._vertical)) {
            if (this._cursorIndex < this._length - 1) {
                this.setCursorIndex(++this._cursorIndex);
                this.firePropertyChange("cursorIndex", this._cursorIndex);
            }
            return false;
        } else {
            return this.$.keyPressNotify.apply(this, [keyPressCombo]);
        }
    },
    
    setEnabled: function(enabled) {
       if (enabled == this.isEnabled()) return;
       this.$.setEnabled.apply(this, [enabled]);
       tw_setFocusCapable(this._box, enabled);
    }
});