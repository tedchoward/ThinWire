/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
var tw_TextArea = tw_BaseText.extend({
    _maxLength: -1,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, [["div", "textarea"], ["textArea", "textAreaEditor"], id, containerId]);
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
        //return keyPressCombo == "Enter" ? false : this.$.keyPressNotify.apply(this, [keyPressCombo]);
        return this.$.keyPressNotify.apply(this, [keyPressCombo]);
    },
        
    _validateInput: function(te) {
        if (this._maxLength <= 0) return; //don't validate if maxLength not defined    
        if (this._editor.value.length > this._maxLength) this._editor.value = this._editor.value.substring(0, this._maxLength);
    }
});

