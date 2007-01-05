/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
//TODO: In opera, when tabbing to a button, the text gets highlighted for some reason.
//TODO: When a button or component is disabled it should be excluded from the tabbing order.
//TODO: focus gain / lose does not work in firefox.
var tw_Button = tw_Component.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "button", id, containerId);
        var s = this._box.style;
        s.borderStyle = "solid";
        s.borderColor = tw_COLOR_WINDOWFRAME;
        s.borderWidth = "0px";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        
        var border = this._borderBox = this._backgroundBox = this._focusBox = document.createElement("div");
        var s = border.style;
        s.overflow = "hidden";
        s.position = "absolute";
        this._box.appendChild(border);

        var surface = this._fontBox = this._focusBox = document.createElement("a");
        var s = surface.style;
        s.display = "block"; 
        s.overflow = "hidden";
        s.cursor = "default";
        s.position = "absolute";
        s.whiteSpace = "nowrap";
        s.textAlign = "center";
        s.paddingLeft = tw_Button.textPadding + "px";
        s.paddingRight = tw_Button.textPadding + "px";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "5% 50%";
        surface.appendChild(document.createTextNode(""));
        border.appendChild(surface);
        
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));    
        tw_addEventListener(this._box, ["mouseup", "mouseout"], this._mouseUpListener.bind(this));    
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        tw_addEventListener(border, "focus", this._focusListener.bind(this));    
        tw_addEventListener(border, "blur", this._blurListener.bind(this));
        
        this.init(-1, props);
    },    
    
    _setStandardStyle: function(state) {
        if (parseInt(this._box.style.borderWidth) == state) return;
        this._boxSizeSub = state ? 2 : 0;
        this._box.style.borderWidth = state ? "1px" : "0px";
        this.setWidth(this._width);
        this.setHeight(this._height);
    },
    
    _mouseDownListener: function(ev) {
        if (!this._enabled || tw_getEventButton(ev) != 1) return;
        this._borderBox.style.borderStyle = "inset";
    },
    
    _mouseUpListener: function(ev) {
        if (this._enabled && tw_getEventButton(ev) == 1 || ev.type == "mouseout") {
            this._borderBox.style.borderStyle = this._borderType;
        }
    },

    //TODO: Will simply returning false from click when disabled, work in Gecko?
    _clickListener: tw_Component.clickListener,
    
    fireClick: function() {
        var ev = {};
        ev.type = "click"
        this._clickListener(ev); 
    },

    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        width -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) width -= this._borderSizeSub;
        if (width < 0) width = 0;
        this._borderBox.style.width = width + (this._borderImage != null ? this._borderSizeSub : 0) + "px";
        var s = this._fontBox.style;
        if (!tw_sizeIncludesBorders) width -= parseInt(s.paddingLeft) + parseInt(s.paddingRight);
        s.width = width < 0 ? "0px" : width + "px";
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._borderSizeSub;
        if (height < 0) height = 0;
        var s = this._fontBox.style;
        s.height = s.lineHeight = height + "px";
        this._borderBox.style.height = height + (this._borderImage != null ? this._borderSizeSub : 0) + "px";
    },
    
    setEnabled: function(enabled) {
        if (!enabled) this._setStandardStyle(false);
        arguments.callee.$.call(this, enabled);
        this._fontBox.style.color = enabled ? this._fontColor : tw_COLOR_GRAYTEXT; 
        tw_setFocusCapable(this._fontBox, enabled);
    },
    
    setFocus: function(focus) {
        if (!this._enabled || !this.isVisible()) return false;
        this._setStandardStyle(focus);
        return arguments.callee.$.call(this, focus);
    },
    
    keyPressNotify: function(keyPressCombo) {
        if (keyPressCombo == "Enter" || keyPressCombo == "Space") {
            this.fireClick();
            return false;
        } else {
            return arguments.callee.$.call(this, keyPressCombo);
        }        
    },
    
    setText: function(text) {
        var b = this._fontBox;         
        b.replaceChild(tw_Component.setRichText(text), b.firstChild);
    },
    
    setImage: function(image) {
        var s = this._fontBox.style;
        s.backgroundImage = tw_Component.expandUrl(image, true);
        s.paddingLeft = (image.length > 0 ? tw_Button.imagePadding : tw_Button.textPadding) + "px";
        this.setWidth(this._width);
        this.setHeight(this._height);
    },

    setStandard: function(state) {
        var w = this.getBaseWindow();
        var sButton = w.getStandardButton();
        
        if (state) {        
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                this._setStandardStyle(true);
            }
            
            w.setStandardButton(this);
        } else if (sButton != null && sButton._id == this._id) {
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                sButton._setStandardStyle(false);
            }
            
            w.setStandardButton(null); 
        }
    },

    getDragBox: function() {
        var dragBox = this._fontBox.cloneNode(true);
        dragBox.style.backgroundColor = tw_COLOR_TRANSPARENT;
        return dragBox;
    },
    
    destroy: function() {
        var w = this.getBaseWindow();
        if (w.getStandardButton() === this) w.setStandardButton(null);
        arguments.callee.$.call(this);
    }
});

tw_Button.textPadding = 1;
tw_Button.imagePadding = 16;
