/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
//TODO: scrollIntoView is not supported by Opera and therefore keyboard nav is somewhat unusable.
//TODO: A column should be visible by default
var tw_GridBox = tw_Component.extend({
    _y: -1,
    _x: -1,    
    _header: null,
    _hresize: null,    
    _content: null,
    _root: null,
    _visibleCheckBoxes: false,
    _visibleHeader: false,
    _fullRowCheckBox: false,
    _childGridBoxes: null,
    _childColumnWidth: 0,
    _childOpen: false, 
    _currentIndex: -1,
    _dropDown: null,
    _parentCell: null,
    
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "gridBox", id, containerId]);
        
        this.setStyle("backgroundColor", tw_COLOR_WINDOW);
        this.setStyle("borderSize", 2);
        this.setStyle("borderType", "inset");
        this.setStyle("borderColor", tw_borderColor);
        this.setStyle("fontColor", tw_COLOR_WINDOWTEXT);
        this.setStyle("fontSize", 8);
        this.setStyle("fontFamily", tw_FONT_FAMILY);                
        
        /*s.borderStyle = "inset";
        s.borderWidth = "2px";
        s.borderColor = tw_borderColor;
        s.backgroundColor = tw_COLOR_WINDOW;
        s.color = tw_COLOR_WINDOWTEXT;
        s.fontFamily = tw_FONT_FAMILY;
        s.fontSize = "8pt";*/
        
        this._root = this;   
        var visibleCheckBoxes = props.visibleCheckBoxes;
        var cols = props.columnData;
        var selectedRow = props.selectedRow;
        var checkedRows = props.checkedRows;
        var parentIndex = props.parentIndex;
        
        delete props.visibleCheckBoxes;        
        delete props.columnData;
        delete props.selectedRow;
        delete props.checkedRows;
        delete props.parentIndex;
        
        var header = document.createElement("div");
        header.className = "gridBoxHeader";
        var s = header.style;
        s.position = "absolute";
        s.backgroundColor = tw_COLOR_BUTTONFACE;        
        s.display = "none";
        this._hresize = {column: null, startX: -1};        
        this._header = header;
        this._box.appendChild(header);
        
        var body = document.createElement("div");
        body.className = "gridBoxBody";
        var s = body.style;
        s.position = "absolute";
        s.width = "100%";
        s.overflow = "auto";        
        s.top = "0px";
        this._box.appendChild(body);
    
        var content = document.createElement("div");
        content.className = "gridBoxContent";
        this._content = content;        
        body.appendChild(content);
    
        var empty = document.createElement("span");
        empty.style.display = "none";
        content.appendChild(empty);
        
        this._focusListener = this._focusListener.bind(this);
        this._blurListener = this._blurListener.bind(this);
        this._cellClickListener = this._cellClickListener.bind(this);
        this._columnClickListener = this._columnClickListener.bind(this);
        this._scrollListener = this._scrollListener.bind(this);
        
        tw_addEventListener(this._box, "focus", this._focusListener);    
        tw_addEventListener(header, "focus", this._focusListener);
        tw_addEventListener(body, "focus", this._focusListener);
        tw_addEventListener(content, "focus", this._focusListener);
        tw_addEventListener(this._box, "blur", this._blurListener);	
        tw_addEventListener(header, "blur", this._blurListener);
        tw_addEventListener(body, "blur", this._blurListener);    
        tw_addEventListener(content, "blur", this._blurListener);
        tw_addEventListener(header, "mousemove", this._headerMouseMoveListener.bind(this));
        tw_addEventListener(header, "mousedown", this._headerMouseDownListener.bind(this));
        tw_addEventListener(header, "mouseup", this._headerMouseUpListener.bind(this));
         
        var showDropDown = false;
        var showChild = false;
        var container = this._parent;

        //NOTE: the cell in the last column of each row may contain a tw_child field that
        //  points to a child gridbox.
        if (container instanceof tw_DropDownGridBox || container instanceof tw_GridBox) {
            this._box.style.zIndex = 1;

            this.setStyle("borderSize", 1);
            this.setStyle("borderType", "solid");
            this.setStyle("borderColor", tw_COLOR_BLACK);
                            
            if (container instanceof tw_GridBox) {
                props.x = 0;
                props.y = 0;
                if (props.width == 0) props.width = container.getWidth();
                if (props.height == 0) props.height = container.getHeight();
                
                var parentContent = container._content; 
                
                if (container._childGridBoxes == null) {
                    var columnHeader = document.createElement("div");
                    columnHeader.style.width = tw_GridBox.childColumnWidth + "px";
                    container._header.appendChild(columnHeader);                
                    
                    var column = document.createElement("div");
                    column.className = "gridBoxColumn";
                    var s = column.style;
                    s.styleFloat = "left";
                    s.overflow = "hidden";                            
                    column.style.width = tw_GridBox.childColumnWidth + "px";                
                                
                    for (var i = parentContent.firstChild.childNodes.length; --i >= 0;) {
                        column.appendChild(this._newCell("", 1));
                    }
                    
                    parentContent.insertBefore(column, parentContent.childNodes.item(parentContent.childNodes.length - 1));
                    container._childColumnWidth = tw_GridBox.childColumnWidth;
                    container.setColumnWidth();                                
                    container._childGridBoxes = [];
                    container._toggleHighlight(container._currentIndex, true);
                }
                
                this._box.style.display = "none";
                var cell = container._lastColumn().childNodes.item(parentIndex);
                
                //Destroy existing child GridBox if it exists
                if (cell.tw_child != undefined) {
                    if (cell.tw_child.isVisible()) showChild = true;
                    props.x = cell.tw_child._x;
                    props.y = cell.tw_child._y;
                    cell.tw_child.destroy(true);
                    if (container._childGridBoxes == null) container._childGridBoxes = [];
                }
                
                cell.tw_child = this;
                this._parentCell = cell;
                this._root = container._root;
                this._box.tw_root = this._root; //we have to do this so that the lose focus handler of dropdown can do the proper checks
                            
                if (container._currentIndex == parentIndex) {
                    container._toggleHighlight(parentIndex, true);
                } else if (container._currentIndex >= 0) {
                    container._toggleHighlight(parentIndex, false);
                }
                
                container._childGridBoxes.push(this);
                //If this gridbox is a member of a dropdown, then an actual click event must
                //be reported to the server so the dropdown text value gets populated.
                if (this._root._dropDown != null) this.registerEventNotifier("action", "click");
            } else {
                if (props.width < container.getWidth()) props.width = container.getWidth();
                if (props.height < container.getHeight()) props.height = container.getHeight();
                props.x = container.getX();
                props.y = container.getY() + container.getHeight();                
                this.setVisible(false);
    
                //Destroy existing GridBox for the dropdown if it exists
                if (container._ddComp != undefined) {
                    showDropDown = container._ddComp.isVisible();
                    container._ddComp.destroy();
                }
    
                container._ddComp = this;
                this._dropDown = container;
                //Since this gridbox is a member of a dropdown, an actual click event must
                //be reported to the server so the dropdown text value gets populated.
                this.registerEventNotifier("action", "click");
            }                        
        }
        
        this.init(-1, props);

        for (var i = 0, cnt = cols.length; i < cnt; i++) {
            var c = cols[i];
            this.addColumn(i, c.v, c.n, c.w, c.a);
        }    
    
        this.setVisibleCheckBoxes(visibleCheckBoxes, checkedRows);
        if (selectedRow >= 0) this.setRowIndexSelected(selectedRow, false);    
        if (showDropDown) this._dropDown.setDropDownVisible(true);    
        if (showChild) this._parent._setChildVisible(parentIndex, true);        
    },
    
    _headerMouseMoveListener: function(event) {
        if (!this.isEnabled() || this._hresize.startX != -1) return;
        var columnHeader = tw_getEventTarget(event);                
        if (columnHeader.className != "gridBoxColumnHeader" && columnHeader.className != "gridBoxHeader") return;
        if (columnHeader.className == this._header.className) this._header.style.cursor = "";    
        var offsetX = tw_getEventOffsetX(event);    
            
        var container = columnHeader;
        var clientLeft = 0;
        
        do {
            clientLeft += container.offsetLeft;
            container = container.offsetParent;        
        } while (container != null && container != undefined);

        if (event.clientX >= (clientLeft + columnHeader.offsetWidth) && event.clientX <= (clientLeft + columnHeader.offsetWidth + 5)) {
            this._hresize.column = columnHeader;
            this._header.style.cursor = "W-resize";
        } else {
            this._hresize.column = null;
            this._header.style.cursor = "";
        }
    },
        
    _headerMouseDownListener: function(event) {
        if (!this.isEnabled() || this._hresize.column == null || tw_getEventTarget(event).className != "gridBoxColumnHeader") return;    
        this._hresize.startX = event.clientX;
    },
        
    _headerMouseUpListener: function(event) {
        if (!this.isEnabled() || this._hresize.startX == -1) return;
        var resizeColumn = this._hresize.column;
        var resizeStartX = this._hresize.startX;
        
        for (var index = this._header.childNodes.length; --index >= 0;) {
            if (this._header.childNodes.item(index) == resizeColumn) break; 
        }
        
        var width = parseInt(resizeColumn.style.width);
        if (!tw_sizeIncludesBorders) width += parseInt(resizeColumn.style.borderWidth);  
        var endX = event.clientX;
        
        if (resizeStartX < endX) {                
            endX = width + (endX - resizeStartX);
        } else {
            endX = width - (resizeStartX - endX);        
        }
        
        if (endX < (tw_CALC_BORDER_PADDING_SUB + 2)) endX = tw_CALC_BORDER_PADDING_SUB + 2; 
        this.setColumnWidth(index, endX, true);    
        this._hresize.column = null;    
        this._hresize.startX = -1;    
    },

    _columnClickListener: function(event) {
        var columnHeader = tw_getEventTarget(event, "gridBoxColumnHeader");
        var cn = this._header.childNodes;        
        for (var index = cn.length; --index >= 0;) if (cn.item(index) === columnHeader) break;
        tw_em.sendViewStateChanged(this._id, "columnSort", index);        
    },

    _scrollListener: function(event) {
        if (!this.isEnabled()) return;
        var body = this._content.parentNode;        
        this._header.style.left = "-" + body.scrollLeft + "px";
        if (this._childOpen) this.closeChildren();
    },
    
    _cellClickListener: function(event) {
        if (!this.isEnabled()) return;
        this.setFocus(true);
        var cell = tw_getEventTarget(event);
        var column = cell.parentNode;    
        var index = 1;
        
        while ((cell = cell.nextSibling) != null)
            index++;
        
        index = column.childNodes.length - index;                
        this.setRowIndexSelected(index, true);
        
        if (this._visibleCheckBoxes) {
            if (this._fullRowCheckBox || (tw_getEventOffsetX(event) < 16 && this._content.childNodes.item(0) == column)) {
                var state = this.setRowIndexCheckState(index, -1);
                this.firePropertyChange("rowChecked", (state ? "t" : "f") + index);
            }
    
            this.fireAction("click", index);
        } else {
            this.fireAction("click", index);
            
            if (this._lastColumn().childNodes.item(index).tw_child != undefined) {
                this._setChildVisible(index, true);
            } else {
                this._root.closeChildren();
                
                if (this._root._dropDown != null) {
                    var dd = this._root._dropDown;
                    dd.setDropDownVisible(false);
                    dd.setFocus(true);
                }
            }
        }
    },
    
    _newCell: function(value, columnIndex, state) {
        var cell = document.createElement("div");
        cell.className = "gridBoxCell";
        var s = cell.style;
        s.height = "14px";
        s.paddingLeft = "3px";
        s.whiteSpace = "nowrap";
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center left";
        s.backgroundColor = tw_COLOR_TRANSPARENT;        
            
        if (columnIndex == 0) {
            var s = cell.style;        
    
            if (state == 0) {
                s.backgroundImage = tw_GridBox.imageUnchecked;
                s.paddingLeft = "18px";
            } else if (state == 1) {
                s.backgroundImage = tw_GridBox.imageChecked;
                s.paddingLeft = "18px";
            }            
        }
        
        if (value != undefined) cell.appendChild(document.createTextNode(value));
        tw_addEventListener(cell, "focus", this._focusListener);
        tw_addEventListener(cell, "blur", this._blurListener);        
        tw_addEventListener(cell, "click", this._cellClickListener);                        
        return cell;
    },

    _destroyChildren: function() {
        if (this._childGridBoxes != null) {
            for (var i = this._childGridBoxes.length; --i >=0;) {
                this._childGridBoxes[i].destroy();
            }
        }
    },
    
    _setChildVisible: function(index, state) {
        if (state) {
            var cell = this._lastColumn().childNodes.item(index);
            var gbc = cell.tw_child;
            
            if (gbc != undefined) {            
                var y = cell.offsetTop - this._content.parentNode.scrollTop;
                if (this._visibleHeader) y += tw_GridBox.rowHeight + this.getStyle("borderSize") * 2;
                var available = tw_getVisibleHeight() - this._box.parentNode.offsetTop - parseInt(this._box.style.top) - y - 5;
                if (available < gbc._height) y -= gbc._height - tw_GridBox.rowHeight;
                if (y < 0) y = 0;
                gbc.setY(y);
                
                var x = this._width;
                var leftGap = -(this._box.parentNode.offsetLeft + parseInt(this._box.style.left));
                available = tw_getVisibleWidth() + leftGap - x - 5;
                if (available < gbc._width) x = -gbc._width;
                if (x < leftGap) x = leftGap;
                gbc.setX(x);
                
                //TODO: if this is part of a dropdown, then we need to store off the selected row
                if (gbc._root._dropDown != null) {
                        
                }
                
                gbc._box.style.display = "block";
                this._childOpen = true;
                gbc.setFocus(true);
            }
        } else {
            //TODO: is there ever a false case? if not, then change API.
        }
    },
    
    _getColumnCount: function() {        
        return this._content == null ? 0 : this._content.childNodes.length - 1;
    },
    
    _getRowCount: function() {
        var col = this._lastColumn();
        return col == null ? 0 : col.childNodes.length;
    },
    
    _lastColumn: function() {
        var cnt = this._getColumnCount() - 1;
        return cnt < 0 ? null : this._content.childNodes.item(cnt);
    },
    
    _toggleHighlight: function(index, state) {
        if (this._getColumnCount() < 1) return; 
        var content = this._content;
        var childNodes = content.childNodes;
    
        if (state) {
            var color = tw_COLOR_HIGHLIGHTTEXT;
            var backgroundColor = tw_COLOR_HIGHLIGHT;
            var arrowImage = tw_GridBox.imageChildArrowInvert;
            var cell = childNodes.item(0).childNodes.item(index);
            var body = content.parentNode;        
            var cellOffset = cell.offsetTop + cell.offsetHeight - body.scrollTop;        
            
            if (cellOffset < tw_GridBox.rowHeight) {        
                cell.scrollIntoView(true);
            } else if (cellOffset > body.clientHeight) {
                cell.scrollIntoView(false);            
            }
        } else {
            var color = this._box.style.color;
            if (color == "") color = tw_COLOR_WINDOWTEXT;
            var backgroundColor = tw_COLOR_TRANSPARENT;
            var arrowImage = tw_GridBox.imageChildArrow;
        }   
        
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var style = childNodes.item(i).childNodes.item(index).style;
            style.color = color;
            style.backgroundColor = backgroundColor;
        }
        
        if (this._childGridBoxes != null) {
            var cell = this._lastColumn().childNodes.item(index);
            if (cell.tw_child != undefined) cell.style.backgroundImage = arrowImage;
        }    
    },
    
    setStyle: function(name, value) {
        var oldCalcBorderSize = this._borderSizeSub;
        this.$.setStyle.apply(this, [name, value]);
        
        if (name == "borderType") {
            var newType = tw_Component.getReverseBorderType(value);
            
            for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
                var h = this._header.childNodes.item(i);
                h.style.borderStyle = newType;
            }            
        } else if (name == "borderSize") {
            for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
                var h = this._header.childNodes.item(i);
                h.style.borderWidth = value + "px";
                var width = parseInt(h.style.width) + oldCalcBorderSize;
                h.style.width = width <= this._borderSizeSub ? "0px" : width - this._borderSizeSub + "px";                      
            }

            this.setVisibleHeader(this._visibleHeader);            
        }
    },
    
    setX: function(x) {
        this._x = x;
        if (this._parent instanceof tw_GridBox) x += parseInt(this._parent._box.style.left);
        this.$.setX.apply(this, [x]);
    },
        
    setY: function(y) {
        this._y = y;
        
        if (this._parent instanceof tw_GridBox) {      
            y += parseInt(this._parent._box.style.top);
            
            if (this._root._dropDown == null && this._root._id == this._parent._id) {
                var win = this.getBaseWindow();
                if (win instanceof tw_Dialog) y += tw_Dialog.titleBarHeight + tw_BORDER_WIDTH;
            }
        }    
        
        this.$.setY.apply(this, [y]);
    },
        
    setWidth: function(width) {
        this.$.setWidth.apply(this, [width]);
        this.setColumnWidth();
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        this.setVisibleHeader(this._visibleHeader);
    },
        
    setVisible: function(visible) {
        if (!visible) this.closeChildren();
        this.$.setVisible.apply(this, [visible]);
    },

    setEnabled: function(enabled) {
        this.$.setEnabled.apply(this, [enabled]);        
        this._box.style.backgroundColor = enabled ? tw_COLOR_WINDOW : tw_COLOR_THREEDFACE;                        
        tw_setFocusCapable(this._box, enabled);
    },

    setFocus: function(focus) {
        if (this._root._dropDown != null || this._root !== this) {
            return true;
        } else {
            return this.$.setFocus.apply(this, [focus]);
        }
    },
        
    keyPressNotify: function(keyPressCombo) {
        if (!this.isEnabled()) return;
        
        //Get highest open GridBox
        var top = this;       
        
        while (top._childOpen && top._childGridBoxes != null) {
            for (var i = 0, cnt = top._childGridBoxes.length; i < cnt; i++) {
                var gbc = top._childGridBoxes[i]; 
                
                if (gbc.isVisible()) {
                    top = gbc;                
                    break;
                }
            }
            
            if (i == cnt) break;
        }
        
        if (this._id == top._id) {
            var isDropDown = this._root._dropDown != null;
            var retVal = false;            
            
            switch (keyPressCombo) {
                case "ArrowUp":
                    var index = this._currentIndex - 1;
                    this.setRowIndexSelected(index, !isDropDown);
                    break;
        
                case "PageUp":
                    var index = this._currentIndex - 5;
                    this.setRowIndexSelected(index, !isDropDown);            
                    break;
        
                case "ArrowDown":
                    var index = this._currentIndex + 1;
                    this.setRowIndexSelected(index, !isDropDown);
                    break;
        
                case "PageDown":
                    var index = this._currentIndex + 5;
                    this.setRowIndexSelected(index, !isDropDown);
                    break;            
        
                case "Esc":
                    //Close a drop-down
                    if (isDropDown && this.isVisible()) {
                        var dd = this._root._dropDown;
                        dd.setDropDownVisible(false);
                        dd.setFocus(true);                
                    }
                    
                    break;
        
                case "ArrowLeft":
                    if (this._parent instanceof tw_GridBox) {
                        this._parent.setFocus(true);
                        this._parent.setRowIndexSelected(this._parent._currentIndex, false);
                    }
        
                    break;
            
                case "Space":
                    var state = this.setRowIndexCheckState(this._currentIndex, -1);
                    this.firePropertyChange("rowChecked", (state ? "t" : "f") + this._currentIndex);
                    break;
        
                case "Enter":                
                case "ArrowRight":                
                    //You cannot have children and visibleCheckBoxes, otherwise if dropdown close.
                    if (this._childGridBoxes != null && this._lastColumn().childNodes.item(this._currentIndex).tw_child != undefined) {
                        this.setRowIndexSelected(this._currentIndex, isDropDown);
                        this.fireAction("click", this._currentIndex);
                        this._setChildVisible(this._currentIndex, true);                    
                    } else if (keyPressCombo == "Enter" && this._root._dropDown != null && this.isVisible()) {
                        this.setRowIndexSelected(this._currentIndex, isDropDown);
                        this.fireAction("click", this._currentIndex);
                        var dd = this._root._dropDown
                        dd.setDropDownVisible(false);
                        dd.setFocus(true);                
                    }
                    
                    break;
        
                default:
                    var charValue = keyPressCombo;
                    
                    if (charValue.length == 1 && /[a-zA-Z0-9]/.test(charValue)) {
                        charValue = charValue.toLowerCase();
                        var content = this._content;
                        
                        if (content.childNodes.length > 0) {
                            var column = content.firstChild;                    
                            var foundIndex = -1;
                            
                            if (this._currentIndex + 1 < column.childNodes.length) {                    
                                for (var i = this._currentIndex + 1, cnt = column.childNodes.length; i < cnt; i++) {
                                    var data = column.childNodes.item(i).firstChild.data;                        
                                    
                                    if (data.length > 0 && data.charAt(0).toLowerCase() == charValue) {
                                        foundIndex = i;
                                        break;
                                    }
                                }
                            }
        
                            if (foundIndex == -1) {
                                for (var i = 0, cnt = this._currentIndex; i < cnt; i++) {
                                    var data = column.childNodes.item(i).firstChild.data;                        
                                    
                                    if (data.length > 0 && data.charAt(0).toLowerCase() == charValue) {
                                        foundIndex = i;
                                        break;
                                    }
                                }
                            }
                            
                            if (foundIndex != -1) this.setRowIndexSelected(foundIndex, !isDropDown);                        
                        }                        
                    } else {
                        retVal = true;
                    }
            }
            
            if (retVal) {
                return this.$.keyPressNotify.apply(this, [keyPressCombo]);
            } else {
                return false;
            }
        } else {
            return top.keyPressNotify(keyPressCombo);
        }
    },
        
    setVisibleHeader: function(visibleHeader) {        
        if (this._header == null || this._content == null) return; 
        var header = this._header;
        var body = this._content.parentNode;
        var gbHeight = this._height - this._borderSizeSub;
        
        if (visibleHeader) {
            var gbWidth = this.getWidth() - this._borderSizeSub;
            if (gbWidth < 0) gbWidth = 0;
            header.style.width = gbWidth + "px";
            header.style.height = tw_GridBox.rowHeight + this.getStyle("borderSize") * 2 + "px";            
            header.style.top = this._box.scrollTop + "px";
            if (this._visibleHeader) tw_addEventListener(this._box.childNodes.item(1), "scroll", this._scrollListener);                    
            header.style.display = "block";
            var headerHeight = tw_GridBox.rowHeight + this.getStyle("borderSize") * 2;
            body.style.top = headerHeight + "px";
            body.style.height = gbHeight < headerHeight ? "0px" : (gbHeight - headerHeight) + "px";
        } else {
            header.style.display = "none";
            body.style.top = "0px";
            body.style.height = gbHeight < 0 ? "0px" : (gbHeight + "px");
            if (!this._visibleHeader) tw_removeEventListener(this._box.childNodes.item(1), "scroll", this._scrollListener);                    
        }
        
        this._visibleHeader = visibleHeader;
    },

    setVisibleCheckBoxes: function(visibleCheckBoxes, checkedIndices) {
        this._visibleCheckBoxes = visibleCheckBoxes;
        
        if (this._getColumnCount() > 0) {
            var childNodes = this._content.firstChild.childNodes;
            var paddingLeft = visibleCheckBoxes ? "18px" : "3px";
            
            for (var i = 0, cnt = childNodes.length; i < cnt; i++) {
                var style = childNodes.item(i).style;
                
                if (visibleCheckBoxes) {                
                    style.backgroundImage = checkedIndices == null || checkedIndices.indexOf("," + i + ",") == -1 ? tw_GridBox.imageUnchecked : tw_GridBox.imageChecked;
                } else {
                    style.backgroundImage = "";
                }
                    
                style.paddingLeft = paddingLeft;
            }
        }
    },

    setFullRowCheckBox: function(fullRowCheckBox) {
        this._fullRowCheckBox = fullRowCheckBox;
    },

    setColumnName: function(index, name) {
        var b = this._header.childNodes.item(index);
        b.replaceChild(document.createTextNode(name), b.firstChild);
    },

    setColumnWidth: function(index, width, sendEvent) {   
        var header = this._header;
        var content = this._content;
        var column = this._content.childNodes.item(index);
    
        if (arguments.length > 0) {
            var columnHeader = header.childNodes.item(index);
            columnHeader.style.width = width <= this._borderSizeSub ? "0px" : width - this._borderSizeSub + "px";
            column.style.width = width + "px";
        }
        
        var totalFixedWidth = 0;
    
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var c = content.childNodes.item(i);                        
            totalFixedWidth += parseInt(c.style.width);
        }
    
        totalFixedWidth += this._childColumnWidth;
        content.style.width = totalFixedWidth + "px";
        var gbWidth = this.getWidth() - this._borderSizeSub;        
        if (totalFixedWidth < gbWidth) totalFixedWidth = gbWidth;      
        header.style.width = totalFixedWidth + "px";        
        if (width > 0 && sendEvent) this.firePropertyChange("columnWidth", index + "," + width);
    },

    setColumnAlignX: function(index, alignX) {
        this._header.childNodes.item(index).style.textAlign = alignX;
        this._content.childNodes.item(index).style.textAlign = alignX;
    },

    setRowIndexSelected: function(index, sendEvent) {
        if (index < 0) {
            index = 0;
        } else {
            var len = this._content.firstChild.childNodes.length;            
            if (index >= len) index = len - 1;
        }
        
        if (this._currentIndex != -1 && this._getColumnCount() > 0) {
            this.closeChildren();
            
            if (this._currentIndex < this._lastColumn().childNodes.length) {
                this._toggleHighlight(this._currentIndex, false);
            }
        }
        
        this._toggleHighlight(index, true);        
        this._currentIndex = index;    
        if (sendEvent) this.firePropertyChange("rowSelected", index);
    },

    setRowIndexCheckState: function(index, state) {
        if (!this._visibleCheckBoxes || index < 0) return;
        var style = this._content.firstChild.childNodes.item(index).style;
        if (state == -1) state = style.backgroundImage == tw_GridBox.imageUnchecked;
        style.backgroundImage = state ? tw_GridBox.imageChecked : tw_GridBox.imageUnchecked;
        return state;
    },

    addColumn: function(index, values, name, width, alignX) {
        if (!(values instanceof Array)) eval("values = " + values);
        
        var columnHeader = document.createElement("div");
        columnHeader.className = "gridBoxColumnHeader";
        var s = columnHeader.style;
        s.styleFloat = "left";
        s.overflow = "hidden";
        s.whiteSpace = "nowrap";        
        s.height = tw_GridBox.rowHeight + this.getStyle("borderSize") * 2 - this._borderSizeSub + "px";
        s.textAlign = alignX;

        s.backgroundColor = tw_COLOR_BUTTONFACE;
        s.borderWidth = this.getStyle("borderSize") + "px";
        s.borderStyle = tw_Component.getReverseBorderType(this.getStyle("borderType"));
        s.borderColor = this.getStyle("borderColor");
        
        columnHeader.appendChild(document.createTextNode(name));
                
        tw_addEventListener(columnHeader, "focus", this._focusListener);
        tw_addEventListener(columnHeader, "blur", this._blurListener);    
        tw_addEventListener(columnHeader, "click", this._columnClickListener);
        
        var column = document.createElement("div");
        column.className = "gridBoxColumn";
        var s = column.style;
        s.styleFloat = "left";
        s.overflow = "hidden";        
        s.textAlign = alignX;
        s.padding = "0px";
        s.margin = "0px";
        tw_addEventListener(column, "focus", this._focusListener);
        tw_addEventListener(column, "blur", this._blurListener);
        var state = this._visibleCheckBoxes ? 0 : -1;
        
        for (var i = 0; i < values.length; i++) {
            var cell = this._newCell(values[i], index, state);
            column.appendChild(cell);
        }                    
        
        this._header.appendChild(columnHeader);        
        this._content.insertBefore(column, this._content.childNodes.item(index));
        this.setColumnWidth(index, width);
    },
    
    removeColumn: function(index) {
        this._header.removeChild(this._header.childNodes.item(index));
        this._content.removeChild(this._content.childNodes.item(index));    
    
        //TODO: this doesn't kill the tw_child reference that is attached to the cell
        if (this._childGridBoxes != null) {
            if (this._getColumnCount() == 1) {
                this._content.removeChild(this._content.firstChild);            
                this._destroyChildren();            
                this._childGridBoxes = null;
                this._childColumnWidth = 0;
                this.setColumnWidth();                            
            }
        }
    },
    
    setColumn: function(index, values, name, width, alignX) {    
        var columnHeader = this._header.childNodes.item(index);    
        var column = this._content.childNodes.item(index);
        
        columnHeader.replaceChild(document.createTextNode(name), columnHeader.firstChild);
                
        column.style.width = width + "px";
        columnHeader.style.width = width <= this._borderSizeSub ? "0px" : width - this._borderSizeSub + "px";  
        column.style.textAlign = columnHeader.style.textAlign = alignX;
        if (!(values instanceof Array)) eval("values = " + values);
                
        for (var i = 0, cnt = values.length; i < cnt; i++) {
            var cell = column.childNodes.item(i);
            cell.replaceChild(document.createTextNode(values[i]), cell.firstChild);
        }
    },
    
    addRow: function(index, values) {
        var content = this._content;
        eval("values = " + values);
        var state = this._visibleCheckBoxes ? 0 : -1;
        
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var cell = this._newCell(values[i], i, state);
            var column = content.childNodes.item(i);        
            var length = column.childNodes.length;
            
            if (length > 0 && index < length)
                column.insertBefore(cell, column.childNodes.item(index));
            else
                column.appendChild(cell);
        }
        
        var size = this._getRowCount();
        
        if (size == 1) {
            if (this._getColumnCount() > 0) {
                this.setRowIndexSelected(0);
            } else {
                this._currentIndex = 0;
            }
        } else if (index <= this._currentIndex) {
            if (this._currentIndex + 1 < size) this.setRowIndexSelected(this._currentIndex + 1);
        }        
    },
    
    removeRow: function(index) {
        var content = this._content;    
        var child = this._lastColumn().childNodes.item(index).tw_child;
        if (child != undefined) child.destroy();
    
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var column = content.childNodes.item(i);
            column.removeChild(column.childNodes.item(index));        
        }
        
        if (index == this._currentIndex) {
            var size = this._getRowCount();
            
            if (index < size) {                
                this.setRowIndexSelected(index);
            } else if (size > 0) {
                this.setRowIndexSelected(size - 1);
            } else {
                this._currentIndex = -1;
            }
        }        
    },
    
    setRow: function(index, values) {
        var content = this._content;    
        if (!(values instanceof Array)) eval("values = " + values);
        
        for (var i = 0, cnt = values.length; i < cnt; i++) {
            var cell = content.childNodes.item(i).childNodes.item(index);
            cell.replaceChild(document.createTextNode(values[i]), cell.firstChild);
        }
    },

    //TODO: extra messages are being sent from the server.  This is caused by gb.rows.add(new Row()) which then
    //results in an ensureSize call within the Table object.  The ensureSize generates alot of empty set cell statements
    setCell: function(columnIndex, rowIndex, value) {
        var childNodes = this._content.childNodes.item(columnIndex).childNodes;
        
        if (rowIndex < childNodes.length) {
            var cell = childNodes.item(rowIndex);
            cell.replaceChild(document.createTextNode(value), cell.firstChild);
        }
    },
    
    closeChildren: function() {
        if (this._childGridBoxes == null) return;
        var child = this;
        
        while (child._childGridBoxes != null) {
            child = child._lastColumn().childNodes.item(child._currentIndex).tw_child;
            
            if (child != undefined) {
                child.setVisible(false);
            } else {
                break;
            }        
        }
    
        this._childOpen = false;
    },    
    
    destroy: function(keepChildColumn) {
        this._destroyChildren();
        
        if (!(this._parent instanceof tw_BaseContainer)) {            
            if (this._parent instanceof tw_GridBox && this._parent._childGridBoxes != null) {
                var ary = this._parent._childGridBoxes;

                for (var i = ary.length; --i >= 0;) {
                    if (ary[i] === this) { 
                        ary.splice(i, 1);
                        break;
                    }
                }
                
                if (ary.length == 0 && !keepChildColumn) {
                    this._parent._header.removeChild(this._parent._header.lastChild);            
                    this._parent._content.removeChild(this._parent._lastColumn());            
                    //this._destroyChildren(); ???
                    this._parent._childGridBoxes = null;                 
                    this._parent._childColumnWidth = 0;
                    this._parent.setColumnWidth();                
                }
                
                this._parentCell.tw_child = undefined;
            }
                  
            this._box.parentNode.removeChild(this._box);
        }
        
        
        this._box.tw_root = this._root = this._header = this._content = this._childGridBoxes = this._dropDown = this._hresize.column = this._parentCell = null;
        this.$.destroy.apply(this, []);
    }    
});

tw_GridBox.rowHeight = 14;
tw_GridBox.childColumnWidth = 12;
tw_GridBox.imageChecked = "url(?_twr_=gbChecked.png)";
tw_GridBox.imageUnchecked = "url(?_twr_=gbUnchecked.png)";
tw_GridBox.imageChildArrow = "url(?_twr_=menuArrow.png)";
tw_GridBox.imageChildArrowInvert = "url(?_twr_=menuArrowInvert.png)";

