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
//TODO: In firefox, when you click a hyperlink, the focus gain / focus lose event gets stuck
//in a loop.
var tw_Hyperlink = tw_BaseBrowserLink.extend({
    _fontColor: null,
    _location: "",
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "a", "hyperlink", id, containerId);
        this._box.appendChild(document.createTextNode(""));
        
        var s = this._box.style;
        s.whiteSpace = "nowrap";
        s.overflow = "hidden";        
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: function(ev) {
        this._superClick(ev);
        tw_Hyperlink.openLocation(this._location, "hl" + this._id);
        return false;
    },
    
    _superClick: tw_Component.clickListener,
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setText: tw_Component.setText,
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        if (name == "color") this._fontColor = value;
    },
    
    setLocation: function(location) {
        this._location = location;
        this._box.href = "javascript:void('" + tw_Component.expandUrl(this._location) + "')";
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._box.style.cursor = enabled ? "hand" : "default";
        this._fontBox.style.color = enabled ? this._fontColor : tw_COLOR_GRAYTEXT; 
    }
});

tw_Hyperlink.openLocation = function(location, target) {
    if (location.length > 0) window.open(tw_Component.expandUrl(location), target);
};

