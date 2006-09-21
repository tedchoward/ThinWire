/*
 #LICENSE_HEADER#
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
        this.$.construct.apply(this, ["frame", id, 0]);
        var s = this._box.style;
        s.cursor = "default";    
        s.top = "0px";
        s.left = "0px";
        s.backgroundColor = tw_COLOR_WINDOW;
        s.width = "100%";
        s.height = "100%";
        
        tw_Frame.active = this;
        this._modalDialogIds = [];
        tw_setSelectionEnabled(false);
                
        this._backgroundBox = this._container = document.createElement("div");
        this._container.className = "container";
        var s = this._container.style;
        s.backgroundColor = tw_COLOR_WINDOW;
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
        s.backgroundColor = tw_COLOR_TRANSPARENT;
        s.zIndex = "1";        
        
        if (tw_isIE) {
            //NOTE: IE allows clicks to propagate if the background-color is transparent.
            //However, if the background is white and the opacity is zero, it works like it should.
            s.filter = "alpha(opacity=0)";
            s.backgroundColor = "white";
        }
        
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
        if (window.innerWidth) {
            var innerWidth = window.innerWidth;
            var innerHeight = window.innerHeight;
        } else {
            var innerWidth = document.body.clientWidth;
            var innerHeight = document.body.clientHeight;
        }

        if (window.outerWidth) {        
            var outerWidth = window.outerWidth;
            var outerHeight = window.outerHeight;
        } else {                
            try {                            
                this._resizeTimerId = null;    
                this._resizeInProgress = true;
                window.resizeTo(innerWidth, innerHeight);
                var outerWidth = innerWidth + (innerWidth - document.body.clientWidth);
                var outerHeight = innerHeight + (innerHeight - document.body.clientHeight);
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
        if (target.className == undefined) return;
        while (target.className != "dialog" && target.className != "frame" && target.className != "mainMenu") target = target.parentNode;       
        
        if (target.className != "dialog" && target.parentNode.className != "dialog") {
            if (tw_Dialog.active != null) tw_Dialog.active.setActive(false);
            if (tw_Frame.active.getMenu() != null && target.className != "mainMenu") tw_Frame.active.getMenu().close();
        }
    },
        
    _windowBoundsListener: function(event) {
        if (this._resizeInProgress) return;
        if (this._resizeTimerId != null) clearTimeout(this._resizeTimerId);
        this._resizeTimerId = setTimeout(this._getFrameBounds, 100);
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
            menu._box.style.height = tw_Dialog.menuBarHeight - (tw_sizeIncludesBorders ? 0 : 5) + "px";
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
        this.$.destroy.apply(this, []);
    }
});

tw_Frame.active = null;
