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
    _borderBox: null,
    _fontBox: null,
    _enabledBackgroundColor: "",
    _disabledBackgroundColor: "threedface",
    
    construct: function(tagName, className, id, containerId, support) {
        var box = document.createElement(tagName);
        box.className = className;
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
        if (width < 0) width = 0;
        this._width = width;
        this._box.style.width = width + "px";
    },
    
    getWidth: function() {
        return this._width;
    },

    setHeight: function(height) {
        if (height < 0) height = 0;
        this._height = height;
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
        this._enabled = enabled;
        
        if (enabled) {
            this._backgroundBox.style.backgroundColor = this._enabledBackgroundColor;
        } else {
            this._enabledBackgroundColor = this._backgroundBox.style.backgroundColor;
            this._backgroundBox.style.backgroundColor = this._disabledBackgroundColor; 
        }
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
        if (!this.isEnabled() || !this.isVisible()) return false;
        
        if (focus) {
            if (tw_Component.currentFocus !== this) {
                if (tw_Component.currentFocus != null) {
                    tw_Component.currentFocus.setFocus(false);
                    tw_Component.currentFocus.firePropertyChange("focus", false);
                }
                
                tw_Component.currentFocus = this;
                this.firePropertyChange("focus", true);
                
                try {
                    if (this._focusBox.focus) this._focusBox.focus();
                } catch (e) {
                    //Firefox sometimes throws an error when attemptting to set focus.
                    //ignore the error for now until solution is found.
                }
                
                return true;
            }
        }
        
        return false;
    },
        
    setStyle: function(name, value) {
        if (name == "backgroundColor") {
            if (this.isEnabled()) {
                this._backgroundBox.style.backgroundColor = value;
            } else {
                this._enabledBackgroundColor = value;
            }
        } else if (name.indexOf("font") == 0) {
            if (name == "fontSize") {
                value += "pt";
            } else if (name == "fontBold") {
                name = "fontWeight";
                value = value.toLowerCase() == "true" ? "bold" : "normal";
            } else if (name == "fontItalic") {
                name = "fontStyle";
                value = value.toLowerCase() == "true" ? "italic" : "normal";
            } else if (name == "fontUnderline") {
                name = "textDecoration";
                value = value.toLowerCase() == "true" ? "underline" : "none";
            } else if (name == "fontColor") {
                name = "color";
            } else if (name != "fontFamily") {
                throw "attempt to set unknown property '" + name + "' to value '" + value + "'";
            }
            
            this._fontBox.style[name] = value;
        } else if (name.indexOf("border") == 0) {
            if (name == "borderType") {
                name = "borderStyle";
            } else if (name == "borderSize") {
                //value += "px";
                return;                
            } else if (name != "borderColor") {
                throw "attempt to set unknown property '" + name + "' to value '" + value + "'";
            }
            
            this._borderBox.style[name] = value;
        } else {
            throw "attempt to set unknown property '" + name + "' to value '" + value + "'";
        }
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
    
    firePropertyChange: function(name, value) {
        var props = undefined;
        if (this._eventNotifiers != null) props = this._eventNotifiers["propertyChange"];                        

        if (props != undefined && props[name] === true) {
            tw_em.postViewStateChanged(this._id, name, value);
        } else {
            tw_em.queueViewStateChanged(this._id, name, value);
        }                           
        
        //if (this instanceof tw_TextField && name == "text") {
        //}

        //tw_em.postViewStateChanged(this._id, name, value);
    },
        
    getNextComponent: function(usable) {
        var parent = this._parent;
        var comp = null;
        
        if (parent != null) {
            var index = this._parentIndex;
            
            //If we are currently focused on a container, then transition into the container
            /*if (this instanceof tw_BaseContainer && this._children.length > 0) {
                if (this instanceof tw_TabFolder && this._currentIndex >= 0) parent = this._children[this._currentIndex];
                else parent = this;                
                index = -1;
            }*/
            
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
                        
                        //TODO: find a better solution to the double-nested tabfolder setup so we 
                        //don't have to code specifially for the tabfolder.
                        /*if (parent instanceof tw_TabFolder) {                            
                            index = parent._parentIndex + 1;
                            parent = parent._parent;
                        }*/
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
                
            //If we are currently focused on a container, then transition into the container
            /*if (this instanceof tw_BaseContainer && this._children.length > 0) {
                if (this instanceof tw_TabFolder && this._currentIndex >= 0) parent = this._children[this._currentIndex];
                else parent = this;                
                index = -1;
            } */                       
            
            do {
                index--;
                    
                //if (index < 0) {                    
                    //Go to the prior container, if that fails, to last component of this container
                  //  parent = parent.getPriorComponent(usable);
                    //if (!(parent instanceof tw_BaseContainer) || parent._children.length == 0) parent = this._parent;
                    //index = parent._children.length - 1;
                //}
                
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
                            //alert(parent instanceof tw_Frame);
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
            //Opera does not allows us to cancel the Tab or Shift-Tab keys, so we can't do this spiffy tab-transition
            //TODO: Check safari.
            if (tw_isOpera) {
                return false;
            } else {
                var comp = keyPressCombo == "Tab" ? this.getNextComponent(true) : this.getPriorComponent(true);
                if (comp == null) comp = this;
                comp.setFocus(true);
                return false;
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
            } else if (this._parent instanceof tw_DropDownGridBox || this._parent instanceof tw_GridBox) {
                this._parent.getBaseWindow().getBox().appendChild(this._box);
            } else {
                alert("No known way to attach this component:" + this._box.className + " to the DOM");
            }
        } else {
            document.body.appendChild(this._box);
        }
    },
    
    destroy: function() {        
        delete tw_Component.instances[this._id];                   
        this._box = this._focusBox = this._eventNotifiers = this._backgroundBox = 
            this._borderBox = this._fontBox = this._parent = null;
        if (tw_Component.currentFocus === this) tw_Component.currentFocus = null; 
    }
});

tw_Component.zIndex = 0;
tw_Component.instances = {};
tw_Component.currentFocus = null;
//NOTE: This function is defined here so it can be shared by the unrelated classes: Image, Hyperlink, Label & BaseCheckRadio.
tw_Component.keyPressNotifySpaceFireAction = function(keyPressCombo) {
    if (keyPressCombo == "Space") {
        this._clickListener();
        return false;
    } else {
        return this.$.keyPressNotify.apply(this, [keyPressCombo]);
    }
};
