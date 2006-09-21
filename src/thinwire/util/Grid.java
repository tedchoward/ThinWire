/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.util;

import java.util.List;

/**
 * An interface used to create a local data source.
 * A Grid is a 2 dimensional table of values.
 * @author Joshua J. Gertzen
 */
public interface Grid<R extends Grid.Row, C extends Grid.Column> {
  
  
	/**
   * A Column is a list of values. The 1st Column of a Grid is the list of
   * values in the Grid's 1st column (i.e. the Column at index 0).
   * 
   * Element j of Column k in a Grid is element k of Row j. 
   * 
   */
  public interface Column extends List<Object> {
    /**
     * Get the name of this Column
     * 
     * @return the name of this Column
     */
    public String getName();

    /**
     * Sets the name of this Column.
     * 
     * @param name the name of this Column
     */
    public void setName(String name);
        
    /**
     * Returns the text to display in column headers.
     * If the column's displayName field has a value, it is returned.
     * Otherwise the column's name value is returned.
     * @return Returns the displayName.
     */
    public String getDisplayName();
    
    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName);    
    
    /**
     * Get the Grid that this Column is part of.
     * 
     * @return the Grid that this column is part of.
     */
    public Grid getParent();

    /**
     * Get the index of this Column.
     * 
     * @return the index for this Column
     */
    public int getIndex();
  }
	
	/**
   * A Row is a list of values. The 1st Row of a Grid is the list of values in
   * the Grid's 1st Row (i.e. the Row at index 0).
   * 
   * Element j of Row k in a Grid is element k of Column j. 
   */
  public interface Row extends List<Object> {

    /**
     * Get the field in the specified Column at this Row.
     * 
     * @param columnName the specified Column
     * @return the field specified by the current row and the column name
     *         passed.
     */
    public Object get(String columnName);

    /**
     * Set the field in the specified Column at this Row.
     * 
     * @param columnName the specified Column
     * @param o the new value to set for the cell.
     * @return the field specified by the current row and the column name
     *         passed.
     */
    public Object set(String columnName, Object o);

    /**
     * Get the Grid this Row is part of.
     * 
     * @return the Grid this Row is part of
     */
    public Grid getParent();

    /**
     * Get the index for this Row.
     * 
     * @return the index for this Row
     */
    public int getIndex();
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
