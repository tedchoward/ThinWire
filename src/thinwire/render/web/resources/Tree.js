/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                email: info@thinwire.com    ph: +1 (888) 644-6405
                            http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
//FIX: Thier is no logic to handle toggling on/off the display of images in a branch.
//FIX: Clicking on an image for a tree item doesn't activate the item.
//NOTE: tw_expanded exists on every node
var tw_Tree = tw_Component.extend({
    _rootItemVisible: false,
    _treeTop: null,
    _rootItem: null,
    _currentItem: null,
    _clickTime: null,
    _lastIndex: null,
    
    construct: function(id, containerId, props) {
        arguments.callee.$.call(this, "div", "tree", id, containerId);
        this._scrollBox = this._box;
        this._box.tw_isLeaf = false;
        var s = this._box.style;
        s.overflow = "auto";
        s.whiteSpace = "nowrap";
        
        this._treeTop = document.createElement("span");
        this._box.appendChild(this._treeTop);

        this._buttonClickListener = this._buttonClickListener.bind(this); 
        this._textClickListener = this._textClickListener.bind(this); 
        
        tw_addEventListener(this._box, "focus", this._focusListener.bind(this));
        tw_addEventListener(this._box, "blur", this._blurListener.bind(this));      
        tw_addEventListener(this._box, ["click", "dblClick"], this._clickListener.bind(this));
        
        var initData = props.initData;
        delete props.initData;
        this.init(-1, props);
        
        this._processInitData(this._treeTop, initData.ti);
        this._addRootItem(initData.rv, initData.rt, initData.ri);
        this._rootItem.tw_expanded = true;
        if (initData.rv && initData.re == false) this._setExpanded(this._rootItem, false);    
    },

    _clickListener: function(event, item) {
        // This listener exists to prevent any parent containers from acting on events
        event.cancelBubble = true;
    },
    
    _textClickListener: function(event, item) {
        if (!this._enabled) return false;
        var item = tw_getEventTarget(event, "treeRow");
        if (this._focusCapable) this.setFocus(true);
        this._select(item, true);
        var index = this._fullIndex(item);
        var action = this._getClickAction(event.type, index);
        if (action == null) return;
        this.fireAction(event, action, index);
    },
    
    _getClickAction: tw_Component.getClickAction,
    
    _buttonClickListener: function(event) {
        if (!this._enabled) return false;
        var item = tw_getEventTarget(event, "treeRow");
        if (this._focusCapable) this.setFocus(true);
        this._setExpanded(item, !item.tw_expanded, true);
    },
    
    _processInitData: function(branch, itemData) {
        for (var i=0; i < itemData.length; i++) {
            var item = itemData[i];
            var timg = null;        
            if (item.tm != undefined) timg = item.tm;        
            this._add(branch, item.tt, timg);
            
            if (item.ti != null && item.ti != undefined && item.ti.length > 0) {
                this._processInitData(branch.childNodes.item(i).subNodes, item.ti);
            }
            
            if (item.te) this._setExpanded(branch.childNodes.item(i), true);
        }
    },
    
    _createTextNode: function() {
        var textNode = document.createElement("span");
        var s = textNode.style;
        s.verticalAlign = "middle";
        s.padding = "2px";
        s.paddingTop = "1px";
        s.paddingBottom = "1px";
        return textNode;        
    },
    
    _addRootItem: function(visible, text, itemImg) {
        if (this._rootItem == undefined) {
            var node = document.createElement("span");
            node.className = "treeRow";
            
            itemImg = this._expandImageURL(itemImg);            
            var imgNode = document.createElement("img");
            imgNode.style.verticalAlign = "middle";
            node.appendChild(imgNode);
            node.assignedImageNode = imgNode;
            
            if (itemImg == null) {
                imgNode.style.display = "none";
            } else {
                imgNode.src = itemImg;
                imgNode.style.display = "inline";
            }            
            
            var textNode = this._createTextNode();
            node.appendChild(textNode);
            textNode.appendChild(tw_Component.setRichText(text));
            node.textNode = textNode;
            tw_addEventListener(textNode, ["click", "dblClick"], this._textClickListener);
            tw_addEventListener(textNode, "dblclick", this._buttonClickListener);
            
            //TODO: Is this the best way to fix this
            var mainBox = this._borderImage == null ? this._box : this._borderImage._box;
            mainBox.insertBefore(node,mainBox.firstChild);
            this._rootItem = node;
        }
        
        this.setRootItemVisible(visible);
    },
        
    _lookupTopImage: function(img) {
        switch (img) {
            case tw_IMAGE_TREE_EXPANDBOTTOM: return tw_IMAGE_TREE_EXPANDBOTTOMTOP;
            case tw_IMAGE_TREE_EXPAND: return tw_IMAGE_TREE_EXPANDTOP;
            case tw_IMAGE_TREE_LEAFBOTTOM: return tw_IMAGE_TREE_LEAFBOTTOMTOP;
            case tw_IMAGE_TREE_LEAF: return tw_IMAGE_TREE_LEAFTOP;
            case tw_IMAGE_TREE_UNEXPANDBOTTOM: return tw_IMAGE_TREE_UNEXPANDBOTTOMTOP;
            case tw_IMAGE_TREE_UNEXPAND: return tw_IMAGE_TREE_UNEXPANDTOP;
            default: return null;
        }
    },

    _lookupBottomImage: function(img) {
        switch (img) {
            case tw_IMAGE_TREE_EXPANDBOTTOMTOP: return tw_IMAGE_TREE_EXPANDBOTTOM;
            case tw_IMAGE_TREE_EXPANDTOP: return tw_IMAGE_TREE_EXPAND;
            case tw_IMAGE_TREE_LEAFBOTTOMTOP: return tw_IMAGE_TREE_LEAFBOTTOM;
            case tw_IMAGE_TREE_LEAFTOP: return tw_IMAGE_TREE_LEAF;
            case tw_IMAGE_TREE_UNEXPANDBOTTOMTOP: return tw_IMAGE_TREE_UNEXPANDBOTTOM;
            case tw_IMAGE_TREE_UNEXPANDTOP: return tw_IMAGE_TREE_UNEXPAND;
            default: return null;
        }
    },
    
    _setTopImage: function() {
        var treeTop = this._treeTop;
        if (treeTop.hasChildNodes() == false) return;
        var bi = treeTop.firstChild.imageNode.src;
        
        if (!this._rootItemVisible) {
            treeTop.firstChild.imageNode.src = this._lookupTopImage(bi);
        } else {
            var img = this._lookupBottomImage(bi);
            if (img == null) return;
            treeTop.firstChild.imageNode.src = img;
        }
    },
    
    _setExpanded: function(node, state, sendEvent) {
        while (node.className != "treeRow") node = node.parentNode;
        if (node.tw_isLeaf || node.tw_expanded == state) return;
        node.tw_expanded = state;
        
        if (this._rootItem !== node) {
            if (state) {
                node.imageNode.src = node == node.parentNode.lastChild ? tw_IMAGE_TREE_EXPANDBOTTOM : tw_IMAGE_TREE_EXPAND;
            } else {
                node.imageNode.src = node == node.parentNode.lastChild ? tw_IMAGE_TREE_UNEXPANDBOTTOM : tw_IMAGE_TREE_UNEXPAND;
            }

            node.subNodes.style.display = state ? "block" : "none";            
        } else {
            var walk = node;
            
            while ((walk = walk.nextSibling) != null) {
                walk.style.display = state ? "block" : "none";
            }
        }

        if (this._treeTop.firstChild === node) this._makeTopImage();
        
        if (sendEvent) {
            var fullIndex = this._fullIndex(node);
            this.firePropertyChange("itemExpanded", (state ? "t" : "f") + fullIndex, "itemExpanded" + fullIndex);
        }
    },
    
    _isRootItemParent: function(rnode) {
        while (rnode !== this._rootItem && rnode.className != "tree") {
            rnode = rnode.parentNode;
        }
        
        return (rnode === this._rootItem) ? rnode : null;
    },    
            
    _makeTopImage: function() {
        if (!this._rootItemVisible) {
            var bi = this._treeTop.firstChild.imageNode.src;
            this._treeTop.firstChild.imageNode.src = this._lookupTopImage(bi);
        } 
    },
    
    _add: function(basenode, text, itemImg, index) {
        var node;
        var bottom = false;
        var troot = this._treeTop;                
        var level = this._level(basenode); 
        
        //Select the image on the parent row (i.e. the parent of basenode).
        if (basenode.hasChildNodes()==false) {
          if (level > 0) {
            basenode.parentNode.tw_isLeaf = false; 
            if (basenode.parentNode.imageNode.src == tw_IMAGE_TREE_LEAF)
                basenode.parentNode.imageNode.src = tw_IMAGE_TREE_UNEXPAND;
            else {
                basenode.parentNode.imageNode.src = tw_IMAGE_TREE_UNEXPANDBOTTOM;
            }
          }
        }
        
        if ((level > 0) && (basenode.parentNode.tw_expanded))
          basenode.style.display = "block";
    
        //Create a row node with the right text and itemImg.
        node = document.createElement("div");
        node.className = "treeRow";
     
        //Add the node to the base node in the position determined by index.
        //Also select an image for the previous row.
        if ((index == undefined) || (index >= basenode.childNodes.length) || (!basenode.hasChildNodes())) {
            if (basenode.hasChildNodes()) {
                var prevRow = basenode.lastChild;
                
                if (prevRow.tw_isLeaf)
                    prevRow.imageNode.src =  tw_IMAGE_TREE_LEAF;
                else if (prevRow.tw_expanded)
                    prevRow.imageNode.src =  tw_IMAGE_TREE_EXPAND;
                else 
                    prevRow.imageNode.src =  tw_IMAGE_TREE_UNEXPAND;
            }
            
            basenode.appendChild(node);
            bottom = true;
        } else {
            basenode.insertBefore(node, basenode.childNodes.item(index));
            bottom = false;
        }
        
        var bimg = bottom ?  tw_IMAGE_TREE_LEAFBOTTOM : tw_IMAGE_TREE_LEAF;
        node.tw_isLeaf = true;
        node.tw_expanded = false;  
    
        //Add treeCell nodes to the new row, one for each level.
        //These images are displayed on 1 line, along with the row's text.
        var tiNode;

        for (var i=0; i < level; i++) {
            tiNode = document.createElement("img");
            tiNode.style.verticalAlign = "middle";
            tiNode.src = tw_IMAGE_TREE_STRAIGHT;
            node.appendChild(tiNode);
        }
        
        if (level > 0) {
            var imgLevel = node.childNodes.length-1;
            var currentRow = node;
            var row = currentRow;
            
            while (row.parentNode != troot) {
                branch = row.parentNode;
                prow = branch.parentNode;
                pbranch = branch.parentNode.parentNode;
                if (prow == pbranch.lastChild) node.childNodes.item(imgLevel).src = tw_IMAGE_TREE_EMPTY;
                row = prow;
                imgLevel--;
            }
        }
    
        //Add another image node to the new node.
        //This image node gets a click event handler.
        tiNode = document.createElement("img");
        tiNode.style.verticalAlign = "middle";
        tiNode.src = bimg;
        node.appendChild(tiNode);
        node.imageNode = tiNode;
        tw_addEventListener(tiNode, "click", this._buttonClickListener);
    
        //Add an assignedImage node to the new node if the 
        //itemImg parameter has a non-empty value.
        itemImg = this._expandImageURL(itemImg);        
        var imgNode = document.createElement("img");        
        imgNode.style.verticalAlign = "middle";
        node.appendChild(imgNode);
        node.assignedImageNode = imgNode;

        if (itemImg == null) {
            imgNode.style.display = "none";
        } else {
            imgNode.src = itemImg;
            imgNode.style.display = "inline";            
        }
      
        //Add a text node to the new row node.
        var textNode = this._createTextNode();
        node.appendChild(textNode);
        node.textNode = textNode;
        textNode.appendChild(tw_Component.setRichText(text));
        tw_addEventListener(textNode, ["click", "dblclick"], this._textClickListener);
        tw_addEventListener(textNode, "dblclick", this._buttonClickListener);
        
        //Add the span which will hold the row's child rows.
        var br = document.createElement("span");
        br.className = "treeBranch";
        br.style.display = "none";
        node.appendChild(br);
        node.subNodes = br;
        
        if (node.previousSibling != null) {
          this._refreshLine(node.previousSibling, level, true);
        }
    },
    
    _remove: function(rnode) {
        var parent = rnode.parentNode;
        var level = this._level(parent); 
    
        if (rnode.previousSibling != null) {
            if (rnode == parent.lastChild) {
                this._refreshLine(rnode.previousSibling, level, false); 
                var prevRow = rnode.previousSibling;
                if (prevRow.tw_isLeaf) {
                    prevRow.imageNode.src = tw_IMAGE_TREE_LEAFBOTTOM;
                } else if (prevRow.tw_expanded) {
                    prevRow.imageNode.src = tw_IMAGE_TREE_EXPANDBOTTOM;
                } else {
                    prevRow.imageNode.src = tw_IMAGE_TREE_UNEXPANDBOTTOM;
                }
            }
        }
    
        rnode.textNode = rnode.imageNode = rnode.assignedImageNode = rnode.subNodes = null;        
        parent.removeChild(rnode);
    
        if (parent.hasChildNodes()==false) {
            if (level > 0) {
                parent.style.display = "none";
                parent.parentNode.tw_isLeaf = true;
                bs = parent.parentNode.imageNode;
                if ((bs.src == tw_IMAGE_TREE_EXPAND) || (bs.src == tw_IMAGE_TREE_UNEXPAND)) {
                    bs.src = tw_IMAGE_TREE_LEAF;
                } else {
                    bs.src = tw_IMAGE_TREE_LEAFBOTTOM;
                }
            }
        }
        
        if (this._currentItem === rnode) this._currentItem = undefined;
    },
    
    _refreshLine: function(row, level, visible) {
        if (row.tw_isLeaf) return;
        var img = visible ? tw_IMAGE_TREE_STRAIGHT : tw_IMAGE_TREE_EMPTY;
        var child;
    
        for (var i=0; i < row.subNodes.childNodes.length; i++) {
            child = row.subNodes.childNodes.item(i);
            child.childNodes.item(level).src = img;
            if (child.tw_isLeaf==false) this._refreshLine(child, level, visible);
        }
    },
    
    _select: function(item, sendEvent) {
        if (item == null || this._currentItem === item) return;
        
        var s = item.textNode.style;
        s.backgroundColor = this._enabled ? tw_COLOR_HIGHLIGHT : tw_COLOR_INACTIVECAPTION;
        s.color = this._enabled ? tw_COLOR_HIGHLIGHTTEXT : tw_COLOR_INACTIVECAPTIONTEXT;
        s.zIndex = 1;
        
        if (this._currentItem != undefined) {
            var s = this._currentItem.textNode.style;
            s.backgroundColor = "";
            s.color = "";
            s.zIndex = 0;
        }
        
        var rowOffset = item.offsetTop + item.offsetHeight - this._box.scrollTop;
        
        if (rowOffset < 0) {
            this._box.scrollTop = item.offsetTop;
        } else if (this._box.clientHeight > 0 && rowOffset > this._box.clientHeight) {
            this._box.scrollTop = (item.offsetTop + item.offsetHeight) - this._box.clientHeight;
        }        
        
        this._currentItem = item;
        if (sendEvent) this.firePropertyChange("itemSelected", this._fullIndex(item));         
    },
        
    _fullIndexItem: function(findex) {
        if (findex == "rootItem")
            return this._rootItem;
        
        var ary = findex.split(".");
        var pnode = this._treeTop;
        var node = null;
    
        for (var i = 0; i < ary.length; i++) {
            var value = parseInt(ary[i]);
            node = pnode.childNodes.item(value);
            pnode = node.subNodes;
        }
        
        if (node == null || node == undefined) alert("_fullIndexItem:not found:" + findex);        
        return node;
    },
    
    _fullIndex: function(titem) {
        if (titem == null) return "";
        if (titem === this._rootItem) return "rootItem";
        
        var index = tw_getElementIndex(titem);
        var value = index;
        
        while (titem.parentNode !== this._treeTop) {
            titem = titem.parentNode.parentNode;
            index = tw_getElementIndex(titem);
            value = index + "." + value;
        }
        
        return value;
    },
    
    _level: function(lnode) {
        var l=0;
        
        while (lnode !== this._treeTop) {
          l++;
          lnode = lnode.parentNode.parentNode;
        }
        
        return l;
    },

    _getItem: function(next, count) {
        var sibling = next ? "nextSibling" : "previousSibling";
        var child = next ? "firstChild" : "lastChild";
        var start = this._currentItem;
        if (count == null) count = 1;
            
        while (count-- > 0) {                
            if (start == null || (!next && start === this._treeTop.firstChild)) {
                if (this._rootItem != null) item = this._rootItem;
                else if (this._treeTop.childNodes.length > 0) this._treeTop.firstChild;            
                else item = null;                
            } else if (start === this._rootItem) {
                if (this._rootItem.tw_expanded && this._treeTop.childNodes.length > 0) item = this._treeTop.firstChild;
                else item = null;
            } else {         
                var curr = start;
                if (!next && curr[sibling] != null) curr = curr[sibling];
                
                if (curr.tw_expanded && curr.lastChild.hasChildNodes()) {                
                    var item = curr.lastChild[child];
                } else {
                    curr = start;
                    var item = curr[sibling];
                    
                    if (item == null) {
                        item = curr.parentNode.parentNode;                        
                        if (item == null || item.className != "treeRow") item = null;
                        if (item != null && next) item = item[sibling];
                    }
                }
            }
                
            if (item == null) {
                item = start;
                break;
            } else
                start = item;
        }

        return item;                        
    },
    
    keyPressNotify: function(keyPressCombo) {        
        switch (keyPressCombo) {
            case "ArrowDown": this._select(this._getItem(true), true); break;                
            case "PageDown": this._select(this._getItem(true, 5), true); break;
            case "ArrowUp": this._select(this._getItem(false), true); break;
            case "PageUp": this._select(this._getItem(false, 5), true); break;
            case "ArrowRight": if (this._currentItem != null) this._setExpanded(this._currentItem, true, true); break;
            case "ArrowLeft": if (this._currentItem != null) this._setExpanded(this._currentItem, false, true); break;                
            case "Enter": 
                if (this._currentItem != null) {
                    this._setExpanded(this._currentItem, !this._currentItem.tw_expanded, true);
                    this.fireAction(null, "click", this._fullIndex(this._currentItem));
                }
                break;
                
            default: return arguments.callee.$.call(this, keyPressCombo);
        }
        
        return false;
    },
    
    setEnabled: function(enabled) {
        arguments.callee.$.call(this, enabled);        
        if (this._focusCapable) tw_setFocusCapable(this._box, enabled);
        var item = this._currentItem;
        this._currentItem = undefined;
        this._select(item);        
    },

    setFocusCapable: function(focusCapable) {
        arguments.callee.$.call(this, focusCapable);
        if (focusCapable && this._enabled) {
            tw_setFocusCapable(this._box, true);
        } else {
            tw_setFocusCapable(this._box, false);
        }
    },
    
    _expandImageURL: function(itemImg) {
        if (itemImg != undefined && itemImg != null && itemImg.length > 0)
            return tw_Component.expandUrl(itemImg);
        else
            return null;
    },
    
    itemAdd: function(value, text, itemImg) {
        var idx = value.lastIndexOf(".");
    
        if (idx < 0) {
            this._add(this._treeTop, text, itemImg, value);
            if (value == 0) this._makeTopImage();
        } else {
            var index = value.slice(idx+1);
            var basevalue = value.slice(0,idx);
            var base = this._fullIndexItem(basevalue);
            this._add(base.subNodes, text, itemImg, index);
        }
    },
    
    itemRemove: function(value) {
        var firstrow = false;
        var rownode = this._fullIndexItem(value);
        if (this._treeTop.firstChild == rownode) firstrow = true;
        this._remove(rownode);    
        if ((firstrow) && (this._treeTop.hasChildNodes())) this._makeTopImage();
    },
    
    itemChange: function(value, text, itemImg) {
        var node = this._fullIndexItem(value);        
        node.textNode.replaceChild(tw_Component.setRichText(text), node.textNode.firstChild); 
        itemImg = this._expandImageURL(itemImg);        
    
        if (itemImg == null) {
            node.assignedImageNode.style.display = "none";
        } else {
            node.assignedImageNode.src = itemImg;
            node.assignedImageNode.style.display = "inline";
        }
    },
    
    itemExpand: function(value, expanded) {
        this._setExpanded(this._fullIndexItem(value), expanded);
    },    
        
    itemSelect: function(value) {
        this._select(this._fullIndexItem(value));
    },
    
    setRootItemVisible: function(visible) {
        this._rootItem.style.display = visible ? "block" : "none";
        this._rootItemVisible = visible;
        this._setTopImage();
    },
    
    getDragBox: function(event) {
        var item = tw_getEventTarget(event, "treeRow");
        if (item == null) return null;
        var dragBox = document.createElement("div");
        var image = item.childNodes.length > 3 ? item.childNodes[item.childNodes.length - 3] : item.childNodes[0];
        dragBox.appendChild(image.cloneNode(true));
        dragBox.appendChild(item.getElementsByTagName("span")[0].cloneNode(true));
        
        var s = dragBox.style;
        s.height = tw_Tree.rowHeight + "px";
        dragBox._dragObject = this._fullIndex(item);
        return dragBox;
    },
    
    getDropTarget: function(event) {
        var item = tw_getEventTarget(event, "treeRow");
        return this._fullIndex(item);
    },

    fireAction: function(ev, action, source) {
        if (action == "click" || action == "doubleClick") {
            if (this._eventNotifiers != null) {
                var actions = this._eventNotifiers["action"];            
            
                if (actions != undefined && actions[action] === true) {
                    var x = 0, y = 0, cellX = 0, cellY = 0;
                
                    if (ev != null) {
                        x = tw_getEventOffsetX(ev, this._box.className);
                        y = tw_getEventOffsetY(ev, this._box.className);
                        
                        //XXX: Find a method for determining coordinates relative to the tree.item
                        cellX = -1;
                        cellY = -1;
            
                        if (x < 0) x = 0;
                        if (y < 0) y = 0;
                        //if (cellX < 0) cellX = 0;
                        //if (cellY < 0) cellY = 0;
                    }
                
                    if (source == null) source = "";
                    tw_em.sendViewStateChanged(this._id, action, x + "," + y + "," + cellX + "," + cellY + "," + source);
                }
            }
        } else {
            arguments.callee.$.call(this, ev, action, source);
        }
    },
    
    destroy: function() {
        this._treeTop = this._currentItem = this._rootItem.textNode = this._rootItem.assignedImageNode = null;
        this._rootItem = null;         
        var divs = this._box.getElementsByTagName("div");
        
        for (var i = 0; i < divs.length; i++) {
            var dv = divs[i];        
            if (dv.className == "treeRow") dv.textNode = dv.imageNode = dv.assignedImageNode = dv.subNodes = null;
        }
        
        arguments.callee.$.call(this);
    }    
});

tw_Tree.rowHeight = 16;

