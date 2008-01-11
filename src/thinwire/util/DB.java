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

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Joshua J. Gertzen
 */
public final class DB {
	public static final class Table {
	    public static enum Type {
	    	TABLE, VIEW, SYSTEM_TABLE, GLOBAL_TEMPORARY, LOCAL_TEMPORARY, ALIAS, SYNONYM;
	    }

	    private DB sql;
		private String catalog;
		private String schema;
		private String name;
		private Type type;
		private Map<String, Column> roColumns;
		private List<Column> roPrimaryKeys;
		
		private Table(DB sql, String catalog, String schema, String name, Type type) {
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
				
				rs.close();
				roColumns = Collections.unmodifiableMap(columns);
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
	        BIT(-7, boolean.class), TINYINT(-6, short.class), SMALLINT(5, short.class), INTEGER(4, int.class), BIGINT(-5, long.class), 
	        FLOAT(6, double.class), REAL(7, float.class), DOUBLE(8, float.class), NUMERIC(2, BigDecimal.class),
	        DECIMAL(3, BigDecimal.class), CHAR(1, String.class), VARCHAR(12, String.class), LONGVARCHAR(-1, String.class),
	        DATE(91, Date.class), TIME(92, Time.class), TIMESTAMP(93, Timestamp.class), BINARY(-2, byte[].class),
	        VARBINARY(-3, byte[].class), LONGVARBINARY(-4, byte[].class), NULL(0), OTHER(1111), JAVA_OBJECT(2000, Object.class), DISTINCT(2001),
	        STRUCT(2002, Struct.class), ARRAY(2003, Array.class), BLOB(2004, Blob.class), CLOB(2005, Clob.class), REF(2006), DATALINK(70), BOOLEAN(16, boolean.class);

	        public static final Type valueOf(int code) {
	            for (Type type : values()) {
	                if (type.code == code) return type;
	            }

	            throw new IllegalArgumentException("No Type exists for the specified code '" + code + "'");
	        }

	        private int code;
	        private Class mapType;

	        Type(int code) {
	            this.code = code;
	        }

	        Type(int code, Class mapType) {
	            this.code = code;
	            this.mapType = mapType;
	        }
	        
	        public Class getMapType() {
	        	return mapType;
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
	
    private static final Pattern ENCODE = Pattern.compile("([^']*)'([^']*)");
	private static final int MAX_RESULT_SEARCH = 20;
	private Map<String, Table> roTables;
	private DataSource ds;
	private Connection con;
	private int lastResultCount;
	private String lastStatement;
	private StringBuilder sb = new StringBuilder();
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;
	private SimpleDateFormat timestampFormat;
	
	public DB(String url, Map<String, String> info) {
		try {
			Properties props = new Properties();
			props.putAll(info);
			init(null, DriverManager.getConnection(url, props));
		} catch (SQLException e) {
			Reflector.throwException(e);
		}
	}
	
	public DB(String url, Properties info) {
		try {
			init(null, DriverManager.getConnection(url, info));
		} catch (SQLException e) {
			Reflector.throwException(e);
		}
	}
	
	public DB(String url, String userName, String password) {
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
	
	public DB(String jndiName) {
		try {
			init((DataSource)new InitialContext().lookup(jndiName), null);
		} catch (NamingException e) {
			Reflector.throwException(e);
		}
	}
	
	public DB(DataSource ds) {
		if (ds == null) throw new IllegalArgumentException("ds == null");
		init(ds, null);
	}
	
	public DB(Connection con) {
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
	
	private Object queryStatement(Object value, Object...statement) {
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
			
			if (value == null) {
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
					result = value;
				} else {
					if (value instanceof Grid) {
						result = populateGrid((Grid)value, rs);
					} else if (value instanceof Class) {
						result = populateObjectList((Class)value, rs);
					} else {
						if (!rs.next()) throw new SQLException("Single result query did not return any results: " + query);

						if (value instanceof Map) {
							result = populateSingleResultMap((Map<String, Object>)value, rs);
						} else if (value instanceof List) {
							result = populateSingleResultList((List<Object>)value, rs);
						} else {
							result = populateSingleResultObject(value, rs);
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
	
	private void executeUpdate(String statement, Grid grid, int[] gridIndices, Column columns[], int count) {
		Connection con = null;
		PreparedStatement stmt = null;
		
		try {
			con = getConnection();
			stmt = con.prepareStatement(statement);
			int resultCount = 0;

			for (Grid.Row r : grid.getRows()) {
	            for (int i = 0, pos = 1; i < count; i++) {
	            	Column c = columns[i];

	            	if (c != null) {
	            		mapValue(stmt, pos, c.type, r.get(gridIndices[i]));
		            	pos++;
	            	}
	            }
	            
	            resultCount += stmt.executeUpdate();
			}
			
			lastStatement = statement;
			lastResultCount = resultCount;
		} catch (SQLException e) {
			throw Reflector.throwException(e);
		} finally {
			cleanup(con, stmt, null);
		}
	}
	
	private void executeUpdate(String statement, List<? extends Object> objects, Reflector.PropertyTarget[] objProps, Column columns[], int count) {
		Connection con = null;
		PreparedStatement stmt = null;
		
		try {
			con = getConnection();
			stmt = con.prepareStatement(statement);
			int resultCount = 0;

			for (Object obj : objects) {
	            for (int i = 0, pos = 1; i < count; i++) {
	            	Column c = columns[i];

	            	if (c != null) {
	            		mapValue(stmt, pos, c.type, objProps[i].get(obj));
		            	pos++;
	            	}
	            }
	            
	            resultCount += stmt.executeUpdate();
			}
			
			lastStatement = statement;
			lastResultCount = resultCount;
		} catch (SQLException e) {
			throw Reflector.throwException(e);
		} finally {
			cleanup(con, stmt, null);
		}
	}

	private static void mapValue(PreparedStatement stmt, int pos, Column.Type type, Object value) throws SQLException {
    	Class mapType = type.mapType;
    	value = Reflector.DEFAULT_CONVERTER.toType(mapType, value);
    	
    	if (value == null) {
    		stmt.setNull(pos, type.code);
    	} else if (mapType == String.class) {
    		stmt.setString(pos, (String)value);
    	} else if (mapType == int.class) {
    		stmt.setInt(pos, (Integer)value);
    	} else if (mapType == boolean.class) {
    		stmt.setBoolean(pos, (Boolean)value);
    	} else if (mapType == short.class) {
    		stmt.setShort(pos, (Short)value);
    	} else if (mapType == long.class) {
    		stmt.setLong(pos, (Long)value);
    	} else if (mapType == float.class) {
    		stmt.setFloat(pos, (Float)value);
    	} else if (mapType == double.class) {
    		stmt.setDouble(pos, (Double)value);
    	} else if (mapType == BigDecimal.class) {
    		stmt.setBigDecimal(pos, (BigDecimal)value);
    	} else if (mapType == Date.class) {
    		stmt.setDate(pos, (Date)value);
    	} else if (mapType == Time.class) {
    		stmt.setTime(pos, (Time)value);
    	} else if (mapType == Timestamp.class) {
    		stmt.setTimestamp(pos, (Timestamp)value);
    	} else {
    		throw new IllegalArgumentException("unsupported target type: " + mapType);
    	}
	}
	
	private void mapValue(StringBuilder sb, Column.Type type, Object value) {
		Class mapType = type.mapType;
    	value = Reflector.DEFAULT_CONVERTER.toType(mapType, value);
    	
    	if (value == null) {
    		sb.append("null");
    	} else if (mapType == String.class) {
    		sb.append("'").append(ENCODE.matcher((String)value).replaceAll("$1''$2")).append("'");
    	} else if (mapType == int.class) {
    		sb.append((Integer)value);
    	} else if (mapType == boolean.class) {
    		sb.append((Boolean)value);
    	} else if (mapType == short.class) {
    		sb.append((Short)value);
    	} else if (mapType == long.class) {
    		sb.append((Long)value);
    	} else if (mapType == float.class) {
    		sb.append((Float)value);
    	} else if (mapType == double.class) {
    		sb.append((Double)value);
    	} else if (mapType == BigDecimal.class) {
    		sb.append(((BigDecimal)value).toPlainString());
    	} else if (mapType == Date.class) {
    		if (dateFormat == null) dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    		sb.append("{d '").append(dateFormat.format((java.util.Date)value)).append("'}");
    	} else if (mapType == Time.class) {
    		if (timeFormat == null) timeFormat = new SimpleDateFormat("HH:mm:ss");
    		sb.append("{t '").append(timeFormat.format((java.util.Date)value)).append("'}");
    	} else if (mapType == Timestamp.class) {
    		if (timestampFormat == null) timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    		sb.append("{ts '").append(timestampFormat.format((java.util.Date)value)).append("'}");
    	} else {
    		throw new IllegalArgumentException("unsupported target type: " + mapType);
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
		return (List<T>)queryStatement(type, statement);
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
		return (Grid)queryStatement(new ArrayGrid(), statement);
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
		return (Grid)queryStatement(grid, statement);
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
			return (T)queryStatement(type.newInstance(), statement);
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
		return (T)queryStatement(object, statement);
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
		return (Map<String, Object>)queryStatement(new HashMap<String, Object>(), statement);
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
		return (Map<String, Object>)queryStatement(map, statement);
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
		return (List<Object>)queryStatement(new ArrayList<Object>(), statement);
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
		return (List<Object>)queryStatement(list, statement);
	}
	
	//The 'setRows' and 'setSingleRow' methods require the map/grid/objects to have primary key values matching the specified table.
	public void setRowGrid(String table, Grid grid) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (grid == null || grid.getRows().size() == 0) throw new IllegalArgumentException("grid == null || grid.getRows().size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		if (keyCount == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");

		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundCount = 0;
		int foundKeys = 0;
        int[] gridIndicies = new int[count];
        Column[] columns = new Column[count];
		List<Grid.Column> gridCols = grid.getColumns();
		
		for (int i = 0, cnt = gridCols.size(); i < cnt; i++) {
			Column c = cols.get(gridCols.get(i).getName());
			
			if (c != null) {
				int index;
				
				if (c.primaryKey) {
					index = count - foundKeys - 1;
					foundKeys++;
				} else {
					index = foundCount;
					foundCount++;
				}
				
				gridIndicies[index] = i;
				columns[index] = c;
			}
		}
		
		if (keyCount != foundKeys) throw new IllegalArgumentException("Grid does not contain a column for every primary key in table '" + table + "'");
		if (foundCount == 0) throw new IllegalArgumentException("Grid does not contain any non-key values that map to the table '" + table + "'");
		
		//Shuffle the primary keys to end of data columns
		int start = count - keyCount;
		System.arraycopy(gridIndicies, start, gridIndicies, foundCount, keyCount);
		System.arraycopy(columns, start, columns, foundCount, keyCount);
		
		//Build prepared updated statement
		sb.setLength(0);
		sb.append("UPDATE ").append(table).append(" SET ");
		
        for (int i = 0; i < count; i++) {
        	Column col = columns[i];
        	sb.append(col.name).append('=').append('?').append(',');
        }
            
        sb.setCharAt(sb.length() - 1, ' ');
        sb.append("WHERE ");
        
        for (int i = count, cnt = count + keyCount; i < cnt; i++) {
        	Column col = columns[i];
        	sb.append(col.name).append("=? AND ");
        }

        sb.setLength(sb.length() - 5);
        
        //Execute the update against the value set
        executeUpdate(sb.toString(), grid, gridIndicies, columns, foundCount + keyCount);
	}
	
	public void setRows(String table, List<? extends Object> objects) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (objects == null || objects.size() == 0 || objects.get(0) == null) throw new IllegalArgumentException("objects == null || objects.size() == 0 || objects.get(0) == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		if (keyCount == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");

		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundCount = 0;
		int foundKeys = 0;
		Reflector.PropertyTarget[] objProps = new Reflector.PropertyTarget[count];
        Column[] columns = new Column[count];
        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(objects.get(0).getClass()).getProperties();
		
		for (Reflector.PropertyTarget prop : props.values()) {
			if (!prop.isReadable()) continue;
			Column c = cols.get(prop.getName());
			
			if (c != null) {
				int index;
				
				if (c.primaryKey) {
					index = count - foundKeys - 1;
					foundKeys++;
				} else {
					index = foundCount;
					foundCount++;
				}
				
				objProps[index] = prop;
				columns[index] = c;
			}
		}
		
		if (keyCount != foundKeys) throw new IllegalArgumentException("Objects in List do not contain a property for every primary key in table '" + table + "'");
		if (foundCount == 0) throw new IllegalArgumentException("Objects in List do not contain any non-key properties that map to the table '" + table + "'");
		
		//Shuffle the primary keys to end of data columns
		int start = count - keyCount;
		System.arraycopy(objProps, start, objProps, foundCount, keyCount);
		System.arraycopy(columns, start, columns, foundCount, keyCount);
		
		//Build prepared updated statement
		sb.setLength(0);
		sb.append("UPDATE ").append(table).append(" SET ");
		
        for (int i = 0; i < count; i++) {
        	Column col = columns[i];
        	sb.append(col.name).append('=').append('?').append(',');
        }
            
        sb.setCharAt(sb.length() - 1, ' ');
        sb.append("WHERE ");
        
        for (int i = count, cnt = count + keyCount; i < cnt; i++) {
        	Column col = columns[i];
        	sb.append(col.name).append("=? AND ");
        }

        sb.setLength(sb.length() - 5);
        
        //Execute the update against the value set
        executeUpdate(sb.toString(), objects, objProps, columns, foundCount + keyCount);
	}
	
	public void setSingleRow(String table, Object obj) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (obj == null) throw new IllegalArgumentException("obj == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		if (keyCount == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		Map<String, Column> cols = t.getColumns();
		
		int foundCount = 0;
		int foundKeys = 0;
        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(obj.getClass()).getProperties();

        sb.setLength(0);
		sb.append("UPDATE ").append(table).append(" SET ");
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("WHERE ");
		
		for (Reflector.PropertyTarget prop : props.values()) {
			if (!prop.isReadable()) continue;
			Column c = cols.get(prop.getName());
			
			if (c != null) {
				if (c.primaryKey) {
		        	sbWhere.append(c.name).append('=');
		        	mapValue(sbWhere, c.type, prop.get(obj));
		        	sbWhere.append(" AND ");
					foundKeys++;
				} else {
		        	sb.append(c.name).append('=');
		        	mapValue(sb, c.type, prop.get(obj));
		        	sb.append(',');
					foundCount++;
				}
			}
		}
		
		if (keyCount != foundKeys) throw new IllegalArgumentException("Object '" + obj.getClass().getName() + "' does not contain a property for every primary key in table '" + table + "'");
		if (foundCount == 0) throw new IllegalArgumentException("Object '" + obj.getClass().getName() + "' does not contain any non-key properties that map to the table '" + table + "'");

        sb.setCharAt(sb.length() - 1, ' ');
        sbWhere.setLength(sbWhere.length() - 5);
        sb.append(sbWhere);
        queryStatement(null, sb.toString());
	}
	
	public void setSingleRowMap(String table, Map<String, ? super Object> map) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (map == null || map.size() == 0) throw new IllegalArgumentException("map == null || map.size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		if (keyCount == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		Map<String, Column> cols = t.getColumns();
		
		int foundCount = 0;
		int foundKeys = 0;

        sb.setLength(0);
		sb.append("UPDATE ").append(table).append(" SET ");
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("WHERE ");
		
		for (Map.Entry<String, ? extends Object> e : map.entrySet()) {
			Column c = cols.get(e.getKey());
			
			if (c != null) {
				if (c.primaryKey) {
		        	sbWhere.append(c.name).append('=');
		        	mapValue(sbWhere, c.type, e.getValue());
		        	sbWhere.append(" AND ");
					foundKeys++;
				} else {
		        	sb.append(c.name).append('=');
		        	mapValue(sb, c.type, e.getValue());
		        	sb.append(',');
					foundCount++;
				}
			}
		}
		
		if (keyCount != foundKeys) throw new IllegalArgumentException("Map does not contain a value for every primary key in table '" + table + "'");
		if (foundCount == 0) throw new IllegalArgumentException("Map does not contain any non-key values that map to the table '" + table + "'");

        sb.setCharAt(sb.length() - 1, ' ');
        sbWhere.setLength(sbWhere.length() - 5);
        sb.append(sbWhere);
        queryStatement(null, sb.toString());
	}
	
	public void addRowGrid(String table, Grid grid) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (grid == null || grid.getRows().size() == 0) throw new IllegalArgumentException("grid == null || grid.getRows().size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundKeys = 0;
		Column[] columns = new Column[count];
        int[] gridIndicies = new int[count];
		sb.setLength(0);
		sb.append("INSERT INTO ").append(table).append(" VALUES(");
		
		for (Column c : cols.values()) {
			columns[c.index] = c;
		}
		
		for (int i = 0, cnt = columns.length; i < cnt; i++) {
			Column c = columns[i];
			Grid.Column col = grid.getColumnByName(c.name);

			if (col == null) {
				sb.append("NULL,");
				columns[i] = null;
			} else {
				if (c.primaryKey) foundKeys++;
				sb.append("?,");
				gridIndicies[i] = col.getIndex();
			}
		}
		
		sb.setCharAt(sb.length() - 1, ')');
		if (keyCount != foundKeys) throw new IllegalArgumentException("Grid does not contain a column for every primary key in table '" + table + "'");
        
        //Execute the update against the value set
        executeUpdate(sb.toString(), grid, gridIndicies, columns, count);
	}

	public void addRows(String table, List<Object> objects) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (objects == null || objects.size() == 0 || objects.get(0) == null) throw new IllegalArgumentException("objects == null || objects.size() == 0 || objects.get(0) == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundKeys = 0;
		Column[] columns = new Column[count];
        Reflector.PropertyTarget[] objProps = new Reflector.PropertyTarget[count];
		sb.setLength(0);
		sb.append("INSERT INTO ").append(table).append(" VALUES(");
		
		for (Column c : cols.values()) {
			columns[c.index] = c;
		}
		
        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(objects.get(0).getClass()).getProperties();
		
		for (int i = 0, cnt = columns.length; i < cnt; i++) {
			Column c = columns[i];
			Reflector.PropertyTarget prop = props.get(c.name);

			if (prop == null || !prop.isReadable()) {
				sb.append("NULL,");
				columns[i] = null;
			} else {
				if (c.primaryKey) foundKeys++;
				sb.append("?,");
				objProps[i] = prop;
			}
		}
		
		sb.setCharAt(sb.length() - 1, ')');
		if (keyCount != foundKeys) throw new IllegalArgumentException("Objects in List do not contain a readable property for every primary key in table '" + table + "'");
        
        //Execute the update against the value set
        executeUpdate(sb.toString(), objects, objProps, columns, count);
	}
	
	public void addSingleRow(String table, Object obj) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (obj == null) throw new IllegalArgumentException("obj == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundKeys = 0;
		Column[] columns = new Column[count];
		sb.setLength(0);
		sb.append("INSERT INTO ").append(table).append(" VALUES(");
		
		for (Column c : cols.values()) {
			columns[c.index] = c;
		}
		
        Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(obj.getClass()).getProperties();
		
		for (int i = 0, cnt = columns.length; i < cnt; i++) {
			Column c = columns[i];
			Reflector.PropertyTarget prop = props.get(c.name);

			if (prop == null || !prop.isReadable()) {
				sb.append("NULL,");
			} else {
				if (c.primaryKey) foundKeys++;
				mapValue(sb, c.type, prop.get(obj));
				sb.append(",");
			}
		}
		
		sb.setCharAt(sb.length() - 1, ')');
		if (keyCount != foundKeys) throw new IllegalArgumentException("Object '" + obj.getClass().getName() + "' does not contain a readable property for every primary key in table '" + table + "'");
		queryStatement(null, sb.toString());
 	}
	
	public void addSingleRowMap(String table, Map<String, ? super Object> map) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (map == null || map.size() == 0) throw new IllegalArgumentException("map == null || map.size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		final int keyCount = t.getPrimaryKeys().size();
		Map<String, Column> cols = t.getColumns();
		final int count = cols.size();
		
		//Build the properly ordered column array and grid column index lookup table
		int foundKeys = 0;
		Column[] columns = new Column[count];
		sb.setLength(0);
		sb.append("INSERT INTO ").append(table).append(" VALUES(");
		
		for (Column c : cols.values()) {
			columns[c.index] = c;
		}
		
		for (int i = 0, cnt = columns.length; i < cnt; i++) {
			Column c = columns[i];

			if (!map.containsKey(c.name)) {
				sb.append("NULL,");
			} else {
				if (c.primaryKey) foundKeys++;
				mapValue(sb, c.type, map.get(c.name));
				sb.append(",");
			}
		}
		
		sb.setCharAt(sb.length() - 1, ')');
		if (keyCount != foundKeys) throw new IllegalArgumentException("Map does not contain a value for every primary key in table '" + table + "'");
		queryStatement(null, sb.toString());
	}
	
	public void removeRowGrid(String table, Grid grid) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (grid == null || grid.getRows().size() == 0) throw new IllegalArgumentException("grid == null || grid.getRows().size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		List<Column> cols = t.getPrimaryKeys();
		final int count = cols.size();
		if (count == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		
		//Build the properly ordered column array and grid column index lookup table
		Column[] columns = new Column[count];
        int[] gridIndicies = new int[count];
		sb.setLength(0);
		sb.append("DELETE FROM ").append(table).append(" WHERE ");
		
		for (int i = 0; i < count; i++) {
			Column c = cols.get(i);
			columns[i] = c;
			Grid.Column col = grid.getColumnByName(c.name);
			if (col == null)  throw new IllegalArgumentException("Grid does not contain a column for every primary key in table '" + table + "'");
			gridIndicies[i] = col.getIndex();
			sb.append(c.name).append("=? AND ");
		}

		sb.setLength(sb.length() - 5);

        //Execute the update against the value set
        executeUpdate(sb.toString(), grid, gridIndicies, columns, count);
	}

	public void removeRows(String table, List<Object> objects) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (objects == null || objects.size() == 0 || objects.get(0) == null) throw new IllegalArgumentException("objects == null || objects.size() == 0 || objects.get(0) == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		List<Column> cols = t.getPrimaryKeys();
		final int count = cols.size();
		if (count == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		
		//Build the properly ordered column array and grid column index lookup table
		Column[] columns = new Column[count];
        Reflector.PropertyTarget[] objProps = new Reflector.PropertyTarget[count];
		sb.setLength(0);
		sb.append("DELETE FROM ").append(table).append(" WHERE ");
		Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(objects.get(0).getClass()).getProperties();
		
		for (int i = 0; i < count; i++) {
			Column c = cols.get(i);
			columns[i] = c;
			Reflector.PropertyTarget prop = props.get(c.name);
			if (prop == null)  throw new IllegalArgumentException("Objects in List do not contain a property for every primary key in table '" + table + "'");
			objProps[i] = prop;
			sb.append(c.name).append("=? AND ");
		}

		sb.setLength(sb.length() - 5);

        //Execute the update against the value set
        executeUpdate(sb.toString(), objects, objProps, columns, count);
	}
	
	public void removeSingleRow(String table, Object obj) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (obj == null) throw new IllegalArgumentException("obj == null");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		List<Column> cols = t.getPrimaryKeys();
		final int count = cols.size();
		if (count == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		
		sb.setLength(0);
		sb.append("DELETE FROM ").append(table).append(" WHERE ");
		Map<String, Reflector.PropertyTarget> props = Reflector.getReflector(obj.getClass()).getProperties();
		
		for (int i = 0; i < count; i++) {
			Column c = cols.get(i);
			Reflector.PropertyTarget prop = props.get(c.name);
			if (prop == null)  throw new IllegalArgumentException("Object '" + obj.getClass() + "' does not contain a property for every primary key in table '" + table + "'");
			sb.append(c.name).append('=');
			mapValue(sb, c.type, prop.get(obj));
			sb.append(" AND ");
		}

		sb.setLength(sb.length() - 5);
		queryStatement(null, sb.toString());
	}
	
	public void removeSingleRowMap(String table, Map<String, ? super Object> map) {
		if (table == null || table.length() == 0) throw new IllegalArgumentException("table == null || table.length() == 0");
		if (map == null || map.size() == 0) throw new IllegalArgumentException("map == null || map.size() == 0");
		
		Table t = getTables().get(table);
		if (t == null) throw new IllegalArgumentException("No table named '" + table + "' was found");

		List<Column> cols = t.getPrimaryKeys();
		final int count = cols.size();
		if (count == 0) throw new IllegalArgumentException("Cannot modify table '" + table + "' since it has no primary key(s)");
		
		sb.setLength(0);
		sb.append("DELETE FROM ").append(table).append(" WHERE ");
		
		for (int i = 0; i < count; i++) {
			Column c = cols.get(i);
			if (!map.containsKey(c.name)) throw new IllegalArgumentException("Map does not contain a value for every primary key in table '" + table + "'");
			sb.append(c.name).append('=');
			mapValue(sb, c.type, map.get(c.name));
			sb.append(" AND ");
		}

		sb.setLength(sb.length() - 5);
		queryStatement(null, sb.toString());
	}
	
	/**
	 * Executes the <code>statement</code> without returning any results.
	 * This method is typically used to execute INSERT, UPDATE or DELETE statements, or statements that do not return results.
	 * @param statement the complete statement as a single string or multiple statement fragments of any type with the first fragment being a string.
	 * @return the result count for INSERT, UPDATE or DELETE, or 0 for statements that do not return a result. 
	 */
	public int execute(Object...statement) {
		queryStatement(null, statement);
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
