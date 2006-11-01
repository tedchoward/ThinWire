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
var tw_Label = tw_Component.extend({
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "label", id, containerId);
        this._box.appendChild(document.createTextNode(""));
        
        var s = this._box.style;
        s.verticalAlign = "middle";
        s.whiteSpace = "nowrap";        
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        this.init(-1, props);
    },
    
    _clickListener: tw_Component.clickListener,    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,
    
    setText: tw_Component.setText,
    
    setAlignX: function(alignX) {
        this._box.style.textAlign = alignX;
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        if (this._box.style.whiteSpace == "nowrap") this._box.style.lineHeight = this._box.style.height;
    },
    
    setWrapText: function(wrapText) {
        var s = this._box.style;

        if (wrapText) {
            s.whiteSpace = "";
            s.lineHeight = "";
        } else {
            s.whiteSpace = "nowrap";
            this.setHeight(this.getHeight());
        }
    }
});

