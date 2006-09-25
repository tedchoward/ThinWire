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
        this.$.construct.apply(this, ["a", className, id, containerId, "text,lineHeight"]);
        this._borderBox = null;

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
        s.left = "4px";
        s.zIndex = 0;
        this._box.appendChild(this._backgroundBox);               
        
        this._image = document.createElement("div");
        var s = this._image.style; 
        s.position = "absolute";
        s.width = "16px";
        s.height = "16px";
        s.left = "1px";
        s.zIndex = 1;
        this._box.appendChild(this._image);
        
        var prefix = (this instanceof tw_CheckBox) ? "cb" : "rb";
        this._imageChecked = "url(?_twr_=" + prefix + "Checked.png)";
        this._imageUnchecked = "url(?_twr_=" + prefix + "Unchecked.png)";
        this._imageDisabledChecked = "url(?_twr_=" + prefix + "DisabledChecked.png)";
        this._imageDisabledUnchecked = "url(?_twr_=" + prefix + "DisabledUnchecked.png)";    
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));    
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this));        
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));         
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        this.setFocus(true)
        var checked = !this.isChecked();
        this.setChecked(checked, true);
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
        this.$.setHeight.apply(this, [height]);
        var top = height / 2 - 8;        
        if (top < 0) top = 0;
        this._image.style.top = top + "px";
        this._backgroundBox.style.top = top + 3 + "px";
    },
    
    setEnabled: function(enabled) {
        tw_setFocusCapable(this._box, enabled);
        if (enabled == this.isEnabled()) return;
        this.$.setEnabled.apply(this, [enabled]);
        this.setChecked(this.isChecked()); //Toggles image to disabled image
    },
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
   
    isChecked: function() {
        return this._image.style.backgroundImage.indexOf("Unchecked") == -1;
    },
    
    setChecked: function(checked, sendEvent) {
        if (this.isEnabled()) {
            this._image.style.backgroundImage = checked ? this._imageChecked : this._imageUnchecked;
        } else {
            this._image.style.backgroundImage = checked ? this._imageDisabledChecked : this._imageDisabledUnchecked;
        }

        if (sendEvent) this.firePropertyChange("checked", checked);
    },
    
    destroy: function() {
        this._image = null;
        this.$.destroy.apply(this, []);        
    }
});

