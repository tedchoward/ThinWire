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
//TODO: A column should be visible by default
var tw_GridBox = tw_Component.extend({
    _header: null,
    _hresize: null,    
    _content: null,
    _root: null,
    _headerBorderSizeSub: 0,
    _visibleCheckBoxes: false,
    _visibleHeader: false,
    _fullRowCheckBox: false,
    _childGridBoxes: null,
    _childColumnWidth: 0,
    _childOpen: false, 
    _currentIndex: -1,
    _parentCell: null,
    _lastIndex: null,
    _clickTime: null,
    _sortAllowed: null,
    _sortTimeStamp: null,
    _cellTemplate: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "gridBox", id, containerId);
        
        this._root = this;   
        var visibleCheckBoxes = props.visibleCheckBoxes;
        var cols = props.columnData;
        var selectedRow = props.selectedRow;
        var checkedRows = visibleCheckBoxes ? props.checkedRows : null;
        var parentIndex = props.parentIndex;
        this._sortAllowed = props.sortAllowed;
        
        delete props.visibleCheckBoxes;        
        delete props.columnData;
        delete props.selectedRow;
        if (visibleCheckBoxes) delete props.checkedRows;
        delete props.parentIndex;
        delete props.sortAllowed;
        
		var cell = document.createElement("div");
        cell.className = "gridBoxCell";
        var s = cell.style;
        var cssText = "height:" + tw_GridBox.rowHeight + "px;padding-left:3px;white-space:nowrap;background-repeat:no-repeat;background-position:center left;" + 
            "background-color:" + tw_COLOR_TRANSPARENT + ";";
        tw_Component.setCSSText(cssText, cell);
        this._cellTemplate = cell;
        
        var header = this._header = document.createElement("div");
        header.className = "gridBoxHeader";
        var s = header.style;
        var cssText = "position:absolute;background-color:" + tw_COLOR_THREEDFACE + ";color:" + tw_COLOR_BUTTONTEXT + ";display:none;";
        tw_Component.setCSSText(cssText, header);
        this._hresize = {column: null, startX: -1};        
        this._box.appendChild(header);
        
        var body = this._scrollBox = document.createElement("div");
        body.className = "gridBoxBody";
        var s = body.style;
        cssText = "position:absolute;width:100%;overflow:auto;top:0px;";
        tw_Component.setCSSText(cssText, body);
        this._box.appendChild(body);
    
        var content = this._content = document.createElement("div");
        content.className = "gridBoxContent";
        body.appendChild(content);
    
        var empty = document.createElement("span");
        tw_Component.setCSSText("display:none;", empty);
        content.appendChild(empty);
        
        this._scrollListener = this._scrollListener.bind(this);
        this._headerMouseUpTimer = this._headerMouseUpTimer.bind(this);
        
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this));    
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this)); 
        tw_addEventListener(header, "mousemove", this._headerMouseMoveListener.bind(this));
        tw_addEventListener(header, "mousedown", this._headerMouseDownListener.bind(this));
        tw_addEventListener(header, "mouseup", this._headerMouseUpListener.bind(this));
        tw_addEventListener(this._box, ["click", "dblclick"], this._clickListener.bind(this));
         
        var showDropDown = false;
        var showChild = false;
        var container = this._parent;

        //NOTE: the cell in the last column of each row may contain a tw_child field that
        //  points to a child gridbox.
        if (container instanceof tw_GridBox) {
            this._box.style.zIndex = 1;
                        
            props.x = 0;
            props.y = 0;
            if (props.width == 0) props.width = container._width;
            if (props.height == 0) props.height = container._height;
            
            var parentContent = container._content; 
            
            if (container._childGridBoxes == null) {
                var columnHeader = document.createElement("div");
                columnHeader.style.width = tw_GridBox.childColumnWidth + "px";
                container._header.appendChild(columnHeader);                
                
                var column = document.createElement("div");
                column.className = "gridBoxColumn";
                var s = column.style;
                cssText = "overflow:hidden;width:" + tw_GridBox.childColumnWidth + "px;";
                s.styleFloat = "left";
                tw_Component.setCSSText(cssText, column);
                            
                for (var i = parentContent.firstChild.childNodes.length; --i >= 0;) {
                    var cell = container._cellTemplate.cloneNode(false);
        			cell.appendChild(document.createTextNode(""));
                    column.appendChild(cell);
                }
                
                parentContent.insertBefore(column, parentContent.childNodes.item(parentContent.childNodes.length - 1));
                container._childColumnWidth = tw_GridBox.childColumnWidth;
                container.setColumnWidth();                                
                container._childGridBoxes = [];
                container._toggleHighlight(container._currentIndex, true);
            }
            
            this.setVisible(false);
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
        }
        
        this.init(-1, props);

        for (var i = 0, cnt = cols.length; i < cnt; i++) {
            var c = cols[i];
            this.addColumn(i, c.v, c.n, c.w, c.a, c.s);
        }
        
        if (visibleCheckBoxes) this.setVisibleCheckBoxes(true, checkedRows);
        if (selectedRow >= 0) this.setRowIndexSelected(selectedRow, false); 
        if (showChild) this._parent._setChildVisible(parentIndex, true);        
    },
    
    _headerMouseMoveListener: function(event) {
        if (!this._enabled || this._hresize.startX != -1) return;
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
        if (!this._enabled || this._hresize.column == null || tw_getEventTarget(event).className != "gridBoxColumnHeader") return;    
        this._hresize.startX = event.clientX;
    },
        
    _headerMouseUpListener: function(event) {
        if (!this._enabled || this._hresize.startX == -1) return;
        var resizeColumn = this._hresize.column;
        var resizeStartX = this._hresize.startX;
        
        for (var index = this._header.childNodes.length; --index >= 0;) {
            if (this._header.childNodes.item(index) == resizeColumn) break; 
        }
        
        var borderSize = parseInt(resizeColumn.style.borderWidth);
        var width = parseInt(resizeColumn.style.width);
        if (!tw_sizeIncludesBorders) width += borderSize;  
        var endX = event.clientX;
        
        if (resizeStartX < endX) {                
            endX = width + (endX - resizeStartX);
        } else {
            endX = width - (resizeStartX - endX);        
        }
        
        borderSize = borderSize * 2 + 2;
        if (endX < borderSize) endX = borderSize;
        this._hresize.index = index;
        this._hresize.endX = endX;
        setTimeout(this._headerMouseUpTimer, 100);
    },
    
    _headerMouseUpTimer: function() {
        this.setColumnWidth(this._hresize.index, this._hresize.endX, true);    
        this._hresize.column = null;    
        this._hresize.startX = -1;
        delete this._hresize.index;
        delete this._hresize.endX;
    },

    _scrollListener: function(event) {
        var body = this._content.parentNode;        
        this._header.style.left = "-" + body.scrollLeft + "px";
        if (this._childOpen) this.closeChildren();
    },
    
    _clickListener: function(event) {
        if (!this._enabled) return;
        if (this._focusCapable) this.setFocus(true);
        var cell = tw_getEventTarget(event, "gridBoxCell");
        
        if (cell == null && this._sortAllowed && this._hresize.column == null) {
	        var columnHeader = tw_getEventTarget(event, "gridBoxColumnHeader");
	        if (columnHeader == null) return;
	        var cn = this._header.childNodes;        
	        for (var index = cn.length; --index >= 0;) if (cn.item(index) === columnHeader) break;
	        tw_em.sendViewStateChanged(this._id, "columnSort", index);        
        } else {
	        var column = cell.parentNode;
	        var position = this._getCellPosition(cell);
	
	        this.setRowIndexSelected(position[1], true);
	        
	        if (this._visibleCheckBoxes) {
	            if (this._fullRowCheckBox || (tw_getEventOffsetX(event) < 16 && this._content.childNodes.item(0) == column)) {
	                this.setRowIndexCheckState(position[1], -1, true);
	            }
	            
	            var msg = position.join("@");
	            var action = this._getClickAction(event.type, msg);
	            if (action == null) return;
	            this.fireAction(event, action, msg);
	        } else {
	            var msg = position.join("@");
	            var action = this._getClickAction(event.type, msg);
	            if (action == null) return;
	            this.fireAction(event, action, msg);
	            
	            if (this._lastColumn().childNodes.item(position[1]).tw_child != undefined) {
	                this._setChildVisible(position[1], true);
	            } else {
	                this._root.closeChildren();
	                
	                //TODO DROPDOWN: GridBox won't play nice
	                if (this._root._parent instanceof tw_DropDown) {
	                    this._root.setVisible(false);
	                }
	            }
	        }
        }
    },
    
    _getClickAction: tw_Component.getClickAction,
    
    _getCellPosition: function(cell, x) {
        if (cell.className == "gridBoxCell") {
            var column = cell.parentNode;
            var columnIndex = tw_getElementIndex(column);
            var rowIndex = tw_getElementIndex(cell);
            return [columnIndex, rowIndex];
        } else if (cell.className == "gridBoxColumnHeader") {
            var column = cell;
            var columnIndex = tw_getElementIndex(column);
            return [columnIndex, -1];
        } else if (cell.className == "gridBoxBody" && x > 0 && this._getColumnCount() > 0) {
            var columnIndex = -1;
            
            for (var i = 0, cnt = this._getColumnCount(), pos = 0; i < cnt; i++) {
                pos += this._header.childNodes.item(i).clientWidth;
                if (x < pos) columnIndex = i;
            }
            
            return [columnIndex, -1];
        } else {
            return [-1, -1];
        }
    },

    _destroyChildren: function() {
        if (this._childGridBoxes != null) {
            for (var i = this._childGridBoxes.length; --i >=0;) {
                this._childGridBoxes[i].destroy();
            }

            this._childGridBoxes = null;
            this._childColumnWidth = 0;
            this.setColumnWidth();                            
        }
    },
    
    _setChildVisible: function(index, state) {
        if (state) {
            var cell = this._lastColumn().childNodes.item(index);
            var gbc = cell.tw_child;
            
            if (gbc != undefined) {
                var baseWindow = this._root.getBaseWindow();
                
                var y = cell.offsetTop - this._content.parentNode.scrollTop + this._y;
                var available = tw_getVisibleHeight();
                if (baseWindow instanceof tw_Dialog) available -= baseWindow._y;
                if (available - y < gbc._height) y -= gbc._height - tw_GridBox.rowHeight;
                if (y < 0) y = available - gbc._height;
                
                var x = this._x + this._width;
                
                var available = tw_getVisibleWidth();
                if (baseWindow instanceof tw_Dialog) available -= baseWindow._x;
                if (available - x < gbc._width) x -= gbc._width + this._width;
                if (x < 0) x = available - gbc._width;
                
                gbc.setX(x);
                gbc.setY(y);
                
                //TODO: if this is part of a dropdown, then we need to store off the selected row
                //if (gbc._root._dropDown != null)
                                        
                gbc.setVisible(true);
                this._childOpen = true;
                if (gbc._focusCapable) gbc.setFocus(true);
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
    
    _scrollIntoView: function(index) {
    	if (this._getColumnCount() > 0) {
            var cell = this._content.childNodes.item(0).childNodes.item(index);

            if(cell != null){
                var body = this._content.parentNode;
                var cellOffset = cell.offsetTop + cell.offsetHeight - body.scrollTop;

                if (cellOffset < 0) {
                    body.scrollTop = cell.offsetTop;
                } else if (cellOffset > body.clientHeight) {
                    body.scrollTop = (cell.offsetTop + cell.offsetHeight) - body.clientHeight;
                }
            }
        }
	},
    
    _toggleHighlight: function(index, state) {
        if (this._getColumnCount() < 1 || index < 0) return;
        var content = this._content;
        var childNodes = content.childNodes;    
        if (childNodes.item(0).childNodes.length < 1) return;
            
        if (state) {
            var color = this._enabled ? tw_COLOR_HIGHLIGHTTEXT : tw_COLOR_INACTIVECAPTIONTEXT;
            var backgroundColor = this._enabled ? tw_COLOR_HIGHLIGHT : tw_COLOR_INACTIVECAPTION;
            var arrowImage = "url(" + tw_IMAGE_GRIDBOX_CHILDARROWINVERT + ")";
            
            if (this._sortTimeStamp != null && (new Date() - this._sortTimeStamp) <= 1000) {
                this._sortTimeStamp = new Date();
            } else {
                this._sortTimeStamp = null;
                if (this._visible) this._scrollIntoView(index);
            }
        } else {
            var color = this._fontBox.style.color;
            var backgroundColor = tw_COLOR_TRANSPARENT;
            var arrowImage = "url(" + tw_IMAGE_GRIDBOX_CHILDARROW + ")";;
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
        arguments.callee.$.call(this, name, value);
        
        if (name == "borderWidth") {
            var oldCalcBorderSize = this._headerBorderSizeSub;
            value = parseInt(value) >= 2 ? 2 : 1;
            this._headerBorderSizeSub = value * 2;
            value += "px";
                
            for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
                var h = this._header.childNodes.item(i);
                h.style.borderWidth = value;
                var width = parseInt(h.style.width) + oldCalcBorderSize;
                h.style.width = width <= this._headerBorderSizeSub ? "0px" : width - this._headerBorderSizeSub + "px";                      
            }

            this.setColumnWidth();
        }
    },
        
    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        this.setColumnWidth();
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        this.setVisibleHeader(this._visibleHeader);
    },
        
    setVisible: function(visible) {
        if (visible) {
        	this._scrollIntoView(this._currentIndex);
        } else {
        	this.closeChildren();
       	}
       	
        arguments.callee.$.call(this, visible);
    },

    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);        
        if (this._focusCapable) tw_setFocusCapable(this._box, enabled);
        this._toggleHighlight(this._currentIndex, true);
        this._header.style.color = this._enabled ? tw_COLOR_BUTTONTEXT : tw_COLOR_GRAYTEXT;
    },

    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._box, true);
        } else {
            tw_setFocusCapable(this._box, false);
        }
    },
        
    keyPressNotify: function(keyPressCombo) {
        if (!this._enabled) return;
        
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
            var retVal = false;            
            
            switch (keyPressCombo) {
                case "ArrowUp":
                    var index = this._currentIndex - 1;
                    this.setRowIndexSelected(index, true);
                    break;
        
                case "PageUp":
                    var index = this._currentIndex - 5;
                    this.setRowIndexSelected(index, true);            
                    break;
        
                case "ArrowDown":
                    var index = this._currentIndex + 1;
                    this.setRowIndexSelected(index, true);
                    break;
        
                case "PageDown":
                    var index = this._currentIndex + 5;
                    this.setRowIndexSelected(index, true);
                    break;            
        
                case "ArrowLeft":
                    if (this._parent instanceof tw_GridBox) {
                        if (this._parent._focusCapable) this._parent.setFocus(true);
                        this._parent.setRowIndexSelected(this._parent._currentIndex, false);
                    }
        
                    break;
            
                case "Space":
                    this.setRowIndexCheckState(this._currentIndex, -1, true);
                    break;
        
                case "Enter":                
                case "ArrowRight":
                    if (this._currentIndex >= 0 && this._currentIndex < this._content.firstChild.childNodes.length) {
                        //You cannot have children and visibleCheckBoxes, otherwise if dropdown close.
                        if (this._childGridBoxes != null && this._lastColumn().childNodes.item(this._currentIndex).tw_child != undefined) {
                            this.setRowIndexSelected(this._currentIndex, true);
                            this.fireAction(null, "click", "0@" + this._currentIndex);
                            this._setChildVisible(this._currentIndex, true);
                        } else if (keyPressCombo == "Enter") {
                            this.setRowIndexSelected(this._currentIndex, true);
                            this.fireAction(null, "click", "0@" + this._currentIndex);
                        }
                    }
                    
                    break;
        
                default:
                    var charValue = keyPressCombo;
                    if (charValue.indexOf("Num") >= 0) charValue = charValue.substring(3);
                    if (charValue == "Dash") charValue = "-";
                    var autoComplete = this._root._parent instanceof tw_DropDown && this._root._parent._editAllowed;
                    if (!autoComplete && charValue.length == 1 && /[a-zA-Z0-9]/.test(charValue)) {
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
                            
                            if (foundIndex != -1) this.setRowIndexSelected(foundIndex, true);                        
                        }                        
                    } else {
                        retVal = true;
                    }
            }
            
            if (retVal) {
                return arguments.callee.$.call(this, keyPressCombo);
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
            var headerHeight = tw_GridBox.rowHeight + this._headerBorderSizeSub;
            header.style.height = headerHeight + "px";
            header.style.top = (this._box.scrollTop = 0) + "px";
            this.setColumnWidth();
            if (!this._visibleHeader) tw_addEventListener(this._box.childNodes.item(1), "scroll", this._scrollListener);                    
            header.style.display = "block";
            body.style.top = headerHeight + "px";
            body.style.height = gbHeight < headerHeight ? "0px" : (gbHeight - headerHeight) + "px";
        } else {
            header.style.display = "none";
            body.style.top = "0px";
            body.style.height = gbHeight < 0 ? "0px" : (gbHeight + "px");
            if (this._visibleHeader) tw_removeEventListener(this._box.childNodes.item(1), "scroll", this._scrollListener);                    
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
                    style.backgroundImage = "url(" + (checkedIndices == null || checkedIndices.indexOf("," + i + ",") == -1 ? tw_IMAGE_GRIDBOX_UNCHECKED : tw_IMAGE_GRIDBOX_CHECKED) + ")";
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
    
    setSortAllowed: function(sortAllowed) {
        this._sortAllowed = sortAllowed;
    },

    setColumnName: function(index, name) {
        var b = this._header.childNodes.item(index);
        b.replaceChild(tw_Component.setRichText(name), b.firstChild);
    },

    setColumnWidth: function(index, width, sendEvent) {   
        var header = this._header;
        var content = this._content;
    
        if (arguments.length > 0) {
			var column = this._content.childNodes.item(index);
            var columnHeader = header.childNodes.item(index);
            columnHeader.style.width = width <= this._headerBorderSizeSub ? "0px" : width - this._headerBorderSizeSub + "px";
            column.style.width = width + "px";
        }
        
        var totalFixedWidth = 0;
    
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var c = content.childNodes.item(i);
            totalFixedWidth += parseInt(c.style.width);
        }
    
        totalFixedWidth += this._childColumnWidth;
        content.style.width = totalFixedWidth + "px";
        var gbWidth = this._width - this._borderSizeSub;        
        if (totalFixedWidth < gbWidth) totalFixedWidth = gbWidth;      
        header.style.width = totalFixedWidth + "px";        
        if (width > 0 && sendEvent) this.firePropertyChange("columnWidth", index + "," + width, "columnWidth" + index);
    },

    setColumnAlignX: function(index, alignX) {
        this._header.childNodes.item(index).style.textAlign = alignX;
        this._content.childNodes.item(index).style.textAlign = alignX;
    },

    setColumnSortOrder: function(index, sortOrder) {
        this._sortTimeStamp = new Date();
        var img = "";
        if (sortOrder == 1) img = "url(" + tw_IMAGE_GRIDBOX_SORTARROWASC + ")";
        else if (sortOrder == 2) img = "url(" + tw_IMAGE_GRIDBOX_SORTARROWDESC + ")";
        this._header.childNodes.item(index).style.backgroundImage = img;
    },
    
    setRowIndexSelected: function(index, sendEvent) {
        if (this._currentIndex == index) return;
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
        
        if (index >= 0) {
            this._toggleHighlight(index, true);
            if (sendEvent) this.firePropertyChange("rowSelected", index);
        }
        
        this._currentIndex = index;
    },

    setRowIndexCheckState: function(index, state, sendEvent) {
        if (!this._visibleCheckBoxes || index < 0 || index >= this._content.firstChild.childNodes.length) return;
        var style = this._content.firstChild.childNodes.item(index).style;
        if (state == -1) state = style.backgroundImage.indexOf("gbUnchecked") >= 0;
        style.backgroundImage = "url(" + (state ? tw_IMAGE_GRIDBOX_CHECKED : tw_IMAGE_GRIDBOX_UNCHECKED) + ")";
        if (sendEvent) this.firePropertyChange("rowChecked", (state ? "t" : "f") + index, "rowChecked" + index);
    },
    
    addColumn: function(index, values, name, width, alignX, sortOrder) {
        if (!(values instanceof Array)) eval("values = " + values);
        
        var columnHeader = document.createElement("div");
        columnHeader.className = "gridBoxColumnHeader";
        var s = columnHeader.style;
        var bgImg = sortOrder != 0 ? "background-image: url(" + 
            (sortOrder == 1 ? tw_IMAGE_GRIDBOX_SORTARROWASC : tw_IMAGE_GRIDBOX_SORTARROWDESC) + "); " : "";
        var borderWidth =  (this._borderSize > 2 ? 2 : this._borderSize) + "px; ";
        var cssText = "overflow:hidden;white-space:nowrap;height:" + tw_GridBox.rowHeight + "px;" + 
            "text-align:" + alignX + ";background-repeat:no-repeat;background-position:center right;" + bgImg + 
            "border-width:" + borderWidth + "background-color:" + tw_COLOR_BUTTONFACE + ";";
            s.styleFloat = "left";
        tw_Component.setCSSText(cssText, columnHeader);
        tw_Component.applyButtonBorder(columnHeader, true, false, false, true);
        
        columnHeader.appendChild(tw_Component.setRichText(name));
                
        var column = document.createElement("div");
        column.className = "gridBoxColumn";
        var s = column.style;
        cssText = "overflow:hidden;text-align:" + alignX + ";";
        s.styleFloat = "left";
        tw_Component.setCSSText(cssText, column);
        
        for (var i = 0; i < values.length; i++) {
            var cell = this._cellTemplate.cloneNode(false);
        	cell.appendChild(tw_Component.setRichText(values[i]));
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
            }
        }
    },
    
    setColumn: function(index, values, name, width, alignX, sortOrder) {    
        var columnHeader = this._header.childNodes.item(index);    
        var column = this._content.childNodes.item(index);
        
        columnHeader.replaceChild(tw_Component.setRichText(name), columnHeader.firstChild);
                
        column.style.width = width + "px";
        columnHeader.style.width = width <= this._headerBorderSizeSub ? "0px" : width - this._headerBorderSizeSub + "px";
        
        column.style.textAlign = columnHeader.style.textAlign = alignX;
        if (sortOrder != 0) columnHeader.backgroundImage = "url(" + (sortOrder == 1 ? tw_IMAGE_GRIDBOX_SORTARROWASC : tw_IMAGE_GRIDBOX_SORTARROWDESC) + ")";        
        if (!(values instanceof Array)) eval("values = " + values);
                
        for (var i = 0, cnt = values.length; i < cnt; i++) {
            var cell = column.childNodes.item(i);
            cell.replaceChild(tw_Component.setRichText(values[i]), cell.firstChild);
        }
    },
    
    addRow: function(index, values, checked, selected) {
        var content = this._content;
        eval("values = " + values);
        var state = this._visibleCheckBoxes ? 0 : -1;
        
        for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
            var cell = this._cellTemplate.cloneNode(false);
        	cell.appendChild(tw_Component.setRichText(values[i]));
            if (this._visibleCheckBoxes) cell.style.paddingLeft = "18px";
            var column = content.childNodes.item(i);        
            var length = column.childNodes.length;
            
            if (length > 0 && index < length)
                column.insertBefore(cell, column.childNodes.item(index));
            else
                column.appendChild(cell);
        }
        
        this.setRowIndexCheckState(index, checked == 1, false);        
        var size = this._getRowCount();
        
        if (selected) {
            this.setRowIndexSelected(index);
        } else if (size == 1) {
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
            this._currentIndex = -1;
            
            if (index < size) {                
                this.setRowIndexSelected(index);
            } else if (size > 0) {
                this.setRowIndexSelected(size - 1);
            }
        } else if (index <= this._currentIndex) {
            if (this._currentIndex - 1 >= 0) this._currentIndex--;
        }
    },
    
    setRow: function(index, values, checked, selected) {
        var content = this._content;    
        if (!(values instanceof Array)) eval("values = " + values);
        
        for (var i = 0, cnt = values.length; i < cnt; i++) {
            var cell = content.childNodes.item(i).childNodes.item(index);
            cell.replaceChild(tw_Component.setRichText(values[i]), cell.firstChild);
        }
        
        this.setRowIndexCheckState(index, checked == 1, false);
        if (selected) this.setRowIndexSelected(index);
    },
    
    clearRows: function() {
        var content = this._content;    
    
        for (var i = 0, cnt = content.childNodes.length; i < cnt; i++) {
            var column = content.childNodes.item(i);
            content.replaceChild(column.cloneNode(false), column);
        }
        
        this._currentIndex = -1;
    },

    //TODO: extra messages are being sent from the server.  This is caused by gb.rows.add(new Row()) which then
    //results in an ensureSize call within the Table object.  The ensureSize generates alot of empty set cell statements
    setCell: function(columnIndex, rowIndex, value) {
        var childNodes = this._content.childNodes.item(columnIndex).childNodes;
        
        if (rowIndex < childNodes.length) {
            var cell = childNodes.item(rowIndex);
            cell.replaceChild(tw_Component.setRichText(value), cell.firstChild);
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
    
    getDragArea: function() {
        return this._content;
    },
    
    getDragBox: function(event) {
        var position = this._getCellPosition(tw_getEventTarget(event), tw_getEventOffsetX(event));
        if (position[0] < 0) return null;
        
        var dragBox = document.createElement("div");
        var s = dragBox.style;
        var cssText = "height:" + tw_GridBox.rowHeight + "px;";
        tw_Component.setCSSText(cssText, dragBox);
        var width = this._width - this._borderSizeSub;
        
        if (position[1] >= 0) {
            var content = this._content;
            var childNodes = content.childNodes;
            var x = 0;
            
            for (var i = 0, cnt = this._getColumnCount(); i < cnt; i++) {
                var cell = childNodes.item(i).childNodes.item(position[1]).cloneNode(true);
                var cs = cell.style;
                cs.position = "absolute";
                cs.width = childNodes.item(i).style.width;
                cs.height = s.height;
                cs.left = x + "px";
                cs.backgroundColor = tw_COLOR_TRANSPARENT;
                cs.color = "";
                x += parseInt(cs.width);
                dragBox.appendChild(cell);
                if (x > width) break;
            }
        }
        
        if (x < width) width = x; 
        s.width = width + "px";
        dragBox._dragObject = position.join("@");
        return dragBox;
    },
    
    getDropArea: function() {
        return this._box;
    },
    
    getDropTarget: function(event) {
        return this._getCellPosition(tw_getEventTarget(event), tw_getEventOffsetX(event)).join("@");
    },

    fireAction: function(ev, action, source) {
        if (action == "click" || action == "doubleClick") {
            if (this._eventNotifiers != null) {
                var actions = this._eventNotifiers["action"];            

                if (actions != undefined && actions[action] === true && !(source instanceof tw_Component)) {
                    var x = 0, y = 0, cellX = 0, cellY = 0;
                
                    if (ev != null) {
                        x = tw_getEventOffsetX(ev, this._box.className);
                        y = tw_getEventOffsetY(ev, this._box.className);
    
                        cellX = tw_getEventOffsetX(ev, "gridBoxCell");
                        cellY = tw_getEventOffsetY(ev);
            
                        if (x < 0) x = 0;
                        if (y < 0) y = 0;
                        if (cellX < 0) cellX = 0;
                        if (cellY < 0) cellY = 0;
                    } else {
                        var cell = this._content.childNodes.item(0).childNodes.item(this._currentIndex);
                        y = cell.offsetTop + this._scrollBox.offsetTop;
                    }
                
                    if (source == null) source = "";
                    tw_em.sendViewStateChanged(this._id, action, x + "," + y + "," + cellX + "," + cellY + "," + source);
                }
            }
        } else {
            arguments.callee.$.call(this, ev, action, source);
        }
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
                    this._parent._destroyChildren();
                }
                
                this._parentCell.tw_child = undefined;
            }
                  
            this._box.parentNode.removeChild(this._box);
        }
        
        this._box.tw_root = this._root = this._header = this._content = this._hresize.column = this._parentCell = this._cellTemplate = null;
        arguments.callee.$.call(this);
    }    
});

tw_GridBox.rowHeight = 14;
tw_GridBox.childColumnWidth = 12;

