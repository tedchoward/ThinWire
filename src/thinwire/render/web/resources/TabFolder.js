/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
//TODO: After removing a tab, if current index is no longer valid, then the server-model must be notified.
//TODO: Once the real estabe for tabs is exahasted, the tabs don't stack or scroll.
//TODO: What should the 'scroll' property do on a TabFolder?
//TODO: Enabled is not handled on a TabFolder currently.
//TODO: No real keyboard navigation for TabFolder or tabs.
var tw_TabFolder = tw_BaseContainer.extend({
    _currentIndex: -1,
    _tabs: null,    
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["tabFolder", id, containerId]);
    
        this._tabs = document.createElement("div");
        this._tabs.className = "tabFolderTabs";
        this._box.appendChild(this._tabs);
        
        this._borderBox = this._container = document.createElement("div");
        this._container.className = "tabFolderContent";
        this._container.style.borderColor = tw_borderColor;
        this._box.appendChild(this._container);    
        this._fontBox = null; 
        
        this._tabClickListener = this._tabClickListener.bind(this);
        
        this.init(-1, props);
    },
        
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this._tabs.style.width = width + "px";        
        var cWidth = width - (tw_sizeIncludesBorders ? 0 : tw_CALC_BORDER_PADDING_SUB);
        if (cWidth < 0) cWidth = 0;
        this._container.style.width = cWidth + "px";        
        width -= tw_CALC_BORDER_SUB;        
        if (width < 0) width = 0;
        
        for (var i = this._children.length; --i >= 0;) {
            this._children[i].setWidth(width);
        }    
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        this._container.style.top = this._tabs.style.height = tw_TabFolder._tabsHeight + "px";        
        var cHeight = height - (tw_sizeIncludesBorders ? 0 : tw_CALC_BORDER_PADDING_SUB) - tw_TabFolder._tabsHeight;
        if (cHeight < 0) cHeight = 0;
        this._container.style.height = cHeight + "px";        
        height = height - tw_CALC_BORDER_SUB - tw_TabFolder._tabsHeight;
        if (height < 0) height = 0;        
        
        for (var i = this._children.length; --i >= 0;) {            
            this._children[i].setHeight(height);
        }    
    },
    
    setStyle: function(name, value) {
        for (var i in this._children) {
            this._children[i].setStyle(name, value);
        }
    },
        
    _tabClickListener: function(event) {
        var tab = tw_getEventTarget(event, "tabSheetTab");
        var tabs = this._tabs;
        
        for (var i = tabs.childNodes.length; --i >= 0;) {
            if (tabs.childNodes.item(i) == tab) {
                this.setCurrentIndex(i, true);
                break;
            }
        }
    },
    
    addComponent: function(insertAtIndex, sheet) {
        var tab = document.createElement("a");
        tab.id = "tab" + sheet._id;
        tab.className = "tabSheetTab";
        tab.style.borderColor = tw_borderColor;
        tab.style.lineHeight = tab.style.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? 2 : 4) + "px";
        tab.style.backgroundImage = "";
        ss = sheet._box.style;
        tab.style.backgroundColor = ss.backgroundColor;
        tab.style.fontSize = ss.fontSize;
        tab.style.fontWeight = ss.fontWeight;
        tab.style.fontStyle = ss.fontStyle;
        tab.style.textDecoration = ss.textDecoration;
        tab.style.color = ss.color;
        if (ss.borderStyle != "") tab.style.borderStyle = ss.borderStyle;
        if (ss.borderColor != "") tab.style.borderColor = ss.borderColor;
        
        var tabImage = document.createElement("div");
        tabImage.className = "tabSheetTabImage";
        tab.appendChild(tabImage);
        
        tab.appendChild(document.createTextNode(""));
        tw_addEventListener(tab, "click", this._tabClickListener);

        sheet.setImage(null, tab);
        sheet.setText(null, tab);
        sheet.setWidth(this._width - tw_CALC_BORDER_SUB);
        sheet.setHeight(this._height - tw_CALC_BORDER_SUB - tw_TabFolder._tabsHeight);
    
        this._setTabActive(this._currentIndex, false);          
        
        if (insertAtIndex == -1 || insertAtIndex >= this._children.length) {
            this._tabs.appendChild(tab);
        } else {
            this._tabs.insertBefore(tab, this._tabs.childNodes.item(insertAtIndex));
        }
        
        this.$.addComponent.apply(this, [insertAtIndex, sheet]);
        if (this._currentIndex != insertAtIndex) this._setTabActive(insertAtIndex, false);
        this._setTabActive(this._currentIndex, true);    
    },
    
    removeComponent: function(tabId) {
        this._setTabActive(this._currentIndex, false);
        this.$.removeComponent.apply(this, [tabId]);
        var tab = document.getElementById("tab" + tabId);
        tab.parentNode.removeChild(tab);
        var len = this._children.length;
        this._setTabActive(this._currentIndex < len ? this._currentIndex : len - 1, true);    
    },

    _setTabActive: function(index, active) {  
        var cn = this._tabs.childNodes;
        if (index < 0 || index >= cn.length) return; 
        var tab = cn.item(index);
        var commonSize = active ? "0px" : "2px";    
        var s = tab.style;
        
        if (active) {
            this._children[index]._box.style.zIndex = 1;
            this._children[index]._box.style.visibility = "visible";
            s.height = tw_TabFolder._tabsHeight + (tw_sizeIncludesBorders ? 2 : 0) + "px";
            s.paddingLeft = "4px";
            s.paddingRight = "4px";
            this._focusBox = tab;
            this._currentIndex = index;
        } else {
            this._children[index]._box.style.zIndex = 0;
            this._children[index]._box.style.visibility = "hidden";
            s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? 2 : 4) + "px";
            s.paddingLeft = "2px";
            s.paddingRight = "2px";
        }
        
        tw_setFocusCapable(tab, active);
        s.marginTop = commonSize;        
        if (index == 0) this._tabs.style.paddingLeft = commonSize;        
        if (index > 0) cn.item(index - 1).style.borderRightWidth = commonSize;        
        if (index + 1 < cn.length) cn.item(index + 1).style.borderLeftWidth = commonSize;
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
            this.setFocus(true);
            return false;
        } else {
            return this.$.keyPressNotify.apply(this, [keyPressCombo]);            
        }
    },    

    setCurrentIndex: function(index, notify) {
        this._setTabActive(this._currentIndex, false);
        this._currentIndex = index;    
        this._setTabActive(this._currentIndex, true);
        if (notify) this.firePropertyChange("currentIndex", index);            
    },
    
    destroy: function() {
        this.$.destroy.apply(this, []);
        this._tabs = null;
    }
});

tw_TabFolder._tabsHeight = 20;


