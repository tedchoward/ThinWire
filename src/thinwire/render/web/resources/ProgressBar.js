/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */

var tw_ProgressBar = tw_BaseRange.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "progressBar", id, containerId]);
        
        this._selection.style.left = "0px";

        this.setStyle("borderSize", 2);
        this.setStyle("borderType", "inset");
        this.setStyle("borderColor", tw_borderColor);
        
        this.init(-1, props);
        this.setStyle("backgroundColor", "activecaption");
        this._box.style.backgroundColor = tw_COLOR_WINDOW;
    },
    
    _recalc: function() {
        if (this.getWidth() != -1 && this.getHeight() != -1) {
            var s = this._selection.style;
            if (this.getWidth() > this.getHeight()) {
                s.top = "0px";
                this._selection.style.height = this.getHeight() + "px";
            } else {
                this._selection.style.width = this.getWidth() + "px";
            }
        }
        this.$._recalc.apply(this, []);
    },
    
    _updateSelection: function() {
        this.$._updateSelection.apply(this, ["width"]);
        if (this._vertical) {
            var s = this._selection.style;
            s.height = this.getHeight() - parseInt(s.top) + "px";
        }
    }
});
