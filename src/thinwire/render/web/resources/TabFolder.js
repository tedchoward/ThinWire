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
//TODO: After removing a tab, if current index is no longer valid, then the server-model must be notified.
//TODO: Once the real estate for tabs is exahasted, the tabs don't stack or scroll.
//TODO: What should the 'scroll' property do on a TabFolder?
//TODO: Enabled is not handled on a TabFolder currently.
var tw_TabFolder = tw_BaseContainer.extend({
    _currentIndex: -1,
    _tabs: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.construct.call(this, "tabFolder", id, containerId);
        this._fontBox = null; 
        var s = this._box.style;
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.overflow = "visible";
    
        this._tabs = document.createElement("div");
        var s = this._tabs.style;
        s.position = "absolute";
        s.zIndex = "1";
        this._box.appendChild(this._tabs);
        
        this._borderBox = this._backgroundBox = this._container = document.createElement("div");
        var s = this._container.style;
        s.position = "absolute";
        s.zIndex = "0";

        this._box.appendChild(this._container);    
        this.init(-1, props);
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.setStyle.call(this, name, value);
        if (name == "borderSize") this._offsetX = this._offsetY = parseInt(value);
        
        if (name.indexOf("border") == 0) {
            for (var i in this._children) {
                this._children[i].setStyle(name, value);
            }
        }
    },    
    
    getOffsetY: function() {
        return arguments.callee.$.getOffsetY.apply(this) + tw_TabFolder._tabsHeight;
    },
        
    setWidth: function(width) {
        arguments.callee.$.setWidth.call(this, width);
        this._tabs.style.width = width + "px";
        var cWidth = width - this._borderSizeSub;
        if (cWidth < 0) cWidth = 0;
        this._container.style.width = cWidth + "px";
        cWidth = width - this.getStyle("borderSize") * 2;
        if (cWidth < 0) cWidth = 0;

        for (var i = this._children.length; --i >= 0;) {
            this._children[i].setWidth(cWidth);
        }
    },
    
    setHeight: function(height) {
        arguments.callee.$.setHeight.call(this, height);
        this._container.style.top = this._tabs.style.height = tw_TabFolder._tabsHeight + "px";        
        var cHeight = height - this._borderSizeSub - tw_TabFolder._tabsHeight;
        if (cHeight < 0) cHeight = 0;
        this._container.style.height = cHeight + "px";
        cHeight = height - (this.getStyle("borderSize") * 2 + tw_TabFolder._tabsHeight);
        if (cHeight < 0) cHeight = 0;
        
        for (var i = this._children.length; --i >= 0;) {            
            this._children[i].setHeight(cHeight);
        }
    },
    
    addComponent: function(insertAtIndex, sheet) {
        var size = this.getWidth() - this.getStyle("borderSize") * 2;
        if (size < 0) size = 0;
        sheet.setWidth(size);
        var size = this.getHeight() - (this.getStyle("borderSize") * 2 + tw_TabFolder._tabsHeight);
        if (size < 0) size = 0;        
        sheet.setHeight(size);

        this._setTabActive(this._currentIndex, false);

        var tab = sheet._tab;
        sheet.setStyle("borderColor", this.getStyle("borderColor"));
        sheet.setStyle("borderType", this.getStyle("borderType"));
        sheet.setStyle("borderSize", this.getStyle("borderSize"));        
        
        if (insertAtIndex == -1 || insertAtIndex >= this._children.length) {
            this._tabs.appendChild(tab);
        } else {
            this._tabs.insertBefore(tab, this._tabs.childNodes.item(insertAtIndex));
        }
        
        arguments.callee.$.addComponent.call(this, insertAtIndex, sheet);
        
        if (this._currentIndex != insertAtIndex) this._setTabActive(insertAtIndex, false);
        this._setTabActive(this._currentIndex, true);
    },
    
    removeComponent: function(componentId) {
        this._setTabActive(this._currentIndex, false);
        var tab = tw_Component.instances[componentId]._tab;
        tab.parentNode.removeChild(tab);
        arguments.callee.$.removeComponent.call(this, componentId);
        var len = this._children.length;
        this._setTabActive(this._currentIndex < len ? this._currentIndex : len - 1, true);    
    },

    _setTabActive: function(index, active) {  
        var cl = this._children;
        if (index < 0 || index >= cl.length) return;
        var sheet = this._children[index];
        sheet.setActiveStyle(active);
        
        if (active) {
            this._focusBox = sheet._tab;
            this._currentIndex = index;
        }

        var commonSize = active ? "0px" : this.getStyle("borderSize") + "px";
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
            return arguments.callee.$.keyPressNotify.call(this, keyPressCombo);            
        }
    },    

    setCurrentIndex: function(index, sendEvent) {
        this._setTabActive(this._currentIndex, false);
        if (sendEvent && this._currentIndex == index) sendEvent = false;            
        this._currentIndex = index;
        this._setTabActive(this._currentIndex, true);
        
        if (sendEvent) {
            this.setFocus(true);
            this.firePropertyChange("currentIndex", index);
        }
    },
    
    destroy: function() {
        arguments.callee.$.destroy.call(this);
        this._tabs = null;
    }
});

tw_TabFolder._tabsHeight = 20;


