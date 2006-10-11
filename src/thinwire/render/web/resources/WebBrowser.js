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
var tw_WebBrowser = tw_BaseBrowserLink.extend({
    
    construct: function(id, containerId, props) {
        arguments.callee.$.construct.call(this, "iframe", "webBrowser", id, containerId);
        this._box.style.overflow = "auto";
        this._fontBox = null;
        this._borderBox = null;
        var s = this._box.style;
        if (tw_isIE) this._box.allowTransparency = true;
        this.init(-1, props);
        s.backgroundColor = "window";
    },
    
    setLocation: function(location) {
        //NOTE: this line throws an error in firefox, but it still works.
        if (location != "") location = tw_Component.expandUrl(location);
        this._box.src = location;
    },
    
    setStyle: function(name, value) {
        if (name != "backgroundColor") arguments.callee.$.setStyle.call(this, name, value);
    }
});

