/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_WebBrowser = tw_BaseBrowserLink.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["iframe", "webBrowser", id, containerId]);
        var s = this._box.style;
        this.init(-1, props);        
        //NOTE: This must happen after the component is added to the document otherwise
        //firefox ignores it.
        s.backgroundColor = tw_COLOR_WINDOW;                
    },
    
    setLocation: function(location) {
        if (location == "") return;
        //NOTE: this line throws an error in firefox, but it still works.
        this._box.src = tw_BaseBrowserLink.expandLocation(location);
    },
    
    setFocus: function(focus) {
        var ret = this.$.setFocus.apply(this, [focus]);        
        if (ret) tw_setSelectionEnabled(focus);
        return ret;
    }    
});

