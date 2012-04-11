/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
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
        arguments.callee.$.call(this, "div", className, id, containerId);
        this._fontBox = null;
        this._container = this._scrollBox = this._box;
        this._children = [];
    },
    
    getContainer: function() {
        return this._container;
    },

    getClickBox: function() {
        return this._container;
    },

    getOffsetX: function() {
        return (this._box == this._borderBox ? this._borderSize : 0) + this._offsetX - this._container.scrollLeft;
    },

    getOffsetY: function() {
        return (this._box == this._borderBox ? this._borderSize : 0) + this._offsetY - this._container.scrollTop;
    },
    
    setScrollType: function(scrollCode) {
        var overflow;
        
        switch (scrollCode) {
            case 1: overflow = "auto"; break;
            case 2: overflow = "scroll"; break;
            default: overflow = "hidden";
        }

        this._container.style.overflow = overflow;
    },
    
    getDragBox: function() {
        var dragBox = this._container.cloneNode(false);
        dragBox.style.backgroundColor = tw_COLOR_TRANSPARENT;
        return dragBox;
    },
    
    getDragArea: function() {
        return this._container;
    },
    
    getDropArea: function() {
        return this._container;
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

            comp._parentIndex = insertAtIndex;
        }        
    },
    
    removeComponent: function(componentId) {
        if (!this._inited) return;
        var child = tw_Component.instances[componentId];
        this._container.removeChild(child._box);
        var i = child._parentIndex;
        this._children.splice(i, 1);

        for (var cnt = this._children.length; i < cnt; i++) {
            this._children[i]._parentIndex--;
        }
        
        child.destroy();
    },
    
    destroy: function() {
        for (var i = this._children.length; --i >= 0;) {
            var child = this._children[i];
            this._container.removeChild(child._box);
            child.destroy();
        }
        
        this._container = this._children = null;
        arguments.callee.$.call(this);
    }
});

//NOTE: This function is defined here so it can be shared by Frame and Dialog.
tw_BaseContainer.keyPressNotifyCtrlEnterButton = function(keyPressCombo) {
    if (keyPressCombo == "Ctrl-Enter" || (keyPressCombo == "Enter" && !(tw_Component.currentFocus instanceof tw_TextArea))) {
        var button = this.getStandardButton();
        if (button != null && button._enabled) button.fireClick();
        return false;
    } else {
        return arguments.callee.$.call(this, keyPressCombo);
    }
};

tw_BaseContainer.containerClickListener = function(ev) {
    if (!this._enabled) return;
    var action = tw_Component.getClickAction(ev.type);
    if (action == null) return;
    this.fireAction(ev, action);
};