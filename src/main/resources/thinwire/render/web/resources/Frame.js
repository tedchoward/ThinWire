/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
//TODO: What does enabled and visible do on a Frame?
//TODO: X & Y do not currently work on a Frame.
//TODO: Focus does not fire on any container
var tw_Frame = tw_BaseContainer.extend({
    _resizeInProgress: false,
    _resizeTimerId: null,
    _menu: null,
    _standardButton: null,
    _modalLayer: null,
    _modalDialogIds: null,
    _focusBeforeDialog: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "frame", id, 0);
        this._fontBox = this._borderBox = null;
        
        var s = this._box.style;
        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;cursor:default;top:0px;left:0px;width:100%;height:100%;";
        tw_Component.setCSSText(cssText, this._box);
        
        tw_Frame.active = this;
        this._modalDialogIds = [];
                
        this._backgroundBox = this._container = this._scrollBox = document.createElement("div");
        this._container.className = "container";
        var s = this._container.style;
        cssText = "position:absolute;overflow:hidden;z-index:0;width:100%;bottom:0px;";
        tw_Component.setCSSText(cssText, this._container);
        this._box.appendChild(this._container);

        this._modalLayer = document.createElement("div");
        var s = this._modalLayer.style; 
        cssText = "display:none;position:absolute;height:100%;width:100%;z-index:1;";
        tw_Component.setCSSText(cssText, this._modalLayer);
        tw_setLayerTransparent(this._modalLayer);        
        
        tw_addEventListener(this._modalLayer, "mousedown", this._modalLayerMouseDownListener);
        document.body.appendChild(this._modalLayer);
        
        this._getFrameBounds = this._getFrameBounds.bind(this);        
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));
        this._windowBoundsListener = this._windowBoundsListener.bind(this);
        tw_addEventListener(window, "resize", this._windowBoundsListener);
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
        this.init(-1, props);
        
        var s = document.body.style;
        s.height = "100%";
        s.padding = "0px";
        s.margin = "0px";
        s.border = "0px";
        
        //NOTE: IE will fire this on load via the windowBoundListener
        if (!tw_isIE) this._getFrameBounds();
    },
    
    _getFrameBounds: function() {        
        var innerWidth = tw_getVisibleWidth();
        var innerHeight = tw_getVisibleHeight();
        
        if (window.outerWidth) {        
            var outerWidth = window.outerWidth;
            var outerHeight = window.outerHeight;
        } else {                
            try {                            
                this._resizeTimerId = null;    
                this._resizeInProgress = true;
                window.resizeTo(innerWidth, innerHeight);
                var outerWidth = innerWidth + (innerWidth - tw_getVisibleWidth());
                var outerHeight = innerHeight + (innerHeight - tw_getVisibleHeight());
                window.resizeTo(outerWidth, outerHeight);
                setTimeout(function() { this._resizeInProgress = false; }.bind(this), 250);
            } catch (e) {
                this._resizeInProgress = false;
                this._resizeTimerId = setTimeout(this._getFrameBounds, 100);
                return;
            }
        }

        this._width = innerWidth;
        this._height = innerHeight - (this._menu != null ? tw_Dialog.menuBarHeight : 0);
        this._container.style.height = this._height + "px";
        tw_em.sendViewStateChanged(this._id, "frameSize", innerWidth + "," + innerHeight + "," + outerWidth + "," + outerHeight);
    },
    
    _modalLayerMouseDownListener: function() {
        tw_Dialog.active.setActive(true, true);
    },
    
    _mouseDownListener: function(event) {
        var target = tw_getEventTarget(event);
        if (target == null || target.className == undefined) return;
        while (target != null && target.className != "dialog" && target.className != "frame" && target.className != "mainMenu") target = target.parentNode;       
        
        if (target != null && target.className != "dialog" && target.parentNode.className != "dialog") {
            if (tw_Dialog.active != null) tw_Dialog.active.setActive(false);
            if (tw_Frame.active.getMenu() != null && target.className != "mainMenu") tw_Frame.active.getMenu().close();
        }
    },
        
    _windowBoundsListener: function(event) {
        if (this._resizeInProgress) return;
        if (this._resizeTimerId != null) clearTimeout(this._resizeTimerId);
        this._resizeTimerId = setTimeout(this._getFrameBounds, 0);
    },
    
    isModalLayerVisible: function() {
        return this._modalLayer.style.display != "none";
    },
    
    setModalLayerVisible: function(state, dialog) {
        if (state) {
            this._focusBeforeDialog = tw_Component.currentFocus;
            tw_Component.currentFocus = null;            
            if (this._modalDialogIds.length == 0) this._modalLayer.style.display = "block";            
            this._modalLayer.style.zIndex = ++tw_Component.zIndex;
            this._modalDialogIds.push(dialog);
        } else {
            for (var i = 0, cnt = this._modalDialogIds.length; i < cnt; i++) {
                if (this._modalDialogIds[i] === dialog) {
                    this._modalDialogIds.splice(i, 1);
                    break;
                }
            }
            
            if (this._modalDialogIds.length == 0) {
                this._modalLayer.style.display = "none";
            } else {
                this._modalDialogIds[this._modalDialogIds.length - 1].setActive(true);
            }
            
            //This would be the case if the component got destroyed while the dialog was visible.
            if (this._focusBeforeDialog != null && this._focusBeforeDialog._box != null) tw_Component.currentFocus = this._focusBeforeDialog;
            this._focusBeforeDialog = null;
        }
    },
    
    getOffsetY: function() {
        return arguments.callee.$.call(this) + (this.getMenu() != null ? tw_Dialog.menuBarHeight : 0);
    },     
    
    setX: function() { },    
    setY: function() { },
    setWidth: function() { },
    setHeight: function() { },
    
    keyPressNotify: tw_BaseContainer.keyPressNotifyCtrlEnterButton,
    
    setMenu: function(menu) {        
        if (this._menu != null) {
            this._container.style.height = parseInt(this._container.style.height, 10) + tw_Dialog.menuBarHeight + "px";
            document.body.removeChild(this._menu._box);
        }
        
        this._menu = menu;
        
        if (menu instanceof tw_Menu) {
			// All components inherit this style from the frame, but since the menu is not actually
			//  attached to the frame in the DOM, the style doesn't cascade down.
			menu._box.style.cursor = "default";
			document.body.appendChild(menu._box);
            this._container.style.height = parseInt(this._container.style.height, 10) - tw_Dialog.menuBarHeight + "px";
        }
    },
    
    getMenu: function() {
        return this._menu;
    },
    
    setTitle: function(title) {
        document.title = title.length > 0 ? title : "(Untitled ThinWire Application)";
    },
    
    getStandardButton: function() {
        return this._standardButton;
    },

    setStandardButton: function(standardButton) {
        this._standardButton = standardButton;
    },

    _clickListener: tw_Component.clickListener,

    destroy: function() {
        if (tw_Frame.active == this) tw_Frame.active = null;
        tw_removeEventListener(window, "resize", this._windowBoundsListener);
        document.body.removeChild(this._modalLayer);
        document.body.removeChild(this._box);
        this._menu = this._standardButton = this._modalLayer = this._modalDialogIds = null;
        arguments.callee.$.call(this);
        tw_shutdownInstance("The application instance has shutdown. Press F5 to restart the application or close the browser to end your session.");
    }
});

tw_Frame.active = null;
