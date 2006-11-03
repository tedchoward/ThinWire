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
var tw_ProgressBar = tw_BaseRange.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "progressBar", id, containerId);
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        this._backgroundBox = this._box;
        this._fontBox = this._selection;
        this._selection.style.left = "0px";
        this.init(-1, props);
    },
    
    _recalc: function() {
        if (this.getWidth() != -1 && this.getHeight() != -1) {
            var s = this._selection.style;
            if (this.getWidth() > this.getHeight()) {
                s.top = "0px";
                this._selection.style.height = this.getHeight() + "px";
            } else {
                this._selection.style.width = this.getWidth() + "px";
            }
        }
        arguments.callee.$.call(this);
    },
    
    _clickListener: tw_Component.clickListener,    
    
    _updateSelection: function() {
        arguments.callee.$.call(this, "width");
        if (this._vertical) {
            var s = this._selection.style;
            s.height = this.getHeight() - parseInt(s.top) + "px";
        }
    },
    
    setStyle: function(name, value) {
        if (name == "fontColor") {
            this._selection.style.backgroundColor = value;
        } else {
            arguments.callee.$.call(this, name, value);
        }
    }
});
