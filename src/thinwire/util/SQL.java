/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.util;

import java.sql.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.*;

/**
 * @author Joshua J. Gertzen
 */
public final class SQL {
    private static final class ColumnIndexComparator implements Comparator<String> {
    	private Map<String, Column> columns;
    	
    	ColumnIndexComparator(Map<String, Column> columns) {
    		this.columns = columns;
    	}
    	
        public int compare(String r1, String r2) {
        	if (columns == null) throw new IllegalStateException("attempt to compare columns without reference to columns");
            int index1 = columns.get(r1).getIndex();
            int index2 = columns.get(r2).getIndex();
            
            if (index1 < index2) {
                return -1;
            } else if (index1 == index2){
                return 0;
            } else {
                return 1;
            }
        }
    };

	public static final class Table {
	    public static enum Type {
	    	TABLE, VIEW, SYSTEM_TABLE, GLOBAL_TEMPORARY, LOCAL_TEMPORARY, ALIAS, SYNONYM;
	    }

	    private SQL sql;
		private String catalog;
		private String schema;
		private String name;
		private Type type;
		private Map<String, Column> roColumns;
		private List<Column> roPrimaryKeys;
		
		private Table(SQL sql, String catalog, String schema, String name, Type type) {
			this.sql = sql;
			this.catalog = catalog;
			this.schema = schema;
			this.name = name;
			this.type = type;
		}
		
		public String getCatalog() {
			return catalog;
		}
		
		public String getSchema() {
			return schema;
		}
		
		public String getName() {
			return name;
		}
		
		public Type getType() {
			return type;
		}
		
		public Map<String, Column> getColumns() {
			if (roColumns == null) loadColumns();
			return roColumns;
		}
		
		public List<Column> getPrimaryKeys() {
			if (roPrimaryKeys == null) loadColumns();
			return roPrimaryKeys;
		}
		
		private void loadColumns() {
			Connection con = null;
			ResultSet rs = null;
			
			try {
				Map<String, Column> columns = new Reflector.CaseInsensitiveChainMap<Column>();
				con = sql.getConnection();
				DatabaseMetaData dmd = con.getMetaData();
				rs = dmd.getColumns(catalog, schema, name, null);
				
				while (rs.next()) {
					String columnName = rs.getString(4); //4=COLUMN_NAME
					columns.put(columnName, new Column(columnName, Column.Type.valueOf(rs.getInt(5)), rs.getInt("ORDINAL_POSITION") - 1)); //5=DATA_TYPE, 17=ORDINAL_POSITION
				}
				
				roColumns = Collections.unmodifiableMap(columns);
				
				rs.close();
				rs = dmd.getPrimaryKeys(catalog, schema, name);
				List<Column> primaryKeys = new ArrayList<Column>(3);
				
				while(rs.next()) {
					Column column = columns.get(rs.getString(4)); //4=COLUMN_NAME
					column.primaryKey = true;
					primaryKeys.add(column);
				}
				
				roPrimaryKeys = Collections.unmodifiableList(primaryKeys);
			} catch (SQLException e) {
				Reflector.throwException(e);
			} finally {
				sql.cleanup(con, null, rs);
				sql = null;
			}
		}
	}
	
	public static final class Column {
	    public static enum Type {
	        BIT(-7), TINYINT(-6), SMALLINT(5), INTEGER(4), BIGINT(-5), FLOAT(6), REAL(7), DOUBLE(8), NUMERIC(2),
	        DECIMAL(3), CHAR(1), VARCHAR(12), LONGVARCHAR(-1), DATE(91), TIME(92), TIMESTAMP(93), BINARY(-2),
	        VARBINARY(-3), LONGVARBINARY(-4), NULL(0), OTHER(1111), JAVA_OBJECT(2000), DISTINCT(2001), STRUCT(2002),
	        ARRAY(2003), BLOB(2004), CLOB(2005), REF(2006), DATALINK(70), BOOLEAN(16);

	        public static final Type valueOf(int code) {
	            for (Type type : values()) {
	                if (type.code == code) return type;
	            }

	            throw new IllegalArgumentException("No Type exists for the specified code '" + code + "'");
	        }

	        private int code;

	        Type(int code) {
	            this.code = code;
	        }
	        
	        public int getCode() {
	            return code;
	        }
	    }

		private String name;
		private Type type;
		private int index;
		private boolean primaryKey;
		
		private Column(String name, Type type, int index) {
			this.name = name;
			this.type = type;
			this.index = index;
		}
		
		public boolean isPrimaryKey() {
			return primaryKey;
		}
		
		public String getName() {
			return name;
		}
		
		public Type getType() {
			return type;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	private static final int MAX_RESULT_SEARCH = 20;
	private Map<String, Table> roTables;
	private DataSource ds;
	private Connection con;
	private int lastResultCount;
	private String lastStatement;
	private StringBuilder sb = new StringBuilder();
	
	public SQL(String url, Map<String, String> info) {
		try {
			Properties props = new Properties();
			props.putAll(info);
			init(null, DriverManager.getConnection(url, props));
		} catch (SQLException e) {
			Reflector.throwException(e);
		}
	}
	
	public SQL(String url, Properties info) {
		try {
			init(null, DriverManager.getConnection(url, info));
		} catch (SQLException e) {
			Reflector.throwException(e);
		}
	}
	
	public SQL(String url, String userName, String password) {
		try {
			if ((userName == null || userName.length() == 0) && (password == null || password.length() == 0)) {
				init(null, DriverManager.getConnection(url));
			} else {
				init(null, DriverManager.getConnection(url, userName, password));
			}
		} catch (SQLException e) {
			Reflector.throwException(e);
		}
	}
	
	public SQL(String jndiName) {
		try {
			init((DataSource)new InitialContext().lookup(jndiName), null);
		} catch (NamingException e) {
			Reflector.throwException(e);
		}
	}
	
	public SQL(DataSource ds) {
		if (ds == null) throw new IllegalArgumentException("ds == null");
		init(ds, null);
	}
	
	public SQL(Connection con) {
		if (con == null) throw new IllegalArgumentException("con == null");
		init(null, con);
	}
	
	private void init(DataSource ds, Connection con) {
		if (ds == null && con == null) throw new IllegalStateException("ds == null && con == null");
		this.ds = ds;
		this.con = con;
	}
	
	public Map<String, Table> getTables() {
		if (roTables == null) {
			Map<String, Table> tables = new Reflector.CaseInsensitiveChainMap<Table>();
			
			Connection con = null;
			ResultSet rs = null;
			
			try {
				con = getConnection();
				DatabaseMetaData dmd = con.getMetaData();
				String catalog = con.getCatalog(); //null means no default catalog, empty string means tables wihout a catalog
				String schema = dmd.getUserName();
				
				//Determine if user name is a valid schema
				rs = dmd.getSchemas();
				boolean foundSchema = false;
				
				while (rs.next()) {
					String tableSchema = rs.getString(1); //TABLE_SCHEM
					String tableCatalog = rs.getString(2); //TABLE_CATALOG
					
					if ((catalog == null || tableCatalog == null || catalog.equals(tableCatalog)) && tableSchema.equals(schema)) {
						foundSchema = true;
						break;
					}
				}
				
				rs.close();
				if (!foundSchema) schema = null;
				rs = dmd.getTables(catalog, schema, null, null);
				
				while (rs.next()) {
					String tableCatalog = rs.getString(1); //TABLE_CAT 
					String tableSchema = rs.getString(2); //TABLE_SCHEM
					
					if ((catalog == null || catalog.equals(tableCatalog)) && (schema == null || schema.equals(tableSchema))) {
						String tableName = rs.getString(3); //TABLE_NAME
						String tableType = rs.getString(4); //TABLE_TYPE
						tables.put(tableName, new Table(this, tableCatalog, tableSchema, tableName, Table.Type.valueOf(tableType)));
					}
				}
			} catch (SQLException e) {
				Reflector.throwException(e);
			}
			
			roTables = Collections.unmodifiableMap(tables);
		}
		
		return roTables;
	}
	
	private Grid populateGrid(Grid grid, ResultSet rs) throws SQLException {
		if (!rs.next()) return grid;
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        if (grid.getColumns().size() == 0) {
            for (int i = 1; i <= columnCount; i++) {
                Grid.Column column = grid.newColumn();
                column.setName(rsmd.getColumnName(i));
                grid.getColumns().add(column);
            }

            do {
                Grid.Row row = grid.newRow();
                
                for (int i = 1; i <= columnCount; i++)
                    row.add(rs.getObject(i));
                
                grid.getRows().add(row);
            } while (rs.next());
		} else {
			Map<Object, Integer> indexMap = new HashMap<Object, Integer>();
			
			for (int i = grid.getColumns().size(); --i >= 0;) {
				indexMap.put(grid.getColumns().get(i).getName(), i);
			}
			
            for (int i = 1; i <= columnCount; i++) {
            	String name = rsmd.getColumnName(i);
            	Integer curIndex = indexMap.get(name);
            	
            	if (curIndex == null) {
            		Grid.Column column = grid.newColumn();
            		column.setName(name);
            		grid.getColumns().add(column);
            		curIndex = grid.getColumns().size() - 1;
            	}
            	
        		indexMap.put(i, curIndex);
            }
			
            do {
                Grid.Row row = grid.newRow();
                
                for (int i = 1; i <= columnCount; i++) {
                	Object value = rs.getObject(i);
                	Integer index = indexMap.get(i);
                	
                	if (index != null) {
                    	while (index >= row.size()) row.add(null);
                    	row.set(index, value);
                	}
                }
                
                grid.getRows().add(row);
            } while (rs.next());
		}
		
		return grid;
	}
	
	private List<Object> populateObjectList(Class type, ResultSet rs) throws SQLException {
		try {
			List<Object> lst = new ArrayList<Object>();
			if (!rs.next()) return lst;
	        ResultSetMetaData rsmd = rs.getMetaData();
	        int count = rsmd.getColumnCount();
	        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(type).getProperties();
	        
	        //When queries are used properly to map to in a one-to-one manner against an object, all the
	        //results will be meaningful, and therefore this is the fastest way to perform lookups.  Even
	        //in cases where the results do not match the object very closely, there are no alternatives
	        //that I can think of that would be faster than this lookup method.
	        int index = 0;
	        int[] columnForProperty = new int[count];
	        Reflector.PropertyTarget[] foundProps = new Reflector.PropertyTarget[count]; 
	
	        for (int i = 1; i <= count; i++) {
	        	String columnName = rsmd.getColumnName(i);
	            if (columnName.indexOf('_') != -1) columnName = columnName.replaceAll("(.*?)_(.*?)", "$1$2");
	            Reflector.PropertyTarget prop = props.get(columnName);
	            
	            if (prop != null) {
	            	foundProps[index] = prop;
	            	columnForProperty[index] = i;
	            	index++;
	            }
	        }
	        
	        count = index;
	
	        do {
	        	Object obj = type.newInstance();
	            
	            for (index = 0; index < count; index++) {
	            	Reflector.PropertyTarget prop = foundProps[index];
	            	int pos = columnForProperty[index];
	            	prop.set(obj, rs.getObject(pos));
	            }
	            
	            lst.add(obj);
	        } while (rs.next());
			
			return lst;
		} catch (Exception e) {
			throw Reflector.throwException(e);
		}
	}
	
	private Map<String, Object> populateSingleResultMap(Map<String, Object> map, ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();

        for (int i = 1, cnt = rsmd.getColumnCount(); i <= cnt; i++) {
        	map.put(rsmd.getColumnName(i), rs.getObject(i));
        }
		
		return map;
	}
	
	private List<Object> populateSingleResultList(List<Object> lst, ResultSet rs) throws SQLException {
        for (int i = 1, cnt = rs.getMetaData().getColumnCount(); i <= cnt; i++) {
        	lst.add(rs.getObject(i));
        }
		
		return lst;
	}
	
	private Object populateSingleResultObject(Object obj, ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(obj.getClass()).getProperties();

        for (int i = 1; i <= columnCount; i++) {
        	String columnName = rsmd.getColumnName(i);
            if (columnName.indexOf('_') != -1) columnName = columnName.replaceAll("(.*?)_(.*?)", "$1$2");
            Reflector.PropertyTarget prop = props.get(columnName);
            if (prop != null) prop.set(obj, rs.getObject(i));
        }
		
		return obj;
	}
	
	private Connection getConnection() throws SQLException {
		if (ds == null) {
			return this.con;
		} else {
			return ds.getConnection();
		}
	}
	
	private void cleanup(Connection con, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (con != null && ds != null) con.close();
		} catch (SQLException e) {
			throw Reflector.throwException(e);
		}
	}
	
	private Object executeStatement(Object handler, Object...statement) {
		if (statement == null || statement.length == 0) throw new IllegalArgumentException("statement == null || statement.length == 0");
		if (!(statement[0] instanceof String)) throw new IllegalArgumentException("!(statement[0] instanceof String)");
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			con = getConnection();
			String query;
			
			if (statement.length == 1) {
				query = (String)statement[0];
			} else {
				sb.setLength(0);
				
				for (Object part : statement) {
					sb.append(part);
				}
				
				query = sb.toString();
			}
			
			stmt = con.createStatement();
			
			Object result;
			
			if (handler == null) {
				lastResultCount = stmt.executeUpdate(query);
				result = null;
			} else {
				boolean moreResults = stmt.execute(query);
				int cnt = 0;
				
				do {
					if (moreResults) {
						rs = stmt.getResultSet();
						break;
					}
					
					moreResults = stmt.getMoreResults();
				} while ((moreResults || stmt.getUpdateCount() >= 0) && ++cnt < MAX_RESULT_SEARCH);
				
				if (rs == null) {
					result = handler;
				} else {
					if (handler instanceof Grid) {
						result = populateGrid((Grid)handler, rs);
					} else if (handler instanceof Class) {
						result = populateObjectList((Class)handler, rs);
					} else {
						if (!rs.next()) throw new SQLException("Single result query did not return any results: " + query);

						if (handler instanceof Map) {
							result = populateSingleResultMap((Map<String, Object>)handler, rs);
						} else if (handler instanceof List) {
							result = populateSingleResultList((List<Object>)handler, rs);
						} else {
							result = populateSingleResultObject(handler, rs);
						}

						if (rs.next()) throw new SQLException("Single result query returned multiple results: " + query);
					}
				}
			}
			
			lastStatement = query;
			return result;
		} catch (SQLException e) {
			throw Reflector.throwException(e);
		} finally {
			cleanup(con, stmt, rs);
		}
	}
	
	/**
	 * Executes the <code>statement</code>, returning the results as a <code>List</code> of new objects of the class <code>type</code>.
	 * For every row returned by the specified <code>statement</code> a new instance of the class <code>type</code> will be
	 * created. The values for the row will then be mapped to the object using the appropriate <code>Reflector</code> for the <code>type</code>.
	 * The new mapped object will then be added to a new <code>List</code> created by this method. After all the results have been
	 * processed, the new <code>List</code> will be returned.
	 * For the object properties to be set properly, each value queried by the <code>statement</code> must have an translatable name as defined
	 * in the {@link #getSingleResult(Class, String)} documentation.
	 * @param type the class of object that should be created for each row returned by the <code>statement</code>.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return a new <code>List</code> of <code>type</code> representing the data returned or an empty <code>List</code> if no results were returned.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> getResults(Class<T> type, Object...statement) {
		return (List<T>)executeStatement(type, statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the results as a <code>Grid</code> of values.
	 * For every row returned by the specified <code>statement</code> a new <code>Grid.Row</code> will be created and populated with the row
	 * values. The new <code>Grid.Row</code> will then be added to a new <code>Grid</code> created by this method. After all the results have been
	 * processed, the new <code>Grid</code> will be returned. The value names defined in the query will be assigned as the column names in the
	 * returned <code>Grid</code>. For values without a name, a column will still be added but the column name will not be set.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return a new <code>Grid</code> with a <code>Grid.Row</code> for every result row returned by the <code>statement</code> or an empty new <code>Grid</code> if no results were returned.
	 */
	@SuppressWarnings("unchecked")
	public Grid getResultGrid(Object...statement) {
		return (Grid)executeStatement(new ArrayGrid(), statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the results appended to the specified <code>grid</code>.
	 * For every row returned by the specified <code>statement</code> a new <code>Grid.Row</code> will be created and populated with the row
	 * values. The new <code>Grid.Row</code> will then be added to the end of the <code>grid</code> passed to this method. 
	 * After all the results have been processed, the modified <code>grid</code> will be returned. For value names that do not
	 * already have a matching named column in the <code>grid</code> or for values with no name, a new <code>Grid.Column</code> be added to the end of the
	 * columns list and the value will be placed at that index in the row. For columns that already exist in the <code>grid</code> but do not have a matching named value,
	 * a 'null' value is set for that column index in the row. 
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the passed <code>grid</code> with a new <code>Grid.Row</code> appended for every result row returned by the <code>statement</code>.
	 * @throws SQLException if no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public Grid getResultGrid(Grid grid, Object...statement) {
		if (grid == null) throw new IllegalArgumentException("grid == null");
		return (Grid)executeStatement(grid, statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the single row result as a new object of the class <code>type</code>.
	 * The values for the single row are mapped to the object using the appropriate <code>Reflector</code> for the <code>type</code>.
	 * For the object properties to be set properly, each value queried by the <code>statement</code> must have a translatable name as defined here:
	 * 
	 * <ul><b>How Result Values Are Mapped:</b>
	 * <li>The result name matches the property name of the class exactly (Ex: firstName -> firstName).
	 * <li>The result name matches the property name of the class in a case-insensitive match (Ex: firstname -> firstName, FIRSTNAME -> firstName).</li>
	 * <li>The result name matches the property name of the class in a case-insensitive match, excluding underscores (Ex: first_name -> firstName, FIRST_NAME -> firstName).</li>
	 * <li>Unnamed values are ignored.</li>
	 * <li>Value names for which no match is found are ignored.</i>
	 * </ul>
	 * 
	 * @param type the class of object that should be created for the single row returned by the <code>statement</code>.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return a new object of class <code>type</code> representing the first row of data returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T getSingleResult(Class<T> type, Object...statement) {
		try {
			return (T)executeStatement(type.newInstance(), statement);
		} catch (Exception e) {
			throw Reflector.throwException(e);
		}
	}
	
	/**
	 * Executes the <code>statement</code>, returning the single row result mapped onto an existing <code>object</code>.
	 * The values for the single row are mapped to the object using the appropriate <code>Reflector</code> for the <code>type</code>.
	 * For the object properties to be set properly, each value queried by the <code>statement</code> must have an translatable name as defined
	 * in the {@link #getSingleResult(Class, String)} documentation.
	 * @param object the object that the values will be mapped to based on the single row returned by the <code>statement</code>.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the passed <code>object</code> with its properties modified based on the data returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T getSingleResult(T object, Object...statement) {
		if (object == null) throw new IllegalArgumentException("object == null");
		return (T)executeStatement(object, statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the single row result as a new <code>Map</code>.
	 * The values for the single row are placed in the map according to the exact value names in the <code>statement</code>. Unnamed values are ignored.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return a new <code>Map</code> representing the data returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSingleResultMap(Object...statement) {
		return (Map<String, Object>)executeStatement(new HashMap<String, Object>(), statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the single row result mapped onto an existing <code>map</code>.
	 * The values for the single row are placed in the map according to the exact value names in the <code>statement</code>, overriding any existing values
	 * in the <code>map</code> and adding new mappings where necessary. Unnamed values are ignored.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the passed <code>map</code> modified based on the column name / value pairs returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSingleResultMap(Map<String, ? super Object> map, Object...statement) {
		if (map == null) throw new IllegalArgumentException("map == null");
		return (Map<String, Object>)executeStatement(map, statement);
	}
	
	/**
	 * Executes the <code>statement</code>, returning the single row values as a new <code>List</code>.
	 * The values for the single row added to the list in the exact order that they appear in the results, which is typically the same order they appeared in the <code>statement</code>.
	 * Since value names are not utilized by this method, all named and unnamed values are returned in the <code>List</code>.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return a new <code>List</code> representing the data returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getSingleResultValues(Object...statement) {
		return (List<Object>)executeStatement(new ArrayList<Object>(), statement);
	}

	/**
	 * Executes the <code>statement</code>, returning the single row values appended onto an existing <code>list</code>.
	 * The values for the single row appended to the <code>list</code> in the exact order that they appear in the results, which is typically the same order they appeared in the <code>statement</code>.
	 * Since value names are not utilized by this method, all named and unnamed values are returned in the <code>List</code>.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the passed <code>map</code> modified based on the column name / value pairs returned.
	 * @throws SQLException if more than one result is returned or no results are returned by the <code>statement</code>.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getSingleResultValues(List<? super Object> list, Object...statement) {
		if (list == null) throw new IllegalArgumentException("values == null");
		return (List<Object>)executeStatement(list, statement);
	}
	
	//The 'setRows' and 'setSingleRow' methods require the map/grid/objects to have primary key values matching the specified table.
	public void setRowGrid(String table, Grid grid) {
	}
	
	public void setRows(String table, List<Object> lst) {
		
	}
	
	public void setSingleRow(String table, Object obj) {
		
	}
	
	public void setSingleRowMap(String table, Map<String, ? super Object> map) {
		
	}
	
	public void addRowGrid(String table, Grid grid) {
		
	}

	public void addRows(String table, List<Object> obj) {
		
	}
	
	public void addSingleRow(String table, Object obj) {
		
	}
	
	public void addSingleRowMap(String table, Map<String, ? super Object> map) {
		
	}
	
	public void removeRowGrid(String table, Grid grid) {
		
	}

	public void removeRows(String table, List<Object> obj) {
		
	}
	
	public void removeSingleRow(String table, Object obj) {
		
	}
	
	public void removeSingleRowMap(String table, Map<String, ? super Object> map) {
		
	}
	
	/**
	 * Executes the <code>statement</code> without returning any results.
	 * This method is typically used to execute INSERT, UPDATE or DELETE statements, or statements that do not return results.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the result count for INSERT, UPDATE or DELETE, or 0 for statements that do not return a result. 
	 */
	public int execute(Object...statement) {
		executeStatement(null, statement);
		return lastResultCount;
	}
	
	/**
	 * Retrieves the complete last statement that was executed against the database from any method of this object.
	 * @return the last statement executed by this object.
	 */
	public String getLastStatement() {
		return lastStatement;
	}
	
	/**
	 * Retrieves the last count of rows that was read or the last count of rows that was modified as a result of the last statement.
	 * This value changes in tandem with the statement returned by <code>getLastStatement</code>.
	 * @return the last count of results that was read from the database.
	 */
	public int getLastResultCount() {
		return lastResultCount;
	}
}
