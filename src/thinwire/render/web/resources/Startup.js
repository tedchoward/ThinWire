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

function tw_getEventOffsetX(event) {
    return event.layerX ? event.layerX : event.offsetX;
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
    iframe.frameBorder = "0";
    //iframe.style.border = "solid 1px black";
    iframe.scrolling = "no";
    iframe.src = "?_twr_=FileUploadPage.html";    
    c.appendChild(iframe);

    iframe.onload = function() {
        if (iframe.readyState != "loading") return;
        //iframe.contentWindow.document.body.style.borderType = "none";
        iframe.contentWindow.tw_showDescription = showDescription;
        iframe.contentWindow.tw_multiFile = multiFile;
        iframe.contentWindow.tw_width = c.style.width;
        iframe.contentWindow.tw_height = c.style.height;
        with(iframe.contentWindow.document.getElementById("files")) {
            width = parseInt(c.style.width) - 20 + "px";
            height = parseInt(c.style.height) - 75 + "px";
        }
        if (multiFile) {
            tw_FileChooser_insertButton("Remove", removeUploadLine, iframe);
            tw_FileChooser_insertButton("Add", addUploadLine, iframe);
        }
        iframe.onreadystatechange = null;
    };
}

function tw_makeFileChooserBtn(buttonId, tfId) {
    var btn = tw_Component.instances[buttonId];
    var tf = tw_Component.instances[tfId];
    var iframe = document.createElement("iframe");
    
    s = iframe.style;
    
    s.top = "0px";
    s.width = btn.getWidth() + "px";
    s.height = btn.getHeight() + "px";
    s.position = "absolute";
    //s.border = "solid 1px black";
    //s.backgroundColor = "red";
    s.overflow = "hidden";
    s.zIndex = "1";
    s.opacity = "0";
    if (tw_isIE) s.filter = "alpha(opacity=0)";
    iframe.scrolling = "no";
    iframe.src = "?_twr_=blank.html";
    iframe.frameBorder = "0";
    iframe.onload = function() {
        input = iframe.contentWindow.document.getElementsByTagName("input")[0];
        if (input != null) {
            input.name = "file0";
            input.onchange = function() {
                tf.setText(this.value);
            };
        }
    };
    btn._iframe = iframe;
    btn._box.appendChild(iframe);
}

function tw_FileChooser_submit(buttonId) {
    var btn = tw_Component.instances[buttonId];
    var iframe = btn._iframe;
    iframe.contentWindow.document.getElementById("uploadForm").submit();
}

function tw_FileChooser_insertButton(text, handler, iframe) {
    var br = iframe.contentWindow.document.getElementById("files");
    var add = iframe.contentWindow.document.createElement("input");
    add.className = "fileUploadButton";
    add.value = text;
    add.onclick = handler;
    add.type = "button";
    br.parentNode.insertBefore(add, files.nextSibling);
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

