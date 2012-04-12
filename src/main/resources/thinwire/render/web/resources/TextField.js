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
var tw_TextField = tw_BaseText.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, ["div", "input", props.inputHidden ? "password" : "text"], "textField", id, containerId);
        if (props.inputHidden) this._useToolTip = false;
        delete props.inputHidden;
        this.init(-1, props);        
    },

    setInputHidden: function(inputHidden) {
        new tw_TextField(this._id, this._parent._id, {"x": this._x, "y": this._y, 
            "width": this._width, "height": this._height, "visible": this.isVisible(),
            "enabled": this._enabled, "text": this._box.value, "editMask": this._editMask,
            "alignX": this._editor.style.textAlign, "inputHidden": inputHidden});
        _parent.removeComponent(this);
    }
});

