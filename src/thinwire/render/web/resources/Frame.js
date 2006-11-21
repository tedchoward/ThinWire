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
        s.cursor = "default";    
        s.top = "0px";
        s.left = "0px";
        s.width = "100%";
        s.height = "100%";
        
        tw_Frame.active = this;
        this._modalDialogIds = [];
                
        this._backgroundBox = this._container = document.createElement("div");
        this._container.className = "container";
        var s = this._container.style;
        s.position = "absolute";        
        s.overflow = "hidden";
        s.zIndex = "0";
        s.width = "100%";
        this._box.appendChild(this._container);

        this._modalLayer = document.createElement("div");
        var s = this._modalLayer.style; 
        s.display = "none";
        s.position = "absolute";
        s.height = "100%";
        s.width = "100%";
        s.zIndex = "1";
        tw_setLayerTransparent(this._modalLayer);        
        
        tw_addEventListener(this._modalLayer, "mousedown", this._modalLayerMouseDownListener);
        document.body.appendChild(this._modalLayer);
        
        this._getFrameBounds = this._getFrameBounds.bind(this);        
        tw_addEventListener(this._box, "mousedown", this._mouseDownListener.bind(this));
        tw_addEventListener(window, "resize", this._windowBoundsListener.bind(this));
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
        var innerWidth = document.body.clientWidth;
        var innerHeight = tw_getVisibleHeight();
        if (window.outerWidth) {        
            var outerWidth = window.outerWidth;
            var outerHeight = window.outerHeight;
        } else {                
            try {                            
                this._resizeTimerId = null;    
                this._resizeInProgress = true;
                window.resizeTo(innerWidth, innerHeight);
                var outerWidth = innerWidth + (innerWidth - document.body.clientWidth);
                var outerHeight = innerHeight + (innerHeight - tw_getVisibleHeight());
                window.resizeTo(outerWidth, outerHeight);
                setTimeout(function() { this._resizeInProgress = false; }.bind(this), 250);
            } catch (e) {
                this._resizeInProgress = false;
                this._resizeTimerId = setTimeout(this._getFrameBounds, 100);
                return;
            }
        }          

        this._container.style.height = innerHeight - (this._menu != null ? tw_Dialog.menuBarHeight : 0) + "px";
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
            this._modalDialogIds.pop();
            
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
            this._container.style.height = parseInt(this._container.style.height) + tw_Dialog.menuBarHeight + "px";
            this._box.removeChild(this._menu._box);
        }
        
        this._menu = menu;
        
        if (menu instanceof tw_Menu) {
            this._box.insertBefore(menu._box, this._container);
            this._container.style.height = parseInt(this._container.style.height) - tw_Dialog.menuBarHeight + "px";
        }
    },
    
    getMenu: function() {
        return this._menu;
    },
    
    setTitle: function(title) {
        document.title = title.length > 0 ? title : "(untitled)";
    },
    
    getStandardButton: function() {
        return this._standardButton;
    },

    setStandardButton: function(standardButton) {
        this._standardButton = standardButton;
    },

    destroy: function() {
        if (tw_Frame.active == this) tw_Frame.active = null;                
        document.body.removeChild(this._modalLayer);
        document.body.removeChild(this._box);
        this._menu = this._standardButton = this._modalLayer = this._modalDialogIds = null;
        arguments.callee.$.call(this);
    }
});

tw_Frame.active = null;
