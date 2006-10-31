
var tw_BorderImage = Class.extend({
    _imgWidth: 0,
    _imgHeight: 0,
    _comp: null,
    _borderSize: -1,
    _cClip: null,
    _borderBox: null,
    _lt: null,
    _lb: null,
    _rt: null,
    _rb: null,
    _l: null,
    _r: null,
    _t: null,
    _b: null,
    _c: null,
    _e: null,    
        
    construct: function() {
        this._cClip = {left:0,right:0,top:0,bottom:0};
        this._e = this._newBorder(null);
        this._lt = this._newBorder({left:0,top:0,backgroundPosition:"left top"});
        this._lb = this._newBorder({left:0,bottom:0,backgroundPosition:"left bottom"});
        this._rt = this._newBorder({right:0,top:0,backgroundPosition:"right top"});
        this._rb = this._newBorder({right:0,bottom:0,backgroundPosition:"right bottom"});
        this._t = this._newBorder({top:0}, true);
        this._b = this._newBorder({bottom:0}, true);
        this._l = this._newBorder({left:0}, true);
        this._r = this._newBorder({right:0}, true);
        this._c = this._newBorder(null, true);
    },
    
    _newBorder: function(props, imgTag) {
        var border = document.createElement(imgTag ? "img" : "div");
        var s = border.style;
        s.position = "absolute";
        s.overflow = "hidden";
        s.fontSize = "0";
        
        for (var prop in props) {
            s[prop] = props[prop];
        }
        
        if (this._e != null) this._e.appendChild(border);
        return border;
    },
    
    setComponent: function(comp) {
        if (this._comp != null) {
            this._e.removeChild(this._borderBox);
            if (this._e.parentNode != null) this._e.parentNode.replaceChild(this._borderBox, this._e);
            
            if (this._comp._box === this._e) {
                this._comp._box = this._borderBox;
                this._borderBox.style.margin = "0px";
            }
        
            this._comp._borderBox = this._borderBox;
            this._borderBox = null;
            this._comp = null;
            this._borderSize = -1;
        }
        
        if (comp != null) {
            this._comp = comp;
            this._borderBox = comp._borderBox;

            if (comp._box === this._borderBox) {
                comp._box = this._e;
                this._borderBox.style.top = "0px";
                this._borderBox.style.left = "0px";
                this._borderBox.style.margin = this._borderSize + "px";
            }
            
            if (this._borderBox.parentNode != null) this._borderBox.parentNode.replaceChild(this._e, this._borderBox);
            this._e.appendChild(this._borderBox);
            comp._borderBox = this._e;
        }
    },
    
    setBorderSize: function(borderSize) {
        this._borderSize = borderSize;
        if (this._comp != null && this._comp._box === this._e) this._borderBox.style.margin = borderSize + "px";
    },
    
    setWidth: function(width) {
        if (this._borderBox == null || this._borderSize < 0 || width < 0) return;
        this._borderBox.style.width = width - this._borderSize * 2 + "px";
        var factor = this._imgWidth / (this._imgWidth - this._borderSize * 2);
        var dim = (width - this._borderSize * 2) * factor;
        var borderDim = dim * (this._borderSize / this._imgWidth);
        var cs = this._c.style;
        var ts = this._t.style;
        var bs = this._b.style;
        cs.width = bs.width = ts.width = dim + "px";
        bs.height = ts.height = this._imgHeight + "px";
        cs.left = bs.left = ts.left = "-" + (borderDim - this._borderSize) + "px";
        ts.clip = "rect(auto " + (dim - borderDim) + "px " + this._borderSize + "px " + borderDim + "px)";
        bs.clip = "rect(" + (this._imgHeight - this._borderSize) + "px " + (dim - borderDim) + "px auto " + borderDim + "px)";
        this._cClip.right = dim - borderDim;
        this._cClip.left = borderDim;
        cs.clip = "rect(" + this._cClip.top + "px " + this._cClip.right + "px " + this._cClip.bottom + "px " + this._cClip.left + "px)";
        this._lt.style.width = this._lb.style.width = this._rt.style.width = this._rb.style.width = this._borderSize + "px";
    },

    setHeight: function(height) {
        if (this._borderBox == null || this._borderSize < 0 || height < 0) return;
        this._borderBox.style.height = height - this._borderSize * 2 + "px";
        var factor = this._imgHeight / (this._imgHeight - this._borderSize * 2);
        var dim = (height - this._borderSize * 2) * factor;
        var borderDim = dim * (this._borderSize / this._imgHeight);
        var cs = this._c.style;
        var ls = this._l.style;
        var rs = this._r.style;
        cs.height = rs.height = ls.height = dim + "px";
        rs.width = ls.width = this._imgWidth + "px";
        cs.top = rs.top = ls.top = "-" + (borderDim - this._borderSize) + "px";
        ls.clip = "rect(" + borderDim + "px " + this._borderSize + "px " + (dim - borderDim) + "px auto)";
        rs.clip = "rect(" + borderDim + "px auto " + (dim - borderDim) + "px " + (this._imgWidth - this._borderSize) + "px)";
        this._cClip.bottom = dim - borderDim;
        this._cClip.top = borderDim;
        cs.clip = "rect(" + this._cClip.top + "px " + this._cClip.right + "px " + this._cClip.bottom + "px " + this._cClip.left + "px)";
        this._lt.style.height = this._lb.style.height = this._rt.style.height = this._rb.style.height = this._borderSize + "px";
    },
    
    setImage: function(image, width, height) {
        this._imgWidth = width;
        this._imgHeight = height;
        this._c.src = this._t.src = this._b.src = this._l.src = this._r.src = tw_Component.expandUrl(image);
        this._lt.style.backgroundImage = this._lb.style.backgroundImage = this._rt.style.backgroundImage =
            this._rb.style.backgroundImage = tw_Component.expandUrl(image, true);
    },
    
    destroy: function() {
        this.setComponent(null);
        this._cClip = this._c = this._t = this._b = this._l = this._r = this._lt = this._lb = this._rt = this._rb = null;
    }
});

