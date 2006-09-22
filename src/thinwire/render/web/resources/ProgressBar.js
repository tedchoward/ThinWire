/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_ProgressBar = tw_BaseRange.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "progressBar", id, containerId]);
        this._backgroundBox = this._box;
        this._selection.style.left = "0px";
        this._selection.style.backgroundColor = tw_COLOR_ACTIVECAPTION; 
        this.init(-1, props);
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
