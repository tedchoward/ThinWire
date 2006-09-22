/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Label = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "label", id, containerId, "text,lineHeight"]);
        var s = this._box.style;
        s.whiteSpace = "nowrap";        
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        if (this.isFocusCapable()) this.setFocus(true);     
        this.fireAction("click");
    },    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setAlignX: function(alignX) {
        this._box.style.textAlign = alignX;
    },
    
    setWrapText: function(wrapText) {
        var s = this._box.style;
        if (wrapText) {
            s.whiteSpace = "";
            s.lineHeight = "";
            this._supportLineHeight = false;
        } else {
            s.whiteSpace = "nowrap";
            this._supportLineHeight = true;
            this.setHeight(this.getHeight());
        }
    }
});

