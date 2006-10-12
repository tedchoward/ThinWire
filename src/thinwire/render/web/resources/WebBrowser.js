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
    _browser: null,
    _dragLayer: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.construct.call(this, "div", "webBrowser", id, containerId);
        this._fontBox = null;
        
        var browser = this._browser = document.createElement("iframe"); 
        browser.frameBorder = "0";
        var s = browser.style;
        s.overflow = "auto";
        if (tw_isIE) this._box.allowTransparency = true;
        s.backgroundColor = "window";
        s.position = "absolute";
        s.width = "100%";
        s.height = "100%";     
        s.zIndex = "1";
        
        var dragLayer = this._dragLayer = document.createElement("div");
        var s = dragLayer.style;
        s.position = "absolute";
        s.width = "100%";
        s.height = "100%";
        s.zIndex = "2";
        tw_setLayerTransparent(dragLayer);
        this.setDragLayerVisible(false);
        
        this._box.appendChild(dragLayer);
        this._box.appendChild(browser);
        tw_WebBrowser.instances[id] = this;
        this.init(-1, props);
    },
    
    setLocation: function(location) {
        //NOTE: this line throws an error in firefox, but it still works.
        if (location != "") location = tw_Component.expandUrl(location);
        this._browser.src = location;
        this._browser.style.display = location != "" ? "block" : "none"; 
    },
    
    setDragLayerVisible: function(state) {
        this._dragLayer.style.display = state ? "block" : "none";
    },
    
    destroy: function() {
        delete tw_WebBrowser.instances[this._id];
        this._browser = this._dragLayer = null;
        arguments.callee.$$.call(this);
    }
});

tw_WebBrowser.instances = {};

