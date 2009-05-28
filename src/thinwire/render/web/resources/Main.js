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
// Determine Standards Compliance
var tw_isOpera = false;
var tw_isSafari = false;
var tw_isKHTML = false;
var tw_isFirefox = false;
var tw_isIE = false;
var tw_isGecko = false;
var tw_bVer = 0;

function parseBrowserType(ua) {
    var msg = "This browser is not officially supported";
    var agent = ua.toLowerCase();

    function parseBrowser(browser, ver) {
        var index = agent.indexOf(browser);
        if (index >= 0) {
            index += browser.length + 1;
            while (index < agent.length && !/[\d.]/.test(agent.charAt(index))) index++;
            var end = index;
            while (end < agent.length && /[\d.]/.test(agent.charAt(end++)));
            end++;
            tw_bVer = parseFloat(agent.substring(index, end));
            if (tw_bVer >= ver) msg = null;
            return true;
        } else {
            return false;
        }
    }

    if (tw_isOpera = parseBrowser("opera", 8)) {}
    else if (tw_isSafari = parseBrowser("webkit", 412)) {}
    else if (tw_isKHTML = parseBrowser("khtml", 3.5)) {}
    else if (tw_isFirefox = tw_isGecko = parseBrowser("firefox", 1)) {}
    else if (tw_isIE = parseBrowser("msie", 6)) {}
    else if (tw_isGecko = parseBrowser("gecko", 20050512)) {};
    if (msg != null) alert(msg + ":" + tw_LF + ua);
}

parseBrowserType(navigator.userAgent);
var tw_isWin = navigator.userAgent.indexOf("Windows") > 0;
var tw_sizeIncludesBorders = tw_isIE && tw_bVer < 6;
var tw_useSmartTab = (tw_isIE && tw_bVer >= 6) || (tw_isFirefox && tw_bVer >= 1.5);
var tw_useCSSText = typeof document.body.style.cssText != "undefined";

var tw_APP_URL = new String(location);
if (tw_APP_URL.indexOf("?") >= 0) tw_APP_URL = tw_APP_URL.substring(0, tw_APP_URL.indexOf("?"));

function tw_include(name) {
	name = tw_Component.expandUrl(name);
	var script = document.createElement("script");
	document.body.appendChild(script);
	tw_addEventListener(script, tw_isIE ? "readystatechange" : "load", tw_includeLoaded);
	script.src = name;
	tw_em.manualSyncResponse();
}

function tw_includeLoaded(ev) {
	var script = tw_getEventTarget(ev);
	tw_removeEventListener(script, tw_isIE ? "readystatechange" : "load", tw_includeLoaded);
	tw_em.sendSyncResponse("");
}

function tw_setFavicon(type, url){
    var head = document.getElementsByTagName('head')[0];
    var links = head.getElementsByTagName("link");

    for (var i=0; i < links.length; i++) {
        var link = links[i];

        if (link.rel=="shortcut icon") {
            head.removeChild(link);
        }
    }

    if(type == null || url == null){
        return;
    }

    var stLink = document.createElement("link");
    stLink.setAttribute("type", type);
    stLink.setAttribute("rel", "shortcut icon");
    stLink.setAttribute("href", url);
    head.appendChild(stLink);
}