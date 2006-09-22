/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
// Determine Standards Compliance
var tw_isOpera = false;
var tw_isSafari = false;
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
    else if (tw_isSafari = parseBrowser("safari", 412)) {}
    else if (tw_isFirefox = tw_isGecko = parseBrowser("firefox", 1)) {}
    else if (tw_isIE = parseBrowser("msie", 5.5)) {}
    else if (tw_isGecko = parseBrowser("gecko", 20050512)) {};
    
    if (msg != null) alert(msg + ":\n" + ua);
}

parseBrowserType(navigator.userAgent);
var tw_isWin = navigator.userAgent.indexOf("Windows") > 0;
var tw_sizeIncludesBorders = tw_isIE && tw_bVer < 6;
var tw_useSmartTab = tw_isWin && ((tw_isIE && tw_bVer >= 6) || (tw_isFirefox && tw_bVer >= 1.5));

var tw_BASE_PATH = new String(location);
var tw_APP_URL = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
tw_BASE_PATH = tw_BASE_PATH.substring(tw_BASE_PATH.indexOf("/", tw_BASE_PATH.indexOf("//") + 2));
if (tw_BASE_PATH.indexOf("?") >= 0) tw_BASE_PATH = tw_BASE_PATH.substring(0, tw_BASE_PATH.indexOf("?")); 
tw_APP_URL += tw_BASE_PATH;

function tw_include(name) {
    try {
        if (tw_include.tw_request == undefined) tw_include.tw_request = new tw_HttpRequest();
        var script = tw_include.tw_request.send("GET", tw_APP_URL + "resources/" + name, "");
    
        if (tw_isIE && window.execScript) {
            window.execScript(script);
        } else {
            window.eval(script);
        }        
    } catch (e) {
        var ret = confirm("Failed to include library '" + name + "'\n" +
            "Exception details:" + e + "\n\n" +
            "Would you like to load the libary via a <script> tag for more accurate error info?");
            
        if (ret) {
            //This causes the same file to be loaded using a script tag, which allows the file to be
            //easily debugged. However a script tag loads content async so it is not appropriate for
            //loading scripts on the fly.
            document.getElementById("jsidebug").src = "?_twr_=" + name + ".js"; 
        }
    }       
}

