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
package thinwire.render.web;

import thinwire.ui.Component;

/**
 * @author Ted C. Howard
 */
final class ProgressBarRenderer extends RangeComponentRenderer {
    private static String PROGRESS_BAR_CLASS = "tw_ProgressBar";
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(PROGRESS_BAR_CLASS, wr, c, container);
        super.render(wr, comp, container);
    }
}
