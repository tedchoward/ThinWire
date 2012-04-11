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
//TODO: After removing a tab, if current index is no longer valid, then the server-model must be notified.
//TODO: Once the real estate for tabs is exahasted, the tabs don't stack or scroll.
//TODO: What should the 'scroll' property do on a TabFolder?
//TODO: Enabled is not handled on a TabFolder currently.
var tw_TabFolder = tw_BaseContainer.extend({
    _currentIndex: -1,
    _tabs: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "tabFolder", id, containerId);
        this._fontBox = null; 
        var s = this._box.style;
        var cssText = "position:absolute;overflow:visible;padding:0px;margin:0px;background-color:" + tw_COLOR_TRANSPARENT + ";";
        tw_Component.setCSSText(cssText, this._box);
    
        this._tabs = document.createElement("div");
        var s = this._tabs.style;
        cssText = "position:absolute;z-index:1;height:" + tw_TabFolder._tabsHeight + "px;";
        tw_Component.setCSSText(cssText, this._tabs);
        this._box.appendChild(this._tabs);
        
        this._borderBox = this._backgroundBox = this._container = this._scrollBox = document.createElement("div");
        var s = this._container.style;
        cssText = "position:absolute;z-index:0;top: " + tw_TabFolder._tabsHeight + "px;";
        tw_Component.setCSSText(cssText, this._container);

        tw_addEventListener(this.getClickBox(), ["click", "dblclick"], this._containerClickListener.bind(this)); 
        
        this._box.appendChild(this._container);    
        this.init(-1, props);
    },
    
    _containerClickListener: tw_BaseContainer.containerClickListener,

    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        if (name == "borderWidth") this._offsetX = this._offsetY = parseInt(value);
        
        if (name.indexOf("border") == 0) {
            if (name == "borderImage") return;
            for (var i in this._children) {
                this._children[i].setStyle(name, value);
            }
        }
    },    
    
    getOffsetY: function() {
        return arguments.callee.$.call(this) + tw_TabFolder._tabsHeight;
    },
        
    setWidth: function(width) {
        this._width = width;
        this._tabs.style.width = width + "px";
        this._box.style.width = width + "px";

        if (this._borderImage != null) {
            this._borderImage.setWidth(width);
            this._borderBox.style.width = width + "px";
            width -= this._borderSizeSub;
            if (width < 0) width = 0;
        } else {
            width -= this._borderSizeSub;
            if (width < 0) width = 0;
            this._borderBox.style.width = width + "px";
        }
        
        for (var i = this._children.length; --i >= 0;) {
            this._children[i].setWidth(width);
        }
    },
    
    setHeight: function(height) {
        this._height = height;
        this._box.style.height = height + "px";
        height -= tw_TabFolder._tabsHeight;
        if (height < 0) height = 0;
        
        if (this._borderImage != null) {
            this._borderImage.setHeight(height);
            this._borderBox.style.height = height + "px"; 
            height -= this._borderSizeSub;
            if (height < 0) height = 0;
        } else {
            height -= this._borderSizeSub;
            if (height < 0) height = 0;
            this._borderBox.style.height = height + "px"; 
        }
        
        for (var i = this._children.length; --i >= 0;) {            
            this._children[i].setHeight(height);
        }
    },
    
    addComponent: function(insertAtIndex, sheet) {
        var size = this._width - this._borderSizeSub;
        if (size < 0) size = 0;
        sheet.setWidth(size);
        var size = this._height - (this._borderSizeSub + tw_TabFolder._tabsHeight);
        if (size < 0) size = 0;        
        sheet.setHeight(size);

        this._setTabActive(this._currentIndex, false);

        var tab = sheet._tab;
        sheet.setStyle("borderColor", this._borderColor);
        sheet.setStyle("borderStyle", this._borderType);
        sheet.setStyle("borderWidth", this._borderSize + "px");        
        
        if (insertAtIndex == -1 || insertAtIndex >= this._children.length) {
            this._tabs.appendChild(tab);
            insertAtIndex = this._children.length;
        } else {
            this._tabs.insertBefore(tab, this._tabs.childNodes.item(insertAtIndex));
        }
        
        arguments.callee.$.call(this, insertAtIndex, sheet);
        
        if (this._currentIndex != insertAtIndex) this._setTabActive(insertAtIndex, false);
        this._setTabActive(this._currentIndex, true);
    },
    
    removeComponent: function(componentId) {
        this._setTabActive(this._currentIndex, false);
        var tab = tw_Component.instances[componentId]._tab;
        tab.parentNode.removeChild(tab);
        arguments.callee.$.call(this, componentId);
        var len = this._children.length;
        this._setTabActive(this._currentIndex < len ? this._currentIndex : len - 1, true);    
    },

    _setTabActive: function(index, active) {  
        var cl = this._children;
        if (index < 0 || index >= cl.length) return;
        var sheet = this._children[index];
        sheet.setActiveStyle(active);
        sheet._container.style.display = active ? "block" : "none";
        
        if (active) {
            this._focusBox = sheet._tab;
            tw_setElementFocus(this, true);            
            this._currentIndex = index;
        }

        if(tw_Component.currentFocus instanceof tw_Component){
            tw_Component.currentFocus.setFocus(false);
        }

        var commonSize = active ? "0px" : this._borderSize + "px";
        if (index == 0) this._tabs.style.paddingLeft = commonSize;        
        if (index > 0) cl[index - 1]._tab.style.borderRightWidth = commonSize;        
        if (index + 1 < cl.length) cl[index + 1]._tab.style.borderLeftWidth = commonSize;
    },
    
    keyPressNotify: function(keyPressCombo) {
        if (keyPressCombo == "Ctrl-Tab" || keyPressCombo == "Ctrl-Shift-Tab") {
            var cn = this._tabs.childNodes;

            if (keyPressCombo == "Ctrl-Tab") {
                var index = this._currentIndex + 1;
                if (index >= cn.length) index = 0;
            } else {
                var index = this._currentIndex - 1;
                if (index < 0) index = cn.length - 1;
            }

            this.setCurrentIndex(index, true);
            return false;
        } else {
            return arguments.callee.$.call(this, keyPressCombo);            
        }
    },    

    setCurrentIndex: function(index, sendEvent) {
        this._setTabActive(this._currentIndex, false);
        if (sendEvent && this._currentIndex == index) sendEvent = false;            
        this._currentIndex = index;
        this._setTabActive(this._currentIndex, true);
        if (this._focusCapable) this.setFocus(true);
        if (sendEvent) this.firePropertyChange("currentIndex", index);
    },
    
    getTabCount: function() {
        return this._container.childNodes.length;
    },
    
    destroy: function() {
        arguments.callee.$.call(this);
        this._tabs = null;
    }
});

tw_TabFolder._tabsHeight = 20;


