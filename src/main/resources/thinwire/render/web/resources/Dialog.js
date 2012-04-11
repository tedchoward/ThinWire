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
//TODO: Opera does not support dragging the dialog window around. (Works in 9.62)
//TODO: When a second modal dialog is presented and then closed the first dialog does not remain modal.
//      Additionally, the prior dialog should be given focus.
//TODO: The menu bar of the frame is still active when a modal dialog is visible.
var tw_Dialog = tw_BaseContainer.extend({
    _closeButton: null,
    _menu: null,
    _standardButton: null,
    _modalFlashCount: -1,
    _moveDrag: null,
    _resizeDrag: null,
    _maximizeButton: null,
    _minimizeButton: null,
    _minimizable:true,
    _maximizable:true,
 


    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "dialog", id, 0);
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));
        var dialog = this._box;
        var s = dialog.style;
        var cssText = "position:absolute;overflow:visible;padding:0px;margin:0px;cursor:default;";
        tw_Component.setCSSText(cssText, this._box);

        var title = this._fontBox = document.createElement("div");
        var s = title.style;
        cssText = "padding-left:2px;margin-bottom:1px;height:18px;line-height:17px;white-space:nowrap;overflow:hidden;" +
            "background-color:" + tw_COLOR_ACTIVECAPTION + ";";
        tw_Component.setCSSText(cssText, title);
        title.appendChild(document.createTextNode(""));
//NOTE: CLOSE/MAX/MIN/RESTORE button images should be 12X12
        var closeButton = this._closeButton = document.createElement("div");
        var s = closeButton.style;
        var fontSizeText = tw_isIE ? "font-size:0px;" : "";
        cssText = "text-align:center;position:absolute;background-position:center;background-repeat:no-repeat;background-image:" +
            "url(" + tw_IMAGE_DIALOG_CLOSE + ");margin:0px;overflow:hidden;top:2px;right:2px;" + fontSizeText + "background-color:" +
            tw_COLOR_BUTTONFACE + ";color:" + tw_COLOR_BUTTONTEXT + ";";
        tw_Component.setCSSText(cssText, closeButton);
        tw_Component.applyButtonBorder(closeButton);

        title.appendChild(closeButton);

        var maximizeButton = this._maximizeButton = document.createElement("img");
        var s = maximizeButton.style;
        maximizeButton['src']= tw_IMAGE_DIALOG_MAXIMIZE ;
        var fontSizeText = tw_isIE ? "font-size:0px;" : "";
        cssText = "width:11px;height:8px;right:21px;display:block;text-align:center;position:absolute;background-position:center;background-repeat:no-repeat;" +
            ";margin:0px;overflow:hidden;top:2px;" + fontSizeText + "background-color:" +
            tw_COLOR_BUTTONFACE + ";color:" + tw_COLOR_BUTTONTEXT + ";";
        tw_Component.setCSSText(cssText, maximizeButton);
        tw_Component.applyButtonBorder(maximizeButton);

        title.appendChild(maximizeButton);
 
        var minimizeButton = this._minimizeButton = document.createElement("img");
        minimizeButton['src']=tw_IMAGE_DIALOG_MINIMIZE ;
        var s = minimizeButton.style;
        var fontSizeText = tw_isIE ? "font-size:0px;" : "";
        cssText = "width:11px;height:8px;right:40px;display:block;text-align:center;position:absolute;background-position:center;background-repeat:no-repeat;"+
        "margin:0px;overflow:hidden;top:2px;" + fontSizeText + "background-color:" +
            tw_COLOR_BUTTONFACE + ";color:" + tw_COLOR_BUTTONTEXT + ";";
        tw_Component.setCSSText(cssText, minimizeButton);
        tw_Component.applyButtonBorder(minimizeButton);

        title.appendChild(minimizeButton);

        
        
        
        
        dialog.appendChild(title);

        
        this._container = this._scrollBox = document.createElement("div");
        this._container.className = "container";
        var s = this._container.style;
        cssText = "position:absolute;background-color:" + tw_COLOR_TRANSPARENT + ";";
        tw_Component.setCSSText(cssText, this._container);
        dialog.appendChild(this._container);

        this.setModal(props.modal);
        if (!props.modal) this._mouseDownListener();
        delete props.modal;

        tw_addEventListener(closeButton, "mousedown", this._closeButtonMouseDownListener.bind(this));
        tw_addEventListener(closeButton, ["mouseup", "mouseout"], this._closeButtonMouseUpListener.bind(this));
        tw_addEventListener(closeButton, "click", this._closeButtonClickListener.bind(this));

        
        tw_addEventListener(maximizeButton, "mousedown", this._maximizeButtonMouseDownListener.bind(this));
        tw_addEventListener(maximizeButton, ["mouseup", "mouseout"], this._maximizeButtonMouseUpListener.bind(this));
        tw_addEventListener(maximizeButton, "click", this._maximizeButtonClickListener.bind(this));

        
        tw_addEventListener(minimizeButton, "mousedown", this._minimizeButtonMouseDownListener.bind(this));
        tw_addEventListener(minimizeButton, ["mouseup", "mouseout"], this._minimizeButtonMouseUpListener.bind(this));
        tw_addEventListener(minimizeButton, "click", this._minimizeButtonClickListener.bind(this));

        
        this.setActive = this.setActive.bind(this);
        this.setActive(true);

        this.init(-1, props);
    },

  
    setMaximizable: function(maximizable){
    	if(maximizable)
    	{
    		this._minimizeButton.style.right='40px';
    		this._maximizeButton.style.display='block';
    	}
    	else
    	{
    		this._minimizeButton.style.right='21px';
    		this._maximizeButton.style.display='none';
    	}
    },
    setMinimizable: function(minimizable){
    	if(minimizable)
    	{
    		this._minimizeButton.style.display='block';
    	}
    	else
    	{
    		this._minimizeButton.style.display='none';
    	}
    },
  
    _mouseDownListener: function(ev) {
        this.setFocus(true);
    },

    _moveDragListener: function(ev) {
        if (ev.type == 1) {
            var x = this._x + ev.changeInX;
            var y = this.getY() + ev.changeInY;
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            this.setX(x);
            this.setY(y);
        } else if (ev.type == 2) {
            this.firePropertyChange("position", this._x + "," + this._y);
        }
    },

    _resizeDragListener: function(ev) {
        if (ev.type == 1) {
            var width = this._width + ev.changeInX;
            var height = this._height + ev.changeInY;
            if (width < 50) width = 50;
            if (height < 50) height = 50;
            this.setWidth(width);
            this.setHeight(height);
        } else if (ev.type == 2) {
            this.firePropertyChange("size", this._width + "," + this._height);
        }
    },

    _closeButtonMouseDownListener: function(ev) {
        this.setFocus(true);
        if (tw_getEventButton(ev) != 1) return;  //only if left click
        this._closeButton.style.borderStyle = "inset";
        tw_cancelEvent(ev);
    },
    _maximizeButtonMouseDownListener: function(ev) {
        this.setFocus(true);
        if (tw_getEventButton(ev) != 1) return;  //only if left click
        this._maximizeButton.style.borderStyle = "inset";
        tw_cancelEvent(ev);
    },
    _minimizeButtonMouseDownListener: function(ev) {
        this.setFocus(true);
        if (tw_getEventButton(ev) != 1) return;  //only if left click
        this._minimizeButton.style.borderStyle = "inset";
        tw_cancelEvent(ev);
    },

    _closeButtonMouseUpListener: function(ev) {
        tw_Component.applyButtonBorder(this._closeButton);
        tw_cancelEvent(ev);
    },
    _maximizeButtonMouseUpListener: function(ev) {
        tw_Component.applyButtonBorder(this._maximizeButton);
        tw_cancelEvent(ev);
    },
    _minimizeButtonMouseUpListener: function(ev) {
        tw_Component.applyButtonBorder(this._minimizeButton);
        tw_cancelEvent(ev);
    },

    _closeButtonClickListener: function(ev) {
        tw_em.sendViewStateChanged(this._id, "closeClick", null);
    },
    _maximizeButtonClickListener: function(ev) {
        tw_em.sendViewStateChanged(this._id, "maximizeClick", null);
    },
    _minimizeButtonClickListener: function(ev) {
        tw_em.sendViewStateChanged(this._id, "minimizeClick", null);
    },

    registerEventNotifier: function(type, subType) {
        arguments.callee.$.call(this, type, subType);

        if (type == "propertyChange") {
            if (subType == "x" || subType == "y") arguments.callee.$.call(this, type, "position");
            else if (subType == "width" || subType == "height") arguments.callee.$.call(this, type, "size");
        }
    },

    setModal: function(modal) {
        if (modal) tw_Frame.active.setModalLayerVisible(modal, this);
    },

    setRepositionAllowed: function(repositionAllowed) {
        if (repositionAllowed) {
            if (this._moveDrag != null) return;
            this._moveDrag = new tw_DragHandler(this._fontBox, this._moveDragListener.bind(this));
        } else {
            if (this._moveDrag == null) return;
            this._moveDrag.destroy();
            this._moveDrag = null;
        }
    },

    setResizeAllowed: function(resizeAllowed) {
        if (resizeAllowed) {
            if (this._resizeDrag != null) return;
            var sizer = document.createElement("div");
            var s = sizer.style;
            var cssText = "width:12px;height:12px;position:absolute;overflow:hidden;background-image:url(" + tw_IMAGE_DIALOG_RESIZE +
                ");background-repeat:no-repeat;background-position:top right;right:0px;bottom:0px;cursor:NW-resize;";
            tw_Component.setCSSText(cssText, sizer);
            this._box.appendChild(sizer);
            this._resizeDrag = new tw_DragHandler(sizer, this._resizeDragListener.bind(this));
        } else {
            if (this._resizeDrag == null) return;
            this._box.removeChild(this._resizeDrag._box);
            this._resizeDrag.destroy();
            this._resizeDrag = null;
        }
    },

    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);

        if (name == "borderWidth" && this._closeButton != null) {
            var s = this._closeButton.style;
            s.borderWidth = value;
            s.width = 16 - this._borderSizeSub + "px";
            s.height = 14 - this._borderSizeSub + "px";
        }
    },

    getOffsetX: function() {
        return -this._container.scrollLeft;
    },

    getOffsetY: function() {
        return tw_Dialog.titleBarHeight + (this.getMenu() != null ? tw_Dialog.menuBarHeight : 0) - this._container.scrollTop;
    },

    setY: function(y) {
        if (tw_Frame.active.getMenu() != null) y += tw_Dialog.menuBarHeight;
        arguments.callee.$.call(this, y);
    },

    getY: function() {
        var y = this._y;
        if (tw_Frame.active.getMenu() != null) y -= tw_Dialog.menuBarHeight;
        return y;
    },

    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        width -= this._borderSizeSub;
        this._container.style.width = width < 0 ? "0px" : width + "px";
    },

    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= tw_Dialog.titleBarHeight + (this._menu == null ? 0 : tw_Dialog.menuBarHeight) + this._borderSizeSub;
        this._container.style.height = height < 0 ? "0px" : height + "px";
    },

    keyPressNotify: tw_BaseContainer.keyPressNotifyCtrlEnterButton,

    setMenu: function(menu) {
        if (this._menu != null) this._box.removeChild(this._menu._box);
        this._menu = menu;

        if (menu instanceof tw_Menu) {
            this._box.insertBefore(menu._box, this._container);
            this.setHeight(this._height);
        }
    },

    getMenu: function() {
        return this._menu;
    },

    setTitle: function(title) {
        var b = this._box.firstChild;
        b.replaceChild(tw_Component.setRichText(title), b.firstChild);
    },

    setFocus: function(focus) {
        if (tw_Frame.active.getMenu() != null) tw_Frame.active.getMenu().close();

        if (focus && tw_Dialog.active !== this) {
            if (tw_Dialog.active != null) tw_Dialog.active.setActive(false);
            this.setActive(true);
            tw_em.removeQueuedViewStateChange("focus");
            this.firePropertyChange("focus", true, "focus");
            if (tw_Component.priorFocus != null && tw_Component.priorFocus.hasPropertyChangeListener("focus")) tw_em.sendGetEvents();
        }

        return true;
    },

    setActive: function(state, flash) {
        if (flash) this._modalFlashCount = 4;
        if (this._box == null) return; // this could be true if the Dialog is set to active after it has been destroyed.
        var s = this._box.firstChild.style;

        if (this._modalFlashCount >= 0) {
            if (this._modalFlashCount == 0) {
                state = true;
            } else {
                setTimeout(this.setActive, 100);
                state = s.backgroundColor == tw_COLOR_INACTIVECAPTION;
            }

            this._modalFlashCount--;
            flash = true;
        }

        if (state) {
            s.backgroundColor = tw_COLOR_ACTIVECAPTION;
            s.color = this._fontColor;
            tw_Dialog.active = this;
            if (!flash) this._box.style.zIndex = ++tw_Component.zIndex;
        } else {
            s.backgroundColor = tw_COLOR_INACTIVECAPTION;
            s.color = tw_COLOR_INACTIVECAPTIONTEXT;
            if (this._moveDrag != null) this._moveDrag.releaseDrag();
            if (this.getMenu() != null) this.getMenu().close();
            if (!flash) tw_Dialog.active = null;
        }
    },

    getStandardButton: function() {
        return this._standardButton;
    },

    setStandardButton: function(standardButton) {
        this._standardButton = standardButton;
    },
    
    setState: function(newState)
    {
   // alert('setting stat to:'+newState);
   // if(this._state=newState)return;
    	//this._state=newState;
    	//NOTE: setting dimension and placement will be handled on the server side
    	if(newState==1)
    	{// set normal
    	//	alert('returning to normal');
    		this._maximizeButton['src']=tw_IMAGE_DIALOG_MAXIMIZE;
    		this._minimizeButton['src']=tw_IMAGE_DIALOG_MINIMIZE;
    	}
    	else if(newState==2)
    	{// set maximized
    		//alert('maximized');
    		this._maximizeButton['src']=tw_IMAGE_DIALOG_RESTORE;
    		this._minimizeButton['src']=tw_IMAGE_DIALOG_MINIMIZE;
    	}
    	else if(newState=3)
    	{// set minimized
    		//alert('minimized');
    		this._maximizeButton['src']=tw_IMAGE_DIALOG_MAXIMIZE;
    		this._minimizeButton['src']=tw_IMAGE_DIALOG_RESTORE;
    	}
    	
    	
    	
    },

    destroy: function() {
        //When removeComponent is called on Frame, this destroy method is called again so we want to stop it.
        if (this._closeButton == null) return;
        if (tw_Dialog.active === this) tw_Dialog.active = null;
        if (this._moveDrag != null) this._moveDrag.destroy();
        if (this._resizeDrag != null) this._resizeDrag.destroy();
        this._menu = this._standardButton = this._moveDrag = this._resizeDrag = this._closeButton = null;
        tw_Frame.active.setModalLayerVisible(false, this);
        document.body.removeChild(this._box);
        arguments.callee.$.call(this);
    }
});

tw_Dialog.menuBarHeight = 23;
tw_Dialog.titleBarHeight = 18 + 1; //actual height plus one for bottom margin
tw_Dialog.active = null;

