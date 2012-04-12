/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
var tw_Image = tw_Component.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "img", "image", id, containerId);
        this._fontBox = null;
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));        
        this.init(-1, props);        
    },
    
    _clickListener: tw_Component.clickListener,
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,    
    
    setImage: function(image) {
        this._box.src = tw_Component.expandUrl(image);
    }
});

