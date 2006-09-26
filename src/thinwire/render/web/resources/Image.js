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
var tw_Image = tw_Component.extend({
    construct: function(id, containerId, props) {
        this.$.construct.apply(this, ["div", "image", id, containerId]);
        this._fontBox = null;
        var s = this._box.style;
        s.backgroundRepeat = "no-repeat";
        s.backgroundPosition = "center center";
        tw_addEventListener(this._box, "click", this._clickListener.bind(this));        
        this.init(-1, props);        
    },
    
    _clickListener: function() {
        if (!this.isEnabled()) return;
        if (this.isFocusCapable()) this.setFocus(true);     
        this.fireAction("click");
    },    
    
    keyPressNotify: tw_Component.keyPressNotifySpaceFireAction,    
    
    setImage: function(image) {
        this._box.style.backgroundImage = image.length > 0 ? "url(" + tw_BaseBrowserLink.expandLocation(image) + ")" : "";
    },
    
    setHeight: function(height) {
        this.$.setHeight.apply(this, [height]);
        
        //NOTE: This is not perfect, the problem here is that when an image has transparent
        //      whitespace on the top, the image does not vertically center in IE.  Technically,
        //      anytime the height is set to something shorter than the height of the image, IE
        //      requires the backgroundPosition to be adjusted.  For now, we only adjust for
        //      small images since they often require precision placement.
        if (tw_isIE) {
            if (height <= 15) {
                this._box.style.backgroundPosition = "center top";
            } else {
                this._box.style.backgroundPosition = "center center";
            }
        }
    }    
});

