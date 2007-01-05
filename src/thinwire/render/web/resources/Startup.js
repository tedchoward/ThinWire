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

function tw_getElementIndex(node) {
    var index = 0;

    while ((node = node.previousSibling) != null)
        index++;
    
    return index;
}

//TODO: Is document.activeElement supported by Firefox?
function tw_getActiveElement() {
    return document.activeElement == undefined ? null : document.activeElement;
}

//Should be a function of Frame, used by GridBox
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

//Should be a function of Frame, used by GridBox & DropDownGridBox
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

function tw_setElementFocus(comp, state) {
    try {
        var elem = comp._focusBox;
        if (state && elem.focus) {
            if (tw_isFirefox) setTimeout(comp._focus, 0);
            else elem.focus();
        } else if (!state && elem.blur) elem.blur();
    } catch (e) {
        //Firefox sometimes throws an error when attemptting to set focus.
        //ignore the error for now until solution is found.
        //Additionally, this can throw an error in IE if the element or one
        //of it's parents is not visible.
    }
}

//Should be a function of Component
//Allow buttons in mozilla to get focus, but prevent click noise in IE
function tw_setFocusCapable(comp, state) {
    if (comp.tagName != "A") return; 
    if (tw_isGecko) comp.href = "javascript:void(false)";
    comp.tabIndex = state ? 0 : -1;
}

function tw_addEventListener(obj, type, handler, capture) {
    if (type instanceof Array) {
        for (var i = 0, cnt = type.length; i < cnt; i++) {
            tw_addEventListener(obj, type[i], handler, capture);
        }        
    } else {    
        if (obj.addEventListener)
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
        if (obj.removeEventListener)
            obj.removeEventListener(type, handler, capture);
        else
            obj.detachEvent("on" + type, handler);
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
    return event.which ? event.which : event.button;
}

function tw_getEventOffsetX(event, className) {
    var source = tw_getEventTarget(event);
    var x;
    
    if (event.layerX) {
        x = event.layerX;
       if (source.className != className) x -= source.offsetLeft;
    } else {
        x = event.offsetX;
    }
    
    if (className != null) {
        while (source != null && source.className != className) {
            x += source.offsetLeft;
            source = source.parentNode;
        }
    }
    
    return x;
}

function tw_getEventOffsetY(event, className) {
    var source = tw_getEventTarget(event);
    var y;

    if (event.layerY) {
        y = event.layerY;
       if (source.className != className) y -= source.offsetTop;
    } else {
        y = event.offsetX;
    }

    if (className != null) {
        while (source != null && source.className != className) {
            y += source.offsetTop;
            source = source.parentNode;
        }
    }

    return y;
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

function tw_setOpacity(box, opacity) {
    if (opacity > 100) opacity = 100;
    box.style.opacity = opacity <= 0 ? 0 : opacity / 100;
    if (tw_isIE) box.style.filter = opacity == 100 ? "" : "alpha(opacity=" + opacity + ")";
}

function tw_setLayerTransparent(box) {
    var s = box.style;
    
    if (tw_isIE) {
        //NOTE: IE allows clicks to propagate if the background-color is transparent.
        //However, if the background is white and the opacity is zero, it works like it should.
        tw_setOpacity(box, 0);
        s.backgroundColor = "white";
    } else {
        s.backgroundColor = tw_COLOR_TRANSPARENT;        
    }
}

function tw_getTime() {
    return new Date().getTime();
}

function tw_getFontMetrics(family, size, bold, italic, underline) {
    var comp = document.createElement("div");
    var s = comp.style;
    s.position = "absolute";
    s.width = "1px";
    s.height = "1px";
    s.top = "0px";
    s.left = "0px";
    if (!tw_isIE) s.overflow = "auto";
    if (tw_isOpera) s.lineHeight = "0px";
    s.whiteSpace = "nowrap";
    s.backgroundColor = tw_COLOR_TRANSPARENT;
    s.color = tw_COLOR_TRANSPARENT;    
    s.fontSize = size + "pt";
    s.fontFamily = family;
    s.fontWeight = bold ? "bold" : "normal";
    s.fontStyle = italic ? "italic" : "normal";
    s.textDecoration = underline ? "underline" : "none";
    document.body.appendChild(comp);    
    
    var metrics = [0];
    
    for (var i = 0, cnt = tw_fontChars.length; i < cnt; i++) {
        var text = document.createTextNode(tw_fontChars[i]);
        comp.appendChild(text);
        if (i == 65) metrics[0] = comp.scrollHeight;
        metrics.push(comp.scrollWidth);
        comp.removeChild(text);
    }
    
    document.body.removeChild(comp);
    return metrics.join(",");
}

var tw_fontChars = [];
for (var i = 32; i < 256; i++) tw_fontChars.push(String.fromCharCode(i));

//Remove scroll bars from browser
if (tw_isIE && tw_bVer >= 6) {
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

//Add Selection Prevention eventlistener
if (tw_isIE) {
    tw_addEventListener(document, "selectstart", tw_selectStartListener);
} else {
    document.onmousedown = tw_selectStartListener;
}

//Prevent context menu from showing on right click
document.oncontextmenu = function(event) {
    var comp = tw_isIE ? tw_getActiveElement() : tw_getEventTarget(event);
    return comp.tagName == "TEXTAREA" || comp.tagName == "INPUT";
};

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
    
    if (window.tw_COLOR_ACTIVECAPTION == undefined) tw_COLOR_ACTIVECAPTION = "rgb(0, 84, 227)";
    if (window.tw_COLOR_CAPTIONTEXT == undefined) tw_COLOR_CAPTIONTEXT = "rgb(255, 255, 255)";
    if (window.tw_COLOR_THREEDFACE == undefined) tw_COLOR_THREEDFACE = "rgb(236, 233, 216)";
    if (window.tw_COLOR_WINDOWFRAME == undefined) tw_COLOR_WINDOWFRAME = "rgb(0, 0, 0)";
    if (window.tw_COLOR_WINDOWTEXT == undefined) tw_COLOR_WINDOWTEXT = "rgb(255, 255, 255)";
    document.title = "ThinWire Application Session Has Ended";
    
    if (text != null) {
        var message = document.createElement("div");
        var title = document.createElement("div");
        var s = title.style;
        s.fontSize = "10pt"
        s.fontWeight = "bold";
        s.backgroundColor = tw_COLOR_ACTIVECAPTION;
        s.padding = "2px";
        var link = document.createElement("a");
        var s = link.style;
        s.textDecoration = "none";
        s.color = tw_COLOR_CAPTIONTEXT;
        link.href = "http://www.thinwire.com";
        link.appendChild(document.createTextNode(document.title));
        title.appendChild(link);
        message.appendChild(title);
        message.appendChild(document.createTextNode(text));
        message.className = "label";
        var s = message.style;
        s.position = "absolute";
        s.width = "400px";
        s.top = ((tw_getVisibleHeight() - 200) / 2) + "px";
        s.left = ((tw_getVisibleWidth() - parseInt(s.width)) / 2) + "px";
        s.backgroundColor = tw_COLOR_THREEDFACE;
        s.border = "1px solid " + tw_COLOR_WINDOWFRAME;
        s.color = tw_COLOR_WINDOWTEXT;
        s.fontFamily = "Tahoma, sans-serif";
        s.whiteSpace = "normal";
        s.fontSize = "14pt";
        s.textAlign = "center";
        s.verticalAlign = "middle";    
        document.body.appendChild(message);
    }
    
    tw_kbm.stop();
    
    if (tw_isIE) {
        tw_removeEventListener(document, "selectstart", tw_selectStartListener);
    } else {
        document.onmousedown = null;
    }
    
    document.oncontextmenu = null;
    
    for (var id in tw_timerMap) {
        var timerId = tw_timerMap[id];    
        if (timerId != undefined) clearTimeout(timerId);
    }
    
    tw_timerMap = {};    
    tw_em.stop();    
}

//TODO: Opera doesn't fire an unload event so proper shutdown isn't currently possible.
tw_addEventListener(window, "unload", tw_shutdownInstance);

var tw_timerMap = {};

var tw_em = new tw_EventManager();
tw_em.start();

