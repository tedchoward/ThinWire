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
//TODO: In opera, when tabbing to a button, the text gets highlighted for some reason.
//TODO: When a button or component is disabled it should be excluded from the tabbing order.
//TODO: focus gain / lose does not work in firefox.
var tw_Button = tw_Component.extend({
    _standardBorderSize: 0,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "button", id, containerId]);
        
        var border = document.createElement("div");
        border.className = "buttonBorder";
        border.style.borderColor = tw_borderColor;
        this._box.appendChild(border);
        
        var surface = document.createElement("a");
        surface.className = "buttonSurface";    
        surface.appendChild(document.createTextNode(""));
        border.appendChild(surface);
        this._focusBox = this._fontBox = surface;
        
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));    
        tw_addEventListener(this._box, ["mouseup", "mouseout"], this._mouseUpListener.bind(this));    
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        tw_addEventListener(surface, "focus", this._focusListener.bind(this));    
        tw_addEventListener(surface, "blur", this._blurListener.bind(this));
        
        this.init(-1, props);
    },
    
    _setStandardStyle: function(state) {
        if (state) {
            this._box.style.border = "1px solid black";
            this._standardBorderSize = 2;            
        } else {
            this._box.style.border = "0px";
            this._standardBorderSize = 0;            
        }

        this.setWidth(this._width);
        this.setHeight(this._height);
    },    
    
    _mouseDownListener: function() {
        if (!this.isEnabled()) return;
        this._box.firstChild.style.border = "2px inset";
        this._box.firstChild.style.borderColor = tw_borderColor;
    },
    
    _mouseUpListener: function() {
        if (!this.isEnabled()) return;
        this._box.firstChild.style.border = "2px outset";
        this._box.firstChild.style.borderColor = tw_borderColor;
    },

    //TODO: Will simply returning false from click when disabled, work in Gecko?
    _clickListener: function() {
        if (!this.isEnabled()) return;
        this.setFocus(true);  
        this.fireAction("click");
    },
    
    fireClick: function() { this._clickListener(); },

    setWidth: function(width) {
        this._width = width;

        if (tw_sizeIncludesBorders) {
            this._box.style.width = width + "px";
            width -= this._standardBorderSize;
            if (width < 0) width = 0;
            this._box.firstChild.style.width = width + "px";
        } else {
            width -= this._standardBorderSize;
            if (width < 0) width = 0;
            this._box.style.width = width + "px";
            width -= tw_CALC_BORDER_PADDING_SUB;
            if (width < 0) width = 0;
            this._box.firstChild.style.width = width + "px";
        }    
    },
    
    setHeight: function(height) {
        this._height = height;
        var s = this._box.firstChild.firstChild.style;

        if (tw_sizeIncludesBorders) {
            this._box.style.height = height + "px";     
            height -= this._standardBorderSize;
            if (height < 0) height = 0;
            this._box.firstChild.style.height = height + "px";
            height -= tw_CALC_BORDER_PADDING_SUB;
            if (height < 0) height = 0;
            s.lineHeight = s.height = height + "px";    
        } else {
            height -= this._standardBorderSize;
            if (height < 0) height = 0;
            this._box.style.height = height + "px";
            height -= tw_CALC_BORDER_PADDING_SUB;
            if (height < 0) height = 0;
            this._box.firstChild.style.height = height + "px";
            s.lineHeight = s.height = height + "px";    
        }    
    },
    
    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);        
        this._box.firstChild.firstChild.style.color = enabled ? "windowtext" : "graytext";            
        tw_setFocusCapable(this._box.firstChild.firstChild, enabled);
    },
    
    setFocus: function(focus) {
        var sButton = this.getBaseWindow().getStandardButton();        
        
        if (focus) {                
            if (sButton != null) sButton._setStandardStyle(false);        
            this._setStandardStyle(true);
        } else {
            this._setStandardStyle(false);
            if (sButton != null) sButton._setStandardStyle(true);
        }
        
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
        var b = this._box.firstChild.firstChild;         
        b.replaceChild(document.createTextNode(text), b.firstChild);
    },
    
    setImage: function(image) {
        var s = this._box.firstChild.firstChild.style;
    
        if (image.length > 0) {
            s.backgroundImage = "url(" + tw_BASE_PATH + "/resources/" + image + ")";
            s.paddingLeft = 16 + 2 + "px";
        } else {
            s.backgroundImage = "";
            s.paddingLeft = "1px";
        }
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

