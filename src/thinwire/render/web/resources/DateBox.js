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
    _oldColor: null,
    _footerClickListener: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "dateBox", id, containerId);
        
        this._cellClickListener = this._cellClickListener.bind(this);
        this._btnClickListener = this._btnClickListener.bind(this);
        this._footerClickListener = this._footerClickListener.bind(this);
        
        var s = this._box.style;
        s.textAlign = "center";
        
        this._curDate = this._today = new Date(props.today);
        delete props.today;
        
        this._header = document.createElement("div");
        s = this._header.style;
        s.overflow = "hidden";
        s.whiteSpace = "nowrap";        
        s.height = tw_GridBox.rowHeight + this.getStyle("borderSize") * 2 - this._borderSizeSub + "px";
        
        var bs = tw_Component.defaultStyles["Button"];        
        s.backgroundColor = bs.backgroundColor;
        s.borderStyle = bs.borderType; 
        s.borderColor = tw_Component.getIEBorder(bs.borderColor, bs.borderType);
        
        this._header.appendChild(document.createTextNode(tw_DateBox.MONTHS[this._today.getMonth()] + " " + this._today.getFullYear()));
        
        this._prev = this._createButton("url(?_twr_=leftArrow.png)", "left");
        this._header.appendChild(this._prev);
        
        this._next = this._createButton("url(?_twr_=rightArrow.png)", "right");
        this._header.appendChild(this._next);
        
        this._box.appendChild(this._header);
        
        this._buildColumnHeaders();
        this._buildTable();
        this._populateCells();
        
        var footer = document.createElement("div");
        s = footer.style;
        s.height = "20px";
        s.textAlign = "center";
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
                cell.style.color = this._oldColor;
            }
        }
        this._selectedDate = new Date(selectedDate);
        cell = this._getCellFromDate(this._selectedDate);
        if (cell == null) {
            this._setMonth(this._selectedDate);
            cell = this._getCellFromDate(this._selectedDate);
        }
        this._oldColor = cell.style.color;
        this._toggleSelection(cell, true);
    },
    
    setWidth: function(width) {
        arguments.callee.$.call(this, width);
        this._table.style.width = (width - 20) + "px";
        this._columnHeaders.style.width = (width - 20) + "px";
    },
    
    setHeight: function(height) {
        arguments.callee.$.call(this, height);
        var tblHeight = height - (parseInt(this._footer.style.height) + parseInt(this._header.style.height) + parseInt(this._columnHeaders.style.height) + 5);
        if (tblHeight < 0) tblHeight = 0;
        this._table.style.height = tblHeight + "px";
    },
    
    setStyle: function(name, value) {
        arguments.callee.$.call(this, name, value);
        if (name == "borderSize") this._header.style.borderWidth = value + "px";
    },
    
    _buildColumnHeaders: function() {
        var table = document.createElement("table");
        var tbody = document.createElement("tbody");
        var days = ["S", "M", "T", "W", "T", "F", "S"];
        var row = document.createElement("tr");
        var s = table.style;
        s.height = "20px";
        s.borderBottom = "1px solid " + tw_COLOR_WINDOWFRAME;
        s.marginLeft = "auto";
        s.marginRight = "auto";
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
        s.borderBottom = "1px solid " + tw_COLOR_WINDOWFRAME;
        s.marginLeft = "auto";
        s.marginRight = "auto";
        for (var i = 0; i < 6; i++) {
            var row = document.createElement("tr");
            for (var j = 0; j < 7; j++) {
                var cell = document.createElement("td");
                cell.style.border = "none 1px " + tw_COLOR_WINDOWFRAME;
                cell.style.padding = "1px";
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
                    cells[j].style.color = tw_COLOR_WINDOWTEXT;
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
        var cell = tw_getEventTarget(event);
        var rowIdx = this._getRowIndex(cell);
        var newDt = parseInt(cell.firstChild.nodeValue);
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
        this.fireAction(action, formattedDate);
    },
    
    _getDate: function(cell) {
        var rowIdx = this._getRowIndex(cell);
        var newDt = parseInt(cell.firstChild.nodeValue);
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
        var btn = tw_getEventTarget(event);
        var dm = btn.style.backgroundImage.indexOf("right") >= 0 ? 1 : -1;
        this._incrementMonth(dm);
    },
    
    _footerClickListener: function(event) {
        var footer = tw_getEventTarget(event);
        this.setSelectedDate(this._today);
        var formattedDate = this._getFormattedDate(this._selectedDate);
        this.firePropertyChange("selectedDate", formattedDate);
        var action = this._getClickAction(event.type, formattedDate);
        if (action == null) return;
        this.fireAction(action, formattedDate);
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
        cell.style.backgroundColor = selected ? tw_COLOR_HIGHLIGHT : "";
        cell.style.color = selected ? tw_COLOR_HIGHLIGHTTEXT : "";
    },
    
    _createButton: function(img, align) {
        var btn = document.createElement("div");
        s = btn.style;
        s.position = "absolute";
        s.margin = "0px";
        s.padding = "1px";    
        s.overflow = "hidden";
        s.top = "0px";
        s.width = "16px";
        s.height = this._header.style.height;
        if (align != null) s[align] = "3px";
        if (img != null) btn.style.backgroundImage = img;
        tw_addEventListener(btn, "click", this._btnClickListener);
        return btn;
    },
    
    _getFormattedDate: function(date) {
        return (date.getMonth() + 1) + "/" + date.getDate() + "/" + date.getFullYear();
    },
    
    _getCellFromDate: function(date, override) {
        if (override == null) override = false;
        if (!override && date.getMonth() != this._curDate.getMonth() || date.getFullYear() != this._curDate.getFullYear()) return;
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
        s.position = "absolute";
        s.textAlign = "center";
        s.width = "75px";
        s.height = "20px";
        s.fontFamily = this.getStyle("fontFamily");
        s.fontSize = this.getStyle("fontSize") + "pt";
        s.backgroundColor = tw_COLOR_WINDOW;
        dragBox._index = this._getFormattedDate(this._getDate(cell));
        dragBox.appendChild(document.createTextNode(dragBox._index));
        
        return dragBox;
    },
    
    getDropArea: function() {
        return this._table;
    },
    
    getDropTarget: function(event) {
        var cell = tw_getEventTarget(event);
        if (cell == null) return null;
        return this._getFormattedDate(this._getDate(cell));
    }
});

tw_DateBox.MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
tw_DateBox.MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October",
    "November", "December"];
