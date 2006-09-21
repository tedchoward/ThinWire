/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_TextField = tw_BaseText.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, [["div", "input", props.inputHidden ? "password" : "text"], "textField", id, containerId, "editMask"]);
        delete props.inputHidden;
        this.init(-1, props);        
    },

    setInputHidden: function(inputHidden) {
        new tw_TextField(this._id, this._parent._id, {"x": parseInt(this._box.style.left), "y": parseInt(this._box.style.top), 
            "width": this._width, "height": this._height, "visible": this.isVisible(),
            "enabled": this.isEnabled(), "text": this._box.value, "editMask": this._editMask,
            "alignX": this._editor.style.textAlign, "inputHidden": inputHidden});
        _parent.removeComponent(this);
    }
});

