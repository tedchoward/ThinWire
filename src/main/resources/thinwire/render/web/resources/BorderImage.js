/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems
Copyright (c) 2013 Ted C Howard

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
var tw_BorderImage = Class.extend({
  _box: null,
  _borderSize: -1,
  _borderSizeSub: -1,

  setBox: function (box) {
    this._box = box;
    return this._box;
  },

  setBorderSize: function (borderSize) {
    this._borderSize = borderSize;
    this._borderSizeSub = borderSize * 2;
  },

  setWidth: function (width) {
    if (this._box == null || this._borderSize < 0 || width < 0) { return; }

    width -= this._borderSizeSub;
    if (width < 0) { width = 0; }
    this._box.style.width = width + "px";
  },

  setHeight: function (height) {
    if (this._box == null || this._borderSize < 0 || height < 0) { return; }

    height -= this._borderSizeSub;
    if (height < 0) { height = 0; }
    this._box.style.height = height + "px";
  },

  setImage: function (image, width, height) {
    var style = this._box.style

    style.borderImage = tw_Component.expandUrl(image, true) + " " + this._borderSize + " fill stretch"
  },

  destroy: function () {
    this._box.style.borderImage = '';
    this._box = null;
  }
});