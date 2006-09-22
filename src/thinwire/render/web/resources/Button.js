/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//TODO: In opera, when tabbing to a button, the text gets highlighted for some reason.
//TODO: When a button or component is disabled it should be excluded from the tabbing order.
//TODO: focus gain / lose does not work in firefox.
var tw_Button = tw_Component.extend({
    _borderType: null,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "button", id, containerId]);
        var s = this._box.style;
        s.borderStyle = "solid";
        s.borderColor = tw_COLOR_WINDOWFRAME;                        
        s.borderWidth = "0px";
        
        var border = this._borderBox = this._focusBox = document.createElement("div");
        var s = border.style;
        s.cursor = "default";
        s.overflow = "hidden";
        s.whiteSpace = "nowrap";
        s.paddingLeft = tw_Button.textPadding + "px";
        s.paddingRight = tw_Button.textPadding + "px";    
        s.textAlign = "center";
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center left";
        
        border.appendChild(document.createTextNode(""));        
        this._box.appendChild(border);
                
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));    
        tw_addEventListener(this._box, ["mouseup", "mouseout"], this._mouseUpListener.bind(this));    
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
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
    _clickListener: function(ev) {
        if (!this.isEnabled()) return;
        this.setFocus(true);  
        this.fireAction("click");
    },

    setStyle: function(name, value) {        
        if (name == "borderSize") {
            this.$.setStyle.apply(this, [name, value]);
        } else {
            if (name == "borderType") this._borderType = value;
            this.$.setStyle.apply(this, [name, value]);
        }
    },
    
    fireClick: function() { this._clickListener(); },

    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        var s = this._borderBox.style;
        width -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) width -= parseInt(s.paddingLeft) + parseInt(s.paddingRight) + this._borderSizeSub;
        if (width < 0) width = 0;
        s.width = width + "px";
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        var s = this._borderBox.style;
        height -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._borderSizeSub;
        if (height < 0) height = 0;
        s.height = s.lineHeight = height + "px";
    },
    
    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);        
        this._fontBox.color = enabled ? tw_COLOR_BUTTONTEXT : tw_COLOR_GRAYTEXT;
    },
    
    setFocus: function(focus) {
        if (!this.isEnabled() || !this.isVisible()) return false;
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
        var b = this._borderBox;         
        b.replaceChild(document.createTextNode(text), b.firstChild);
    },
    
    setImage: function(image) {
        var s = this._borderBox.style;
    
        if (image.length > 0) {
            s.backgroundImage = "url(" + tw_BASE_PATH + "/resources/" + image + ")";
            s.paddingLeft = tw_Button.imagePadding + "px";
        } else {
            s.backgroundImage = "";
            s.paddingLeft = tw_Button.textPadding + "px";
        }
        
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
