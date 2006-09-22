/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_BaseBrowserLink = tw_Component.extend({    
    construct: function(tagName, className, id, containerId, support) {
        this.$.construct.apply(this, [tagName, className, id, containerId, support]);        
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this)); 
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));        
    },
    
    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);
        this._box.disabled = !enabled;
    }   
});

tw_BaseBrowserLink.expandLocation = function(location) {
    if (location.indexOf("%SYSROOT%") == 0) location = tw_BASE_PATH + "resources/" + location.substring(9);
    return location;
} 

