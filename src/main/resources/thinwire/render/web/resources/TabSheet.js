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
//TODO: No proper support for tabsheet setEnabled.
var tw_TabSheet = tw_BaseContainer.extend({
    _tab: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "tabSheet", id, containerId);
        var s = this._box.style;
        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;top:0px;left:0px;z-index:0;";
        tw_Component.setCSSText(cssText, this._box);
        
        var tab = this._tab = this._fontBox = this._borderBox = document.createElement("a");
        tab.className = "floatDivLeft"; //hack for FF
        var s = tab.style;
        cssText = "cursor:default;overflow:hidden;white-space:nowrap;";
        s.styleFloat = "left";
        tw_Component.setCSSText(cssText, tab);
        
        var tabImage = document.createElement("div");
        tabImage.className = "floatDivLeft"; //hack for FF
        var s = tabImage.style;
        cssText = "background-repeat:no-repeat;background-position:center center;width:16px;height:16px; " +
            "padding-left:3px;overflow:hidden;display:none;";
        s.styleFloat = "left";
        tw_Component.setCSSText(cssText, tabImage);

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
            s.height = tw_TabFolder._tabsHeight + (tw_sizeIncludesBorders ? this._borderSize : margin) + "px";
            s.paddingLeft = s.paddingRight = "4px";
        } else {
            var margin = 2;
            bs.zIndex = 0;
            s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? this._borderSize : this._borderSize + margin) + "px";
            s.paddingLeft = s.paddingRight = "2px";
        }

        s.marginTop = margin + "px";
        if (this._focusCapable) tw_setFocusCapable(this._tab, active);
    },    

    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._tab, true);
        } else {
            tw_setFocusCapable(this._tab, false);
        }
    },
        
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        
        if (name == "backgroundColor") {
            this._tab.style.backgroundColor = value;
        } else if (name == "borderWidth") {
            this._tab.style.lineHeight = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? this._borderSize : this._borderSizeSub) + "px";
            this._borderBox.style.borderBottomWidth = "0px";
            this.setActiveStyle(this._box.style.zIndex == 1);
        }
    },
    
    setVisible: function(visible) {
        this._visible = visible;
        this._container.style.display = this._tab.style.display = visible ? "block" : "none";
        
        if (this._inited && this._tab != null && this._parent != null) { 
            var index = tw_getElementIndex(this._tab);
            var current = this._parent._currentIndex;
            
            if (current >= 0 && index >= 0) {
                if (index == current) {
                    this._parent._setTabActive(current, false);
                } else {
                    this._parent.setCurrentIndex(current, false);
                }
            }
        }
    },
    
    setOpacity: function(opacity) {
        this._opacity = opacity;
        tw_setOpacity(this._tab, opacity);
        this._tab.style.display = opacity > 0 && this._visible ? "block" : "none";
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
    
    getClickBox: function() {
        return this._tab;
    },

    destroy: function() {
        arguments.callee.$.call(this);
        this._tab = null;
    }    
});
