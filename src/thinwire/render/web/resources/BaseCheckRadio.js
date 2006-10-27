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
var tw_BaseCheckRadio = tw_Component.extend({
    _imageChecked: "",
    _imageUnchecked: "",
    _imageDisabledChecked: "",
    _imageDisabledUnchecked: "",
    _image: null,
    
    construct: function(className, id, containerId) {
        arguments.callee.$.call(this, "a", className, id, containerId, "text,lineHeight");

        var s = this._box.style;
        s.display = "block";
        s.cursor = "default";        
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center left";        
        s.backgroundColor = tw_COLOR_TRANSPARENT;        
        s.textDecoration = "none";
        s.border = "0px";
        s.whiteSpace = "nowrap";
        s.paddingLeft = "18px";

        this._backgroundBox = document.createElement("div");
        var s = this._backgroundBox.style; 
        s.position = "absolute";
        s.width = "9px";
        s.height = "9px";
        s.overflow = "hidden";
        s.zIndex = 0;
        this._box.appendChild(this._backgroundBox);     
        
        this._borderBox = document.createElement("div");
        var s = this._borderBox.style; 
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.position = "absolute";
        s.width = "9px";
        s.height = "9px";
        s.left = "1px";
        s.zIndex = 1;
        this._box.appendChild(this._borderBox);
        
        this._image = document.createElement("div");
        var s = this._image.style; 
        s.backgroundPosition = "center";
        s.backgroundRepeat = "no-repeat";
        s.position = "absolute";
        s.left = "1px";
        s.zIndex = 1;
        this._box.appendChild(this._image);
        
        var prefix = (this instanceof tw_CheckBox) ? "cb" : "rb";
        this._imageChecked = "url(?_twr_=" + prefix + "Checked.png)";
        this._imageDisabledChecked = "url(?_twr_=" + prefix + "DisabledChecked.png)";    
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));    
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this));        
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));         
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        this.setFocus(true)
        //#IFNDEF V1_1_COMPAT
        if (this instanceof tw_CheckBox || !this.isChecked()) {
        //#ENDIF
            var checked = !this.isChecked();
            this.setChecked(checked, true);
        //#IFNDEF V1_1_COMPAT
        }
        //#ENDIF
    },

    setWidth: function(width) {
        this._width = width;
        
        if (!tw_sizeIncludesBorders) {
            var sub = parseInt(this._box.style.paddingLeft);
            width = width <= sub ? 0 : width - sub;
        }
        
        this._box.style.width = width + "px";
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        var top = height / 2 - 8;        
        if (top < 0) top = 0;
        this._image.style.top = top + "px";
        this._borderBox.style.top = top + "px";
        this._backgroundBox.style.top = top + this.getStyle("borderSize") + "px";
    },
    
    setEnabled: function(enabled) {
        tw_setFocusCapable(this._box, enabled);
        if (enabled == this.isEnabled()) return;
        arguments.callee.$.call(this, enabled);
        if (this.isChecked()) {
            this._image.style.backgroundImage = enabled ? this._imageChecked : this._imageDisabledChecked;
        }
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        if (name == "borderSize") {
            var imageSize = (value * 2) + 9;
            this._image.style.width = imageSize + "px";
            this._image.style.height = imageSize + "px";
            this._box.style.paddingLeft = imageSize + 5 + "px";
            this._backgroundBox.style.left = value + 1 + "px";
            var top = this.getHeight() / 2 - 8;
            if (top < 0) top = 0;
            this._backgroundBox.style.top = top + value + "px";
        }
    },
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
   
    isChecked: function() {
        return this._image.style.backgroundImage.indexOf("Checked") != -1;
    },
    
    setChecked: function(checked, sendEvent) {
        if (checked) {
            this._image.style.backgroundImage = this.isEnabled() ? this._imageChecked : this._imageDisabledChecked;
        } else {
            this._image.style.backgroundImage = "";
        }

        if (sendEvent) this.firePropertyChange("checked", checked);
    },
    
    destroy: function() {
        this._image = null;
        arguments.callee.$.call(this);        
    }
});

