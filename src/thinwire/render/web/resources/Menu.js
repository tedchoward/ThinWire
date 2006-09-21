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
var tw_menu_imageMenuItem = "?_twr_=menuItem.png";
var tw_menu_imageMenuArrow = "url(?_twr_=menuArrow.png)";
var tw_menu_imageMenuArrowInvert = "url(?_twr_=menuArrowInvert.png)";
//FIX: Firefox does not display the menu arrow.

var tw_Menu = tw_Component.extend({
    _menusAreVisible: false,
    _activeMenuItem: null,
    _windowMenu: false,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "mainMenu", id, containerId]);
        this._windowMenu = props.windowMenu;                
        delete props.windowMenu;

        var s = this._box.style;
        s.overflow = "visible";
        s.background = "threedface";
        s.padding = "1px";
        s.borderBottom = "2px groove";
        s.marginBottom = "1px";
        s.zIndex = "1";
        s.borderColor = tw_borderColor;
        
        if (!this._windowMenu) {
            s.borderWidth = "2px";
            s.borderStyle = "outset";
        } else {
            s.position = "";
        }
        
        this._mainMenuMouseOver = this._mainMenuMouseOver.bind(this);
        this._mainMenuMouseDown = this._mainMenuMouseDown.bind(this);
        this._mainMenuMouseOut = this._mainMenuMouseOut.bind(this);
        this._mainMenuClick = this._mainMenuClick.bind(this);
        
        this._itemMouseOver = this._itemMouseOver.bind(this);
        this._itemClick = this._itemClick.bind(this);        

        var initData = props.initData;
        delete props.initData;
                
        this._load(this._box, initData);
        this.init(-1, props);
    },
            
    _mainMenuMouseOver: function(event) {
        var overItem = tw_getEventTarget(event, "menuItem");
        
        if (overItem == null) {
            var overItem = tw_getEventTarget(event, "mainMenuItem");                
            var mainMenu = overItem.parentNode;
                
            for (var i = mainMenu.childNodes.length - 1; i >= 0; i--) {
                var item = mainMenu.childNodes.item(i);
                
                if (item === overItem) {
                    this._setHighlight(item, true);
                    if (this._menusAreVisible) this._open(item);
                } else {
                    this._setHighlight(item, false);
                    this.close(item);
                }
            }
        }
    },
        
    _mainMenuMouseDown: function(event) {
        var overItem = tw_getEventTarget(event, "menuItem");
        
        if (overItem == null) {
            var item = tw_getEventTarget(event, "mainMenuItem");    
            this.close(item);
            this._menusAreVisible = this._menusAreVisible ? false : true;
            this._setHighlight(item, true);
            if (this._menusAreVisible) this._open(item);
        }
    },
                
    _mainMenuMouseOut: function(event) {
        var overItem = tw_getEventTarget(event, "menuItem");
        
        if (overItem == null) {        
            var item = tw_getEventTarget(event, "mainMenuItem");
            if (!this._menusAreVisible) this._setHighlight(item, false);
        }
    },
                
    _mainMenuClick: function(event) {
        var overItem = tw_getEventTarget(event, "menuItem");
        
        if (overItem == null) {
            var item = tw_getEventTarget(event, "mainMenuItem");
            
            if (!item.disabled && item.lastChild.childNodes.length == 0) {
                this.fireAction("click", this._fullIndex(item));
                this._setHighlight(item, false);
                this._menusAreVisible = false;
            }
        }
    },
    
    _itemMouseOver: function(event) {
        var item = tw_getEventTarget(event, "menuItem");
        var parentContent = item.parentNode;            
        this._setHighlight(item, true);
        
        if (!item.disabled)
            this._open(item);
    },
    
    _itemClick: function(event) {
        var item = tw_getEventTarget(event, "menuItem");
        
        if (this._menusAreVisible && !item.disabled && item.lastChild.childNodes.length == 0) {
            this.fireAction("click", this._fullIndex(item));
            var mainMenuItem = tw_getEventTarget(event, "mainMenuItem");            
            this._menusAreVisible = false;
            this.close(mainMenuItem);
        }
    },    
    
    _calcEmWidth: function(item) {
        var parent = item.parentNode;
        var update = false;
        
        //Need better size calc routine
        var width = new Number((this._getText(item).length - item.tw_textAmpCnt) * .7);                
        
        if (parent.tw_maxTextWidth < width) {
            parent.tw_maxTextWidth = width;
            update = true;
        }
        
        width = new Number(this._getKeyPressCombo(item).length * .7);
        
        if (parent.tw_maxShortcutTextWidth < width) {
            parent.tw_maxShortcutTextWidth = width;
            update = true;
        }
        
        if (update)
            parent.style.width = (parent.tw_maxTextWidth + parent.tw_maxShortcutTextWidth + 3).toFixed(2) + "em";
    },
                
    _setImageUrl: function(item, imageUrl) {
        imageUrl = imageUrl == null || imageUrl.length == 0 ? "" : "url(" + tw_BASE_PATH + "/resources/" + imageUrl + ")";
        
        if (item.className == "menuItem") {
            item.firstChild.childNodes.item(0).style.backgroundImage = imageUrl;
        } else {
            var s = item.firstChild.style; 
            s.backgroundImage = imageUrl;
            s.backgroundRepeat = "no-repeat";
            s.backgroundPosition = "center left";
            s.paddingLeft = imageUrl == "" ? "" : "18px";            
        }
    },
        
    _setKeyPressCombo: function(item, text) {
        if (item.className == "menuItem") {
            var textNode = item.firstChild.childNodes.item(2);        
            text = document.createTextNode(text);
    
            if (textNode.childNodes.length == 0) {
                textNode.appendChild(text);
            } else {
                textNode.replaceChild(text, textNode.firstChild);
            }
    
            this._calcEmWidth(item);
        } else {
            item.title = text;
        }
    },
    
    _getKeyPressCombo: function(item) {
        var textNode = item.firstChild.childNodes.item(2);
        return textNode.childNodes.length == 0 ? "" : textNode.firstChild.data;
    },
    
    _setArrowVisible: function(item, visible) {
        if (visible) {
            var invertArrow = false;
            
            if (item.firstChild.tw_invertArrow)
                invertArrow = true;
                
            item.firstChild.lastChild.style.backgroundImage = invertArrow ? tw_menu_imageMenuArrowInvert : tw_menu_imageMenuArrow;
        } else
            item.firstChild.lastChild.style.backgroundImage = "";
    },    
    
    _open: function(item) {
        var content = item.lastChild;
        
        if (content.childNodes.length > 0) {                    
            if (item.className != "mainMenuItem") {
                var parent = item.parentNode;
                content.style.left = (parent.tw_maxTextWidth + parent.tw_maxShortcutTextWidth + 2.50) + "em";
            } else {
                content.style.zIndex = ++tw_Component.zIndex;            
            }
            
            content.style.visibility = "visible";
        }
    },

    _setHighlight: function(item, highlight) {        
        var button = item.firstChild;                
        
        if (item.className == "menuItem") {
            if (highlight) {
                button.tw_invertArrow = true;
                button.style.color = "highlighttext";
                button.style.backgroundColor = "highlight";
                if (this._activeMenuItem != null && this._activeMenuItem !== item) this._setHighlight(this._activeMenuItem, false);
                this._activeMenuItem = item;
            } else {                
                button.tw_invertArrow = false;
                button.style.color = "menutext";
                button.style.backgroundColor = "threedface";
            }
            
            this._setArrowVisible(item, item.lastChild.childNodes.length > 0);
        } else {                
            if (this._activeMenuItem != null) {
                this._setHighlight(this._activeMenuItem, false);
                this._activeMenuItem = null;
            }

            if (highlight) {
                if (this._menusAreVisible)
                    button.style.borderColor = "threedshadow threedhighlight threedhighlight threedshadow";
                else
                    button.style.borderColor = "threedhighlight threedshadow threedshadow threedhighlight";                    
            } else {
                button.style.borderColor = "threedface";
            }
        }        
    },

    _appendSetTextItem: function(node, text) {
        text = text.replace(/([^&])&([^&])/g, "$1$2");
        text = text.replace(/&&/g, "&");
        node.appendChild(document.createTextNode(text));        
    },
    
    _setText: function(item, text) {
        var child = item.firstChild;
        child.tw_text = text;        
        var index = -2;
        
        do {
            index = text.indexOf("&", index + 2);
        } while (text.charAt(index + 1) == "&");
        
        if (index >= 0) {            
            item.tw_textAmpCnt = 1;
            //Replace first single ampersand with an underline span tag
            var first = text.substring(0, index);
            var under = text.charAt(index + 1); 
            var second = text.substring(index + 2);
            
            var text = document.createElement("span");
            if (first.length > 0) this._appendSetTextItem(text, first);
            
            if (under.length > 0) {
                var underSpan = document.createElement("span");
                underSpan.appendChild(document.createTextNode(under));
                underSpan.style.textDecoration = "underline";
                text.appendChild(underSpan);
            }
            
            if (second.length > 0) this._appendSetTextItem(text, second);
        } else {
            item.tw_textAmpCnt = 0;
            text = document.createTextNode(text);
        }

        if (item.className == "menuItem") {
            var textNode = child.childNodes.item(1); 

            if (textNode.childNodes.length == 0) {
                textNode.appendChild(text);
            } else {
                textNode.replaceChild(text, textNode.firstChild);
            }
                        
            this._calcEmWidth(item);
        } else {            
            if (child.childNodes.length == 0) {
                child.appendChild(text);
            } else {
                child.replaceChild(text, child.firstChild);
            }
        }
    },
    
    _getText: function(item) {
        return item.firstChild.tw_text;
    },
    
    _add: function(menu, index) {
        var prefix = menu.className == "mainMenu" ? "mainM" : "m";
    
        var item = document.createElement("div");
        item.className = prefix + "enuItem";
                                                
        var button = document.createElement("div");
        button.className = prefix + "enuButton";
                
        if (menu.className != "mainMenu") {
            var s = item.style;
            s.position = "relative";
            
            var s = button.style;
            s.position = "relative";
            s.margin = "0px";
            s.padding = "2px";
            s.backgroundColor = "threedface";
            s.fontFamily = "tahoma, sans-serif";
            s.fontSize = "8pt";
            
            var image = document.createElement("img");
            image.className = "menuButtonImage";
            var s = image.style;
            s.border = "0px";
            s.margin = "0px";
            s.padding = "0px";
            s.height = "16px";
            s.width = "16px";
            s.backgroundRepeat = "no-repeat";
            s.backgroundPosition = "center center";
            
            image.src = tw_menu_imageMenuItem;        
            button.appendChild(image);
    
            var text = document.createElement("span");
            text.className = "menuButtonText";
            var s = text.style;
            s.position = "absolute";
            s.top = "3px";
            s.left = "20px";            
            button.appendChild(text);
            
            var shortcutText = document.createElement("span");
            shortcutText.className = "menuButtonSText";
            var s = shortcutText.style;
            s.position = "absolute";
            s.top = "2px";
            s.right = "20px";
            button.appendChild(shortcutText);                    
            
            var arrow = document.createElement("div");
            arrow.className = "menuButtonArrow";
            var s = arrow.style;
            s.position = "absolute";
            s.border = "0px";
            s.margin = "0px";
            s.padding = "0px";
            s.right = "1px";
            s.height = "16px";
            s.width = "16px";
            s.backgroundRepeat = "no-repeat";
            s.backgroundPosition = "center center";            
            button.appendChild(arrow);
            
            if (menu.className != "mainMenuItem")
                this._setArrowVisible(menu, true);
        } else {
            var s = item.style;
            s.styleFloat = "left";
            
            var s = button.style;
            s.margin = "0px";
            s.paddingTop = "2px";
            s.paddingBottom = "2px";
            s.paddingLeft = "5px";
            s.paddingRight = "5px";
            s.border = "1px solid threedface";
            s.backgroundColor = "threedface";
            s.fontFamily = "tahoma, sans-serif";
            s.fontSize = "8pt";            
        }
    
        item.appendChild(button);
        
        var content = document.createElement("div");
        content.className = prefix + "enuContent";
        var s = content.style;        
        s.position = "absolute";
        s.margin = "0px";
        s.padding = "0px";
        s.border = "2px outset";
        s.backgroundColor = "threedface";
        s.fontFamily = "tahoma, sans-serif";
        s.fontSize = "8pt";
        s.visibility = "hidden";
        s.borderColor = tw_borderColor;
        if (prefix == "m") s.top = "-2px";        
        content.tw_maxTextWidth = 0;
        content.tw_maxShortcutTextWidth = 0;
        item.appendChild(content);
    
        var parent;
    
        if (menu.className == "mainMenu") {
            parent = menu;            
            tw_addEventListener(item, "mouseover", this._mainMenuMouseOver); 
            tw_addEventListener(item, "mousedown", this._mainMenuMouseDown);
            tw_addEventListener(item, "mouseout", this._mainMenuMouseOut);
            tw_addEventListener(item, "click", this._mainMenuClick);            
        } else {
            parent = menu.lastChild;                        
            tw_addEventListener(item, "mouseover", this._itemMouseOver); 
            tw_addEventListener(item, "click", this._itemClick);
        }
    
        if (index == -1) {
            parent.appendChild(item);
        } else if (index >= parent.childNodes.length) {
            parent.appendChild(item);
        } else {
            var before = parent.childNodes.item(index);
            parent.insertBefore(item, before);
        }
    
        return item;
    },    
            
    _remove: function(menu, index) {
        var parent = menu.className == "mainMenu" ? menu : menu.lastChild;
        var nodes = parent.childNodes;
        var item = nodes.item(index);
        parent.removeChild(item);
    
        if (menu.className != "mainMenu") {                
            parent.tw_maxTextWidth = 0;
            parent.tw_maxShortcutTextWidth = 0;
            nodes = parent.childNodes;
            
            if (nodes.length > 0) {                    
                for (var i = nodes.length - 1; i >= 0; i--) {
                    var item = nodes.item(i);                
                    if (item.className != "menuDivider") this._calcEmWidth(item);
                }
            } else if (menu.className != "mainMenuItem")
                this._setArrowVisible(menu, false);                    
        }
    },
    
    itemRemove: function(value) {
        var idx = value.lastIndexOf(".");
        
        if (idx < 0) {
            this._remove(this._box, value);
        } else {
            var item = this._fullIndexItem(value.slice(0, idx));
            item._remove(item, value.slice(idx + 1));
        }
    },    
    
    _addDivider: function(menu, index) {
        var item = document.createElement("div");
        item.className = "menuDivider";
        var s = item.style;
        s.border = "1px solid";
        s.borderColor = "threedshadow threedface threedhighlight threedface";                
        s.marginTop = "3px";
        s.marginBottom = "3px";
        s.marginLeft = "2px";
        s.marginRight = "1px";
        
        var parent = menu.lastChild;
        
        if (index == -1) {
            parent.appendChild(item);
        } else if (index >= parent.childNodes.length) {
            parent.appendChild(item);
        } else {
            var before = parent.childNodes.item(index);
            parent.insertBefore(item, before);
        }
        
        return item;
    },           
        
    _load: function(menu, data) {
        if (data instanceof Array) {
            for (var i = 0, cnt = data.length; i < cnt; i++)
                this._load(menu, data[i]);        
        } else {
            var index = data.x == undefined ? -1 : data.x;
            
            if (data.t == undefined) {
                this._addDivider(menu, index);
            } else {
                var item = this._add(menu, index);
                this._setText(item, data.t);
                if (data.g != undefined) this._setImageUrl(item, data.g);        
                if (data.k != undefined) this._setKeyPressCombo(item, data.k);
                if (data.d != undefined) item.disabled = true;
                
                if (data.c != undefined) {
                    for (var i = 0, cnt = data.c.length; i < cnt; i++) {
                        this._load(item, data.c[i]);
                    }
                }                
    
                if (data.en == false) item.disabled = true;
            }
        }
    },
    
    _getIndex: function(node) {
        var index = 0;
        while ((node = node.previousSibling) != null)
            index++;
        return index;
    },
    
    _fullIndex: function(item) {        
        var index = this._getIndex(item);
        var value = index;            
        
        while (item.className != "mainMenuItem" ) {
            item = item.parentNode.parentNode;
            var index = this._getIndex(item);
            var value = index + "." + value;
        }
        
        return value;
    },    
    
    _fullIndexItem: function(findex) {
        if (findex == "rootItem") return this._box;
        
        var ary = findex.split(".");
        var value = parseInt(ary[0]);
        var node = this._box.childNodes.item(value);
        
        for (var i = 1; i < ary.length; i++) {
            pnode = node.childNodes.item(1);
            value = parseInt(ary[i]);
            node = pnode.childNodes.item(value);
        }
        
        return node;
    },
        
    itemLoad: function(data, itemPos) {
        var item = this._fullIndexItem(itemPos);
        this._load(item, data);
    },
        
    itemSetText: function(itemPos, text) {
        var item = this._fullIndexItem(itemPos);
        this._setText(item, text);
    },
    
    itemSetKeyPressCombo: function(itemPos, keyPressCombo) {
        if (item.className == "menuItem") {
            var item = this._fullIndexItem(itemPos);
            this._setKeyPressCombo(item, text);
        }
    },
    
    itemSetEnabled: function(itemPos, enabled) {
        var item = this._fullIndexItem(itemPos);
        item.disabled = !enabled;
    },

    itemSetImageUrl: function(itemPos, image) {
        if (item.className == "menuItem") {
            var item = this._fullIndexItem(itemPos);
            this._setImageUrl(item, image);
        }
    },
    
    clear: function(menu) {
        var content = menu.className == "mainMenu" ? menu : menu.lastChild;
        var nodes = content.childNodes;
        
        for (var i = nodes.length - 1; i >= 0; i--) {
            var item = nodes.item(i);
            if (item.className != "menuDivider") this._clear(item);
            this._remove(menu, i);
        }
    },
    
    close: function(menu) {
        if (menu == null) menu = this._box;
        
        if (menu.className == "mainMenu") {
            if (!this._menusAreVisible) return;                
            var nodes = menu.childNodes;
            
            for (var i = nodes.length; --i >= 0;) {
                var item = nodes.item(i); 
                this.close(item);
                this._setHighlight(item, false);
            }
            
            this._menusAreVisible = false;
        } else {
            var content = menu.lastChild;
            var nodes = content.childNodes;            
            content.style.visibility = "hidden";
            
            for (var i = nodes.length; --i >= 0;) {
                var item = nodes.item(i);
                if (item.className == "menuDivider") continue;
                
                if (item.lastChild.style.visibility != "hidden") {
                    this.close(item);
                    this._setHighlight(item, false);
                }
            }

            this._setHighlight(menu, false);
        }
    },
    
    destroy: function() {
        this._activeMenuItem = null;
        this.$.destroy.apply(this, []);        
    }
});
