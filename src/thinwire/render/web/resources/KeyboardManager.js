/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
//TODO: In IE, menu accelerators are not blocked by this approach.
//TODO: In IE, F1 is not blocked, but all other Function keys are.
//TODO: In Opera, no key strokes are blocked, but key bindings do fire.
var tw_KeyboardManager = Class.extend({
    _ctrlAltShift: "Ctrl-Alt-Shift-",
    _ctrlAlt: "Ctrl-Alt-",
    _ctrlShift: "Ctrl-Shift-",
    _ctrl: "Ctrl-",
    _altShift: "Alt-Shift-",
    _alt: "Alt-",
    _shift: "Shift-",
    _keyNameTable: {189: "Dash", 187: "=", 192: "`", 219: "[", 221: "]", 220: "\\", 186: ";", 222: "'", 
                    188: ",", 190: ".", 191: "/", 33: "PageUp", 34: "PageDown",
                    35: "End", 36: "Home", 37: "ArrowLeft", 39: "ArrowRight", 40: "ArrowDown",
                    38: "ArrowUp", 32: "Space", 13: "Enter", 27: "Esc",  9: "Tab", 8: "BackSpace",
                    112: "F1", 113: "F2", 114: "F3", 115: "F4", 116: "F5", 117: "F6", 118: "F7",
                    119: "F8", 120: "F9", 121: "F10", 122: "F11", 123: "F12", 
                    46: "Del", 45: "Ins", 19: "Pause", 145: "ScrollLock", 144: "NumLock",
                    96: "Num0", 97: "Num1", 98: "Num2", 99: "Num3", 100: "Num4", 101: "Num5",
                    102: "Num6", 103: "Num7", 104: "Num8", 105: "Num9", 106: "Num*", 107: "Num+",
                    109: "NumDash", 111: "Num/", 110: "Num."},
    
    construct: function() {
        this._keyDownListener = this._keyDownListener.bind(this);
    },
        
    getKeyPressCombo: function(keyCode, ctrlKey, altKey, shiftKey) {
        var key;
        
        if ((keyCode >= 48 && keyCode <= 57) || (keyCode >= 65 && keyCode <= 90)) {
            key = String.fromCharCode(keyCode);
        } else {
            key = this._keyNameTable[keyCode];
            if (key == undefined) key = "";
        }
                
        if (key.length > 0) {
            var mod;
            
            if (ctrlKey) {
                if (altKey) {
                    if (shiftKey) {
                        mod = this._ctrlAltShift;
                    } else {
                        mod = this._ctrlAlt;
                    }
                } else if (shiftKey) {
                    mod = this._ctrlShift;
                } else {
                    mod = this._ctrl;
                }
            } else if (altKey) {            
                if (shiftKey) {
                    mod = this._altShift;
                } else {
                    mod = this._alt;
                }            
            } else if (shiftKey) {
                mod = this._shift;
            } else {
                mod = "";
            }
            
            if (mod.length > 0) key = mod + key;
        }
        
        return key;
    },
    
    _keyDownListener: function(event) {    
        var comp = tw_Component.currentFocus;
        if (comp == null) comp = tw_Dialog.active;
        if (comp == null) comp = tw_Frame.active;

        if (comp != null) {
            var keyPressCombo = this.getKeyPressCombo(tw_getEventKeyCode(event), event.ctrlKey, event.altKey, event.shiftKey);
            var bubbleEvent = true;
            
            do {
                if (comp._enabled) bubbleEvent = comp.keyPressNotify(keyPressCombo);
                comp = comp._parent;  
            } while (comp != null && bubbleEvent);
            
            if (!bubbleEvent) tw_cancelEvent(event);
        }        
    },

    _getBrowserKeyEvent: function() {
        //NOTE: Opera doesn't allow you to cancel keydown events, so we use keypress.  Firefox doesn't
        //work with keypress, but IE & Firefox work with keydown, so we use it by default.
        return tw_isOpera ? "keypress" : "keydown";    
    },
    
    start: function() {
        tw_addEventListener(document, this._getBrowserKeyEvent(), this._keyDownListener);
    },
    
    stop: function() {
        tw_removeEventListener(document, this._getBrowserKeyEvent(), this._keyDownListener);
    }
});
