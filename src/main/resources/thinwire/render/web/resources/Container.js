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
//NOTE: Horizontal scrolling in Opera doesn't appear to work correctly when the scroll type is AS_NEEDED
var tw_Container = tw_BaseContainer.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "container", id, containerId);
        tw_addEventListener(this.getClickBox(), ["click", "dblclick"], this._containerClickListener.bind(this)); 
        this._fontBox = null;
        this.init(-1, props);
    },
    
    _containerClickListener: tw_BaseContainer.containerClickListener
});

