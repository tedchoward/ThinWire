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
var tw_BorderImage = Class.extend({
    _imgWidth: 0,
    _imgHeight: 0,
    _pctLeft: 0,
    _pctWidth: 0,
    _pctTop: 0,
    _pctHeight: 0,
    _clipLeft: 0,
    _clipRight: 0,
    _clipTop: 0,
    _clipBottom: 0,
    _box: null,
    _borderSize: -1,
    _borderSizeSub: -1,
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
        this._lt = this._newBorder({left:"0px",top:"0px",backgroundPosition:"left top"});
        this._lb = this._newBorder({left:"0px",bottom:"0px",backgroundPosition:"left bottom"});
        this._rt = this._newBorder({right:"0px",top:"0px",backgroundPosition:"right top"});
        this._rb = this._newBorder({right:"0px",bottom:"0px",backgroundPosition:"right bottom"});
        this._t = this._newBorder({top:"0px"}, true);
        this._b = this._newBorder({bottom:"0px"}, true);
        this._l = this._newBorder({left:"0px"}, true);
        this._r = this._newBorder({right:"0px"}, true);
        this._c = this._newBorder(null, true);
    },
    
    _newBorder: function(props, imgTag) {
        var border = document.createElement(imgTag ? "img" : "div");
        var s = border.style;
        s.position = "absolute";
        s.overflow = "hidden";        
        s.border = s.fontSize = "0px";
        
        for (var prop in props) {
            s[prop] = props[prop];
        }

        return border;
    },
    
    setBox: function(box) {
        if (this._box != null) {
            this._e.removeChild(this._box);
            if (this._e.parentNode != null) this._e.parentNode.replaceChild(this._box, this._e);
            this._e.removeChild(this._lt);
            this._e.removeChild(this._lb);
            this._e.removeChild(this._rt);
            this._e.removeChild(this._rb);
            this._e.removeChild(this._t);
            this._e.removeChild(this._b);
            this._e.removeChild(this._l);
            this._e.removeChild(this._r);
            this._e.removeChild(this._c);
            this._box.style.top = this._e.style.top;
            this._box.style.left = this._e.style.left;
            this._box.style.margin = this._e.style.margin;
            this._box = null;
            this._e = null;
            this._borderSize = -1;
            this._borderSizeSub = -1;
        }
        
        if (box != null) {
            this._box = box;
            this._e = box.cloneNode(false);
            this._e.style.borderWidth = "0px";
            this._e.style.backgroundImage = "";
            this._e.style.backgroundColor = "transparent";
            
            this._e.appendChild(this._t);
            this._e.appendChild(this._b);
            this._e.appendChild(this._l);
            this._e.appendChild(this._r);
            this._e.appendChild(this._lt);
            this._e.appendChild(this._lb);
            this._e.appendChild(this._rt);
            this._e.appendChild(this._rb);
            this._e.appendChild(this._c);
            if (box.parentNode != null) box.parentNode.replaceChild(this._e, box);
            box.style.top = "0px";
            box.style.left = "0px";
            box.style.borderWidth = "0px";
            this._e.appendChild(box);
        }
        
        return this._e;
    },
    
    setBorderSize: function(borderSize) {
        this._borderSize = borderSize;
        this._borderSizeSub = borderSize * 2;
        this._box.style.margin = borderSize + "px";
        
        this._pctLeft = this._borderSize / this._imgWidth;
        this._pctWidth = this._imgWidth / (this._imgWidth - this._borderSizeSub);
        this._pctTop = this._borderSize / this._imgHeight;
        this._pctHeight = this._imgHeight / (this._imgHeight - this._borderSizeSub);
        
        this._lt.style.width = this._lb.style.width = this._rt.style.width = this._rb.style.width =
            this._lt.style.height = this._lb.style.height = this._rt.style.height = this._rb.style.height = this._borderSize + "px";
    },
    
    setImage: function(image, width, height) {
        this._imgWidth = width;
        this._imgHeight = height;
        this._c.src = this._t.src = this._b.src = this._l.src = this._r.src = tw_Component.expandUrl(image);
        this._lt.style.backgroundImage = this._lb.style.backgroundImage = this._rt.style.backgroundImage =
            this._rb.style.backgroundImage = tw_Component.expandUrl(image, true);
        this._b.style.height = this._t.style.height = this._imgHeight + "px";
        this._l.style.width = this._r.style.width = this._imgWidth + "px";
    },
    
    setWidth: function(width) {
        if (this._box == null || this._borderSize < 0 || width < 0) return;
        width -= this._borderSizeSub;
        this._box.style.width = width + "px";
        width = width * this._pctWidth;
        this._clipLeft = Math.floor(width * this._pctLeft);
        width = Math.floor(width);
        this._clipRight = width - this._clipLeft;
        
        var cs = this._c.style, ts = this._t.style, bs = this._b.style;
        cs.left = ts.left = bs.left = -(this._clipLeft - this._borderSize) + "px";
        cs.width = ts.width = bs.width = width + "px";
        ts.clip = "rect(auto " + this._clipRight + "px " + this._borderSize + "px " + this._clipLeft + "px)";
        bs.clip = "rect(" + (this._imgHeight - this._borderSize) + "px " + this._clipRight + "px auto " + this._clipLeft + "px)";
        cs.clip = "rect(" + this._clipTop + "px " + this._clipRight + "px " + this._clipBottom + "px " + this._clipLeft + "px)";
    },

    setHeight: function(height) {
        if (this._box == null || this._borderSize < 0 || height < 0) return;
        height -= this._borderSizeSub;
        this._box.style.height = height + "px";
        height *= this._pctHeight;
        this._clipTop = Math.floor(height * this._pctTop);
        height = Math.floor(height);
        this._clipBottom = height - this._clipTop;

        var cs = this._c.style, ls = this._l.style, rs = this._r.style;
        cs.top = ls.top = rs.top = -(this._clipTop - this._borderSize) + "px";
        cs.height = ls.height = rs.height = height + "px";
        ls.clip = "rect(" + this._clipTop + "px " + this._borderSize + "px " + this._clipBottom + "px auto)";
        rs.clip = "rect(" + this._clipTop + "px auto " + this._clipBottom + "px " + (this._imgWidth - this._borderSize) + "px)";
        cs.clip = "rect(" + this._clipTop + "px " + this._clipRight + "px " + this._clipBottom + "px " + this._clipLeft + "px)";
    },
    
    destroy: function() {
        this.setBox(null);
        this._c = this._t = this._b = this._l = this._r = this._lt = this._lb = this._rt = this._rb = null;
    }
});

