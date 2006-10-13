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

