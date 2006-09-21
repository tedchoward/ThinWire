/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//TODO: No proper support for tabsheet setVisible or setEnabled.
var tw_TabSheet = tw_BaseContainer.extend({
    _tab: null,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["tabSheet", id, containerId]);
        var s = this._box.style;
        s.top = "0px";
        s.left = "0px";
        s.zIndex = "0";
        
        var tab = this._tab = this._fontBox = this._borderBox = document.createElement("a");
        tab.className = "floatDivLeft"; //hack for FF
        var s = tab.style;
        s.cursor = "default";
        s.whiteSpace = "nowrap";
        s.styleFloat = "left"; //Doesn't work in FF.
        s.display = "block";
        s.lineHeight = s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? 2 : 4) + "px";

        var tabImage = document.createElement("div");
        tabImage.className = "floatDivLeft"; //hack for FF
        var s = tabImage.style;
        s.styleFloat = "left"; //Doesn't work in FF.
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";
        s.width = "16px";
        s.height = "16px";
        s.paddingLeft = "3px";
        s.overflow = "hidden";
        s.display = "none";

        tab.appendChild(tabImage);       
        tab.appendChild(document.createTextNode(""));
        tw_addEventListener(tab, "click", this._tabClickListener.bind(this));        
        
        this.setStyle("backgroundColor", tw_COLOR_THREEDFACE);
        this.setStyle("fontFamily", tw_FONT_FAMILY);
        this.setStyle("fontSize", 8);
        this.setStyle("fontColor", tw_COLOR_WINDOWTEXT);
        this.setStyle("fontBold", false);
        this.setStyle("fontItalic", false);
        this.setStyle("fontUnderline", false);
        
        var tabIndex = props.tabIndex;
        delete props.tabIndex;
        this.init(tabIndex, props);
    },

    _tabClickListener: function(event) {
        if (this._parent == null) return;
        var children = this._parent._children;
        
        for (var i = children.length; --i >= 0;) {
            if (children[i] == this) {
                this._parent.setCurrentIndex(i, true);
                break;
            }
        }
    },
    
    setText: function(text) {        
        this._tab.replaceChild(document.createTextNode(text), this._tab.lastChild);    
    },
            
    setImage: function(image) {
        var s = this._tab.firstChild.style;
        
        if (image.length > 0) {
            s.backgroundImage = "url(" + tw_BASE_PATH + "/resources/" + image + ")";
            s.display = "block";
        } else {
            s.backgroundImage = "";
            s.display = "none";
        }
    },
    
    setActiveStyle: function(active) {  
        var bs = this._box.style;
        var s = this._tab.style;
        var borderSize = this.getStyle("borderSize");
        
        if (active) {
            bs.zIndex = 1;
            bs.visibility = "visible";
            s.height = tw_TabFolder._tabsHeight + (tw_sizeIncludesBorders ? borderSize : 0) + "px";
            s.paddingLeft = "4px";
            s.paddingRight = "4px";
        } else {
            bs.zIndex = 0;
            bs.visibility = "hidden";
            s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? borderSize : borderSize * 2) + "px";
            s.paddingLeft = "2px";
            s.paddingRight = "2px";
        }

        s.marginTop = active ? "0px" : borderSize + "px";        
        tw_setFocusCapable(this._tab, active);
    },    
        
    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        if (name == "backgroundColor") this._tab.style.backgroundColor = value;
        else if (name == "borderSize") this._borderBox.style.borderBottomWidth = "0px";
    },

    destroy: function() {
        this.$.destroy.apply(this, []);
        this._tab = null;
    }    
});
