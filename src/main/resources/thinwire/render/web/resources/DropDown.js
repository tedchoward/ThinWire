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
var tw_DropDown = tw_BaseText.extend({
    _ddComp: null,
    _button: null,
    _buttonBorder: null,
    _editAllowed: true,
    _buttonWidth: 16,
    _focusBorderSize: 0,
    _buttonBorderSize: 0,

    construct: function(id, containerId, props) {
        var button = this._button = document.createElement("div");
        var buttonBorder = this._buttonBorder = document.createElement("div");
        arguments.callee.$.call(this, ["div", "input", "text"], "dropDownGridBox", id, containerId);
        //Hack to work around IE height sizing issue (font-size: 1px;)
        tw_Component.setCSSText("position:absolute;overflow:hidden;padding:0px;margin:0px;font-size:1px;", this._box);
        this._subtractEditorWidth += this._buttonWidth;

        var s = button.style;
        var cssText = "background-repeat:no-repeat;background-position:center center;background-color:" + tw_COLOR_BUTTONFACE + ";";
        tw_Component.setCSSText(cssText, button);
        tw_Component.applyButtonBorder(button);

        var s = buttonBorder.style;
        cssText = "position:absolute;right:0px;background-color:" + tw_COLOR_TRANSPARENT + ";border-style:solid;border-color:" +
            tw_COLOR_WINDOWFRAME + ";border-width:0px;";
        tw_Component.setCSSText(cssText, buttonBorder);
        buttonBorder.appendChild(button);
        this._box.appendChild(buttonBorder);

        tw_addEventListener(this._box, "focus", this._focusListener);
        tw_addEventListener(this._box, "blur", this._blurListener);

        tw_addEventListener(button, "mousedown", this._buttonMouseDownListener.bind(this));
        tw_addEventListener(button, ["mouseup", "mouseout"], this._buttonMouseUpListener.bind(this));
        this._buttonClickListener = this._buttonClickListener.bind(this);
        tw_addEventListener(button, "click", this._buttonClickListener);

        this._parentScrollListener = this._parentScrollListener.bind(this);
        tw_addEventListener(this._parent._container, "scroll", this._parentScrollListener);

        this.init(-1, props);
    },

    setComponent: function(compId) {
        var comp = tw_Component.instances[compId];
        var showDropDown = false;
        //Destroy existing Component for the dropdown if it exists
        if (this._ddComp != null) {
            showDropDown = this._ddComp.isVisible();
            this._ddComp.destroy();
        }
        this._ddComp = comp;
        this._ddComp._box.style.zIndex = 1;

        this._ddComp.setCompVisible = this._ddComp.setVisible;
        this._ddComp.setVisible = this.setDropDownVisible.bind(this);

        this._ddComp.setVisible(showDropDown);
    },

    addCloseComponent: function(compId) {
        var comp = tw_Component.instances[compId];
        comp._dropDownId = this._id;
        comp.fireCompAction = comp.fireAction;
        comp.fireAction = this._fireCloseAction;
    },

    _fireCloseAction: function(subType, action, data) {
        if (action == "click") tw_Component.instances[this._dropDownId]._ddComp.setVisible(false);
        this.fireCompAction(subType, action, data);
    },

    _buttonMouseDownListener: function(ev) {
        if (!this._enabled || tw_getEventButton(ev) != 1) return;
        var s = this._button.style;
        s.borderStyle = "solid";
        var pad = this._buttonBorderSize;
        var width = Math.floor(pad / 2);
        pad -= width;
        s.borderWidth = width + "px";
        s.borderColor = tw_COLOR_THREEDSHADOW;
        s.padding = pad + "px";
    },

    _buttonMouseUpListener: function(ev) {
        if (this._enabled && tw_getEventButton(ev) == 1 || ev.type == "mouseout") {
            var s = this._button.style;
            tw_Component.applyButtonBorder(this._button);
            s.borderWidth = this._buttonBorderSize + "px";
            s.padding = "0px";
        }
    },

    _buttonClickListener: function(ev) {
        if (!this._enabled || this._ddComp == null) return;

        if (!this._ddComp.isVisible()) {
            if (this._focusCapable) this.setFocus(true);
            this.setDropDownVisible(true);
        } else {
            this.setDropDownVisible(false);
        }
    },

    _parentScrollListener: function(ev) {
        if (this._ddComp.isVisible()) {
            this._ddComp.setVisible(false);
        }
    },

    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        width = this._buttonWidth;
        width -= this._focusBorderSize * 2;
        if (width < 0) width = 0;
        this._buttonBorder.style.width = width + "px";
        width -= this._buttonBorderSize * 2;
        if (width < 0) width = 0;
        this._button.style.width = width + "px";
    },

    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        height -= this._borderSizeSub;
        height -= this._focusBorderSize * 2;
        if (height < 0) height = 0;
        this._buttonBorder.style.height = height + "px";
        height -= this._buttonBorderSize * 2;
        if (height < 0) height = 0;
        this._button.style.height = height + "px";
    },

    setVisible: function(visible) {
        arguments.callee.$.call(this, visible);
        if (!visible) this.setDropDownVisible(false);
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        this._button.style.backgroundImage = "url(" + (enabled ? tw_IMAGE_DROPDOWN_BUTTONARROW : tw_IMAGE_DROPDOWN_BUTTONARROWDISABLED) + ")";
        if (enabled && !this._editAllowed) this._editor.readOnly = true;
    },

    setFocus: function(focus) {
        if (!this._enabled || !this.isVisible()) return false;

        if (!focus && this._ddComp != null) {
            var active = tw_getActiveElement();

            if (active != null) {
                while (active.id <= 0) active = active.parentNode;
                if (active.id == this._ddComp._id) return false;
            }
            this._ddComp.setVisible(false);
        }

        this._setFocusStyle(focus);
        return arguments.callee.$.call(this, focus);
    },

    setStyle: function(name, value) {
        if (name == "borderWidth") {
            this._buttonBorderSize = parseInt(value, 10) >= 2 ? 2 : 1;
            this._button.style.borderWidth = this._buttonBorderSize + "px";
        }

        arguments.callee.$.call(this, name, value);
    },

    keyPressNotify: function(keyPressCombo) {
        if (this._ddComp == null) return;
        if (!this._ddComp.isVisible()) {
            if (keyPressCombo == "ArrowDown") {
                this.setDropDownVisible(true);
                return false;
            } else {
                return arguments.callee.$.call(this, keyPressCombo);
            }
        } else {
            if (keyPressCombo == "Esc" || keyPressCombo == "Enter") {
                var retval = false;
                if (keyPressCombo == "Enter") retval = this._ddComp.keyPressNotify(keyPressCombo);
                this.setDropDownVisible(false);
                return retval;
            } else {
                return this._ddComp.keyPressNotify(keyPressCombo);
            }
        }
    },

    setEditAllowed: function(editAllowed) {
        this._editAllowed = editAllowed;
        if (this._enabled) this.setEnabled(true); //This will trigger the proper editAllowed state.

        //If edit is not allowed we want a click on the edit field to trigger the drop down.
        if (editAllowed) {
            tw_removeEventListener(this._editor, "click", this._buttonClickListener);
            this._editor.style.cursor = "";
        } else {
            tw_addEventListener(this._editor, "click", this._buttonClickListener);
            this._editor.style.cursor = "default";
        }
    },

    _setFocusStyle: function(state) {
        if (this._focusBorderSize == state) return;
        this._focusBorderSize = state ? 1 : 0;
        this._buttonBorder.style.borderWidth = this._focusBorderSize + "px";
        this.setWidth(this._width);
        this.setHeight(this._height);
    },

    setDropDownVisible: function(state) {
        if (this._ddComp == null) return;

        if (state) {
            var parent = this._parent;
            var offsetX = this._x;
            var offsetY = this._y;

            while (!(parent instanceof tw_Frame || parent instanceof tw_Dialog)) {
                offsetX += parent._x + parent.getOffsetX();
                offsetY += parent._y + parent.getOffsetY();
                parent = parent._parent;
            }

            offsetX += parent.getOffsetX();
            offsetY += parent.getOffsetY();

            var comp = this._ddComp;
            var availableHeight = tw_getVisibleHeight() - comp._box.parentNode.offsetTop - offsetY - this._height;
            offsetY += (availableHeight < comp._height ? -comp._height : this._height);

            comp.setY(offsetY);
            comp.setX(offsetX);
            comp.setCompVisible(true);
            if (comp._focusCapable) comp.setFocus(true);
        } else {
            if (!this._ddComp.isVisible()) return;
            this._ddComp.setCompVisible(false);
            if (this._focusCapable) this.setFocus(true);
        }
    },

    destroy: function() {
        tw_removeEventListener(this._parent._container, "scroll", this._parentScrollListener);
        this._ddComp.destroy();
        this._ddComp = this._button = this._buttonBorder = null;
        arguments.callee.$.call(this);
    }
});


