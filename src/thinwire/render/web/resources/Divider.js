/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Divider = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "divider", id, containerId]);
        var s = this._box.style;
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));

        var tagLine = document.createElement("div");
        var s = tagLine.style;
        s.position = "absolute";
        s.border = "1px solid";
        s.borderColor = "buttonshadow buttonhighlight buttonhighlight buttonshadow";
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
                s.top = Math.floor(this._height / 2) + "px";
                s.width = this._width + "px";
                s.height = "0px";
            } else if (this._width < this._height) {
                s.left = Math.floor(this._width / 2) + "px"
                s.top = "0px";
                s.width = "0px";
                s.height = this._height + "px";                
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


