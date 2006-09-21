/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//TODO: Toggle between checked radio buttons on the client-side so that it looks more visually responsive. 
var tw_RadioButton = tw_BaseCheckRadio.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["radioButton", id, containerId]);        
        this.init(-1, props);
    }
});

