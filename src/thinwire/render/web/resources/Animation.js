/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
var tw_Animation = Class.extend({
    _obj: null,
    _setter: "",
    _unitTime: 0,
    _position: 0,
    _sequence: null,
    
    construct: function(obj, setter, time, sequence) {
        this._obj = obj;
        this._setter = setter;
        this._run = this._run.bind(this);
        
        this._sequence = sequence;
        this._unitTime = Math.floor(time / sequence.length + .5);
    },
        
    start: function() {
        setTimeout(this._run, 0);
    },    
    
    _run: function() {
        var size = this._sequence[this._position];
        this._obj[this._setter](size);
        this._position++;
        
        if (this._position < this._sequence.length) {
            setTimeout(this._run, this._unitTime);
        } else {
            this.destroy();
        }
    },

    destroy: function() {
        this._obj = this._sequence = null;
    }
});

