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
var tw_Animation = Class.extend({
    _obj: null,
    _getter: "",
    _setter: "",
    _neg: false,
    _adjust: false,
    _lastTime: null,
    _dist: 0,
    _time: 0,
    _unitSize: 0,
    _unitTime: 0,
    
    construct: function(obj, getter, setter, dist, time, adjust) {
        if (obj == null) obj = window;
        this._obj = obj;
        this._getter = getter;
        this._setter = setter;
        
        if (dist < 0) {
            this._neg = true;
            dist = ~dist + 1;
        }
        
        this._dist = dist;
        this._time = time;
        this._adjust = adjust;        
        this._unitSize = dist / time;
        
        if (this._unitSize < 1) {
            this._unitTime = Math.floor(1 / this._unitSize + .5);
            this._unitSize = 1;
        } else {
            this._unitTime = 1;
        }
        
        this.run = this.run.bind(this);
    },
    
    run: function() { 
        this._obj[this._setter](this._obj[this._getter]() + (this._neg ? -this._unitSize : this._unitSize));
        this._dist -= this._unitSize;
        
        if (this._adjust) {
            var thisTime = new Date().getTime();

            if (this._lastTime != null) {
                var actTime = thisTime - this._lastTime;
                
                if (actTime > this._unitTime) {
                    var scale = actTime / this._unitTime;
                    this._unitSize = Math.floor(this._unitSize * scale + .5);
                    this._unitTime = Math.floor(this._unitTime * scale + .5);
                }
            }
            
            this._lastTime = thisTime;                
        }
        
        if (this._dist > 0) setTimeout(this.run, this._unitTime);
    }    
});

tw_Animation.setBounds = function(id, x, y, width, height, time, adjust) {    
    var comp = tw_Component.instances[id];
    
    if (comp != null) {
        if (x != comp.getX()) new tw_Animation(comp, "getX", "setX", x - comp.getX(), time).run(); 
        if (y != comp.getY()) new tw_Animation(comp, "getY", "setY", y - comp.getY(), time).run(); 
        if (width != comp.getWidth()) new tw_Animation(comp, "getWidth", "setWidth", width - comp.getWidth(), time).run(); 
        if (height != comp.getHeight()) new tw_Animation(comp, "getHeight", "setHeight", height - comp.getHeight(), time).run(); 
    }
    
    tw_em.sendViewStateChanged(comp._id, "bounds", x + "," + y + "," + width + "," + height);
};

tw_Animation.setVisible = function(id, visible, time, adjust) {    
    var comp = tw_Component.instances[id];
    
    if (comp != null) {
        var opacity = visible ? 100 : 0;
        var fx = new tw_Animation(comp, "getOpacity", "setOpacity", opacity - comp.getOpacity(), time);
        fx.run();        
    }
    
    tw_em.sendViewStateChanged(comp._id, "visible", visible);
};


