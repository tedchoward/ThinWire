/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
// Determine Standards Compliance
var tw_userAgent = navigator.userAgent.toLowerCase();
var tw_isOpera = tw_userAgent.indexOf("opera") >= 0;
var tw_isGecko = !tw_isOpera && tw_userAgent.indexOf("gecko") >= 0;
var tw_isIE = !tw_isOpera && !tw_isGecko && tw_userAgent.indexOf("msie") >= 0;
var tw_isIE55 = tw_isIE && tw_userAgent.indexOf("msie 5.5") >= 0;
if (!tw_isIE && !tw_isIE55 && !tw_isGecko && !tw_isOpera) alert("This browser is not officially supported:" + tw_userAgent);

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

var tw_COLOR_WINDOW = "window";
var tw_COLOR_WINDOWTEXT = "windowtext";
var tw_COLOR_HIGHLIGHT = "highlight";
var tw_COLOR_HIGHLIGHTTEXT = "highlighttext";
var tw_COLOR_GRAYTEXT = "graytext";
var tw_COLOR_BUTTONFACE = "buttonface";
var tw_COLOR_THREEDFACE = "threedface";
var tw_COLOR_THREEDSHADOW = "threedshadow";
var tw_COLOR_TRANSPARENT = "transparent";

var tw_COLOR_BLACK = "black";

var tw_FONT_FAMILY = "tahoma, sans-serif";

