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
var tw_BaseText = tw_Component.extend({
    _supportEditMask: false,
    _timerId: 0,
    _editor: null,
    _subtractEditorWidth: 0,
    _paddingSize: tw_isFirefox || (tw_isOpera && tw_bVer < 9) ? 0 : 2, //FireFox places the padding outside the scrollbars,
    _lastValue: "",
    _useToolTip: true,
    _editMask: "",
    _validatedValue: "",
    _escapeLastPressedAt: null,
    _valueOnFocus: null,
    _selectionOld: undefined,
    _decimalPointTyped: false,
    
    construct: function(tagNames, className, id, containerId) {
        arguments.callee.$.call(this, tagNames[0], className, id, containerId);
        var s = this._box.style;
        
        var editor = document.createElement(tagNames[1]);    
        var s = editor.style;
        var left =  tw_isKHTML || tw_isSafari ? "-1px" : "0px";
        var top = tw_isKHTML || tw_isSafari ? "-1px" : "0px";
        var cssText = "position:absolute;" + "left:" + left + ";" + "top:" + top + ";" + "margin:0px;border:0px;padding:" + 
            this._paddingSize + "px;background-color:" + tw_COLOR_TRANSPARENT + ";";
        tw_Component.setCSSText(cssText, editor);
        
        if (tagNames.length > 2) editor.type = tagNames[2];
        this._box.appendChild(editor);
        this._editor = this._focusBox = this._fontBox = editor;
        
        this._focusListener = this._focusListener.bind(this);
        this._blurListener = this._blurListener.bind(this);
        
        tw_addEventListener(editor, ["click", "dblclick"], this._clickListener.bind(this));
        tw_addEventListener(editor, "keyup", this._keyUpListener.bind(this));
        tw_addEventListener(editor, "mouseup", this._mouseUpListener.bind(this));
        tw_addEventListener(editor, "focus", this._focusListener);
        tw_addEventListener(editor, "blur", this._blurListener);        
        
    },
        
    _keyUpListener: function(event) {
        if (!this._enabled) return;
        var keyCode = tw_getEventKeyCode(event);
        if (keyCode == 190) this._decimalPointTyped = true;
		this._textChange(keyCode);
    },
        
    _mouseUpListener: function(event) {
        if (!this._enabled) return;
		this._textChange(0);
    },
    
    setWidth: function(width) {     
        arguments.callee.$.call(this, width);
        width -= this._borderSizeSub + this._subtractEditorWidth + this._paddingSize * 2;
        if (width < 0) width = 0;
        if (tw_isKHTML || tw_isSafari) {
            this._editor.style.width = width - ((parseInt(this._editor.style.left, 10) - 1) * 3) + "px";
        } else {
            this._editor.style.width = width + "px";
        }
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= this._borderSizeSub + this._paddingSize * 2;
        if (height < 0) height = 0;
        if (tw_isKHTML || tw_isSafari) {
            this._editor.style.height = height - ((parseInt(this._editor.style.top, 10) - 1) * 3) + "px";
        } else {
            this._editor.style.height = height + "px";
        }
    },
        
    setText: function(text) {
        var length = parseInt(this._editor.maxLength, 10);
        if (length != -1) this._editor.maxLength = text.length;
        this._lastValue = this._editor.value = text;
        if (this._useToolTip) this._editor.title = text; 
		this._textChange(0);
        if (length != -1) this._editor.maxLength = length;
    },

    setEnabled: function(enabled) {        
        arguments.callee.$.call(this, enabled);        
        this._editor.readOnly = !enabled;
    },

    setFocus: function(focus) {
        if (focus) {
            var ret = arguments.callee.$.call(this, focus);        
            this._valueOnFocus = this._editor.value;
        } else {
            var ret = arguments.callee.$.call(this, focus);            
        }
        
        return ret;
    },    
    
    _getStringCount: function(text, str, start, end) {
        var count = 0;
        if (start == null) start = 0;
        if (end == null) end = text.length;
        
        while ((start = text.indexOf(str, start)) >= 0) {
            if (start >= end) break;
            count++;
            start++;
        }
        
        return count;
    },
        
    _getIESelectionIndexToRange: function(comp, r, compareType) {
        //Try the duplication method first, if that fails, then use the components create range.
        try {
            var cr = r.duplicate();
            cr.moveToElementText(comp);
            r.compareEndPoints(compareType, cr);
        } catch (e) {
            try {
                var cr = comp.createTextRange();
                r.compareEndPoints(compareType, cr);
            } catch (e) {
                return -1;
            }
        }

        var startIndex = 0;
        var endIndex = comp.value.length - this._getStringCount(comp.value);
        var index = endIndex == 1 ? 1 : Math.floor(endIndex / 2); //index at the middle of the text.
        var direction;
        cr.moveStart("character", index); //initial move        
                
        while ((direction = r.compareEndPoints(compareType, cr)) != 0) {            
            if (direction < 0) { //the selection (r) is further to the left.
                if (index == endIndex && startIndex != 0) break;
                endIndex = index;
                var diff = index - startIndex;                 
                diff = -(diff > 1 ? Math.floor(diff / 2) : 1);
                
                if (index < 0) {
                    index = 0;
                    break;
                }
            } else { // the selection (r) is further to the right.
                if (index == startIndex && startIndex == endIndex - 1) break;
                startIndex = index;
                var diff = endIndex - index;
                diff = diff > 1 ? Math.floor(diff / 2) : 1;
                if (startIndex == endIndex) break;
            }
                            
            index += diff;
            cr.moveStart("character", diff);
        }
        
        return index;
    },    
    
    //Don't call this until the textfield has been attached to a document.
    //Calling it first resulted in an "unspecified" HTML error.  
    setSelectionRange: function(beginIndex, endIndex) {
        this._selectionOld = beginIndex + "," + endIndex;        
        
        if (tw_Component.currentFocus === this) {
            var comp = this._editor;
            //NOTE: We have to do this because the text range does not count CRLF as two characters.
            beginIndex -= this._getStringCount(comp.value, "\n", 0, beginIndex);
            endIndex -= this._getStringCount(comp.value, "\n", 0, endIndex);        
            
            if (tw_isIE) {
                endIndex = -(comp.value.length - this._getStringCount(comp.value, "\n") - endIndex);
                var r = comp.createTextRange();
                var movedStart = r.moveStart("character", beginIndex);
                var movedEnd = r.moveEnd("character", endIndex);        
                r.select();
            } else {
                comp.setSelectionRange(beginIndex, endIndex);
            }
        }            
    },
    
    getSelectionRange: function() {
        var comp = this._editor;
                
        if (tw_isIE) {       
            var r = document.selection.createRange();        
            var start = this._getIESelectionIndexToRange(comp, r, "StartToStart");
            var end = this._getIESelectionIndexToRange(comp, r, "EndToStart");
            if (start == -1 || end == -1) return [-1, -1];
            var value = comp.value.replace(/\r\n/g, "\n");        
            if (start > value.length) start = value.length;
            if (end > value.length) end = value.length;
        } else {           
            try {
                var start = comp.selectionStart ? comp.selectionStart : 0;
                var end = comp.selectionEnd ? comp.selectionEnd : 0;
                var value = comp.value;
            } catch (e) {
                return [0, 0];
            }
        }
        
        start += this._getStringCount(value, "\n", 0, start);
        end += this._getStringCount(value, "\n", 0, end);       
        return [start, end];
    },


    setAlignX: function(alignX) {
        this._editor.style.textAlign = alignX;
    },
    
    setEditMask: function(editMask) {
        this._lastValue = this._editor.value;
        if (this._useToolTip) this._editor.title = this._lastValue;
    
        //If the editMask is all numeric then it indicates the maximum length, not a true mask.
        if (editMask.indexOf("<=") == 0) {
            var ary = [];
            var maxLength = parseInt(editMask.substring(2), 10);
            
            while (--maxLength >= 0) {
                ary.push("x");
            }
            
            editMask = ary.join("");
        }
            
        this._editMask = editMask;
        if (editMask.length > 0) this._editor.maxLength = editMask.length;
        this._validatedValue = "";
		this._textChange(0);
    },

	_textChange: function(keyCode) {
		this._validateInput(keyCode);
		var value = this._editor.value;
		var reset = false;

        if (value != this._lastValue) {
	        this.firePropertyChange("text", value);
			reset = true;
	        this._lastValue = value;
	        if (this._useToolTip) this._editor.title = this._lastValue;
		}
		
		if (this._visible && this._enabled) {
	        if (this._selectionOld == undefined) this._selectionOld = value.length + "," + value.length;
	        var selectionNew = this.getSelectionRange().join();

	        if (this._selectionOld != selectionNew && selectionNew.indexOf("-1") == -1) {
	            this.firePropertyChange("selectionRange", selectionNew);
				reset = true;
	            this._selectionOld = selectionNew;
	        }
		}
		
		if (reset) tw_em.resetSendEventsTimer(400);
	},
    
    _validateInput: function(keyCode) {
        var newValue = this._editor.value;
        var editMask = this._editMask;
        var valid = true;
        var cntr;
        var maskChars = "9#MdyAaXxhmp";
    
        if (editMask.length > 0 && newValue.length > 0) {
            //Reformat amount masks, otherwise Validate data
            if (editMask.indexOf('#') >= 0) {
                var maxLength = editMask.length;
        
                var reChar;
                var vChar;
                var mCntr;
                var value = newValue;                               
                var mask = new String(editMask);                                
                var isNegative = false;
                
                if (mask.charAt(0) == "-") {
                    mask = mask.substring(1);                    
                    
                    if (value.charAt(0) == "-") {
                        value = value.substring(1);
                        isNegative = true;
                    }                        
                }
    
                //if the first character is '.' then add a zero to the begining automatically
                if (value.charAt(0) == '.') value = "0" + value;
                var valueDot = value.indexOf(".");
                var maskDot = mask.indexOf(".");
    
                if (valueDot >= 0) {
                    var valueBefore = value.substring(0, valueDot);
                    var valueAfter = value.substring(valueDot);
                    var chop = valueAfter.length - mask.substring(maskDot).length;
                    if (chop > 0) value = valueBefore + valueAfter.substring(0, valueAfter.length - chop);
                    chop = valueBefore.length - mask.substring(0, maskDot).length;
                    if (chop > 0) value = valueBefore.substring(0, valueBefore.length - chop) + valueAfter;
                } else {
                    this._decimalPointTyped = false;
                }
                
                var chop = value.length - mask.length;
                if (chop > 0) value = value.substring(chop);
                valueDot = value.indexOf(".");
                
                if (maskDot >= 0) {
                    if (valueDot >= 0) {
                        mask = mask.substring(0, maskDot + (value.length - valueDot));
                    } else {
                        mask = mask.substring(0, maskDot);
                    }
                }
                
                if (value.length > mask.length) {
                    valid = false;
                } else {                    
                    for (cntr = value.length - 1; cntr >= 0; cntr--) {
                        mCntr = (mask.length - value.length) + cntr;
                        reChar = this._reFromMask(value, mask, mCntr);
                        vChar = value.charAt(cntr);
                        
                        //If this mask character does not match the value's character then either
                        //remove the character from the value if the mask character is a format character,
                        //or insert the mask charcter.                        
                        if (!reChar.test(vChar)) {
                            if (valueDot >= 0 && cntr >= valueDot) {
                                valid = false;
                                break;
                            }
    
                            if (maskChars.indexOf(mask.charAt(mCntr)) >= 0) {
                                value = value.substring(0, cntr) + value.substring(cntr + 1);
                            } else {
                                value = value.substring(0, cntr) + vChar + mask.charAt(mCntr) + value.substring(cntr + 1);
                            }
                        }
                    }
    
                    do { 
                        vChar = value.charAt(0);
                        reChar = this._reFromMask(vChar, "#");
                        
                        if (!reChar.test(vChar)) {
                            value = value.substring(1);
                        } else {
                            break;
                        }
                    } while (true);
                    
                    if (isNegative) value = "-" + value;
                    newValue = value;
                    if (newValue.length > maxLength) {
                        valid = false;
                    }
                }
            } else {                          
                var dateMask = false;
                
                for (var i = 0, size = newValue.length; i < size; i++) {
                    var mch = editMask.charAt(i);
                    
                    if (i >= editMask.length) { //Truncate the value if it exceeds the mask                    
                        newValue = newValue.substring(0, i);                    
                        break;
                    } else if (!dateMask && mch == "y" && (/\d\d\d\d/).test(newValue.substring(i, i + 4)) && editMask.charAt(i + 1) == "y" && editMask.charAt(i + 2) != "y") { //special rule to refromat four digit year to two digit year
                        dateMask = true;
                        newValue = newValue.substring(0, i) + newValue.substring(i + 2);
                        i--;
                    } else if (!this._reFromMask(newValue, editMask, i, i + 1).test(newValue.charAt(i))) {
                        //if not a maskchar, then insert the format char from the mask into
                        //the value at this point.                    
                        if (maskChars.indexOf(mch) == -1) {
                            newValue = newValue.substring(0, i) + mch + newValue.substring(i);
                            size++;
                        } else {
                            if (mch == "A" || mch == "X") {                                                                        
                                var charCode = newValue.charCodeAt(i);
                                
                                if ((charCode >= 0x61 && charCode <= 0x7A) || (charCode >= 0xE0 && charCode <= 0xFE && charCode != 0xF7)) {
                                    newValue = newValue.substring(0, i) + String.fromCharCode(charCode - 32) + newValue.substring(i + 1);
                                    i--;
                                } else {
                                    valid = false;
                                    break;
                                }
                            } else {
                                valid = false;
                                break;
                            }
                        }
                    }
                }
                
                //Append trailing format characters from the mask
                if (valid && keyCode != 8) {
                    for (var i = newValue.length, size = editMask.length; i < size; i++) {
                        var mch = editMask.charAt(i); 
                        
                        if (maskChars.indexOf(mch) == -1) {
                            newValue += mch;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        
        if (valid) this._validatedValue = newValue;
        if (this._editor.value != this._validatedValue) {
            this._editor.value = this._validatedValue;
            //If it's a # mask and the '.' hasn't been typed by the user, and the value before the '.' is
            // within the mask, leave the cursor to the left of the '.'.
            if (editMask.indexOf("#") >= 0 && !this._decimalPointTyped) {
                var valueDot = this._validatedValue.indexOf(".");
                
                if (valueDot >= 0) {
                    var mask = editMask.charAt(0) == "-" && this._validatedValue.charAt(0) != "-" ? editMask.substring(1) : editMask;
                    if (valueDot < mask.indexOf(".")) this.setSelectionRange(valueDot, valueDot);
                }
            }
        }
        return valid;
    },    

    _reFromMask: function(data, mask, start, end) {
        var chMask, chData, reCh, tmp, fmcntr;
        var reMask = "";
        
        if (start == null) {
            start = 0;
            end = data.length;
        } else if (end == null)
            end = start + 1;
    
        for (fmcntr = start; fmcntr < end; fmcntr++) {
            chMask = mask.charAt(fmcntr);
            chData = data.charAt(fmcntr);
    
            switch (chMask) {
                case 'M': case 'h':
                    reCh = "[0-1]";
    
                    if (fmcntr > 0) {
                        if ("Mh".indexOf(mask.charAt(fmcntr - 1)) >= 0) {
                            if (data.charAt(fmcntr - 1) == '0')
                                reCh = "[1-9]";
                            else
                                reCh = "[0-2]";
                        }
                    }
    
                    break;
    
                case 'd':
                    reCh = "[0-3]";
    
                    if (fmcntr > 0) {
                        tmp = new Number(data.substring(mask.indexOf("MM"), mask.indexOf("MM") + 2));
    
                        if (mask.charAt(fmcntr - 1) == 'd') {
                            if (data.charAt(fmcntr - 1) == '3') {
                                if (tmp == 4 || tmp == 6 || tmp == 9 || tmp == 11)
                                    reCh = "[0]";
                                else
                                    reCh = "[0-1]";
                            } else if (data.charAt(fmcntr - 1) == '0')
                                reCh = "[1-9]";
                            else
                                reCh = "[0-9]";
                        } else {
                            if (tmp == 2)
                                reCh = "[0-2]";
                        }
                    }
    
                    break;
    
                case 'm':
                    reCh = "[0-5]";
    
                    if (fmcntr > 0) {
                        if (mask.charAt(fmcntr - 1) == 'm')
                            reCh = "[0-9]";
                    }
    
                    break;
    
                case 'p':
                    reCh = "[A|P|a|p]";
    
                    if (fmcntr > 0) {
                        if (mask.charAt(fmcntr - 1) == 'p')
                            reCh = "[M|m]";
                    }
    
                    break;
    
                //From: http://unicode.org/charts/PDF/U0080.pdf
                case 'a':
                    reCh = "[A-Za-z ]|[\u00c0-\u00d6]|[\u00d8-\u00f6]|[\u00f8-\u00ff]";
                    break;
    
                case 'A':
                    reCh = "[A-Z ]|[\u00c0-\u00d6]|[\u00d8-\u00de]";
                    break;
                    
                case 'x':
                    reCh = "[ -~]|[\u00a1-\u00ff]";
                    break;
                    
                case 'X':
                    reCh = "[\u0020-`]|[{-~]|[\u00a1-\u00de]|\u00f7";
                    break;
                    
                case '9': case '#':
                    reCh = "\\d";
                    break;
    
                case 'y':
                   if ((tmp = mask.indexOf("yyyy")) >= 0) {
                       if (tmp == fmcntr)
                           reCh = "[1-2]";
                       else if (tmp == fmcntr - 1) {
                           if (data.charAt(fmcntr - 1) == '1')
                               reCh = "[8-9]";
                           else
                               reCh = "[0-2]";
                       } else
                           reCh = "[0-9]";
                   } else
                       reCh = "[0-9]";
   
                   break;

                case '(':
                    reCh = "\\(";
                    break;
    
                case ')':
                    reCh = "\\)";
                    break;
    
                case '.':
                    reCh = "\\.";
                    break;
    
                default:
                    reCh = chMask;
            }
    
            reMask += reCh;
        }
    
        return new RegExp(reMask);
    },

    keyPressNotify: function(keyPressCombo) {        
        if (keyPressCombo == "Esc") {
            var time = new Date().getTime();
            
            if (this._escapeLastPressedAt == null || time - this._escapeLastPressedAt > 400) {
                this._escapeLastPressedAt = time;
                this._editor.value = this._valueOnFocus.length > 0 ? this._valueOnFocus : this._lastValue;                
            } else {
                this._escapeLastPressedAt = null;
                this._editor.value = this._valueOnFocus = "";
            }
            
            return false;
        } else {
            return arguments.callee.$.call(this, keyPressCombo);
        }        
    },
    
    getDragBox: function() {
        var dragBox = document.createElement("div");
        var s = dragBox.style;
        s.width = (this._width - this._borderSizeSub) + "px";
        s.height = (this._height - this._borderSizeSub) + "px";
        dragBox.appendChild(document.createTextNode(this._editor.value));
        return dragBox;
    },
    
    _clickListener: tw_Component.clickListener,
        
    destroy: function() {
        this._editor = null;
        arguments.callee.$.call(this);
    }
});

