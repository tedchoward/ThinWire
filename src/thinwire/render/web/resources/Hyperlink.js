/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                email: info@thinwire.com    ph: +1 (888) 644-6405
                            http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
//TODO: In firefox, when you click a hyperlink, the focus gain / focus lose event gets stuck
//in a loop.
var tw_Hyperlink = tw_BaseBrowserLink.extend({
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
    
    setLocation: function(location) {
        this._location = location;
        this._box.href = "javascript:void('" + tw_Component.expandUrl(this._location) + "')";
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._box.style.cursor = enabled ? "pointer" : "default";
    }
});

tw_Hyperlink.openLocation = function(location, target) {
    if (location.length > 0) window.open(tw_Component.expandUrl(location), target);
};

