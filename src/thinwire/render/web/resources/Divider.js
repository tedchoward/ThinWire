/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Divider = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "divider", id, containerId]);
        this._fontBox = null;
        var s = this._box.style;
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));

        var tagLine = this._borderBox = document.createElement("div");
        var s = tagLine.style;
        s.position = "absolute";
        s.lineHeight = "0px";        
        this._box.appendChild(tagLine);
        this.init(-1, props);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        if (this.isFocusCapable()) this.setFocus(true);     
        this.fireAction("click");
    },    
         
    _recalc: function() {
        if (this._width != -1 && this._height != -1) {
            var s = this._box.firstChild.style;
            
            if (this._width >= this._height) {
                s.left = "0px";
                s.top = Math.floor((this._height - this.getStyle("borderSize") * 2) / 2) + "px";
                var width = this._width - this._borderSizeSub;
                if (width < 0) width = 0;
                s.width = width + "px";
                s.height = "0px";
            } else if (this._width < this._height) {
                s.left = Math.floor((this._width - this.getStyle("borderSize") * 2) / 2) + "px"
                s.top = "0px";
                s.width = "0px";
                var height = this._height - this._borderSizeSub;
                if (height < 0) height = 0;
                s.height = height + "px";                
            }
        }
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this._recalc();
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);        
        this._recalc();
    }        
});


