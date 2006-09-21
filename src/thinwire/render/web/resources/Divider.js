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
var tw_Divider = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "divider", id, containerId]);
        var s = this._box.style;
        s.backgroundColor = "transparent";
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));

        var tagLine = document.createElement("div");
        var s = tagLine.style;
        s.position = "absolute";
        s.border = "1px solid";
        s.borderColor = "buttonshadow buttonhighlight buttonhighlight buttonshadow";
        s.lineHeight = "0px";        
        this._box.appendChild(tagLine);
        this.init(-1, props);
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        if (this.isFocusCapable()) this.setFocus(true);     
        this.fireAction("click");
    },    
         
    _recalc: function() {
        if (this._width != -1 && this._height != -1) {
            var s = this._box.firstChild.style;
            
            if (this._width >= this._height) {
                s.left = "0px";
                s.top = Math.floor(this._height / 2) + "px";
                s.width = this._width + "px";
                s.height = "0px";
            } else if (this._width < this._height) {
                s.left = Math.floor(this._width / 2) + "px"
                s.top = "0px";
                s.width = "0px";
                s.height = this._height + "px";                
            }
        }
    },
    
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this._recalc();
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);        
        this._recalc();
    }        
});


