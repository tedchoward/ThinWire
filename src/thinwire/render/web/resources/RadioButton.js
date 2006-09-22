/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_RadioButton = tw_BaseCheckRadio.extend({
    _groupId: 0,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["radioButton", id, containerId]);        
        this.init(-1, props);
    },
    
    setGroup: function(groupId) {
        if (this._groupId != 0) {
            var ary = tw_RadioButton.groups[this._groupId];
            
            for (var i = ary.length; --i >= 0;) {
                if (ary[i] === this) {
                    ary.splice(i, 1);
                    break;
                }
            }
            
            if (ary.length <= 0) delete tw_RadioButton.groups[this._groupId];
        }
        
        this._groupId = groupId;

        if (groupId != 0) {
            var ary = tw_RadioButton.groups[groupId];
            if (ary == undefined) tw_RadioButton.groups[groupId] = ary = [];
            ary.push(this);
        }
    },
    
    setChecked: function(checked) {
        if (checked && this._groupId != 0) {
            var ary = tw_RadioButton.groups[this._groupId];

            for (var i = ary.length; --i >= 0;) {
                if (ary[i].isChecked()) ary[i].setChecked(false, true);
            }
        }
        
        this.$.setChecked.apply(this, [checked, true]);
    },
    
    destroy: function() {
        this.setGroup(0);
        this.$.destroy.apply(this, []);
    }
});

tw_RadioButton.groups = {};

