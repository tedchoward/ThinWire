/*
 #LICENSE_HEADER#
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

//Should be a function of Component
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
    if (!tw_isIE && !tw_isGecko) s.lineHeight = "0px";
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

// Initialize Logger Instance
//var tw_log = new tw_Logger();

//Remove scroll bars from browser
if (tw_isIE && tw_bVer >= 6) {
    document.documentElement.style.overflow = "hidden";
} else {
    document.body.style.overflow = "hidden";
}

var tw_kbm = new tw_KeyboardManager();
tw_kbm.start();


//This only applies to Gecko based browsers
function tw_setSelectionEnabled(state) {
    if (tw_isGecko) tw_Frame.active.getBox().style.MozUserSelect = state ? "text" : "none";
}

function tw_selectStartListener(event) {
    var comp = tw_getEventTarget(event);
    return comp.tagName == "TEXTAREA" || comp.tagName == "INPUT";
}

//Add Selection Prevention eventlistener for IE, Gecko is handled within each component
if (tw_isIE) tw_addEventListener(document, "selectstart", tw_selectStartListener);


//Prevent context menu from showing on right click
document.oncontextmenu = function() { return false; };


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
        s.backgroundColor = tw_COLOR_THREEDFACE;
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

//TODO: Opera doesn't fire an unload event so proper shutdown isn't currently possible.
tw_addEventListener(window, "unload", tw_shutdownInstance);


var tw_BASE_PATH = new String(location);
var tw_APP_URL = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
tw_BASE_PATH = tw_BASE_PATH.substring(tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
if (tw_BASE_PATH.indexOf("?") >= 0) tw_BASE_PATH = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("?")); 
tw_APP_URL += tw_BASE_PATH; 

var tw_BORDER_WIDTH = 2;
var tw_PADDING_WIDTH = 1;
var tw_CALC_BORDER_SUB = tw_BORDER_WIDTH * 2;
var tw_CALC_BORDER_PADDING_SUB = (tw_BORDER_WIDTH + tw_PADDING_WIDTH) * 2;

var tw_timerMap = {};

var tw_borderColor = tw_isIE ? "" : tw_COLOR_BUTTONFACE;

var tw_em = new tw_EventManager();
tw_em.start();


