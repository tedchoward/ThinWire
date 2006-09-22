/*
 #LICENSE_HEADER#
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
        this.$.construct.apply(this, [["div", "input", "text"], "dropDownGridBox", id, containerId, "editMask"]);
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
            this._focusListener();
        }
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        width = this._buttonWidth;
        if (!tw_sizeIncludesBorders) width -= this._buttonBorderWidth * 2;        
        if (width < 0) width = 0;
        this._buttonBorder.style.width = width + "px";
        width -= this._borderSizeSub;
        if (width < 0) width = 0;        
        this._button.style.width = width + "px"; 
    },

    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        height -= this._borderSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._buttonBorderWidth * 2;
        if (height < 0) height = 0;        
        this._buttonBorder.style.height = height + "px";
        height -= this._borderSizeSub;
        if (height < 0) height = 0;        
        this._button.style.height = height + "px"; 
    },
    
    setVisible: function(visible) {
        this.$.setVisible.apply(this, [visible]);
        if (!visible) this.setDropDownVisible(false);
    },

    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);
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
        return this.$.setFocus.apply(this, [focus]);
    },

    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        if (name == "borderSize") this._button.style.borderWidth = value + "px";
    },
    
    keyPressNotify: function(keyPressCombo) {        
        if (!this._ddComp.isVisible()) {
            if (keyPressCombo == "ArrowDown") {
                this.setDropDownVisible(true);
                return false;                
            } else {
                return this.$.keyPressNotify.apply(this, [keyPressCombo]);                
            }
        } else {
            return this._ddComp.keyPressNotify(keyPressCombo);
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
            comp.setVisible(true);
            comp.setFocus(true);
        } else {
            this._ddComp.setVisible(false);
        }
    },
        
    destroy: function() {        
        this._ddComp.destroy();
        this._ddComp = this._button = this._buttonBorder = null;
        this.$.destroy.apply(this, []);
    }
});


