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
        s.overflow = "hidden";
        s.whiteSpace = "nowrap";
        s.styleFloat = "left"; //Doesn't work in FF.
        s.display = "block";
        //  s.lineHeight = s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? 2 : 4) + "px";

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
                this._parent.setFocus(true);
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
            s.backgroundImage = "url(" + tw_BASE_PATH + "resources/" + image + ")";
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
            var margin = 0;
            bs.zIndex = 1;
            bs.visibility = "visible";
            s.height = tw_TabFolder._tabsHeight + (tw_sizeIncludesBorders ? borderSize : margin) + "px";
            s.paddingLeft = "4px";
            s.paddingRight = "4px";
        } else {
            var margin = 2;
            bs.zIndex = 0;
            bs.visibility = "hidden";
            s.height = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? borderSize : borderSize + margin) + "px";
            s.paddingLeft = "2px";
            s.paddingRight = "2px";
        }

        s.marginTop = margin + "px";
        tw_setFocusCapable(this._tab, active);
    },    
        
    setStyle: function(name, value) {
        this.$.setStyle.apply(this, [name, value]);
        
        if (name == "backgroundColor") {
            this._tab.style.backgroundColor = value;
        } else if (name == "borderSize") {
            var borderSize = this.getStyle("borderSize");
            this._tab.style.lineHeight = tw_TabFolder._tabsHeight - (tw_sizeIncludesBorders ? borderSize : borderSize * 2) + "px";
            this._borderBox.style.borderBottomWidth = "0px";
            this.setActiveStyle(this._box.style.visibility == "visible");
        }
    },
    
    setOpacity: function(opacity) {
        this.$.setOpacity.apply(this, [opacity]);
        this._tab.style.display = opacity > 0 ? "block" : "none";        
        this._tab.style.opacity = opacity / 100;
        if (tw_isIE) this._tab.style.filter = opacity >= 100 ? "" : "alpha(opacity=" + opacity + ")";
    },

    destroy: function() {
        this.$.destroy.apply(this, []);
        this._tab = null;
    }    
});
