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
        if (tw_isIE) this._box.allowTransparency = true;
        var cssText = "overflow:auto;background-color:window;position:absolute;width:100%;height:100%;z-index:1;";
        tw_Component.setCSSText(cssText, browser);
        
        var dragLayer = this._dragLayer = document.createElement("div");
        var s = dragLayer.style;
        cssText = "position:absolute;width:100%;height:100%;z-index:2;";
        tw_Component.setCSSText(cssText, dragLayer);
        tw_setLayerTransparent(dragLayer);
        this.setDragLayerVisible(false);
        
        this._box.appendChild(dragLayer);
        this._box.appendChild(browser);
        tw_WebBrowser.instances[id] = this;
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
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
        var hls = defaultStyles["hyperlink"];
        var cssText = "height:16px;font-family:" + hls.fontFamily + ";font-size:" + hls.fontSize + ";font-weight:" + hls.fontWeight +
            ";font-style:" + hls.fontStyle + ";text-decoration:" + hls.textDecoration + ";color:" + hls.color + ";";
        tw_Component.setCSSText(cssText, dragBox);
        dragBox.appendChild(document.createTextNode(this._browser.src));
        return dragBox;
    },
    
    _clickListener: tw_Component.clickListener,
    
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

