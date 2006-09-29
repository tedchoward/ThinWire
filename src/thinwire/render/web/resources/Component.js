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
function tw_newComponent(className, id, containerId, props) {
    var clazz = window[className];
    
    if (clazz == undefined) {
        throw "Class '" + className + "' is undefined!";
    } else {
        new clazz(id, containerId, props);
    }
}

//TODO: Opera, it's still possible to drag-highlight text.
var tw_Component = Class.extend({
    _box: null,
    _inited: false,
    _opacity: 100,
    _focusBox: null,
    _id: -1,
    _parent: null,
    _parentIndex: -1,
    _width: 0,
    _height: 0,
    _eventNotifiers: null,
    _supportLineHeight: false,
    _supportText: false,
    _enabled: true,
    _focusCapable: true,
    _backgroundBox: null,
    _backgroundColor: "",
    _disabledBackgroundColor: null,
    _borderBox: null,
    _borderColor: null,
    _borderType: null,
    _borderSize: 0,
    _borderSizeSub: 0,
    _boxSizeSub: 0,
    _fontBox: null,
    _clickTime: null,
    
    construct: function(tagName, className, id, containerId, support) {
        var box = document.createElement(tagName);
        box.className = className;
        var s = box.style;
        s.position = "absolute";
        s.overflow = "hidden";
        s.padding = "0px";
        s.margin = "0px";
        box.id = id;
        this._box = this._focusBox = this._backgroundBox = this._borderBox = this._fontBox = box;
        this._id = id;
        
        support = support == null ? ",," : "," + support + ",";

        if (support.indexOf(",lineHeight,") >= 0) this._supportLineHeight = true;

        if (support.indexOf(",text,") >= 0) {
            this._supportText = true;
            this._box.appendChild(document.createTextNode(""));
        }
        
        tw_Component.instances[id] = this;
        this._parent = containerId instanceof Class ? containerId : tw_Component.instances[containerId];
    },
    
    getParent: function() {
        return this._parent;
    },
    
    getBox: function() {
        return this._box;
    },
    
    getId: function() {
        return this._id;
    },
            
    setX: function(x) {
        this._box.style.left = x + "px";
    },
    
    getX: function() {
        return parseInt(this._box.style.left);
    },
    
    setY: function(y) {
        this._box.style.top = y + "px";
    },

    getY: function() {
        return parseInt(this._box.style.top);
    },    

    setWidth: function(width) {
        this._width = width;
        width -= this._boxSizeSub;        
        if (this._box == this._borderBox) width -= this._borderSizeSub;
        if (width < 0) this._width = width = 0;
        this._box.style.width = width + "px";
    },
    
    getWidth: function() {
        return this._width;
    },

    setHeight: function(height) {
        this._height = height;
        height -= this._boxSizeSub;
        if (this._box == this._borderBox) height -= this._borderSizeSub;
        if (height < 0) this._height = height = 0;
        this._box.style.height = height + "px";
        if (this._supportLineHeight) this._box.style.lineHeight = this._box.style.height;
    },
    
    getHeight: function() {
        return this._height;
    },
    
    isVisible: function() {
        return this._opacity > 0;
    },
    
    setVisible: function(visible) {
        this.setOpacity(visible ? 100 : 0);
    },
    
    setOpacity: function(opacity) {
        this._box.style.display = opacity > 0 ? "block" : "none";        
        this._box.style.opacity = opacity / 100;
        if (tw_isIE) this._box.style.filter = opacity >= 100 ? "" : "alpha(opacity=" + opacity + ")";
        this._opacity = opacity;
    },
    
    getOpacity: function() {
        return this._opacity;
    },
    
    isEnabled: function() {
        return this._enabled;
    },
    
    setEnabled: function(enabled) {
        if (this === tw_Component.currentFocus) {
            var nextComp = this.getNextComponent(true);
            if (nextComp != null && nextComp !== this) nextComp.setFocus(true);
        }
        
        this._enabled = enabled;
        this._backgroundBox.style.backgroundColor = enabled ? this._backgroundColor : (this._disabledBackgroundColor == null ? this.getParent().getStyle("backgroundColor") : this._disabledBackgroundColor);
    },
        
    setFocusCapable: function(focusCapable) {
        this._focusCapable = focusCapable;
    },
    
    isFocusCapable: function() {
        return this._focusCapable;
    },
    
    setPropertyWithEffect: function(prop, value, unitSize, time) {
        prop = prop.charAt(0).toUpperCase() + prop.substring(1);
        var get = "get" + prop;
        var set = "set" + prop;
        new tw_Animation(this, get, set, value - this[get](), unitSize, time).start();
    },
    
    _focusListener: function() {
        this.setFocus(true);
    },
    
    _blurListener: function() {
        this.setFocus(false);        
    },
        
    setFocus: function(focus) {
        if (!this.isEnabled() || !this.isVisible()) return;
        
        if (focus) {
            if (tw_Component.currentFocus !== this) {
                //We don't need to send a false event to the server, because a true event on the current
                //component will trigger a false event on the prior.
                if (tw_Component.currentFocus != null) {
                    tw_Component.currentFocus.setFocus(false);

                    try {
                        if (tw_Component.currentFocus._focusBox.blur) tw_Component.currentFocus._focusBox.blur();
                    } catch (e) {
                        //Firefox sometimes throws an error when attemptting to set focus.
                        //ignore the error for now until solution is found.
                    }
                }
                
                tw_Component.priorFocus = tw_Component.currentFocus; 
                tw_Component.currentFocus = this;
                tw_em.removeQueuedViewStateChange("focus");
                this.firePropertyChange("focus", true, "focus");
                if (tw_Component.priorFocus != null && tw_Component.priorFocus.hasPropertyChangeListener("focus")) tw_em.sendGetEvents();
                
                try {
                    if (this._focusBox.focus) this._focusBox.focus();
                } catch (e) {
                    //Firefox sometimes throws an error when attemptting to set focus.
                    //ignore the error for now until solution is found.
                }
                
                var isPriorButton = tw_Component.priorFocus instanceof tw_Button;
                var isButton = this instanceof tw_Button;
                
                if (!isButton && isPriorButton) {
                    var sButton = this.getBaseWindow().getStandardButton();
                    if (sButton != null && sButton.isEnabled()) sButton._setStandardStyle(true);
                } else if (!isPriorButton && isButton) {
                    var sButton = this.getBaseWindow().getStandardButton();
                    if (sButton != null && this !== sButton) sButton._setStandardStyle(false);
                }
            }
        }
    },
    
    setStyle: function(name, value) {
        var realName = tw_Component.styleNameMap[name];
        if (realName == undefined) throw "attempt to set unknown property '" + name + "' to value '" + value + "'";
        
        if (this._backgroundBox != null && name == "backgroundColor") {
            this._backgroundColor = value;
            if (this.isEnabled()) this._backgroundBox.style.backgroundColor = value;
        } else if (this._fontBox != null && name.indexOf("font") == 0) {
            if (name == "fontSize") {
                value += "pt";
            } else if (name != "fontFamily" && name != "fontColor") {
                value = typeof(value) == "string" || value instanceof String ? value.toLowerCase() == "true" : value;
            
                if (name == "fontBold") {                
                    value = value ? "bold" : "normal";
                } else if (name == "fontItalic") {
                    value = value ? "italic" : "normal";
                } else if (name == "fontUnderline") {
                    value = value ? "underline" : "none";
                }
            }
            
            this._fontBox.style[realName] = value;
        } else if (this._borderBox != null && name.indexOf("border") == 0) {
            if (name == "borderSize") {
                this._borderSize = parseInt(value);
                this._borderSizeSub = tw_sizeIncludesBorders ? 0 : this._borderSize * 2;                
                value += "px";
                this._borderBox.style[realName] = value;
                
                if (this._inited) {
                    this.setWidth(this.getWidth());
                    this.setHeight(this.getHeight());
                }
            } else if (name == "borderColor") {
                this._borderColor = value;
                this._borderBox.style[realName] = tw_Component.getIEBorder(value, this._borderType);
            } else {
                this._borderType = value;
                this._borderBox.style[realName] = value;
                
                if (tw_isIE && this._borderColor != null) {
                    var keep = this._borderColor;
                    this.setStyle("borderColor", tw_Component.getIEBorder(keep, this._borderType));
                    this._borderColor = keep;
                }
            }
        }
    },
    
    getStyle: function(name) {
        var realName = tw_Component.styleNameMap[name];
        if (realName == undefined) throw "attempt to get unknown property '" + name + "'";
        var value;
        
        if (this._backgroundBox != null && name == "backgroundColor") {
            value = this._backgroundColor;
        } else if (this._fontBox != null && name.indexOf("font") == 0) {
            value = this._fontBox.style[realName];
            
            if (name == "fontSize") {
                value = parseInt(value);
            } else if (name == "fontBold") {
                value = value == "bold" ? true : false;
            } else if (name == "fontItalic") {
                value = value == "italic" ? true : false;
            } else if (name == "fontUnderline") {
                value = value == "underline" ? true : false;
            }
        } else if (this._borderBox != null && name.indexOf("border") == 0) {
            if (name == "borderSize") {
                return this._borderSize;
            } else if (name == "borderColor") {
                return this._borderColor;
            } else {
                return this._borderType;
            }
        } else {
            throw "attempt to get unsupported property '" + name + "'";
        }
        
        return value;
    },

    setText: function(text) {
        if (!this._supportText) { alert("'text' property not supported by this component"); return; }
        this._box.replaceChild(document.createTextNode(text), this._box.firstChild);
    },
        
    getBaseWindow: function() {
        var parent = this;
        
        do {        
            var parent = parent.getParent();
        } while (parent != null && !(parent instanceof tw_Dialog) && !(parent instanceof tw_Frame));
        
        return parent;
    },
    
    registerEventNotifier: function(type, subType) {
        if (this._eventNotifiers == null) this._eventNotifiers = {};
        var subTypes = this._eventNotifiers[type];       
        if (subTypes == undefined) subTypes = this._eventNotifiers[type] = {};
        
        if (subTypes[type] == undefined) {
            subTypes[subType] = true;
            subTypes.count++;
        }
    },
    
    unregisterEventNotifier: function(type, subType) {
        var subTypes = this._eventNotifiers[type];        
        
        if (subTypes != undefined && subTypes[subType] == true) {            
            delete subTypes[subType];
            subTypes.count--;
            if (subTypes.count == 0) delete this._eventNotifiers[type];
        }
    },
    
    //NOTE: We never need to update the server about a clicked event if their are no listeners
    // since no state change occurs.
    fireAction: function(subType, eventData) {
        if (this._eventNotifiers != null) {
            var subTypes = this._eventNotifiers["action"];            
            
            if (subTypes != undefined && subTypes[subType] === true) {
                tw_em.postViewStateChanged(this._id, subType, eventData);
            }
        }
    },
    
    firePropertyChange: function(name, value, key) {
        var props = undefined;
        if (this._eventNotifiers != null) props = this._eventNotifiers["propertyChange"];                        

        if (props != undefined && props[name] === true) {
            tw_em.postViewStateChanged(this._id, name, value);
        } else {
            tw_em.queueViewStateChanged(this._id, name, value, key);
        }                          
    },
    
    hasPropertyChangeListener: function(name) {
        var props = undefined;
        if (this._eventNotifiers != null) props = this._eventNotifiers["propertyChange"];                        
        return props != undefined && props[name] === true;
    },

        
    getNextComponent: function(usable) {
        var parent = this._parent;
        var comp = null;
        
        if (parent != null) {
            var index = this._parentIndex;
            
            if (this instanceof tw_TabFolder && this._currentIndex >= 0) {
                parent = this._children[this._currentIndex];
                index = -1;
            }
            
            do {    
                index++;                
                
                while (index >= parent._children.length) {                    
                    //Go to the next container, if that fails, to first component of this container                    
                    if (parent._parent != null) {                        
                        index = parent._parentIndex + 1;
                        parent = parent._parent;
                    } else {
                        index = 0;
                    }
                }
                
                var comp = parent._children[index];

                if (comp instanceof tw_BaseContainer && !(comp instanceof tw_TabFolder)) {
                    parent = comp;
                    index = -1;
                    var notUsable = true;
                } else if (usable) {
                    var notUsable = !(comp.isVisible() && comp.isEnabled() && comp.isFocusCapable());
                } else {
                    var notUsable = false;
                }
            } while (notUsable);            
        }
        
        return comp;
    },
    
    getPriorComponent: function(usable) {
        var parent = this._parent;
        var comp = null;
        
        if (parent != null) {
            var index = this._parentIndex;
            
            do {
                index--;
                
                while (index < 0) {                    
                    //Go to the prior container, if that fails, to last component of this container                    
                    if (parent._parent != null) {
                        index = parent._parentIndex - 1;
                        parent = parent._parent;
                        
                        //TODO: find a better solution to the double-nested tabfolder setup so we 
                        //don't have to code specifically for the tabfolder.
                        if (parent instanceof tw_TabFolder) {
                            index = parent._parentIndex - 1;
                            parent = parent._parent;
                        }
                    } else {
                        index = parent._children.length - 1;
                    }
                }                
                
                var comp = parent._children[index];

                if (comp instanceof tw_BaseContainer) {
                    parent = comp;
                    if (parent instanceof tw_TabFolder) parent = parent._children[parent._currentIndex];                    
                    index = parent._children.length;
                    var notUsable = true;
                } else if (usable) {
                    var notUsable = !(comp.isVisible() && comp.isEnabled() && comp.isFocusCapable());
                } else {
                    var notUsable = false;
                }
            } while (notUsable);
        }
        
        return comp;
    },
        
    //return true if the key should propagate up to other listeners
    keyPressNotify: function(keyPressCombo) {
        if ((keyPressCombo == "Tab" || keyPressCombo == "Shift-Tab") && this._parent instanceof tw_BaseContainer) {
            if (tw_useSmartTab) {
                var comp = keyPressCombo == "Tab" ? this.getNextComponent(true) : this.getPriorComponent(true);
                if (comp == null) comp = this;
                comp.setFocus(true);
                return false;
            } else {
                return true;
            }
        } else if (this._eventNotifiers != null) {
            var keyPressNotifiers = this._eventNotifiers["keyPress"]; 
            
            if (keyPressNotifiers != undefined) {
                if (keyPressNotifiers[keyPressCombo] == true) {
                    tw_em.postViewStateChanged(this._id, "keyPress", keyPressCombo);
                    return false;
                }
            }
        }

        return true;
    },
            
    init: function(insertAtIndex, props) {
        var styleClass = props.styleClass;
        var style = tw_Component.defaultStyles[styleClass];
        delete props.styleClass;
        
        var styleProps = props.styleProps;
        if (styleProps != undefined) {
            delete props.styleProps;
            
            for (var name in style) {
                if (styleProps[name] == undefined) styleProps[name] = style[name]; 
            }
            
            style = styleProps;
        }
        
        this.setStyle("backgroundColor", style["backgroundColor"]);
        this.setStyle("borderType", style["borderType"]);
        this.setStyle("borderSize", style["borderSize"]);
        this.setStyle("borderColor", style["borderColor"]);
        this.setStyle("fontFamily", style["fontFamily"]);
        this.setStyle("fontColor", style["fontColor"]);
        this.setStyle("fontSize", style["fontSize"]);
        this.setStyle("fontItalic", style["fontItalic"]);
        this.setStyle("fontBold", style["fontBold"]);
        this.setStyle("fontUnderline", style["fontUnderline"]);
        
        if (props.insertAtIndex != undefined) {
            insertAtIndex = props.insertAtIndex;
            delete props.insertAtIndex;
        }
        
        for (var prop in props) {
            this[this.__setters__[prop]](props[prop]);
        }
        
        if (this._parent != null) {
            if (this._parent instanceof tw_BaseContainer) {
                if (this instanceof tw_Menu && this._windowMenu) {
                    this._parent.setMenu(this);
                } else {
                    this._parent.addComponent(insertAtIndex, this);
                }
            } else if (this._parent instanceof tw_DropDown || this._parent instanceof tw_GridBox) {
                this._parent.getBaseWindow()._box.appendChild(this._box);
            } else {
                alert("No known way to attach this component:" + this._box.className + " to the DOM");
            }
        } else {
            document.body.appendChild(this._box);
        }
        
        //TODO: We shouldn't have to call this a second time, but it appears that either a browser
        //bug is requiring that we do, or there is a glitch in the framework.
        this.setStyle("backgroundColor", style["backgroundColor"]);
        this._inited = true;
    },
    
    destroy: function() {        
        this._inited = false;
        delete tw_Component.instances[this._id];                   
        this._box = this._focusBox = this._eventNotifiers = this._backgroundBox = 
            this._borderBox = this._fontBox = this._parent = null;
        if (tw_Component.priorFocus === this) tw_Component.priorFocus = null;
        if (tw_Component.currentFocus === this) tw_Component.currentFocus = null;
    }
});

tw_Component.zIndex = 0;
tw_Component.instances = {};
tw_Component.priorFocus = null;
tw_Component.currentFocus = null;
tw_Component.styleNameMap = {
    backgroundColor: "backgroundColor",
    fontSize: "fontSize",
    fontBold: "fontWeight",
    fontItalic: "fontStyle",
    fontUnderline: "textDecoration",
    fontColor: "color",
    fontFamily: "fontFamily",
    borderType: "borderStyle",
    borderSize: "borderWidth",
    borderColor: "borderColor"
};

tw_Component.defaultStyles = { };
tw_Component.setDefaultStyle = function(styleName, style) {
    tw_Component.defaultStyles[styleName] = style;
};

tw_Component.getIEBorder = function(color, type) {
    if (tw_isIE && (type == "inset" || type == "outset" || type == "groove" || type == "ridge") && (color == tw_COLOR_BUTTONFACE || color == tw_COLOR_THREEDFACE)) color = "";
    return color;
};

tw_Component.setSystemColors = function(systemColors) {
    for (var name in systemColors) {
        varName = "tw_COLOR_" + name.toUpperCase();
        window[varName] = systemColors[name];
    }
};

tw_Component.expandUrl = function(url, wrapInUrl) {
    if (url == null) return "";
    if (url.indexOf("%SYSROOT%") == 0) url = tw_BASE_PATH + "resources/" + url.substring(9);
    if (wrapInUrl && url.length > 0) url = "url(" + url + ")";
    return url;
};

//NOTE: This function is defined here so it can be shared by the unrelated classes: Image, Hyperlink, Label & BaseCheckRadio.
tw_Component.keyPressNotifySpaceFireAction = function(keyPressCombo) {
    if (keyPressCombo == "Space") {
        this._clickListener();
        return false;
    } else {
        return this.$.keyPressNotify.apply(this, [keyPressCombo]);
    }
};

tw_Component.clickListener = function(ev) {
    if (!this.isEnabled()) return;
    if (this.isFocusCapable()) this.setFocus(true);
    var action = tw_Component.getClickAction(ev.type);
    if (action == null) return;
    this.fireAction(action);
};

tw_Component.getClickAction = function(type, index) {
    if (type == "click") {
        var now = new Date();
        if (now - this._clickTime < 500 && (index == null || index == this._lastIndex)) return null; 
        this._clickTime = now;
        if (index != null) this._lastIndex = index;
        return "click";
    } else if (type == "dblclick") {
        return "doubleClick";
    }
}

