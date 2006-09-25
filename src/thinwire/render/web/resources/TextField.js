/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
var tw_TextField = tw_BaseText.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, [["div", "input", props.inputHidden ? "password" : "text"], "textField", id, containerId, "editMask"]);
        if (props.inputHidden) this._useToolTip = false;
        delete props.inputHidden;
        this.init(-1, props);        
    },

    setInputHidden: function(inputHidden) {
        new tw_TextField(this._id, this._parent._id, {"x": this.getX(), "y": this.getY(), 
            "width": this.getWidth(), "height": this.getHeight(), "visible": this.isVisible(),
            "enabled": this.isEnabled(), "text": this._box.value, "editMask": this._editMask,
            "alignX": this._editor.style.textAlign, "inputHidden": inputHidden});
        _parent.removeComponent(this);
    }
});

