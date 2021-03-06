/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
var tw_RadioButton = tw_BaseCheckRadio.extend({
    _groupId: 0,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "radioButton", id, containerId);        
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
    
    setChecked: function(checked, sendEvent) {
        if (checked && this._groupId != 0) {
            var ary = tw_RadioButton.groups[this._groupId];

            for (var i = ary.length; --i >= 0;) {
                if (ary[i].isChecked()) ary[i].setChecked(false, sendEvent);
            }
        }
        
        arguments.callee.$.call(this, checked, sendEvent);
    },
    
    destroy: function() {
        this.setGroup(0);
        arguments.callee.$.call(this);
    }
});

tw_RadioButton.groups = {};

