/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
var tw_WebBrowser = tw_BaseBrowserLink.extend({
    _browser: null,
    _dragLayer: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "webBrowser", id, containerId);
        this._fontBox = null;
        if (tw_isIE) this._IELoadListener = tw_WebBrowser._IELoadListener.bind(this);
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
        if (this._IELoadListener != null) this._browser.onreadystatechange = location.indexOf(tw_APP_URL) == 0 ? this._IELoadListener : null;
        this._browser.src = location;
        this._browser.style.display = location != "" ? "block" : "none";
    },
    
    setDragLayerVisible: function(state) {
        this._dragLayer.style.display = state ? "block" : "none";
    },
    
    getDragBox: function() {
        var dragBox = document.createElement("div");
        var s = dragBox.style;
        //s.position = "absolute";
        //s.textAlign = "center";
        s.height = "16px";
        //s.backgroundColor = tw_COLOR_WINDOW;
        var hls = defaultStyles["hyperlink"];
        s.fontFamily = hls.fontFamily;
        s.fontSize = hls.fontSize;
        s.fontWeight = hls.fontWeight;
        s.fontStyle = hls.fontStyle;
        s.textDecoration = hls.textDecoration;
        s.color = hls.color;
        dragBox.appendChild(document.createTextNode(this._browser.src));
        return dragBox;
    },
    
    destroy: function() {
        delete tw_WebBrowser.instances[this._id];
        this._browser = this._dragLayer = null;
        arguments.callee.$.call(this);
    }
});

tw_WebBrowser._IELoadListener = function() {
    if (this._browser.readyState != "complete") return;
    tw_Component.styleScrollBars(this._browser.contentWindow.document.body);
};

tw_WebBrowser.instances = {};

