/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
var tw_Animation = Class.extend({
    _obj: null,
    _getter: "",
    _setter: "",
    _neg: false,
    _dist: 0,
    _unitSize: 0,
    _unitTime: 0,
    _lastTime: 0,
    _calcUnitTime: 0,
    
    construct: function(obj, getter, setter, dist, unitSize, time) {        
        if (obj == null) obj = window;
        this._obj = obj;
        this._getter = getter;
        this._setter = setter;        
        this._run = this._run.bind(this);
        
        if (dist < 0) {
            this._neg = true;
            dist = ~dist + 1;
        }
        
        this._dist = dist;
        this._unitSize = unitSize;        
        this._calcUnitTime = Math.floor(time / Math.floor(dist / this._unitSize + .5) + .5);
        this._unitTime = Math.floor(this._calcUnitTime / 2);
    },
        
    start: function() {
        setTimeout(this._run, 0);
    },    
    
    _run: function() { 
        if (this._unitSize > this._dist) {
            this._unitSize = this._dist;                        
        } else {
            var thisTime = new Date().getTime();

            if (this._lastTime > 0) {
                var actTime = thisTime - this._lastTime;                
                if (actTime > 0) this._unitTime = Math.floor(this._unitTime * (this._calcUnitTime / actTime) + .5);
            }
            
            this._lastTime = thisTime;
        }
        
        if (this._obj instanceof tw_Component && this._obj._inited) {
            this._obj[this._setter](this._obj[this._getter]() + (this._neg ? -this._unitSize : this._unitSize));
            this._dist -= this._unitSize;
            if (this._dist > 0) setTimeout(this._run, this._unitTime);
        }
    }
});

