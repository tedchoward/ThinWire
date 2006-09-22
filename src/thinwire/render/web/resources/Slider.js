/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Slider = tw_BaseRange.extend({
    _cursorDrag: null,
    _max: null,
    _clickListener: null,
    _line: null,
   
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "slider", id, containerId]);
        var s = this._box.style;
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        
        var line = document.createElement("div");
        var s = line.style;
        s.position = "absolute";
        s.lineHeight = "0px";
        
        var ds = tw_Component.defaultStyles["Divider"];
        s.borderWidth = ds.borderSize + "px";
        s.borderStyle = ds.borderType;
        s.borderColor = tw_Component.getIEBorderColor(ds.borderColor);
        this._box.insertBefore(line, this._selection);
        this._line = line;
        
        this._borderBox = this._selection;
        this._cursorDrag = new tw_DragHandler(this._selection, this._cursorDragListener.bind(this));
        
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        tw_setFocusCapable(this._box, true);
        this.init(-1, props);
    },
    
    _cursorDragListener: function(ev) {
        var s = this._selection.style;
        if (ev.type == 1) {
            var pos_prop = this._vertical ? "top" : "left";
            var change_prop = this._vertical ? "changeInY" : "changeInX";
            var x = parseInt(s[pos_prop]) + ev[change_prop];
            if (x < 0) x = 0;
            if (x > this.getMax()) x = this.getMax();
            s[pos_prop] = x + "px";
        } else if (ev.type == 2) {
            var newValue = 0;
            if (this._vertical) {
                newValue = Math.round((this.getMax() - parseFloat(s.top)) / this._multiplier);
            } else {
                newValue = Math.round(parseFloat(s.left) / this._multiplier);
            }
            this.setCurrentIndex(newValue);
            this.firePropertyChange("currentIndex", newValue);
        }
    },
    
    _recalc: function() {
        if (this.getWidth() != -1 && this.getHeight() != -1) {
            
            if (this.getWidth() > this.getHeight()) {
                var l = this._line.style;
                l.left = "0px";
                l.top = Math.floor(this.getHeight() / 2) + "px";
                l.width = this.getWidth() + "px";
                l.height = "0px";
                
                var c = this._selection.style;
                c.top = "0px";
                c.width = "5px";
                c.height = this.getHeight() > this.getStyle("borderSize") ? this.getHeight() - (this.getStyle("borderSize") * 2) + "px" : "0px";
                
                this._max = this.getWidth() - (parseInt(c.width) + (this.getStyle("borderSize") * 2));
            } else {
                
                var l = this._line.style;
                l.left = Math.floor(this.getWidth() / 2) + "px";
                l.top = "0px";
                l.width = "0px";
                l.height = this.getHeight() + "px";
                
                var c = this._selection.style;
                c.left = "0px";
                c.width = this.getWidth() > this.getStyle("borderSize") ? this.getWidth() - (this.getStyle("borderSize") * 2) + "px" : "0px";
                c.height = "5px";
                
                this._max = this.getHeight() - (parseInt(c.height) + this.getStyle("borderSize") * 2);
            }
            this.$._recalc.apply(this, []);
        }
    },
    
    _updateSelection: function() {
        this.$._updateSelection.apply(this, ["left"]);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
       this.setFocus(true);
    },
    
    getMax: function() {
        return this._max;
    },
    
    setEnabled: function(enabled) {
       if (enabled == this.isEnabled()) return;
       this.$.setEnabled.apply(this, [enabled]);
       tw_setFocusCapable(this._box, enabled);
    },
    
    keyPressNotify: function(keyPressCombo) {
        if ((keyPressCombo == "ArrowDown" && this._vertical) || (keyPressCombo == "ArrowLeft" && !this._vertical)) {
            if (this.getCurrentIndex() > 0) {
                this.setCurrentIndex(this.getCurrentIndex() - 1);
                this.firePropertyChange("currentIndex", this.getCurrentIndex());
            }
            return false;
        } else if ((keyPressCombo == "ArrowUp" && this._vertical) || (keyPressCombo == "ArrowRight" && !this._vertical)) {
            if (this.getCurrentIndex() < this._length - 1) {
                this.setCurrentIndex(this.getCurrentIndex() + 1);
                this.firePropertyChange("currentIndex", this.getCurrentIndex());
            }
            return false;
        } else {
            return this.$.keyPressNotify.apply(this, [keyPressCombo]);
        }
    }
});