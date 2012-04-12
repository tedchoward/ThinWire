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
var tw_BaseBrowserLink = tw_Component.extend({    
    construct: function(tagName, className, id, containerId, support) {
        arguments.callee.$.call(this, tagName, className, id, containerId, support);        
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this)); 
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));        
    },
    
    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._box.disabled = !enabled;
    }   
});

