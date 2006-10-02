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
//TODO: In opera, when tabbing to a button, the text gets highlighted for some reason.
//TODO: When a button or component is disabled it should be excluded from the tabbing order.
//TODO: focus gain / lose does not work in firefox.
var tw_Button = tw_Component.extend({
    _fontColor: null,
        
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "button", id, containerId]);
        var s = this._box.style;
        s.borderStyle = "solid";
        s.borderColor = tw_COLOR_WINDOWFRAME;                        
        s.borderWidth = "0px";
        
        var border = this._borderBox = this._focusBox = document.createElement("div");
        var s = border.style;
        s.overflow = "hidden";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.zIndex = "0";
        this._box.appendChild(border);

        var surface = this._fontBox = this._focusBox = document.createElement("a");
        var s = surface.style;
        s.display = "block";        
        s.overflow = "hidden";
        s.cursor = "default";
        s.whiteSpace = "nowrap";
        s.textAlign = "center";
        s.paddingLeft = tw_Button.textPadding + "px";
        s.paddingRight = tw_Button.textPadding + "px";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "5% 50%";
        surface.appendChild(document.createTextNode(""));
        border.appendChild(surface);
        this._fontBox = surface;
        
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
        this.setWidth(this.getWidth());
        this.setHeight(this.getHeight());
    },
    
    _mouseDownListener: function(ev) {
        if (!this.isEnabled() || tw_getEventButton(ev) != 1) return;
        this._borderBox.style.borderStyle = "inset";
    },
    
    _mouseUpListener: function(ev) {
        if (!this.isEnabled() || tw_getEventButton(ev) != 1) return;
        this._borderBox.style.borderStyle = this._borderType;
    },

    //TODO: Will simply returning false from click when disabled, work in Gecko?
    _clickListener: tw_Component.clickListener,
    
    fireClick: function() {
        var ev = {};
        ev.type = "click"
        this._clickListener(ev); 
    },

    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        if (name == "backgroundColor") this._disabledBackgroundColor = value;
        if (name == "fontColor") this._fontColor = value;
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        var s = this._fontBox.style;
        width -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) width -= parseInt(s.paddingLeft) + parseInt(s.paddingRight) + this._borderSizeSub;
        if (width < 0) width = 0;
        s.width = width + "px";
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        var s = this._fontBox.style;
        height -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._borderSizeSub;
        if (height < 0) height = 0;
        s.height = s.lineHeight = height + "px";
    },
    
    setEnabled: function(enabled) {
        if (!enabled) this._setStandardStyle(false);
        this.$.setEnabled.apply(this, [enabled]);
        this._fontBox.style.color = enabled ? this._fontColor : tw_COLOR_GRAYTEXT; 
        tw_setFocusCapable(this._fontBox, enabled);
    },
    
    setFocus: function(focus) {
        if (!this.isEnabled() || !this.isVisible()) return false;
        this._setStandardStyle(focus);
        return this.$.setFocus.apply(this, [focus]);
    },
    
    keyPressNotify: function(keyPressCombo) {
        if (keyPressCombo == "Enter" || keyPressCombo == "Space") {
            this.fireClick();
            return false;
        } else {
            return this.$.keyPressNotify.apply(this, [keyPressCombo]);
        }        
    },
    
    setText: function(text) {
        var b = this._fontBox;         
        b.replaceChild(document.createTextNode(text), b.firstChild);
    },
    
    setImage: function(image) {
        var s = this._fontBox.style;
        s.backgroundImage = tw_Component.expandUrl(image, true);
        s.paddingLeft = (image.length > 0 ? tw_Button.imagePadding : tw_Button.textPadding) + "px";
        this.setWidth(this.getWidth());
        this.setHeight(this.getHeight());
    },

    setStandard: function(state) {
        var w = this.getBaseWindow();
        var sButton = w.getStandardButton();
        
        if (state) {        
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                this._setStandardStyle(true);
            }
            
            w.setStandardButton(this);
        } else if (sButton != null && sButton.getId() == this._id) {
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                sButton._setStandardStyle(false);
            }
            
            w.setStandardButton(null); 
        }
    },
    
    destroy: function() {
        var w = this.getBaseWindow();
        if (w.getStandardButton() === this) w.setStandardButton(null);
        this.$.destroy.apply(this, []);
    }
});

tw_Button.textPadding = 1;
tw_Button.imagePadding = 16;
