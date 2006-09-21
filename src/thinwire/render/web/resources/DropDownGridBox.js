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
var tw_DropDownGridBox = tw_BaseText.extend({
    _gridBox: null,
    _button: null,
    _editAllowed: true,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, [["div", "input", "text"], "dropDownGridBox", id, containerId, "editMask"]);
        this._box.style.fontSize = "1px";
        this._subtractEditorWidth = 17;
        
        var button = document.createElement("div");
        var s = button.style;
        s.position = "absolute";
        s.left = "0px";
        s.top = "0px";
        s.backgroundColor = "transparent";
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";                
        s.border = "2px outset";
        s.borderColor = tw_borderColor;        
        s.width = tw_sizeIncludesBorders ? "16px" : "12px";    
        s.backgroundImage = "url(?_twr_=tbArrowDown.png)";
        this._button = button;
        
        var buttonBorder = document.createElement("div");
        var s = buttonBorder.style;
        s.position = "absolute";
        s.right = "0px";
        s.backgroundColor = "buttonface";
        s.borderStyle = "solid";
        s.borderColor = "black";                        
        s.borderWidth = "0px";
        s.width = "16px";
        buttonBorder.appendChild(button);
        this._box.appendChild(buttonBorder);
                
        tw_addEventListener(this._box, "focus", this._focusListener);
        tw_addEventListener(button, "focus", this._focusListener);
        tw_addEventListener(this._box, "blur", this._blurListener);
        tw_addEventListener(button, "blur", this._blurListener);
            
        tw_addEventListener(button, "mousedown", this._buttonMouseDownListener.bind(this));
        tw_addEventListener(button, ["mouseup", "mouseout"], this._buttonMouseUpListener.bind(this));
        tw_addEventListener(button, "click", this._buttonClickListener.bind(this)); 

        this.init(-1, props);
    },
    
    _buttonMouseDownListener: function(event) {
        if (!this.isEnabled() || tw_getEventButton(event) != 1) return; 
        var s = this._button.style;
        s.border = "1px solid";
        s.borderColor = "threedshadow";
        s.padding = "1px";
    },
        
    _buttonMouseUpListener: function(event) {
        if (!this.isEnabled() || tw_getEventButton(event) != 1) return; 
        var s = this._button.style;
        s.border = "2px outset";
        s.borderColor = tw_borderColor;
        s.padding = "0px";    
    },
    
    _buttonClickListener: function(event) {
        if (!this.isEnabled()) return;
        
        if (!this._gridBox.isVisible()) {
            this.setDropDownVisible(true);
        } else {
            this.setDropDownVisible(false);
            this._focusListener();
        }
    },
        
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        this._box.lastChild.style.height = height - tw_CALC_BORDER_SUB + "px";        
        this._button.style.height = height - 
            (tw_sizeIncludesBorders ? tw_CALC_BORDER_SUB : tw_CALC_BORDER_SUB * 2) + "px";
    },
    
    setVisible: function(visible) {
        this.$.setVisible.apply(this, [visible]);
        if (!visible) this.setDropDownVisible(false);
    },

    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);
        if (enabled && !this._editAllowed) this._editor.readOnly = true;
    },
            
    setFocus: function(focus) {          
        if (!this.isEnabled() || !this.isVisible()) return false;
               
        if (!focus) {            
            var active = tw_getActiveElement();
            
            if (active != null) {
                if (active.className.substring(0, 8) == "dropDown") {
                    while (active.className != "dropDownGridBox") active = active.parentNode;                    
                    if (this._id == active.id) return false;
                } else if (active.className.substring(0, 7) == "gridBox") {
                    while (active.className != "gridBox") active = active.parentNode;                    
                    if (this._gridBox._id == active.id || (active.tw_root != null && active.tw_root._id == this._gridBox._id)) return false;
                }
            }
            
            this._gridBox.closeChildren();
            this._gridBox.setVisible(false);
        }
                        
        this._setFocusStyle(focus);        
        return this.$.setFocus.apply(this, [focus]);
    },

    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        if (this._gridBox != null) this._gridBox.setStyle(name, value);
    },
    
    keyPressNotify: function(keyPressCombo) {        
        if (!this._gridBox.isVisible()) {
            if (keyPressCombo == "ArrowDown") {
                this.setDropDownVisible(true);
                return false;                
            } else {
                return this.$.keyPressNotify.apply(this, [keyPressCombo]);                
            }
        } else {
            return this._gridBox.keyPressNotify(keyPressCombo);
        }
    },
    
    setEditAllowed: function(editAllowed) {        
        this._editAllowed = editAllowed;
        if (this.isEnabled()) this.setEnabled(true); //This will trigger the proper editAllowed state.
    },
    
    _setFocusStyle: function(state) {
        var buttonBorder = this._box.lastChild;
        if (parseInt(buttonBorder.style.borderWidth) == state) return; 
        buttonBorder.style.borderWidth = state ? "1px" : "0px";
        var offset = state ? -2 : 2; 
    
        if (!tw_sizeIncludesBorders) {
            buttonBorder.style.width = parseInt(buttonBorder.style.width) + offset + "px"
            buttonBorder.style.height = parseInt(buttonBorder.style.height) + offset + "px"
        }    
        
        var button = this._button;
        button.style.width = parseInt(button.style.width) + offset + "px";
        button.style.height = parseInt(button.style.height) + offset + "px";
    },

    setDropDownVisible: function(state) {
        if (this._gridBox == null) return;
        
        if (state) {
            var gb = this._gridBox;
            var container = this._box;
            var offsetX = 0;
            var offsetY = 0;                    
            
            while (container.className != "dialog" && container.className != "frame") {
                offsetX += container.offsetLeft - container.scrollLeft;
                offsetY += container.offsetTop - container.scrollTop;
                container = container.offsetParent;
            }
                                    
            var availableHeight = tw_getVisibleHeight() - gb._box.parentNode.offsetTop - offsetY - this._height;
            offsetY += (availableHeight < gb._height ? -gb._height : this._height);               
            
            gb.setY(offsetY);
            gb.setX(offsetX);
            gb.setVisible(true);
            gb.setFocus(true);
        } else {
            this._gridBox.closeChildren();
            this._gridBox.setVisible(false);
        }
    },
        
    destroy: function() {        
        this._gridBox.destroy();
        this._gridBox = this._button = null;
        this.$.destroy.apply(this, []);
    }
});


