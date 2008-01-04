/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

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
        var cssText = "top:0px;width:" + btn._width + "px;height:" + btn._height + "px;position:absolute;overflow:hidden;z-index:1;";
        tw_Component.setCSSText(cssText, iframe);
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

