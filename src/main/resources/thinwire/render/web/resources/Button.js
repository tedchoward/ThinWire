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
//TODO: In opera, when tabbing to a button, the text gets highlighted for some reason.
//TODO: When a button or component is disabled it should be excluded from the tabbing order.
//TODO: focus gain / lose does not work in firefox.
var tw_Button = tw_Component.extend({
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "button", id, containerId);
        var s = this._box.style;
        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;border-style:solid;border-color:" + tw_COLOR_WINDOWFRAME + 
            ";border-width:0px;background-color:" + tw_COLOR_TRANSPARENT + ";";
        tw_Component.setCSSText(cssText, this._box, true);

        var border = this._borderBox = this._backgroundBox = this._focusBox = document.createElement("div");
        var s = border.style;
        cssText = "overflow:hidden;position:absolute;";
        tw_Component.setCSSText(cssText, border);
        this._box.appendChild(border);

        var surface = this._fontBox = this._focusBox = document.createElement("a");
        var s = surface.style;
        cssText = "display:block;overflow:hidden;cursor:default;position:absolute;white-space:nowrap;text-align:center;" +
            "padding-left:" + tw_Button.textPadding + "px;padding-right:" + tw_Button.textPadding + "px;background-color:" + 
            tw_COLOR_TRANSPARENT + ";background-repeat:no-repeat;background-position:center;";
        tw_Component.setCSSText(cssText, surface);
        surface.appendChild(document.createTextNode(""));
        border.appendChild(surface);
        
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));    
        tw_addEventListener(this._box, ["mouseup", "mouseout"], this._mouseUpListener.bind(this));    
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        tw_addEventListener(border, "focus", this._focusListener.bind(this));    
        tw_addEventListener(border, "blur", this._blurListener.bind(this));
        
        this.init(-1, props);
    },    
    
    _setStandardStyle: function(state) {
        if (parseInt(this._box.style.borderWidth) == state) return;
        this._boxSizeSub = state ? 2 : 0;
        this._box.style.borderWidth = state ? "1px" : "0px";
        this.setWidth(this._width);
        this.setHeight(this._height);
    },
    
    _mouseDownListener: function(ev) {
        if (!this._enabled ) return; 
        this._borderBox.style.borderStyle = "inset";
    	  var now=new Date();
    	  var milliseconds=now.getMinutes()*60*1000+now.getSeconds()*1000+now.getMilliseconds();
       	this._down=milliseconds;
    },
    
    _mouseUpListener: function(ev) {
        if (this._enabled || ev.type == "mouseout") {
            this._borderBox.style.borderStyle = this._borderType;
        }
  	  var now=new Date();
	  var milliseconds=now.getMinutes()*60*1000+now.getSeconds()*1000+now.getMilliseconds();
	  var diff=milliseconds-this._down;
	
	   	if(ev.type=="mouseup"&&diff <400&&this._enabled) // <700 milliseconds represents a click
	  {
		var type="click";
		if(this._clickTime&&milliseconds-this._clickTime<400)
		{
			type="dblclick";
			this._clickTime=null;
		}
		else{
			this._clickTime=milliseconds;
		}
		  this.fireAction(ev, type, this);
	  }
        
    },

    //TODO: Will simply returning false from click when disabled, work in Gecko?
    _clickListener: tw_Component.clickListener,
    
    fireClick: function() {
        var ev = {};
        ev.type = "click"
        this._clickListener(ev); 
    },

    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        width -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) width -= this._borderSizeSub;
        if (width < 0) width = 0;
        this._borderBox.style.width = width + (this._borderImage != null ? this._borderSizeSub : 0) + "px";
        var s = this._fontBox.style;
        if (!tw_sizeIncludesBorders) width -= parseInt(s.paddingLeft) + parseInt(s.paddingRight);
        s.width = width < 0 ? "0px" : width + "px";
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= this._boxSizeSub;
        if (!tw_sizeIncludesBorders) height -= this._borderSizeSub;
        if (height < 0) height = 0;
        var s = this._fontBox.style;
        s.height = s.lineHeight = height + "px";
        this._borderBox.style.height = height + (this._borderImage != null ? this._borderSizeSub : 0) + "px";
    },
    
    setEnabled: function(enabled) {
        if (!enabled) this._setStandardStyle(false);
        arguments.callee.$.call(this, enabled);
        this._fontBox.style.color = enabled ? this._fontColor : tw_COLOR_GRAYTEXT; 
        if (this._focusCapable) tw_setFocusCapable(this._fontBox, enabled);
    },
    
    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._fontBox, true);
        } else {
            tw_setFocusCapable(this._fontBox, false);
        }
    },
    
    setFocus: function(focus) {
        if (!this._enabled || !this.isVisible()) return false;
        this._setStandardStyle(focus);
        return arguments.callee.$.call(this, focus);
    },
    
    keyPressNotify: function(keyPressCombo) {
        if (keyPressCombo == "Enter" || keyPressCombo == "Space") {
            this.fireClick();
            return false;
        } else {
            return arguments.callee.$.call(this, keyPressCombo);
        }        
    },
    
    setText: function(text) {
        var b = this._fontBox;         
        b.replaceChild(tw_Component.setRichText(text), b.firstChild);
        b.style.backgroundPosition = text.length > 0 ? "5% 50%" : "center";
    },
    
    setImage: function(image) {
        var s = this._fontBox.style;
        s.backgroundImage = tw_Component.expandUrl(image, true);
        s.paddingLeft = (image.length > 0 ? tw_Button.imagePadding : tw_Button.textPadding) + "px";
        this.setWidth(this._width);
        this.setHeight(this._height);
    },

    setStandard: function(state) {
        var w = this.getBaseWindow();
        var sButton = w.getStandardButton();
        
        if (state) {        
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                this._setStandardStyle(true);
            }
            
            w.setStandardButton(this);
        } else if (sButton != null && sButton._id == this._id) {
            if (tw_Component.currentFocus == null || !(tw_Component.currentFocus instanceof tw_Button)) {
                sButton._setStandardStyle(false);
            }
            
            w.setStandardButton(null); 
        }
    },

    getDragBox: function() {
        var dragBox = this._fontBox.cloneNode(true);
        dragBox.style.backgroundColor = tw_COLOR_TRANSPARENT;
        return dragBox;
    },
    
    destroy: function() {
        var w = this.getBaseWindow();
        if (w.getStandardButton() === this) w.setStandardButton(null);
        arguments.callee.$.call(this);
    }
});

tw_Button.textPadding = 1;
tw_Button.imagePadding = 16;
