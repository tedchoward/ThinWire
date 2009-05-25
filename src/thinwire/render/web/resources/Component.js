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
    _eventNotifiers: null,
    _inited: false,
    _opacity: 100,
    _focusBox: null,
    _isInFocus: false,
    _id: -1,
    _parent: null,
    _parentIndex: -1,
    _x: 0,
    _y: 0,
     _menuOpenedTime:-1,
    _width: 0,
    _height: 0,
    _enabled: true,
    _visible: true,
    _focusCapable: true,
    _backgroundBox: null,
    _backgroundColor: "",
    _fontColor: null,
    _borderBox: null,
    _scrollBox: null,
    _borderColor: null,
    _borderType: null,
    _borderImage: null,
    _borderSize: 0,
    _borderSizeSub: 0,
    _boxSizeSub: 0,
    _fontBox: null,
    _clickTime: null,
    _dragAndDropHandler: null,
    
    construct: function(tagName, className, id, containerId) {
        var box = document.createElement(tagName);
        box.className = className;
        var s = box.style;
		var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;";
		tw_Component.setCSSText(cssText, box);
        box.id = id;
        this._box = this._focusBox = this._backgroundBox = this._borderBox = this._fontBox = box;
        this._id = id;
        
        tw_Component.instances[id] = this;
        this._parent = containerId instanceof Class ? containerId : tw_Component.instances[containerId];
        this._focus = this._focus.bind(this);
    },
            
    setX: function(x) {
        this._x = x;
        this._box.style.left = x + "px";
    },
    
    setY: function(y) {
        this._y = y;
        this._box.style.top = y + "px";
    },

    setWidth: function(width) {
        this._width = width;
        width -= this._boxSizeSub;        
        if (this._box === this._borderBox && this._borderImage == null) width -= this._borderSizeSub;
        if (width < 0) this._width = width = 0;
        this._box.style.width = width + "px";
        if (this._borderImage != null) this._borderImage.setWidth(this._width);
    },

    setHeight: function(height) {
        this._height = height;
        height -= this._boxSizeSub;
        if (this._box === this._borderBox && this._borderImage == null) height -= this._borderSizeSub;
        if (height < 0) this._height = height = 0;
        this._box.style.height = height + "px";
        if (this._borderImage != null) this._borderImage.setHeight(this._height);
    },
    
    isVisible: function() {
        return this._box.style.display == "block";
    },
    
    setVisible: function(visible) {
        this._visible = visible;
        this._box.style.display = visible ? "block" : "none";
    },
    
    setOpacity: function(opacity) {
        this._opacity = opacity;
        tw_setOpacity(this._box, opacity);
        this._box.style.display = opacity > 0 && this._visible ? "block" : "none";
    },
    
    setFocusCapable: function(focusCapable) {
        this._focusCapable = focusCapable;
    },
    
    setEnabled: function(enabled) {
        if (!enabled && this === tw_Component.currentFocus) {
            var nextComp = this.getNextComponent(true);
            if (nextComp != null && nextComp !== this) nextComp.setFocus(true);
        }
        
        this._enabled = enabled;
        
        if (this._fontBox != null) {
	        if (enabled) {
	        	this._fontBox.style.color = this._fontColor;
	        } else {
	        	this._fontColor = this._fontBox.style.color;
				if (this instanceof tw_Button || this instanceof tw_Menu || tw_COLOR_GRAYTEXT != "graytext") this._fontBox.style.color = tw_COLOR_GRAYTEXT;
			}
        }
        
        
        if (this._backgroundBox != null) {
	        if (enabled) {
	        	this._backgroundBox.style.backgroundColor = this._backgroundColor;
	        } else {
	        	this._backgroundColor = this._backgroundBox.style.backgroundColor;
	        	if (tw_COLOR_BACKGROUND != "background" && (this instanceof tw_BaseCheckRadio || this instanceof tw_BaseText || 
                	this instanceof tw_DateBox || this instanceof tw_GridBox || this instanceof tw_Tree || this instanceof tw_WebBrowser ||
                	this instanceof tw_ProgressBar))
                	this._backgroundBox.style.backgroundColor = tw_COLOR_BACKGROUND == tw_COLOR_WINDOW ? this._parent._backgroundBox.style.backgroundColor : tw_COLOR_BACKGROUND;
	        }
        }
    },
    
    setBackgroundColor: function(color) { this.setStyle("backgroundColor", color); },
    setBorderColor: function(color) { this.setStyle("borderColor", color); },
    setFontColor: function(color) { this.setStyle("color", color); },
    
    setPropertyWithEffect: function(prop, time, seq, value) {
        if (!this._inited) return;
		var realSetter = null;
		var after = null;
		
		if (prop == "visible") {
			prop = "opacity";
			realSetter = "setVisible";
			after = !value;
		}
		
        prop = prop.charAt(0).toUpperCase() + prop.substring(1);
        var set = "set" + prop;
        new tw_Animation(this, set, time, eval(seq), realSetter, value, after).start();
    },
    
    _focusListener: function() {
        if (this._focusCapable) this.setFocus(true);
    },
    
    _blurListener: function() {
        this.setFocus(false);        
    },

    _focusGained: function(){
        this._isInFocus = true;
    },

    _focusLost: function(){
        this._isInFocus = false;
    },

        
    setFocus: function(focus) {
        if (!this._enabled || !this.isVisible()) return;
        if (!(this._parent instanceof tw_BaseContainer)) return;
        
        if (focus) {
            if (tw_Component.currentFocus !== this) {
                //We don't need to send a false event to the server, because a true event on the current
                //component will trigger a false event on the prior. The same holds true for client code.
                tw_Component.priorFocus = tw_Component.currentFocus; 
                tw_Component.currentFocus = this;
                tw_em.removeQueuedViewStateChange("focus");
                this.firePropertyChange("focus", true, "focus");
                if (tw_Component.priorFocus != null && tw_Component.priorFocus.hasPropertyChangeListener("focus")) tw_em.sendGetEvents();
                tw_setElementFocus(this, true);                
                var isPriorButton = tw_Component.priorFocus instanceof tw_Button;
                var isButton = this instanceof tw_Button;
                
                if (!isButton && isPriorButton) {
                    var sButton = this.getBaseWindow().getStandardButton();
                    if (sButton != null && sButton._enabled) sButton._setStandardStyle(true);
                } else if (!isPriorButton && isButton) {
                    var sButton = this.getBaseWindow().getStandardButton();
                    if (sButton != null && this !== sButton) sButton._setStandardStyle(false);
                }

                this._focusGained();
            }
        }
        else{
            if (tw_Component.currentFocus !== this) {
                this._focusLost();
            }
        }
    },
    
    _focus: function() {
        if(this._focusBox != null){
            this._focusBox.focus();
        }
    },
    
    setStyles: function(styles) {
        for (var name in styles) {
            this.setStyle(tw_Component.rtStyleMap[name], styles[name]); 
        }
    },
    
    setStyle: function(name, value) {
        if (name.indexOf("ba") == 0) {
            var s = this._backgroundBox == null ? null : this._backgroundBox.style;
            
            if (name == "backgroundColor") {
                this._backgroundColor = value;
                if (!this._enabled && tw_COLOR_BACKGROUND != "background" && tw_COLOR_BACKGROUND != "window") s = null;
            } else if (name == "backgroundImage") {
                value = tw_Component.expandUrl(value, true);
            }
        } else if (name.indexOf("bo") == 0) {
            var s = this._borderBox == null ? null : this._borderBox.style;
            
            if (name == "borderWidth") {
                this._borderSize = parseInt(value, 10);
                this._borderSizeSub = this._borderSize * 2;
                
                if (this._borderImage == null) {                    
					s[name] = value;
                } else {
                    this._borderImage.setBorderSize(this._borderSize);
                }

                s = null;
                
                if (this._inited) {
                    this.setWidth(this._width);
                    this.setHeight(this._height);
                }
            } else if (name == "borderStyle") {
                this._borderType = value;
               	s[name] = value;

                if (tw_isIE && this._borderColor != null) {
                    name = "borderColor";
                    value = tw_Component.getIEBorder(this._borderColor, this._borderType);
                }
            } else if (name == "borderColor") {
                this._borderColor = value;
                value = tw_Component.getIEBorder(value, this._borderType);
            } else if (name == "borderImage") {
                var bi = this._borderImage;

                if (value.length == 0 && bi != null) {
                    this._borderImage = null;
                    if (this._borderBox === this._box) this._box = bi._box;
                    this._borderBox = bi._box;
                    bi.destroy();
                } else if (value.length > 0) {
                    if (bi == null) {
                        bi = this._borderImage = new tw_BorderImage();
                        var bb = bi.setBox(this._borderBox);
                        if (this._borderBox === this._box) this._box = bb;
                        this._borderBox = bb;
                    }
                    
                    var ary = value.split(',');
                    bi.setImage(ary[0], ary[1], ary[2]);
                }
                
                this.setStyle("borderWidth", this._borderSize + "px");
                s = null;                
            }
        } else {
            var s = this._fontBox == null ? null : this._fontBox.style;
            if (name == "color") {
            	this._fontColor = value;
				if (!this._enabled && tw_COLOR_GRAYTEXT != "graytext") s = null;
           	}
        }
        
        if (s != null) s[name] = value;
    },
    
    getBaseWindow: function() {
        var parent = this;
        
        do {        
            var parent = parent._parent;
        } while (parent != null && !(parent instanceof tw_Dialog) && !(parent instanceof tw_Frame));
        
        return parent;
    },
    
    getDragBox: function(event) {
        var box = this._box.cloneNode(true);
        box._dragObject = "";
        return box;
    },
    
    getDragArea: function() {
        return this._box;
    },
    
    getDropArea: function() {
        return this._box;
    },
    
    getDropTarget: function() {
        return "";
    },
    
    addDragTarget: function(compId) {
        if (this._dragAndDropHandler == null) this._dragAndDropHandler = new tw_DragAndDropHandler(this);
        var target = tw_Component.instances[compId];
        this._dragAndDropHandler.addTarget(target);
    },
    
    removeDragTarget: function(compId) {
        if (this._dragAndDropHandler.removeTarget(compId)) {
            this._dragAndDropHandler.destroy();
            this._dragAndDropHandler = null;
        }
    },
    
    registerEventNotifier: function(type, subType) {
        if (this._eventNotifiers == null) this._eventNotifiers = {};
        var subTypes = this._eventNotifiers[type];       
        if (subTypes == undefined) subTypes = this._eventNotifiers[type] = {};
        
        if (subTypes[subType] == undefined) {
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
    
    //NOTE: We never need to update the server about a clicked event if there are no listeners
    // since no state change occurs.
    fireAction: function(ev, action, source) {
        if (this._eventNotifiers != null) {
            var actions = this._eventNotifiers["action"];            
            
            if (actions != undefined && actions[action] === true) {
                var x = 0, y = 0;
                
                if (ev != null) {
                    var clickBox = this.getClickBox();
                    x = tw_getEventOffsetX(ev, clickBox.className);
                    y = tw_getEventOffsetY(ev, clickBox.className);
                    if (x < 0) x = 0;
                    if (y < 0) y = 0;
                }
                
                if (source == null) source = "";
                tw_em.sendViewStateChanged(this._id, action, x + "," + y + "," + source);
            }
        }
    },
    
    getClickBox: function() {
        return this._box;
    },
        
    fireDrop: function(source, dragComponent, dragObject, dragX, dragY, dropX, dropY) {
        tw_em.sendViewStateChanged(this._id, "drop", source + "," + dragComponent._id + "," + dragObject + "," + dragX + "," + dragY + "," + dropX + "," + dropY);
    },
    
    firePropertyChange: function(name, value, key) {
        var props = undefined;
        if (this._eventNotifiers != null) props = this._eventNotifiers["propertyChange"];                        

        if (props != undefined && props[name] === true) {
            tw_em.sendViewStateChanged(this._id, name, value);
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

                if (comp instanceof tw_BaseContainer && comp._focusCapable && comp.isVisible() && !(comp instanceof tw_TabFolder)) {
                    parent = comp;
                    index = -1;
                    var notUsable = true;
                } else if (usable) {
                    var notUsable = !(comp.isVisible() && comp._enabled && comp._focusCapable);
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

                if (comp instanceof tw_BaseContainer && comp._focusCapable && comp.isVisible()) {
                    parent = comp;
                    if (parent instanceof tw_TabFolder) parent = parent._children[parent._currentIndex];                    
                    index = parent._children.length;
                    var notUsable = true;
                } else if (usable) {
                    var notUsable = !(comp.isVisible() && comp._enabled && comp._focusCapable);
                } else {
                    var notUsable = false;
                }
            } while (notUsable);
        }
        
        return comp;
    },
        
    //return true if the key should propagate up to other listeners
    keyPressNotify: function(keyPressCombo) {
        if ((keyPressCombo == "Tab" || keyPressCombo == "Shift-Tab") && (this._parent instanceof tw_BaseContainer || this._parent instanceof tw_DropDown)) {
            if (tw_useSmartTab) {
                if (this._parent instanceof tw_DropDown) this.setVisible(false);
                var tabComp = this._parent instanceof tw_BaseContainer ? this : this._parent;
                var comp = keyPressCombo == "Tab" ? tabComp.getNextComponent(true) : tabComp.getPriorComponent(true);
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
                    tw_em.sendViewStateChanged(this._id, "keyPress", keyPressCombo);
                    return false;
                }
            }
        }

        return true;
    },
            
    init: function(insertAtIndex, props) {
        if (this._scrollBox != null) tw_Component.styleScrollBars(this._scrollBox);
        var styleClass = props.styleClass;
        var style = tw_Component.defaultStyles[styleClass];
        delete props.styleClass;
        var styleProps = props.styleProps;
        
        if (styleProps != undefined) {
            delete props.styleProps;
            
            for (var name in styleProps) {
                styleProps[tw_Component.rtStyleMap[name]] = styleProps[name];
            }
            
            for (var name in style) {
                if (styleProps[name] == undefined) styleProps[name] = style[name]; 
            }
            
            style = styleProps;
        }
		
       	this.setStyle("backgroundColor", style["backgroundColor"]);
        this.setStyle("backgroundImage", style["backgroundImage"]);
        this.setStyle("backgroundRepeat", style["backgroundRepeat"]);
        this.setStyle("backgroundPosition", style["backgroundPosition"]);
        
        if (this._borderBox != null) {
			var borderStyle = style["borderStyle"];
           	if (borderStyle != undefined) this.setStyle("borderStyle", borderStyle);
            this.setStyle("borderWidth", style["borderWidth"]);
            this.setStyle("borderColor", style["borderColor"]);
			this.setStyle("borderImage", style["borderImage"]);
        }
        
        if (this._fontBox != null) {
           	this.setStyle("fontFamily", style["fontFamily"]);
            this.setStyle("color", style["color"]);
            this.setStyle("fontSize", style["fontSize"]);
            this.setStyle("fontStyle", style["fontStyle"]);
            this.setStyle("fontWeight", style["fontWeight"]);
           	this.setStyle("textDecoration", style["textDecoration"]);
        }
        
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
                alert("init:append failed");
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
        if (this._borderImage != null) this._borderImage.destroy(); 
        delete tw_Component.instances[this._id];
        this._box = this._focusBox = this._eventNotifiers = this._backgroundBox = 
            this._borderBox = this._fontBox = this._scrollBox = this._parent = this._borderImage = null;
            
        if (tw_Component.priorFocus === this) tw_Component.priorFocus = null;
        if (tw_Component.currentFocus === this) tw_Component.currentFocus = null;
        if (this._dragAndDropHandler != null) {
            this._dragAndDropHandler.destroy();
            this._dragAndDropHandler = null;
        }
    }
});

tw_Component.zIndex = 0;
tw_Component.instances = {};
tw_Component.priorFocus = null;
tw_Component.currentFocus = null;
tw_Component.defaultStyles = null;
tw_Component.setDefaultStyles = function(defaultStyles) {
    var styles = {};
    
    for (var key in defaultStyles) {
        var nStyle = styles[key] = {};
        var oStyle = defaultStyles[key];
        
        for (var key in oStyle) {
            nStyle[tw_Component.rtStyleMap[key]] = oStyle[key];
        }
    }
    
    tw_Component.defaultStyles = styles;
};

tw_Component.styleScrollBars = function(box) {
    var s = box.style;
    
    if (s.scrollbarBaseColor != undefined && tw_COLOR_SCROLLBAR != "scrollbar") {
        if (tw_COLOR_SCROLLBAR != tw_COLOR_BUTTONFACE) {
            s.scrollbarBaseColor = tw_COLOR_SCROLLBAR;
        } else {
            s.scrollbarFaceColor = tw_COLOR_BUTTONFACE;
            
            if (tw_COLOR_BUTTONHIGHLIGHT == tw_COLOR_BUTTONSHADOW) {
                var color = tw_COLOR_BUTTONSHADOW == tw_COLOR_BUTTONFACE ? "" : tw_COLOR_BUTTONSHADOW;
                s.scrollbarHighlightColor = s.scrollbarShadowColor = s.scrollbarDarkShadowColor = s.scrollbar3dLightColor = color;
            } else {
                s.scrollbarHighlightColor = s.scrollbar3dLightColor = tw_COLOR_BUTTONHIGHLIGHT;
                s.scrollbarShadowColor = s.scrollbarDarkShadowColor = tw_COLOR_BUTTONSHADOW;
            }
        }
    }
};

tw_Component.applyButtonBorder = function(box, top, right, bottom, left) {
    var s = box.style;
    
    if (tw_COLOR_BUTTONHIGHLIGHT == tw_COLOR_BUTTONSHADOW) {
        if (tw_COLOR_BUTTONSHADOW == tw_COLOR_BUTTONFACE) {
            s.borderStyle = "outset";
            s.borderColor = tw_Component.getIEBorder(tw_COLOR_BUTTONFACE, "outset");
        } else {
            s.borderStyle = "solid";
                        
            if (top || right || bottom || left) {
                var BF = tw_COLOR_BUTTONFACE;
                var BS = tw_COLOR_BUTTONSHADOW;
                s.borderColor = (top ? BF : BS) + " " + (right ? BF : BS) + " " + (bottom ? BF : BS) + " " + (left ? BF : BS);
            } else {
                s.borderColor = tw_COLOR_BUTTONSHADOW;
            }
        }
    } else {
        s.borderStyle = "solid";
        s.borderColor = tw_COLOR_BUTTONHIGHLIGHT + " " + tw_COLOR_BUTTONSHADOW + " " + tw_COLOR_BUTTONSHADOW + " " + tw_COLOR_BUTTONHIGHLIGHT;
    }
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

tw_Component.setSystemImages = function(systemImages) {
    for (var name in systemImages) {
        varName = "tw_IMAGE_" + name.toUpperCase();
        window[varName] = tw_Component.expandUrl(systemImages[name]);
    }
};

tw_Component.expandUrl = function(url, wrapInUrl) {
    if (url == null) return "";
    if (url.indexOf("%SYSROOT%") == 0) url = tw_APP_URL + "?_twr_=" + url.substring(9);
    if (wrapInUrl && url.length > 0) url = "url(" + url + ")";
    return url;
};

//NOTE: This function is defined here so it can be shared by the unrelated classes: Image, Hyperlink, Label & BaseCheckRadio.
tw_Component.keyPressNotifySpaceFireAction = function(keyPressCombo) {
    if (keyPressCombo == "Space") {
        var ev = {type:"click"};
        this._clickListener(ev);
        return false;
    } else {
        return arguments.callee.$.call(this, keyPressCombo);
    }
};

tw_Component.clickListener = function(ev, comp) {
    if (comp == null) comp = this;
    if (!comp._enabled) return;
    if (comp._focusCapable) comp.setFocus(true);
    var action = tw_Component.getClickAction(ev.type);
    if (action == null) return;
    comp.fireAction(ev, action);
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
};

tw_Component.setText = function(text) {
    this._box.replaceChild(tw_Component.setRichText(text), this._box.firstChild);
};

tw_Component.rtStyleMap = {
    ff: "fontFamily",
    fc: "color",
    fs: "fontSize",
    fw: "fontWeight",
    fd: "textDecoration",
    ft: "fontStyle",
    dt: "borderStyle",
    dw: "borderWidth",
    dc: "borderColor",
    di: "borderImage",
    dtl: "borderLeftStyle",
    dwl: "borderLeftWidth",
    dcl: "borderLeftColor",
    dtr: "borderRightStyle",
    dwr: "borderRightWidth",
    dcr: "borderRightColor",
    dtt: "borderTopStyle",
    dwt: "borderTopWidth",
    dct: "borderTopColor",
    dtb: "borderBottomStyle",
    dwb: "borderBottomWidth",
    dcb: "borderBottomColor",
    bc: "backgroundColor",
    bp: "backgroundPosition",
    br: "backgroundRepeat",
    bi: "backgroundImage"
};

tw_Component.rtAttrMap = {
    r: "href",
    t: "target",
    s: "src",
    w: "width",
    h: "height"
};

tw_Component.nonBreakingSpaceRegEx = /( ) |^ /g;
tw_Component.nonBreakingSpaceReplace = "$1" + String.fromCharCode(160);

tw_Component.processRichTextNode = function(node, element) {
    if (node instanceof Object) {
        if (element == null) element = document.createElement(node.t);

        if (node.s != undefined) {
            for (var sty in node.s) {
                var val = node.s[sty];
                if (sty == "bi") val = tw_Component.expandUrl(val, true);
                sty = tw_Component.rtStyleMap[sty];
                element.style[sty] = val;
            }
        }
        
        if (node.a != undefined) {
            for (var atr in node.a) {
                var val = node.a[atr];
                if (atr == "s" || atr == "h") val = tw_Component.expandUrl(val);
                atr = tw_Component.rtAttrMap[atr];
                element[atr] = val;
            }
        }
        
        if (node.c != undefined) element.appendChild(tw_Component.setRichText(node.c));    
        return element;
    } else {
        var textNode = document.createTextNode(node.replace(tw_Component.nonBreakingSpaceRegEx, tw_Component.nonBreakingSpaceReplace));
        if (element != null) element.appendChild(textNode);
        return textNode;
    }
};

tw_Component.setRichText = function(text, element) {
    if (typeof(text) == "string" || text instanceof String) {
        var textNode = document.createTextNode(text.replace(tw_Component.nonBreakingSpaceRegEx, tw_Component.nonBreakingSpaceReplace));
    
        if (element == null) {
        	return textNode;
        } else {
        	element.appendChild(textNode);
        	return element;
        }
    } else {
        if (text instanceof Array) {
            if (text.length > 1) {
                if (element == null) element = document.createElement("span");
                
                for (var n in text) {
                    element.appendChild(tw_Component.processRichTextNode(text[n]));       
                }
            } else {
                element = tw_Component.processRichTextNode(text[0], element);
            }
        } else {
            element = tw_Component.processRichTextNode(text, element);
        }

	    return element;
    }
};

tw_Component.camelCaseRegex = /\-(.)/g;

tw_Component.camelCaseReplaceFunc = function(m, l) { return l.toUpperCase(); };

tw_Component.setCSSText = function(cssText, box) {
	if (tw_useCSSText) {
		box.style.cssText = cssText;
	} else {
		var s = box.style;
		var styleEntries = cssText.split(";");
		
		for (var i = 0; i < styleEntries.length; i++) {
			var entry = styleEntries[i].split(":");
			if (entry.length == 2) s[entry[0].replace(tw_Component.camelCaseRegex, tw_Component.camelCaseReplaceFunc)] = entry[1];
		}
	}
};