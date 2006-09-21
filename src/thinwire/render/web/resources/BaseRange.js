/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_BaseRange = tw_Component.extend({
    _selection: null,
    _length: null,
    _multiplier: null,
    _vertical: false,
    _currentIndex: null,
        
    construct: function(tagName, className, id, containerId) {
        this.$.construct.apply(this, [tagName, className, id, containerId]);
        
        var selection = document.createElement("div");     
        var s = selection.style;
        s.position = "absolute";
        s.lineHeight = "0px";
        this._box.appendChild(selection);
        this._selection = this._backgroundBox = selection;
    },
    
    _recalc: function() {
        this._vertical = this.getWidth() <= this.getHeight();
        this._updateMultiplier();
    },
    
    getMax: function() {
        return this._vertical ? this.getHeight() : this.getWidth();
    },
    
    setLength: function(length) {
        if (length != null) this._length = length;
        this._updateMultiplier();
    },
    
    setCurrentIndex: function(currentIndex) {
        if (currentIndex != null) this._currentIndex = currentIndex;
        this._updateSelection();
    },
    
    getCurrentIndex: function() {
        return this._currentIndex;
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this._recalc();
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        this._recalc();
    },
    
    _updateMultiplier: function() {
        this._multiplier = (this.getMax()) / (this._length - 1);
        this._updateSelection();
    },
    
    _updateSelection: function(hprop) {
        if (this.getCurrentIndex() == null || this._multiplier == null) return;
        var s = this._selection.style;
        if (this._vertical) {
            s.top = Math.floor(this.getMax() - (this.getCurrentIndex() * this._multiplier)) + "px";
        } else {
            s[hprop] = Math.floor(this.getCurrentIndex() * this._multiplier) + "px";
        }
    }
});
