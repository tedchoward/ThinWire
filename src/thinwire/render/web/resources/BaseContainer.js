/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//TODO: Provide a way to determine the base container
var tw_BaseContainer = tw_Component.extend({
    _container: null,
    _children: null,
    _offsetX: 0,
    _offsetY: 0,
    
    construct: function(className, id, containerId) {
        this.$.construct.apply(this, ["div", className, id, containerId]);
        this._fontBox = null;
        this._container = this._box;
        this._children = [];
        this.setStyle("borderSize", 0);
    },
    
    getContainer: function() {
        return this._container;
    },

    getOffsetX: function() {
        return this.getStyle("borderSize") + this._offsetX - this._box.scrollLeft;
    },

    getOffsetY: function() {
        return this.getStyle("borderSize") + this._offsetY - this._box.scrollTop;
    },
    
    setScroll: function(scrollCode) {
        var overflow;
        
        switch (scrollCode) {
            case 1: overflow = "auto"; break;
            case 2: overflow = "scroll"; break;
            default: overflow = "hidden";
        }

        this._container.style.overflow = overflow;
    },
        
    addComponent: function(insertAtIndex, comp) {
        if (insertAtIndex == -1 || insertAtIndex >= this._children.length) {
            this._container.appendChild(comp._box);
            this._children.push(comp);
            comp._parentIndex = this._children.length - 1;
        } else {
            this._container.insertBefore(comp._box, this._children[insertAtIndex]._box);
            this._children.splice(insertAtIndex, 0, comp);
            
            for (var i = this._children.length; --i > insertAtIndex;) {
                this._children[i]._parentIndex++;
            }
        }        
    },
    
    removeComponent: function(componentId) {        
        for (var i = this._children.length; --i >= 0;) {
            var child = this._children[i];
            
            if (child._id == componentId) {
                this._container.removeChild(child._box);                
                this._children.splice(i, 1);

                for (var cnt = this._children.length; i < cnt; i++) {
                    this._children[i]._parentIndex--;
                }
                
                child.destroy();
                break;
            }
        }
    },
    
    destroy: function() {
        for (var i = this._children.length; --i >= 0;) {
            this._children[i].destroy();
        }
        
        this._container = this._children = null;
        this.$.destroy.apply(this, []);
    }
});

//NOTE: This function is defined here so it can be shared by Frame and Dialog.
tw_BaseContainer.keyPressNotifyCtrlEnterButton = function(keyPressCombo) {
    if (keyPressCombo == "Ctrl-Enter" || (keyPressCombo == "Enter" && !(tw_Component.currentFocus instanceof tw_TextArea))) {
        var button = this.getStandardButton();
        if (button != null && button.isEnabled()) button.fireClick();
        return false;
    } else {
        return this.$.keyPressNotify.apply(this, [keyPressCombo]);
    }
};

