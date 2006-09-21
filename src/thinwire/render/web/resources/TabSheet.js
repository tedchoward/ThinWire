/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
//TODO: No proper support for tabsheet setVisible or setEnabled.
var tw_TabSheet = tw_BaseContainer.extend({
    _text: null,
    _image: null,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["tabSheet", id, containerId]);
        var s = this._box.style;
        s.background = "buttonface";
        s.top = "0px";
        s.left = "0px";
        s.zIndex = "0";
        
        var tabIndex = props.tabIndex;
        delete props.tabIndex;
        this.init(tabIndex, props);    
    },
    
    setText: function(text, tab) {        
        if (tab == null) {
            this._text = text;
            tab = document.getElementById("tab" + this._id);
        }
        
        if (tab == null) return;
        tab.replaceChild(document.createTextNode(this._text), tab.lastChild);    
    },
    
    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        
        var tab = document.getElementById("tab" + this._id);
        
        if (tab != null) {
            this._borderBox = this._backgroundBox = this._fontBox = tab;    
            this.$.setStyle.apply(this, [name, value, true]);
            this._borderBox = this._backgroundBox = this._fontBox = this._box;
        }
    },
            
    setImage: function(image, tab) {
        if (tab == null) {
            this._image = image;
            tab = document.getElementById("tab" + this._id);
        }
        
        if (tab == null) return;
        var s = tab.firstChild.style;
        
        if (this._image.length > 0) {
            s.backgroundImage = "url(" + tw_BASE_PATH + "/resources/" + this._image + ")";
            s.display = "block";
        } else {
            s.backgroundImage = "";
            s.display = "none";
        }
    }
});
