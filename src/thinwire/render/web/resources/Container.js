/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//NOTE: Horizontal scrolling in Opera doesn't appear to work correctly when the scroll type is AS_NEEDED
var tw_Container = tw_BaseContainer.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["container", id, containerId]);
        var s = this._box.style;
        s.backgroundColor = tw_COLOR_THREEDFACE;
        this.init(-1, props);
    }
});

