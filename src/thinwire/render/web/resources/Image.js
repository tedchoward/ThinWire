/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Image = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "image", id, containerId]);
        this._fontBox = null;
        var s = this._box.style;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));        
        this.init(-1, props);        
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        if (this.isFocusCapable()) this.setFocus(true);     
        this.fireAction("click");
    },    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,    
    
    setImage: function(image) {
        this._box.style.backgroundImage = image.length > 0 ? "url(" + tw_BASE_PATH + "resources/" + image + ")" : "";
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);        
        
        //NOTE: This is not perfect, the problem here is that when an image has transparent
        //      whitespace on the top, the image does not vertically center in IE.  Technically,
        //      anytime the height is set to something shorter than the height of the image, IE
        //      requires the backgroundPosition to be adjusted.  For now, we only adjust for
        //      small images since they often require precision placement.
        if (tw_isIE) {
            if (height <= 15) {
                this._box.style.backgroundPosition = "center top";
            } else {
                this._box.style.backgroundPosition = "center center";
            }
        }
    }    
});

