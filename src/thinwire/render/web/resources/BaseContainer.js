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
//TODO: Provide a way to determine the base container
var tw_BaseContainer = tw_Component.extend({
    _container: null,
    _children: null,
    _offsetX: 0,
    _offsetY: 0,
    
    construct: function(className, id, containerId) {
        arguments.callee.$.construct.call(this, "div", className, id, containerId);
        this._fontBox = null;
        this._container = this._box;
        this._children = [];
    },
    
    getContainer: function() {
        return this._container;
    },

    getOffsetX: function() {
        return (this._box == this._borderBox ? this.getStyle("borderSize") : 0) + this._offsetX - this._box.scrollLeft;
    },

    getOffsetY: function() {
        return (this._box == this._borderBox ? this.getStyle("borderSize") : 0) + this._offsetY - this._box.scrollTop;
    },
    
    setScroll: function(scrollCode) {
        var overflow;
        
        switch (scrollCode) {
            case 1: overflow = "auto"; break;
            case 2: overflow = "scroll"; break;
            default: overflow = "hidden";
        }

        this._container.style.overflow = overflow;
    },
        
    addComponent: function(insertAtIndex, comp) {
        if (insertAtIndex == -1 || insertAtIndex >= this._children.length) {
            this._container.appendChild(comp._box);
            this._children.push(comp);
            comp._parentIndex = this._children.length - 1;
        } else {
            this._container.insertBefore(comp._box, this._children[insertAtIndex]._box);
            this._children.splice(insertAtIndex, 0, comp);
            
            for (var i = this._children.length; --i > insertAtIndex;) {
                this._children[i]._parentIndex++;
            }
        }        
    },
    
    removeComponent: function(componentId) {        
        for (var i = this._children.length; --i >= 0;) {
            var child = this._children[i];
            
            if (child._id == componentId) {
                this._container.removeChild(child._box);                
                this._children.splice(i, 1);

                for (var cnt = this._children.length; i < cnt; i++) {
                    this._children[i]._parentIndex--;
                }
                
                child.destroy();
                break;
            }
        }
    },
    
    destroy: function() {
        for (var i = this._children.length; --i >= 0;) {
            this._children[i].destroy();
        }
        
        this._container = this._children = null;
        arguments.callee.$.destroy.call(this);
    }
});

//NOTE: This function is defined here so it can be shared by Frame and Dialog.
tw_BaseContainer.keyPressNotifyCtrlEnterButton = function(keyPressCombo) {
    if (keyPressCombo == "Ctrl-Enter" || (keyPressCombo == "Enter" && !(tw_Component.currentFocus instanceof tw_TextArea))) {
        var button = this.getStandardButton();
        if (button != null && button.isEnabled()) button.fireClick();
        return false;
    } else {
        return arguments.callee.$.keyPressNotify.call(this, keyPressCombo);
    }
};

