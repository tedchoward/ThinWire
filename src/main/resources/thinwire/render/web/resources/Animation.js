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
var tw_Animation = Class.extend({
    _obj: null,
    _setter: "",
	_realSetter: "",
    _unitTime: 0,
    _position: 0,
    _sequence: null,
	_value: null,
	_after: null,
    
    construct: function(obj, setter, time, sequence, realSetter, value, after) {
        this._obj = obj;
        this._setter = setter;
		this._realSetter = realSetter;
		this._value = value;
		this._after = after;
        this._run = this._run.bind(this);
        
        this._sequence = sequence;
        this._unitTime = Math.floor(time / sequence.length + .5);
    },
        
    start: function() {
        setTimeout(this._run, 0);
    },    
    
    _run: function() {
        if (this._obj._inited) {
            var size = this._sequence[this._position];
            this._obj[this._setter](size);

			if (!this._after && this._realSetter != null) {
				this._obj[this._realSetter](this._value);
				this._realSetter = null;
			}
			
            this._position++;
            
            if (this._position < this._sequence.length) {
                setTimeout(this._run, this._unitTime);
            } else {
                this.destroy();
            }
        } else {
            this.destroy();
        }
    },

    destroy: function() {
		if (this._after && this._realSetter != null) this._obj[this._realSetter](this._value);
        this._obj = this._sequence = null;
    }
});

