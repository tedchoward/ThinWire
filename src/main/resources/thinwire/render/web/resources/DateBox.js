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
var tw_DateBox = tw_Component.extend({
    _today: null,
    _curDate: null,
    _selectedDate: null,
    _table: null,
    _next: null,
    _prev: null,
    _header: null,
    _footer: null,
    _columnHeaders: null,
    _headerBorderSizeSub: 0,

    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "dateBox", id, containerId);
        
        this._cellClickListener = this._cellClickListener.bind(this);
        this._btnClickListener = this._btnClickListener.bind(this);
        this._footerClickListener = this._footerClickListener.bind(this);
        
        var s = this._box.style;
        var cssText = "position:absolute;overflow:hidden;padding:0px;margin:0px;text-align:center;";
        tw_Component.setCSSText(cssText, this._box);
        
        this._curDate = this._today = new Date(props.today);
        delete props.today;
        
        this._header = document.createElement("div");
        s = this._header.style;
        cssText = "overflow:hidden;white-space:nowrap;height:" + tw_DateBox.rowHeight + "px;color:" + tw_COLOR_BUTTONTEXT +
            ";background-color:" + tw_COLOR_BUTTONFACE + ";";
        tw_Component.setCSSText(cssText, this._header);
        tw_Component.applyButtonBorder(this._header, true, true, false, true);

        this._header.appendChild(document.createTextNode(tw_DateBox.MONTHS[this._today.getMonth()] + " " + this._today.getFullYear()));
        
        this._prev = this._createButton("url(" + tw_IMAGE_DATEBOX_PREVARROW + ")", "left");
        this._header.appendChild(this._prev);
        
        this._next = this._createButton("url(" + tw_IMAGE_DATEBOX_NEXTARROW + ")", "right");
        this._header.appendChild(this._next);
        
        this._box.appendChild(this._header);
        
        this._buildColumnHeaders();
        this._buildTable();
        this._populateCells();
        
        var footer = document.createElement("div");
        s = footer.style;
        cssText = "height:18px;text-align:center;";
        tw_Component.setCSSText(cssText, footer);
        footer.appendChild(document.createTextNode("Today: " + this._getFormattedDate(this._today)));
        this._box.appendChild(footer);
        this._footer = footer;
        tw_addEventListener(this._footer, ["click", "dblclick"], this._footerClickListener);
        
        this.init(-1, props);
    },
    
    setSelectedDate: function(selectedDate) {
        var cell;
        if (this._selectedDate != null) {
            cell = this._getCellFromDate(this._selectedDate);
            if (cell != undefined) {
                this._toggleSelection(cell, false);
                cell.style.color = "";
            }
        }
        this._selectedDate = new Date(selectedDate);
        cell = this._getCellFromDate(this._selectedDate);
        if (cell == null) {
            this._setMonth(this._selectedDate);
            cell = this._getCellFromDate(this._selectedDate);
        }
        this._toggleSelection(cell, true);
    },
    
    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        var tblWidth = width - 14;
        if (tblWidth < 0) tblWidth = 0;
        this._columnHeaders.style.width = this._table.style.width = tblWidth + "px";
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        var tblHeight = height - (parseInt(this._footer.style.height, 10) + parseInt(this._header.style.height, 10) +
            this._borderSizeSub +this._headerBorderSizeSub + parseInt(this._columnHeaders.style.height, 10) + 1);
        if (tblHeight < 0) tblHeight = 0;
        this._table.style.height = tblHeight + "px";
    },
    
    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);
        var cell = this._getCellFromDate(this._selectedDate);
        if (cell != undefined && cell != null) this._toggleSelection(cell, true);
        this._header.style.color = this._enabled ? tw_COLOR_BUTTONTEXT : tw_COLOR_GRAYTEXT;
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        
        if (name == "borderWidth") {
            value = parseInt(value, 10) >= 2 ? 2 : 1;
            this._headerBorderSizeSub = value * 2;
            value += "px";
            this._header.style.borderWidth = value;
            if (this._inited) this.setHeight(this._height);
        }
    },
    
    _buildColumnHeaders: function() {
        var table = document.createElement("table");
        var tbody = document.createElement("tbody");
        var days = ["S", "M", "T", "W", "T", "F", "S"];
        var row = document.createElement("tr");
        var s = table.style;
        var cssText = "height:17px;border-bottom:1px solid " + tw_COLOR_WINDOWFRAME + ";margin-left:auto;margin-right:auto;";
        tw_Component.setCSSText(cssText, table);

        for (var i = 0; i < days.length; i++) {
            var cell = document.createElement("th");
            cell.appendChild(document.createTextNode(days[i]));
            row.appendChild(cell);
        }
        tbody.appendChild(row);
        table.appendChild(tbody);
        this._box.appendChild(table);
        this._columnHeaders = table;
    },
    
    _buildTable: function() {
        var table = document.createElement("table");
        table.className = "dateBoxTable";
        var tbody = document.createElement("tbody");
        table.appendChild(tbody);
        var s = table.style;
        var cssText = "border-bottom:1px solid " + tw_COLOR_WINDOWFRAME + ";margin-left:auto;margin-right:auto;";
        tw_Component.setCSSText(cssText, table);

        for (var i = 0; i < 6; i++) {
            var row = document.createElement("tr");
            for (var j = 0; j < 7; j++) {
                var cell = document.createElement("td");
                tw_Component.setCSSText("border:none 1px " + tw_COLOR_WINDOWFRAME + ";padding:1px;", cell);

                cell.appendChild(document.createTextNode(""));
                tw_addEventListener(cell, ["click", "dblclick"], this._cellClickListener);
                
                row.appendChild(cell);
            }
            tbody.appendChild(row);
        }
        this._box.appendChild(table);
        this._table = table;
    },
    
    _populateCells: function() {
        var highlight = this._selectedDate != null && this._curDate.getFullYear() == this._selectedDate.getFullYear() && this._curDate.getMonth() == this._selectedDate.getMonth();
        if (this._selectedDate != null) var oldSelectedCell = this._getCellFromDate(this._selectedDate, true);
        var first = new Date(this._curDate.getFullYear(), this._curDate.getMonth(), 1).getDay();
        var last = new Date(new Date(this._curDate.getFullYear(), this._curDate.getMonth() + 1, 1).getTime() - tw_DateBox.MILLISECONDS_IN_DAY).getDate();
        var rows = this._table.firstChild.childNodes;
        var dayIdx = -first;
        for (var i = 0, cnt = rows.length; i < cnt; i++) {
            var cells = rows[i].childNodes;
            for (var j = 0; j < cells.length; j++) {
                dayIdx++;
                var newValue;
                if (dayIdx > 0 && dayIdx <= last) {
                    newValue = dayIdx;
					cells[j].style.color = "";
                } else {
                    newValue = new Date(this._curDate.getFullYear(), this._curDate.getMonth(), dayIdx).getDate();
                    cells[j].style.color = tw_COLOR_GRAYTEXT;
                }
                cells[j].replaceChild(document.createTextNode(newValue), cells[j].firstChild);
                
                if (new Date(this._curDate.getFullYear(), this._curDate.getMonth(), dayIdx).getTime() == this._today.getTime()) {
                    cells[j].style.borderStyle = "solid";
                    cells[j].style.padding = "0px";
                } else {
                    cells[j].style.borderStyle = "none";
                    cells[j].style.padding = "1px";
                }
                
                if (highlight && dayIdx == this._selectedDate.getDate()) {
                    this._toggleSelection(cells[j], true);
                } else if (oldSelectedCell != null && cells[j] == oldSelectedCell) {
                    this._toggleSelection(cells[j], false);
                }
            }
        }
    },
    
    _getClickAction: tw_Component.getClickAction,
    
    _cellClickListener: function(event) {
        if (!this._enabled) return;
        var cell = tw_getEventTarget(event);
        var rowIdx = this._getRowIndex(cell);
        var newDt = parseInt(cell.firstChild.nodeValue, 10);
        if (rowIdx == 0 && newDt >= 23) {
            this._incrementMonth(-1);
        } else if (rowIdx >= 4 && newDt <= 14) {
            this._incrementMonth(1); 
        }
        this.setSelectedDate((this._curDate.getMonth() + 1) + "/" + newDt + "/" + this._curDate.getFullYear());
        var formattedDate = this._getFormattedDate(this._selectedDate);
        this.firePropertyChange("selectedDate", formattedDate);
        var action = this._getClickAction(event.type, formattedDate);
        if (action == null) return;
        this.fireAction(event, action, formattedDate);
    },
    
    _getDate: function(cell) {
        var rowIdx = this._getRowIndex(cell);
        var newDt = parseInt(cell.firstChild.nodeValue, 10);
        var tmpDate = this._selectedDate;
        if (rowIdx == 0 && newDt >= 23) {
            tmpDate = new Date(this._curDate.getFullYear(), this._curDate.getMonth() - 1, this._curDate.getDate());
        } else if (rowIdx >= 4 && newDt <= 14) {
            tmpDate = new Date(this._curDate.getFullYear(), this._curDate.getMonth() + 1, this._curDate.getDate());
        }
        return new Date(tmpDate.getFullYear(), tmpDate.getMonth(), newDt);
    },
    
    _getRowIndex: function(cell) {
        for (var rowIdx in this._table.firstChild.childNodes) {
            if (this._table.firstChild.childNodes[rowIdx] == cell.parentNode) return rowIdx;
        }
    },
    
    _btnClickListener: function(event) {
        if (!this._enabled) return;
        var btn = tw_getEventTarget(event);
        var dm = btn.style.backgroundImage.indexOf("right") >= 0 ? 1 : -1;
        this._incrementMonth(dm);
    },
    
    _footerClickListener: function(event) {
        if (!this._enabled) return;
        var footer = tw_getEventTarget(event);
        this.setSelectedDate(this._today);
        var formattedDate = this._getFormattedDate(this._selectedDate);
        this.firePropertyChange("selectedDate", formattedDate);
        var action = this._getClickAction(event.type, formattedDate);
        if (action == null) return;
        this.fireAction(event, action, formattedDate);
    },
    
    _setMonth: function(date) {
        this._curDate = date;
        this._header.replaceChild(document.createTextNode(tw_DateBox.MONTHS[this._curDate.getMonth()] + " " + this._curDate.getFullYear()), this._header.firstChild);
        this._populateCells();
    },
    
    _incrementMonth: function(inc) {
        var tmpDate = new Date(this._curDate.getFullYear(), this._curDate.getMonth() + inc, this._curDate.getDate());
        this._setMonth(tmpDate);
    },
    
    _toggleSelection: function (cell, selected) {
        if (this._enabled) {
            cell.style.backgroundColor = selected ? tw_COLOR_HIGHLIGHT : "";
            cell.style.color = selected ? tw_COLOR_HIGHLIGHTTEXT : "";
        } else {
            cell.style.backgroundColor = selected ? tw_COLOR_INACTIVECAPTION : "";
            cell.style.color = selected ? tw_COLOR_INACTIVECAPTIONTEXT : "";
        }
    },
    
    _createButton: function(img, align) {
        var btn = document.createElement("div");
        var imgText = img != null ? "background-image:" + img + ";" : "";
        var alignText = align != null ? align.replace(tw_Component.cssRegex, tw_Component.cssReplaceFunc) + ":3px;" : "";
        s = btn.style;
        var cssText = "position:absolute;margin:0px;padding:1px;overflow:hidden;top:0px;width:16px;height:" + 
            this._header.style.height + ";" + alignText + imgText;
        tw_Component.setCSSText(cssText, btn);

        tw_addEventListener(btn, "click", this._btnClickListener);
        return btn;
    },
    
    _getFormattedDate: function(date) {
        return (date.getMonth() + 1) + "/" + date.getDate() + "/" + date.getFullYear();
    },
    
    _getCellFromDate: function(date, override) {
        if (override == null) override = false;
        if (!override && (date.getMonth() != this._curDate.getMonth() || date.getFullYear() != this._curDate.getFullYear())) return;
        var dateInt = date.getDate();
        var rows = this._table.firstChild.childNodes;
        for (var i = 0; i < rows.length; i++) {
            var cells = rows[i].childNodes;
            for (var j = 0; j < cells.length; j++) {
                if (dateInt == cells[j].firstChild.nodeValue) {
                    var rowIdx = this._getRowIndex(cells[j]);
                    if (!((rowIdx == 0 && dateInt >= 23) || (rowIdx >= 4 && dateInt <= 14))) return cells[j];
                }
            }
        }
    },
    
    getDragArea: function() {
        return this._table;
    },
    
    getDragBox: function(event) {
        var cell = tw_getEventTarget(event);
        if (cell == null) return null;
        var dragBox = document.createElement("div");
        var s = dragBox.style;
        dragBox._dragObject = this._getFormattedDate(this._getDate(cell));
        dragBox.appendChild(document.createTextNode(dragBox._dragObject));
        
        return dragBox;
    },
    
    getDropArea: function() {
        return this._table;
    },
    
    getDropTarget: function(event) {
        var cell = tw_getEventTarget(event);
        if (cell == null) return null;
        return this._getFormattedDate(this._getDate(cell));
    },
    
    destroy: function() {
        this._today = this._curDate = this._selectedDate = this._table = this._next = this._prev
            = this._header = this._footer = _columnHeaders = null;
        arguments.callee.$.call(this);
    }
});

tw_DateBox.rowHeight = 14;
tw_DateBox.MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
tw_DateBox.MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October",
    "November", "December"];
