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
var tw_Hyperlink = tw_BaseBrowserLink.extend({
    _location: "",
    _target: "",
    _visibleChrome: true,
    _resizeAllowed: true,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "a", "hyperlink", id, containerId);
        this._box.appendChild(document.createTextNode(""));
        
        var s = this._box.style;
        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;white-space:nowrap;";
        tw_Component.setCSSText(cssText, this._box);
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: function(ev) {
        this._superClick(ev);
        tw_Hyperlink.openLocation(this._location, this._target, this._visibleChrome, this._resizeAllowed);
        return false;
    },
    
    _superClick: tw_Component.clickListener,
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setText: tw_Component.setText,

    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        this._box.style.lineHeight = this._box.style.height;
    },
    
    setLocation: function(location) {
        this._location = location;
        this._box.href = "javascript:void('" + tw_Component.expandUrl(this._location) + "')";
    },

    setTarget: function(target) {
        this._target = target;
    },

    setVisibleChrome: function(visibleChrome) {
        this._visibleChrome = visibleChrome;
    },

    setResizeAllowed: function(resizeAllowed) {
        this._resizeAllowed = resizeAllowed;
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._box.style.cursor = enabled ? "pointer" : "default";
    }
});

tw_Hyperlink.openLocation = function(location, target, visibleChrome, resizeAllowed) {
    if (location.length > 0) {
        var vc = visibleChrome ? "1" : "0";
        var chromeOptions = "scrollbars=1,menubar=" + vc + ",toolbar=" + vc + ",location=" + vc + ",directories=" + vc + 
            ",status=" + vc + ",resizable=" + (resizeAllowed ? "1" : "0");
        window.open(tw_Component.expandUrl(location), target, chromeOptions);
    }
};
