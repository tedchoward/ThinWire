/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_CheckBox = tw_BaseCheckRadio.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["checkBox", id, containerId]);
        this.init(-1, props);
    }
});

