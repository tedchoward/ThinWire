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
//TODO: In firefox, when you click a hyperlink, the focus gain / focus lose event gets stuck
//in a loop.
var tw_Hyperlink = tw_BaseBrowserLink.extend({
    _location: "",
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["a", "hyperlink", id, containerId, "text"]);
        var s = this._box.style;
        s.border = "0px";
        s.fontFamily = tw_FONT_FAMILY;
        s.fontSize = "8pt";
        s.whiteSpace = "nowrap";        
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return false;
        this.setFocus(true);        
        this.fireAction("click");
        tw_Hyperlink.openLocation(this._location, "hl" + this._id);
        return false;
    },    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setLocation: function(location) {
        this._location = location;
        this._box.href = "javascript:void('" + tw_BaseBrowserLink.expandLocation(this._location) + "')";
    },

    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);
        this._box.style.color = enabled ? "" : tw_COLOR_GRAYTEXT;
    }
});

tw_Hyperlink.openLocation = function(location, target) {
    if (location.length > 0) window.open(tw_BaseBrowserLink.expandLocation(location), target);
};

