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
var tw_DropDown = tw_BaseText.extend({
    _ddComp: null,
    _button: null,
    _buttonBorder: null,
    _editAllowed: true,
    _buttonWidth: 16,
    _buttonBorderWidth: 0,
    
    construct: function(id, containerId, props) {
        var button = this._button = document.createElement("div");
        var buttonBorder = this._buttonBorder = document.createElement("div");
        arguments.callee.$.call(this, ["div", "input", "text"], "dropDownGridBox", id, containerId, "editMask");
        this._box.style.fontSize = "1px"; //Hack to work around IE height sizing issue
        this._subtractEditorWidth += this._buttonWidth;
        
        var s = button.style;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";                

        var bs = tw_Component.defaultStyles["Button"];        
        s.backgroundColor = bs.backgroundColor;
        s.borderStyle = bs.borderType;
        s.borderColor = tw_Component.getIEBorder(bs.borderColor, bs.borderType);        
        
        var s = buttonBorder.style;
        s.position = "absolute";
        s.right = "0px";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.borderStyle = "solid";
        s.borderColor = tw_COLOR_WINDOWFRAME;                        
        s.borderWidth = "0px";
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
    
    setComponent: function(compId) {
        var comp = tw_Component.instances[compId];
        var showDropDown = false;
        //Destroy existing Component for the dropdown if it exists
        if (this._ddComp != null) {
            showDropDown = this._ddComp.isVisible();
            this._ddComp.destroy();
        }
        this._ddComp = comp;
        this._ddComp._box.style.zIndex = 1;

        this._ddComp.setCompVisible = this._ddComp.setVisible;
        this._ddComp.setVisible = this.setDropDownVisible.bind(this);
        
        this._ddComp.setVisible(showDropDown);  
    },
    
    addCloseComponent: function(compId) {
        var comp = tw_Component.instances[compId];
        comp._dropDownId = this._id;
        comp.fireCompAction = comp.fireAction;
        comp.fireAction = this._fireCloseAction;
    },
    
    _fireCloseAction: function(subType, eventData) {
        if (subType == "click") tw_Component.instances[this._dropDownId]._ddComp.setVisible(false);
        this.fireCompAction(subType, eventData);
    },
    
    _buttonMouseDownListener: function(event) {
        if (!this.isEnabled() || tw_getEventButton(event) != 1) return; 
        var s = this._button.style;
        s.borderStyle = "solid";
        var pad = parseInt(s.borderWidth);
        var width = Math.floor(pad / 2);
        pad -= width;
        s.borderWidth = width + "px"; 
        s.borderColor = tw_COLOR_THREEDSHADOW;
        s.padding = pad + "px";
    },
        
    _buttonMouseUpListener: function(event) {
        if (!this.isEnabled() || tw_getEventButton(event) != 1) return; 
        var s = this._button.style;
        var bs = tw_Component.defaultStyles["Button"];
        s.borderStyle = bs.borderType;
        s.borderWidth = this._box.style.borderWidth;
        s.borderColor = tw_Component.getIEBorder(bs.borderColor, bs.borderType);
        s.padding = "0px";    
    },
    
    _buttonClickListener: function(event) {
        if (!this.isEnabled()) return;
        
        if (!this._ddComp.isVisible()) {
            this.setDropDownVisible(true);
        } else {
            this.setDropDownVisible(false);
        }
    },
    
    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        width = this._buttonWidth;
        if (!tw_sizeIncludesBorders) width -= this._buttonBorderWidth * 2;        
        if (width < 0) width = 0;
        this._buttonBorder.style.width = width + "px";
        width -= this._borderSizeSub;
        if (width < 0) width = 0;        
        this._button.style.width = width + "px"; 
    },

    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= this._borderSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._buttonBorderWidth * 2;
        if (height < 0) height = 0;        
        this._buttonBorder.style.height = height + "px";
        height -= this._borderSizeSub;
        if (height < 0) height = 0;        
        this._button.style.height = height + "px"; 
    },
    
    setVisible: function(visible) {
        arguments.callee.$.call(this, visible);
        if (!visible) this.setDropDownVisible(false);
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._button.style.backgroundImage = enabled ? "url(?_twr_=ddButton.png)" : "url(?_twr_=ddDisabledButton.png)";
        if (enabled && !this._editAllowed) this._editor.readOnly = true;
    },
            
    setFocus: function(focus) {          
        if (!this.isEnabled() || !this.isVisible()) return false;
        
        if (!focus) {
            var active = tw_getActiveElement();
            
            if (active != null) {
                while (active.id <= 0) active = active.parentNode;
                if (active.id == this._ddComp._id) return false;
            }
            this._ddComp.setVisible(false);
        }
        
        this._setFocusStyle(focus);        
        return arguments.callee.$.call(this, focus);
    },

    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        if (name == "borderSize") this._button.style.borderWidth = value + "px";
    },
    
    keyPressNotify: function(keyPressCombo) {        
        if (!this._ddComp.isVisible()) {
            if (keyPressCombo == "ArrowDown") {
                this.setDropDownVisible(true);
                return false;                
            } else {
                return arguments.callee.$.call(this, keyPressCombo);                
            }
        } else {
            if (keyPressCombo == "Esc" || keyPressCombo == "Enter") {
                var retval = false;
                if (keyPressCombo == "Enter") retval = this._ddComp.keyPressNotify(keyPressCombo);
                this.setDropDownVisible(false);
                return retval;
            } else {
                return this._ddComp.keyPressNotify(keyPressCombo);
            }
        }
    },
    
    setEditAllowed: function(editAllowed) {        
        this._editAllowed = editAllowed;
        if (this.isEnabled()) this.setEnabled(true); //This will trigger the proper editAllowed state.
    },
    
    _setFocusStyle: function(state) {
        if (this._buttonBorderWidth == state) return;
        this._buttonBorderWidth = state ? 1 : 0;
        this._buttonBorder.style.borderWidth = this._buttonBorderWidth + "px";
        this.setWidth(this.getWidth());
        this.setHeight(this.getHeight());
    },

    setDropDownVisible: function(state) {
        if (this._ddComp == null) return;
        
        if (state) {
            var parent = this.getParent(); 
            var offsetX = this.getX();
            var offsetY = this.getY();

            while (!(parent instanceof tw_Frame || parent instanceof tw_Dialog)) {
                offsetX += parent.getX() + parent.getOffsetX();
                offsetY += parent.getY() + parent.getOffsetY();
                parent = parent.getParent();
            }
            
            var borderSize = parent instanceof tw_Dialog ? parent.getStyle("borderSize") : 0;
            offsetX += parent.getOffsetX() - borderSize;
            offsetY += parent.getOffsetY() - borderSize;
            
            var comp = this._ddComp;
            var availableHeight = tw_getVisibleHeight() - comp._box.parentNode.offsetTop - offsetY - this._height;
            offsetY += (availableHeight < comp._height ? -comp._height : this._height);
            comp.setY(offsetY);
            comp.setX(offsetX);
            comp.setCompVisible(true);
            comp.setFocus(true);
        } else {
            this._ddComp.setCompVisible(false);
            this.setFocus(true);
        }
    },
        
    destroy: function() {        
        this._ddComp.destroy();
        this._ddComp = this._button = this._buttonBorder = null;
        arguments.callee.$.call(this);
    }
});


