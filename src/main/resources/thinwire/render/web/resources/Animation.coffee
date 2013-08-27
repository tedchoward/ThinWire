###
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems
Copyright (c) 2013 Ted C. Howard

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
###
class tw_Animation
  _obj: null
  _setter: ''
  _realSetter: ''
  _unitTime: 0
  _position: 0
  _sequence: null
  _value: null
  _after: null

  constructor: (obj, setter, time, sequence, realSetter, value, after) ->
    @_obj = obj
    @_setter = setter
    @_realSetter = realSetter
    @_value = value
    @_after = after

    @_sequence = sequence
    @_unitTime = Math.floor(time / sequence.length + 0.5)

  start: ->
    setTimeout @_run, 0
    return

  _run: =>
    if @_obj._inited
      size = @_sequence[@_position]
      @_obj[@_setter](size)

      if !@_after && @_realSetter?
        @_obj[@_realSetter](@_value)
        @_realSetter = null

      @_position += 1

      if @_position < @_sequence.length
        setTimeout @_run, @_unitTime
      else
        @destroy()

    else
      @destroy()

    return

  destroy: ->
    @_obj[@_realSetter](@_value) if @_after && @_realSetter?
    @_obj = @_sequence = null
    return
