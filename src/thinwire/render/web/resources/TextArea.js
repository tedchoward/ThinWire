/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_TextArea = tw_BaseText.extend({
    _maxLength: -1,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, [["div", "textarea"], "textArea", id, containerId]);
        this._useToolTip = false;
        this.init(-1, props);
    },
        
    setMaxLength: function(len) {    
        this._maxLength = len;
        this._lastValue = this._editor.value;        
        this._validateInput();
        this._textStateChange(false, true);
    },
    
    keyPressNotify: function(keyPressCombo) {
        return this.$.keyPressNotify.apply(this, [keyPressCombo]);
    },
        
    _validateInput: function(te) {
        if (this._maxLength <= 0) return; //don't validate if maxLength not defined    
        if (this._editor.value.length > this._maxLength) this._editor.value = this._editor.value.substring(0, this._maxLength);
    }
});

