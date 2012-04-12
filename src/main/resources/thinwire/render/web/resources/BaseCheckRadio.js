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
var tw_BaseCheckRadio = tw_Component.extend({
    _imageChecked: "",
    _imageUnchecked: "",
    _imageDisabledChecked: "",
    _imageDisabledUnchecked: "",
    _image: null,
    _checked: false,
    
    construct: function(className, id, containerId) {
        arguments.callee.$.call(this, "a", className, id, containerId);
        this._box.appendChild(document.createTextNode(""));
        
        var s = this._box.style;

        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;display:block;cursor:default;background-repeat:no-repeat;" +
            "background-position:center left;text-decoration:none;border:0px;white-space:nowrap;";
        tw_Component.setCSSText(cssText, this._box);
        
        this._backgroundBox = this._borderBox = document.createElement("div");
        var s = this._borderBox.style;
        
        cssText = "position:absolute;overflow:hidden;font-size:0px;left:3px;";
        tw_Component.setCSSText(cssText, this._borderBox);
        
        this._image = document.createElement("div");
        var s = this._image.style; 

        cssText = "position:absolute;line-height:0px;background-color:" + tw_COLOR_TRANSPARENT + ";background-position:center;" +
            "background-repeat:no-repeat;width:" + tw_BaseCheckRadio.boxSize + "px;height:" + tw_BaseCheckRadio.boxSize + "px;";
        tw_Component.setCSSText(cssText, this._image);

        this._borderBox.appendChild(this._image);        
        this._box.appendChild(this._borderBox);
                
        if (this instanceof tw_CheckBox) {
            this._imageChecked = "url(" + tw_IMAGE_CHECKBOX_CHECK + ")";
            this._imageDisabledChecked = "url(" + tw_IMAGE_CHECKBOX_CHECKDISABLED + ")";
        } else {
            this._imageChecked = "url(" + tw_IMAGE_RADIOBUTTON_CHECK + ")";
            this._imageDisabledChecked = "url(" + tw_IMAGE_RADIOBUTTON_CHECKDISABLED + ")";
        }
        
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));    
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this));        
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));         
    },
    
    _clickListener: function(ev) {
        if (!this._enabled) return;
        if (this._focusCapable) this.setFocus(true)
        //#IFNDEF V1_1_COMPAT
        if (this instanceof tw_CheckBox || !this.isChecked()) {
        //#ENDIF
            var checked = !this.isChecked();
            this.setChecked(checked, true);
        //#IFNDEF V1_1_COMPAT
        }
        //#ENDIF
        
        var action = tw_Component.getClickAction(ev.type);
        if (action == null) return;
        this.fireAction(ev, action);
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
        this._height = height;
        this._box.style.height = height + "px";
        this._box.style.lineHeight = this._box.style.height;
        var top = Math.floor(height / 2 - (tw_BaseCheckRadio.boxSize + this._borderSize * 2) / 2);        
        if (top < 0) top = 0;
        this._borderBox.style.top = top + "px";
    },
    
    setEnabled: function(enabled) {
        if (this._focusCapable) tw_setFocusCapable(this._box, enabled);
        arguments.callee.$.call(this, enabled);
        if (enabled == this._enabled) return;
        if (this.isChecked()) this._image.style.backgroundImage = enabled ? this._imageChecked : this._imageDisabledChecked;
    },

    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._box, true);
        } else {
            tw_setFocusCapable(this._box, fal);
        }
    },
    
    setStyle: function(name, value) {
        var hadBorderImage = this._borderImage != null;
        arguments.callee.$.call(this, name, value);
        
        if (name == "borderWidth") {
            var size = tw_BaseCheckRadio.boxSize + this._borderSize * 2;
            this._box.style.paddingLeft = size + tw_BaseCheckRadio.pad * 2 + "px";

            if (this._borderImage != null) {
                this._borderImage.setWidth(size);
                this._borderImage.setHeight(size);
            } else {
                size = tw_BaseCheckRadio.boxSize;
            }
            
            this._borderBox.style.height = this._borderBox.style.width = size + "px"; 
        } else if (name == "borderImage") {
            if (this._borderImage == null) {
                if (this._oldBackgroundBox != undefined) {
                    this._backgroundBox = this._oldBackgroundBox;
                    delete this._oldBackgroundBox;
                }
            } else if (!hadBorderImage) {
                this._backgroundBox.style.backgroundColor = tw_COLOR_TRANSPARENT;
                this._oldBackgroundBox = this._backgroundBox;
                this._backgroundBox = this._borderImage._c;
            }
            this._backgroundBox.style.backgroundColor = this._backgroundColor;
            this.setEnabled(this._enabled);
        }
    },
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setText: tw_Component.setText,
   
    isChecked: function() {
        return this._checked;
    },
    
    setChecked: function(checked, sendEvent) {
        if (checked) {
            this._image.style.backgroundImage = this._enabled ? this._imageChecked : this._imageDisabledChecked;
        } else {
            this._image.style.backgroundImage = "";
        }

        this._checked = checked;
        if (sendEvent) this.firePropertyChange("checked", checked);
    },
    
    destroy: function() {
        this._image = null;
        arguments.callee.$.call(this);        
    }
});

tw_BaseCheckRadio.pad = 3;
tw_BaseCheckRadio.boxSize = 9;
