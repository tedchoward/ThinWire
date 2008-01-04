/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
var tw_TextArea = tw_BaseText.extend({
    _maxLength: -1,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, ["div", "textarea"], "textArea", id, containerId);
        this._useToolTip = false;
        tw_addEventListener(this._editor, "keypress", this._keyDownListener.bind(this));
        this.init(-1, props);
    },
    
    _keyDownListener: function(ev) {
        if (this._maxLength > 0 && this._editor.value.length >= this._maxLength) {
            tw_cancelEvent(ev);
        } else {
            return true;
        }
    },
        
    setMaxLength: function(len) {    
        this._maxLength = len;
        this._lastValue = this._editor.value;        
		this._textChange();
    },
    
    keyPressNotify: function(keyPressCombo) {
        return arguments.callee.$.call(this, keyPressCombo);
    },
    
    setEditMask: function(editMask) { },
        
    _validateInput: function(te) {
        if (this._maxLength <= 0) return; //don't validate if maxLength not defined    
        if (this._editor.value.length > this._maxLength) this._editor.value = this._editor.value.substring(0, this._maxLength);
    }
});

