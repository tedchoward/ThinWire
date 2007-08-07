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
var tw_FileChooser = Class.extend({
    _tf: null,
    _btn: null,
    _tfDestroy: null,
    _iframe: null,
    
    construct: function(buttonId, tfId) {
        var btn = this._btn = tw_Component.instances[buttonId];
        var tf = this._tf = tw_Component.instances[tfId];
        this._tfDestroy = tf.destroy;
        tf.destroy = this.destroy.bind(this);
        tf._fileChooser = this;
        var iframe = this._iframe = document.createElement("iframe");
        iframe.scrolling = "no";
        iframe.onreadystatechange = iframe.onload = this._onload.bind(this);
        iframe.src = "?_twr_=FileUploadPage.html";
        iframe.frameBorder = "0";
        s = iframe.style;
        s.top = "0px";
        s.width = btn._width + "px";
        s.height = btn._height + "px";
        s.position = "absolute";
        s.overflow = "hidden";
        s.zIndex = "1";
        tw_setOpacity(iframe, 0);
        btn._box.appendChild(iframe);
    },
    
    _onload: function() {
        if (tw_isIE && this._iframe.readyState != "complete") return;
        var input = this._iframe.contentWindow.document.getElementsByTagName("input")[0];
        if (input != null) {
            input.name = "file";
            input.onchange = this._onchange.bind(this);
            this._tf.setText(input.value);
        }
    },
    
    _onchange: function() {
        var input = this._iframe.contentWindow.document.getElementsByTagName("input")[0];
        this._tf.setText(input.value);
        this._tf.firePropertyChange("text", input.value);
    },
    
    destroy: function() {
        if (this._tf != null) {
            delete this._tf._fileChooser;
            this._tf.destroy = this._tfDestroy;
            if (this._tf._inited) this._tf.destroy();
            this._iframe = this._tf = this._tfDestroy = null;
        }
    }
});

tw_FileChooser.newInstance = function(buttonId, tfId) {
    new tw_FileChooser(buttonId, tfId);
}

tw_FileChooser.submit = function(tfId) {
    var tf = tw_Component.instances[tfId];
    
    if (tf != undefined && tf._fileChooser != null && tf._fileChooser._iframe != null) {
        tf._fileChooser._iframe.contentWindow.document.getElementById("uploadForm").submit();
    }
}

