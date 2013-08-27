###
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
###
class tw_BorderImage
  _box: null
  _borderSize: -1
  _borderSizeSub: -1


  setBox: (box) ->
    @_box = box

  setBorderSize: (borderSize) ->
    @_borderSize = borderSize
    @_borderSizeSub = borderSize * 2

  setWidth: (width) ->
    return if !@_box? or @_borderSize < 0 or width < 0

    width -= @_borderSizeSub
    width = 0 if width < 0
    @_box.style.width = "#{width}px"

  setHeight: (height) ->
    return if !@_box? or @_borderSize < 0 or height < 0

    height -= @_borderSizeSub
    height = 0 if height < 0
    @_box.style.height = "#{height}px"

  setImage: (image, width, height) ->
    style = @_box.style

    style.borderImage = "#{tw_Component.expandUrl image, true} #{@_borderSize} fill stretch"

  destroy: ->
    @_box.style.borderImage = ''
    @_box = null
