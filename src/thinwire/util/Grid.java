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
package thinwire.util;

import java.util.List;

/**
 * An interface used to create a local data source. A Grid is a 2 dimensional table of values.
 * @author Joshua J. Gertzen
 */
public interface Grid<R extends Grid.Row, C extends Grid.Column> {
    /**
     * A Column is a list of values. The 1st Column of a Grid is the list of values in the Grid's 1st column (i.e. the Column at
     * index 0). Element j of Column k in a Grid is element k of Row j.
     */
    public interface Column extends List<Object> {
        /**
         * Get the name of this Column
         * @return the name of this Column
         */
        public String getName();

        /**
         * Sets the name of this Column.
         * @param name the name of this Column
         */
        public void setName(String name);

        /**
         * Get the Grid that this Column is part of.
         * @return the Grid that this column is part of.
         */
        public Grid getParent();

        /**
         * Get the index of this Column.
         * @return the index for this Column
         */
        public int getIndex();
        
        /**
         * Get the developer/user defined object that has been associated to this Grid.Column.
         * @return the general purpose object that has been associated to this Grid.Column.
         */
        public Object getUserObject();

        /**
         * Set the developer/user defined object for this Grid.Column.
         */
        public void setUserObject(Object value);
    }

    /**
     * A Row is a list of values. The 1st Row of a Grid is the list of values in the Grid's 1st Row (i.e. the Row at index 0).
     * Element j of Row k in a Grid is element k of Column j.
     */
    public interface Row extends List<Object> {

        /**
         * Get the field in the specified Column at this Row.
         * @param columnName the specified Column
         * @return the field specified by the current row and the column name passed.
         */
        public Object get(String columnName);

        /**
         * Set the field in the specified Column at this Row.
         * @param columnName the specified Column
         * @param o the new value to set for the cell.
         * @return the field specified by the current row and the column name passed.
         */
        public Object set(String columnName, Object o);

        /**
         * Get the Grid this Row is part of.
         * @return the Grid this Row is part of
         */
        public Grid getParent();

        /**
         * Get the index for this Row.
         * @return the index for this Row
         */
        public int getIndex();
        
        /**
         * Get the developer/user defined object that has been associated to this Grid.Row.
         * @return the general purpose object that has been associated to this Grid.Row.
         */
        public Object getUserObject();

        /**
         * Set the developer/user defined object for this Grid.Row.
         */
        public void setUserObject(Object value);
    }

    /**
     * Get the Columns belonging to this Grid.
     * @return a list of all the columns in the Grid.
     */
    public List<C> getColumns();

    /**
     * Get the Rows belonging to this Grid.
     * @return a list of all the rows in the Grid.
     */
    public List<R> getRows();
}
