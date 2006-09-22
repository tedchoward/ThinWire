/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//TODO: In firefox, when you click a hyperlink, the focus gain / focus lose event gets stuck
//in a loop.
var tw_Hyperlink = tw_BaseBrowserLink.extend({
    _location: "",
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["a", "hyperlink", id, containerId, "text"]);
        var s = this._box.style;
        s.whiteSpace = "nowrap";
        s.overflow = "hidden";        
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return false;
        this.setFocus(true);        
        this.fireAction("click");
        tw_Hyperlink.openLocation(this._location, "hl" + this._id);
        return false;
    },    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setLocation: function(location) {
        this._location = location;
        this._box.href = "javascript:void('" + tw_BaseBrowserLink.expandLocation(this._location) + "')";
    },

    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);
        this._box.style.color = enabled ? "" : tw_COLOR_GRAYTEXT;
    }
});

tw_Hyperlink.openLocation = function(location, target) {
    if (location.length > 0) window.open(tw_BaseBrowserLink.expandLocation(location), target);
};

