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
        if (this._width != -1 && this._height != -1) {
            var s = this._selection.style;
            if (this._width > this._height) {
                s.top = "0px";
                this._selection.style.height = this._height + "px";
            } else {
                this._selection.style.width = this._width + "px";
            }
        }
        arguments.callee.$.call(this);
    },
    
    _clickListener: tw_Component.clickListener,    
    
    _updateSelection: function() {
        arguments.callee.$.call(this, "width");
        if (this._vertical) {
            var s = this._selection.style;
            s.height = this._height - parseInt(s.top) + "px";
        }
    },
    
    setStyle: function(name, value) {
        if (name == "color") {
            this._fontColor = value;
            this._selection.style.backgroundColor = this._enabled ? this._fontColor : tw_COLOR_INACTIVECAPTION;
        } else {
            arguments.callee.$.call(this, name, value);
        }
    },
    
    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._selection.style.backgroundColor = enabled ? this._fontColor : tw_COLOR_INACTIVECAPTION; 
    }
});
