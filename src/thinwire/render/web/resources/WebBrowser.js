/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_WebBrowser = tw_BaseBrowserLink.extend({
    _location: "",
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["iframe", "webBrowser", id, containerId]);
        this._box.style.overflow = "auto";        
        this._fontBox = null;
        this._setLocationTimer = this._setLocationTimer.bind(this);
        var s = this._box.style;
        this.init(-1, props);        
    },
    
    setLocation: function(location) {
        this._location = location;
        setTimeout(this._setLocationTimer, 100);
    },
    
    _setLocationTimer: function() {
        var location = this._location;
        if (location != "") location = tw_BaseBrowserLink.expandLocation(location);
        //NOTE: this line throws an error in firefox, but it still works.
        this._box.src = location;
    }
});

