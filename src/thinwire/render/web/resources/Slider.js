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
var tw_Slider = tw_BaseRange.extend({
    _cursorDrag: null,
    _max: null,
    _clickListener: null,
    _line: null,
   
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "slider", id, containerId);
        var s = this._box.style;
        s.fontSize = "1px"; //Hack to work around IE height sizing issue
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        
        var line = this._line = document.createElement("div");
        var s = line.style;
        s.position = "absolute";
        s.lineHeight = "0px";
        var ds = tw_Component.defaultStyles["Divider"];
        s.borderStyle = ds.borderType;
        this._box.insertBefore(line, this._selection);
        
        this._borderBox = this._selection;
        this._cursorDrag = new tw_DragHandler(this._selection, this._cursorDragListener.bind(this));
        
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        var s = this._line.style;
        
        if (name == "borderColor") {
            s[name] = tw_Component.getIEBorder(value, this.getStyle("borderType"));
        } else if (name == "borderSize") {
            value = Math.floor(parseInt(value) / 2);
            if (value < 1) value = 1;
            s[tw_Component.styleNameMap[name]] = value + "px";
        }
    },
    
    _cursorDragListener: function(ev) {
        if (!this.isEnabled()) return;
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
        if (this.getWidth() != -1 && this.getHeight() != -1) {
            
            if (this.getWidth() > this.getHeight()) {
                var l = this._line.style;
                l.left = "0px";
                l.top = Math.floor(this.getHeight() / 2) + "px";
                l.width = this.getWidth() + "px";
                l.height = "0px";
                
                var c = this._selection.style;
                c.top = "0px";
                c.width = "5px";
                c.height = this.getHeight() > this.getStyle("borderSize") ? this.getHeight() - (this.getStyle("borderSize") * 2) + "px" : "0px";
                
                this._max = this.getWidth() - (parseInt(c.width) + (this.getStyle("borderSize") * 2));
            } else {
                
                var l = this._line.style;
                l.left = Math.floor(this.getWidth() / 2) + "px";
                l.top = "0px";
                l.width = "0px";
                l.height = this.getHeight() + "px";
                
                var c = this._selection.style;
                c.left = "0px";
                c.width = this.getWidth() > this.getStyle("borderSize") ? this.getWidth() - (this.getStyle("borderSize") * 2) + "px" : "0px";
                c.height = "5px";
                
                this._max = this.getHeight() - (parseInt(c.height) + this.getStyle("borderSize") * 2);
            }
            arguments.callee.$.call(this);
        }
    },
    
    _updateSelection: function() {
        arguments.callee.$.call(this, "left");
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
       this.setFocus(true);
    },
    
    getMax: function() {
        return this._max;
    },
    
    setEnabled: function(enabled) {
       tw_setFocusCapable(this._box, enabled);
       if (enabled == this.isEnabled()) return;
       arguments.callee.$.call(this, enabled);
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