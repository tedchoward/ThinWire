/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
//TODO: No proper support for tabsheet setEnabled.
var tw_TabSheet = tw_BaseContainer.extend({
    _tab: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "tabSheet", id, containerId);
        var s = this._box.style;
        s.top = "0px";
        s.left = "0px";
        s.zIndex = "0";
        
        var tab = this._tab = this._fontBox = this._borderBox = document.createElement("a");
        tab.className = "floatDivLeft"; //hack for FF
        var s = tab.style;
        s.cursor = "default";
        s.overflow = "hidden";
        s.whiteSpace = "nowrap";
        s.styleFloat = "left"; //Doesn't work in FF.
        s.display = "block";

        var tabImage = document.createElement("div");
        tabImage.className = "floatDivLeft"; //hack for FF
        var s = tabImage.style;
        s.styleFloat = "left"; //Doesn't work in FF.
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";
        s.width = "16px";
        s.height = "16px";
        s.paddingLeft = "3px";
        s.overflow = "hidden";
        s.display = "none";

        tab.appendChild(tabImage);       
        tab.appendChild(document.createTextNode(""));
        tw_addEventListener(tab, ["click", "dblclick"], this._tabClickListener.bind(this));        
                
        var tabIndex = props.tabIndex;
        delete props.tabIndex;
        this.init(tabIndex, props);
    },

    _tabClickListener: function(event) {
        if (this._parent == null) return;
        var children = this._parent._children;
        
        for (var i = children.length; --i >= 0;) {
            if (children[i] == this) {
                this._parent.setCurrentIndex(i, true);
                tw_Component.clickListener(event, this);
                break;
            }
        }
    },
    
    setText: function(text) {        
        this._tab.replaceChild(tw_Component.setRichText(text), this._tab.lastChild);    
    },
            
    setImage: function(image) {
        var s = this._tab.firstChild.style;
        s.backgroundImage = tw_Component.expandUrl(image, true);
        s.display = image.length > 0 ? "block" : "none";
    },
    
    setActiveStyle: function(active) {  
        var bs = this._box.style;
        var s = this._tab.style;
        
        if (active) {
            var margin = 0;
            bs.zIndex = 1;
            bs.display = "block";
            s.height = tw_TabFolder._tabsHeight + (tw_sizeIncludesBorders ? this._borderSize : margin) + "px";
            s.paddingLeft = s.paddingRight = "4px";
        } else {
            var margin = 2;
            bs.zIndex = 0;
            bs.display = "none";
            s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? this._borderSize : this._borderSize + margin) + "px";
            s.paddingLeft = s.paddingRight = "2px";
        }

        s.marginTop = margin + "px";
        tw_setFocusCapable(this._tab, active);
    },    
        
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        
        if (name == "backgroundColor") {
            this._tab.style.backgroundColor = value;
        } else if (name == "borderWidth") {
            this._tab.style.lineHeight = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? this._borderSize : this._borderSizeSub) + "px";
            this._borderBox.style.borderBottomWidth = "0px";
            this.setActiveStyle(this._box.style.display == "block");
        }
    },
    
    setOpacity: function(opacity) {
        this._tab.style.display = opacity > 0 ? "block" : "none";
        tw_setOpacity(this._tab, opacity);        
        this._opacity = opacity;
    },
    
    getDragArea: function() {
        return this._tab;
    },
    
    getDragBox: function() {
        var dragBox = this._tab.cloneNode(true);
        dragBox.style.backgroundColor = tw_COLOR_TRANSPARENT;
        return dragBox;
    },
    
    getDropArea: function() {
        return this._tab;
    },

    destroy: function() {
        arguments.callee.$.call(this);
        this._tab = null;
    }    
});
