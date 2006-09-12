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
function tw_addTimerTask(id, timeout) {
    var timerId = tw_timerMap[id];
    if (timerId != undefined) clearTimeout(timerId);
    tw_timerMap[id] = setTimeout(function(){tw_em.sendRunTimer(id)}, timeout);
}

function tw_removeTimerTask(id) {
    var timerId = tw_timerMap[id];
    
    if (timerId != undefined) {        
        clearTimeout(timerId);
        delete tw_timerMap[id];
    }
}

function tw_setSelectionEnabled(state) {
    //This only applies to Gecko based browsers
    if (tw_isGecko) {
        tw_Frame.active.getBox().style.MozUserSelect = state ? "text" : "none";
    }
}

function tw_getEventTarget(event, className, alt) {
    if (event == null) {
        return alt; 
    } else {
        var source = event.target ? event.target : event.srcElement;
        
        if (className != null) {
            while (source != null && source.className != className) {
                source = source.parentNode;
            }
        }
        
        return source;
    }        
}

function tw_getEventButton(event) {
    return tw_isGecko ? event.which : event.button;
}

function tw_getEventOffsetX(event) {
    if (tw_isGecko) {
        return event.layerX;
    } else {
        return event.offsetX;// + tw_BORDER_WIDTH;
    }
}

function tw_getStringCount(text, str, start, end) {
    var count = 0;
    if (start == null) start = 0;
    if (end == null) end = text.length;
    
    while ((start = text.indexOf(str, start)) >= 0) {
        if (start >= end) break;
        count++;
        start++;
    }
    
    return count;
}    

function tw_setSelectionRange(comp, start, end) {
    //NOTE: We have to do this because the text range does not count CRLF as two characters.
    start -= tw_getStringCount(comp.value, "\n", 0, start);
    end -= tw_getStringCount(comp.value, "\n", 0, end);        
    
    if (tw_isIE) {
        end = -(comp.value.length - tw_getStringCount(comp.value, "\n") - end);
        var r = comp.createTextRange();
        var movedStart = r.moveStart("character", start);
        var movedEnd = r.moveEnd("character", end);        
        r.select();
    } else {
        comp.setSelectionRange(start, end);
    }
}

function tw_getSelectionRange(comp) {
    function getIndexToRange(comp, r, compareType) {
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
        var endIndex = comp.value.length - tw_getStringCount(comp.value);
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
    }    
    
    if (tw_isIE) {       
        var r = document.selection.createRange();        
        var start = getIndexToRange(comp, r, "StartToStart");
        var end = getIndexToRange(comp, r, "EndToStart");
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
    
    start += tw_getStringCount(value, "\n", 0, start);
    end += tw_getStringCount(value, "\n", 0, end);       
    return [start, end];
}

function tw_getEventKeyCode(event) {
	return event.which ? event.which : event.keyCode;
}

function tw_cancelEvent(event) {
    if (event.stopPropagation) {
        event.stopPropagation();
        event.preventDefault();
    } else { 
        event.cancelBubble = true;
        event.returnValue = false;
        event.keyCode = 505; //Hack that prevents keys like F5 in IE.
    }
}

//TODO: Is document.activeElement supported by Firefox?
function tw_getActiveElement() {
    return document.activeElement;
}

function tw_getWindowScrollX() {
	if (tw_isGecko)
	    return window.scrollX;
    else if (!tw_isIE55)
		return document.documentElement.scrollLeft;
    else
        return document.body.scrollLeft;
}

function tw_getWindowScrollY() {
	if (tw_isGecko)
	    return window.scrollY;
    else if (!tw_isIE55)
		return document.documentElement.scrollTop;
    else
        return document.body.scrollTop;
}

function tw_getVisibleWidth() {
    if (document.body.clientWidth) {
        if (document.documentElement.clientWidth) {
            return Math.max(document.body.clientWidth, document.documentElement.clientWidth); 
        } else {
            return document.body.clientWidth;
        }
    } else if (document.documentElement.clientWidth) {
        return document.documentElement.clientWidth;
    } else {
        return 800;
    }
}

function tw_getVisibleHeight() {
    if (document.body.clientHeight) {
        if (document.documentElement.clientHeight) {
            return Math.max(document.body.clientHeight, document.documentElement.clientHeight); 
        } else {
            return document.body.clientHeight;
        }
    } else if (document.documentElement.clientHeight) {
        return document.documentElement.clientHeight;
    } else {
        return 600;
    }
}

function tw_setComponentFocus(comp) {
    if (comp.focus) comp.focus();
}   

//Allow buttons in mozilla to get focus, but prevent click noise in IE
function tw_setFocusCapable(comp, state) {
    if (tw_isGecko) {
        comp.href = state ? "javascript:void(false)" : null;
    } else {
        comp.tabIndex = state ? 0 : -1;
    }
}

function tw_addEventListener(obj, type, handler, capture) {
    if (type instanceof Array) {
        for (var i = 0, cnt = type.length; i < cnt; i++) {
            tw_addEventListener(obj, type[i], handler, capture);
        }        
    } else {    
        if (tw_isGecko)
            obj.addEventListener(type, handler, capture);
        else
            obj.attachEvent("on" + type, handler);
    }
}

function tw_removeEventListener(obj, type, handler, capture) {
    if (type instanceof Array) {
        for (var i = 0, cnt = type.length; i < cnt; i++) {
            tw_removeEventListener(obj, type[i], handler, capture);
        }        
    } else {    
        if (tw_isGecko)
            obj.removeEventListener(type, handler, capture);
        else
            obj.detachEvent("on" + type, handler);
    }
}

var tw_shutdownInProgress = false;

function tw_shutdownInstance(text) {
    if (tw_shutdownInProgress) return;
    tw_shutdownInProgress = true;
    
    if (tw_Frame.active != null) {        
        for (var i = tw_Frame.active.getContainer().childNodes.length; --i >= 0;) {
            var comp = tw_Frame.active.getContainer().childNodes.item(i);            
            if (comp.className == "dialog") comp.tw_destroy();
        }
        
        tw_Frame.active.destroy();
    }
    
    document.title = "[ThinWire]";
    
    if (text != null) {
        var message = document.createElement("div");    
        message.appendChild(document.createTextNode(text));
        message.className = "label";
        var s = message.style;    
        s.width = "320px";
        s.top = ((tw_getVisibleHeight() - 200) / 2) + "px";
        s.left = ((tw_getVisibleWidth() - parseInt(s.width)) / 2) + "px";
        s.backgroundColor = "threedface";
        s.border = "1px solid black";
        s.whiteSpace = "normal";
        s.fontSize = "14pt";
        s.textAlign = "center";
        s.verticalAlign = "middle";    
        document.body.appendChild(message);
    }
    
    tw_kbm.stop();
    if (tw_isIE) tw_removeEventListener(document, "selectstart", tw_selectStartListener);
    document.oncontextmenu = null;
    
    for (var id in tw_timerMap) {
        var timerId = tw_timerMap[id];    
        if (timerId != undefined) clearTimeout(timerId);
    }
    
    tw_timerMap = {};    
    tw_em.stop();    
}

function tw_prepareFileChooser(containerId, showDescription, multiFile) {
    var c = document.getElementById(containerId);
    var iframe = document.createElement("iframe");
    iframe.style.width = c.style.width;
    iframe.style.height = c.style.height;
    iframe.style.position = "absolute";
    iframe.frameBorder = false;
    iframe.scrolling = "no";
    iframe.src = "?_twr_=FileUploadPage.html";    
    c.appendChild(iframe);

    iframe.onreadystatechange = function() {
        if (iframe.readyState != "loading") return;
        iframe.contentWindow.tw_showDescription = showDescription;
        iframe.contentWindow.tw_multiFile = multiFile;
        iframe.contentWindow.tw_width = c.style.width;
        iframe.contentWindow.tw_height = c.style.height;
        iframe.onreadystatechange = null;
    };
}

function tw_getTime() {
	var time = new Date().getTime();
    return time;
}

// Initialize Logger Instance
//var tw_log = new tw_Logger();

var tw_borderColor = tw_isIE ? "" : "buttonface";
var tw_sizeIncludesBorders = tw_isIE55; 

//Remove scroll bars from browser
if (!tw_isIE55) {
    document.documentElement.style.overflow = "hidden";
} else {
    document.body.style.overflow = "hidden";
}

var tw_kbm = new tw_KeyboardManager();
tw_kbm.start();

function tw_selectStartListener(event) {
    var comp = tw_getEventTarget(event);
    return comp.tagName == "TEXTAREA" || comp.tagName == "INPUT";
}

//Add Selection Prevention eventlistener for IE, Gecko is handled within each component
if (tw_isIE) tw_addEventListener(document, "selectstart", tw_selectStartListener);

//Prevent context menu from showing on right click
document.oncontextmenu = function() { return false; };

//TODO: Opera doesn't fire an unload event so proper shutdown isn't currently possible.
tw_addEventListener(window, "unload", tw_shutdownInstance);

var tw_BASE_PATH = new String(location);
var tw_APP_URL = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
tw_BASE_PATH = tw_BASE_PATH.substring(tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
if (tw_BASE_PATH.indexOf("?") >= 0) tw_BASE_PATH = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("?")); 
tw_APP_URL += tw_BASE_PATH; 
//
var tw_BORDER_WIDTH = 2;
var tw_PADDING_WIDTH = 1;
var tw_CALC_BORDER_SUB = tw_BORDER_WIDTH * 2;
var tw_CALC_BORDER_PADDING_SUB = (tw_BORDER_WIDTH + tw_PADDING_WIDTH) * 2;

var tw_timerMap = {};

var tw_KEY_PAGE_UP = 33;
var tw_KEY_PAGE_DOWN = 34;
var tw_KEY_END = 35;
var tw_KEY_HOME = 36;
var tw_KEY_ARROW_LEFT = 37;
var tw_KEY_ARROW_RIGHT = 39;
var tw_KEY_ARROW_DOWN = 40;
var tw_KEY_ARROW_UP = 38;
var tw_KEY_SPACE = 32;
var tw_KEY_ENTER = 13;
var tw_KEY_ESCAPE = 27;
var tw_KEY_TAB = 9;

var tw_em = new tw_EventManager();
tw_em.start();


