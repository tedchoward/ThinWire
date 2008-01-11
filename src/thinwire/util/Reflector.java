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

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

/**
 * @author Joshua J. Gertzen
 */
public class Reflector {
	private static final Logger log = Logger.getLogger(Reflector.class.getName());
	private static final Level LEVEL = Level.FINER;
	
    private interface Converter {
        Object toType(Class type, Object value);
    }    

    private static class DefaultConverter implements Converter {
        public Object toType(Class type, Object value) {
            if (value == null) return null;
            if (type.isInstance(value)) return value;
    		if (log.isLoggable(LEVEL)) log.log(LEVEL, "Convert value from type '" + value.getClass().getName() + "' to '" + type.getName() + "'");
            String str = value.toString();
            if (str.equals("null")) str = null;
            
            if (type == String.class) {
                value = str;
            } else if (type == boolean.class || type == Boolean.class) {
                value = str.equals("") ? Boolean.FALSE : Boolean.valueOf(str);
            } else if (type == int.class || type == Integer.class) {
                value = str.equals("") ? new Integer(0) : new Integer(Double.valueOf(str).intValue());
            } else if (type == long.class || type == Long.class) {                            
                value = str.equals("") ? new Long(0) : new Long(Double.valueOf(str).longValue());
            } else if (type == short.class || type == Short.class) {
                value = str.equals("") ? new Short((short)0) : new Short(Double.valueOf(str).shortValue());
            } else if (type == byte.class || type == Byte.class) {
                value = str.equals("") ? new Byte((byte)0) : new Byte(Double.valueOf(str).byteValue());
            } else if (type == float.class || type == Float.class) {
                value = str.equals("") ? new Float(0) : new Float(Double.valueOf(str).floatValue());
            } else if (type == double.class || type == Double.class) {
                value = str.equals("") ? new Double(0) : Double.valueOf(str);                                
            } else if (type == char.class || type == Character.class) {                                
                value = new Character(str.charAt(0));
            } else {
                try {
                    Field f = type.getField(str.toUpperCase().replace('-', '_'));                        
                    value = f.get(null);
                } catch (NoSuchFieldException e) {
                    try {
                        Method m = type.getMethod("valueOf", String.class);
                        if (m.getReturnType() != type) throw new NotFoundException(type.getName(), "valueOf", true);
                        value = m.invoke(null, value);
                    } catch (Exception e2) {
                    	throwException(e2);
                    }
                } catch (IllegalAccessException e) {
                    throwException(e);
                }
            }
            
            return value;
        }
    }
    
    private static final Converter DEFAULT_CONVERTER = new DefaultConverter();
    
    public static interface CallTarget {
    	boolean isStatic();
    	String getName();
    	Object call(Object obj, Object...args) throws CallException;
    }
    
	private static abstract class AbstractCallTarget implements CallTarget {
		final Reflector reflector;
        final String name;
        Class type;
        boolean statik;
        
        private AbstractCallTarget(Reflector reflector, String name, boolean statik, Class type) {
        	this.reflector = reflector;
        	this.type = type;
        	this.name = name;
        	this.statik = statik;
        }
        
        public boolean isStatic() {
        	return statik;
        }
        
        public String getName() {
        	return name;
        }
	}
	
    public static class PropertyTarget extends AbstractCallTarget {
        private final boolean readable;
        private final boolean writable;
        
        private Method getter;
        private Method setter;

        private PropertyTarget(Reflector reflector, Method m, String name, Class type, boolean isGetter) {
        	super(reflector, name, false, type);
            name = name.length() == 1 ? String.valueOf(Character.toUpperCase(name.charAt(0))) : Character.toUpperCase(name.charAt(0)) + name.substring(1);
            
            if (isGetter) {
            	getter = m;
            	readable = true;
            	
                try {
                    setter = getter.getDeclaringClass().getMethod("set" + name, type);
                } catch (NoSuchMethodException e) {
                    setter = null;
                }

                writable = setter != null;
            } else {
            	setter = m;
            	writable = true;
            	Class decClazz = setter.getDeclaringClass();
            	
                try {
                    getter = decClazz.getMethod((type == boolean.class || type == Boolean.class ? "is" : "get") + name);
                } catch (NoSuchMethodException e) {
                    try {
                        getter = decClazz.getMethod((type == boolean.class || type == Boolean.class ? "get" : "is") + name);
                    } catch (NoSuchMethodException e2) {
                        getter = null;
                    }
                }
                
                readable = getter != null;
            }
        }
        
        public boolean isReadable() {
        	return readable;
        }
        
        public boolean isWritable() {
        	return writable;
        }

        public void set(Object obj, Object value) throws CallException {
        	if (!writable) throw new CallException(reflector.className, name, false, "property is not writable");
    		if (log.isLoggable(LEVEL)) log.log(LEVEL, "Set property " + name + "='" + value + "' for object '" + obj + "'");
        	
    		value = reflector.converter.toType(type, value);
        	
        	try {
    			setter.invoke(obj, value);
        	} catch (IllegalAccessException e) {
        		try {
	        		setter.setAccessible(true);
	        		setter.invoke(obj, value);
        		} catch (Exception e2) {
        			if (e2 instanceof RuntimeException) throw (RuntimeException)e2;
        			throw new CallException(reflector.className, name, false, e2.getMessage());
        		}
    		} catch (Exception e) {
    			if (e instanceof RuntimeException || e instanceof CallException || e instanceof NotFoundException) throw (RuntimeException)e;
    			//throw new CallException(reflector.className, name, false, e.getMessage());
    			throw new RuntimeException(e);
    		}
        }
        
        public Object get(Object obj) throws CallException {
        	if (!readable) throw new CallException(reflector.className, name, false, "property is not readable");
    		Object ret;
    		
        	try {
    			ret = getter.invoke(obj);
        	} catch (IllegalAccessException e) {
        		try {
	        		getter.setAccessible(true);
	        		ret = getter.invoke(obj);
        		} catch (Exception e2) {
        			if (e2 instanceof RuntimeException) throw (RuntimeException)e2;
        			throw new CallException(reflector.className, name, false, e2.getMessage());
        		}
    		} catch (Exception e) {
    			if (e instanceof RuntimeException) throw (RuntimeException)e;
    			throw new CallException(reflector.className, name, false, e.getMessage());
    		}

    		if (log.isLoggable(LEVEL)) log.log(LEVEL, "Get property " + name + "='" + ret + "' for object '" + obj + "'");
    		return ret;
        }
    	
    	public Object call(Object obj, Object...args) throws CallException {
    		if (args == null || args.length == 0) {
    			return get(obj);
    		} else if (args.length == 1) {
    			set(obj, args[0]);
                return null;
    		} else {
    			throw new CallException(reflector.className, name, false, "number of arguments '" + args.length + "' not appropriate for a setter or getter.");
    		}
    	}
    }
    
    private static class MethodTarget extends AbstractCallTarget {
    	private static class MethodSignature {
            Method method;
        	Class[] argTypes;
    		
    		MethodSignature(Method method, Class[] argTypes) {
    			this.method = method;
    			this.argTypes = argTypes;
    		}
    	}
    	
    	private Object sigs;

    	private MethodTarget(Reflector reflector, String name, Method method, Class retType, Class[] argTypes) {
    		super(reflector, name, Modifier.isStatic(method.getModifiers()), retType);
    		addSignature(method, argTypes);
    	}
    	
    	@SuppressWarnings("unchecked")
    	void addSignature(Method method, Class[] argTypes) {
    		if (Modifier.isStatic(method.getModifiers()) != isStatic()) throw new CovariantTypeException(reflector.className, name);
    			
    		if (this.sigs == null) {
    			sigs = new MethodSignature(method, argTypes);
    		} else {
    			Map<Integer, Object> map;
    			
    			if (sigs instanceof MethodSignature) {
	    			map = new HashMap<Integer, Object>(2);
	    			MethodSignature sig = (MethodSignature)sigs;
	    			map.put(sig.argTypes.length, sig);
	    			sigs = map;
    			} else {
    				map = (Map<Integer, Object>)sigs;
    			}
    			
    			Object set = map.get(argTypes.length);
    			
    			if (set == null) {
    				map.put(argTypes.length, new MethodSignature(method, argTypes));
    			} else {
    				List<MethodSignature> lst;
    				
    				if (set instanceof MethodSignature) {
    					map.put(argTypes.length, lst = new ArrayList<MethodSignature>(2));
    					lst.add((MethodSignature)set);
    				} else {
    					lst = (List<MethodSignature>)set;
    				}
    				
    				lst.add(new MethodSignature(method, argTypes));
    			}
    		}
    	}
    	
    	//@SuppressWarnings("unchecked")
    	public Object call(Object obj, Object...args) throws CallException {
    		if (log.isLoggable(LEVEL)) log.log(LEVEL, "Call method " + name + " with " + args.length + " arguments for object '" + obj + "'");

    		MethodSignature sig;
    		
			if (sigs instanceof MethodSignature) {
    			sig = (MethodSignature)sigs;
    		} else {
    			Map<Integer, Object> map = (Map<Integer, Object>)sigs;
    			int cnt = args.length;
    			Object set = null;
    			
    			//First we search for an exact match or match with less params then we have args
    			while (cnt >= 0 && (set = map.get(cnt--)) == null);
    			
    			//Failing that we search for a match with more params then we have args, we limit this to 64 params
    			if (set == null) {
    				cnt = args.length + 1;
    				while (cnt < 64 && (set = map.get(cnt++)) == null);
    			}
    			
    			//Note: It's not technically possible for 'set' to be null at this point.  If it is, then there is a bug.
    			if (set instanceof MethodSignature) {
    				sig = (MethodSignature)set;
    			} else {
    				List<MethodSignature> search = new ArrayList<MethodSignature>((List<MethodSignature>)set);
    				cnt = args.length;
    				
    				outer: for (int i = 0; i < cnt; i++) {
    					for (Iterator<MethodSignature> it = search.iterator(); it.hasNext();) {
    						MethodSignature s = it.next();
    						
    						if (s.argTypes[i] != args[i].getClass()) {
    							//If there is only one signature left, then don't remove it
    							//Instead break out of the search and use the last remaining
    							//signature because it is the closest match.
    							if (search.size() > 1) break outer;
    							it.remove();
    						}
    					}
    				}
    				
        			//Note: It's not technically possible for 'search' to zero elements.  If it does, then there is a bug.
    				sig = search.get(0);
    			}
    		}
    		
			//Truncate extra args
			if (sig.argTypes.length < args.length) {
				Object[] ary = new Object[sig.argTypes.length];
				System.arraycopy(args, 0, ary, 0, sig.argTypes.length);
				args = ary;
			} else if (sig.argTypes.length > args.length) {
				throw new CallException(reflector.className, name, true, "closest matching signature requires " + sig.argTypes.length + ", but only " + args.length + " were provided");
			}
			
    		for (int i = args.length; --i >=0;) {
    			args[i] = reflector.converter.toType(sig.argTypes[i], args[i]);
    		}

    		try {
    			return sig.method.invoke(obj, args);
        	} catch (IllegalAccessException e) {
        		try {
	        		sig.method.setAccessible(true);
	    			return sig.method.invoke(obj, args);
        		} catch (Exception e2) {
        			if (e2 instanceof RuntimeException) throw (RuntimeException)e2;
        			throw new CallException(reflector.className, name, true, e2.getMessage());
        		}
    		} catch (Exception e) {
    			if (e instanceof RuntimeException) throw (RuntimeException)e;
    			throw new CallException(reflector.className, name, true, e.getMessage());
    		}
    	}
    }
    
    public static class CallException extends RuntimeException {
    	private CallException(String className, String name, boolean isMethod, String msg) {
    		super("Failure calling " + (isMethod ? "method" : "property") + " '" + className + "." + name + "': " + msg);
    	}
    }
    
    public static class NotFoundException extends RuntimeException {
    	private NotFoundException(String className, String name, boolean isMethod) {
    		super((isMethod ? "Method" : "Property") + " '" + name + "' was not found for class '" + className);
    	}
    }
    
    public static class CovariantTypeException extends RuntimeException {
    	private CovariantTypeException(String className, String name, Class firstType, Class secondType) {
    		super("Reflection collision on class '" + className + "' for the property named '" + name + "': " +
    				"covariant value types not supported, found '" + firstType.getName() + "' and '" + secondType.getName() + "'");
    	}

    	private CovariantTypeException(String className, String methodName) {
    		super("Reflection collision on class '" + className + "' for the method named '" + methodName + "': " +
    				"both a static and non-static exist that differ only by there arguments");
    	}
    }
    
    private static ConcurrentMap<Class, Reflector> REFLECTORS = new ConcurrentHashMap<Class, Reflector>(50);
    private static final int MAX_REFLECTORS = 512;
    
    public static Reflector getReflector(Class clazz) throws CovariantTypeException {
    	Reflector reflector = REFLECTORS.get(clazz);

    	if (reflector == null) {
    		if (REFLECTORS.size() > MAX_REFLECTORS) REFLECTORS.clear();
    		reflector = new Reflector(clazz);
    		REFLECTORS.putIfAbsent(clazz, reflector);
    	}
    	
    	return reflector;
    }
    
    static class CaseInsensitiveChainMap<V> implements Map<String, V> {
    	private Map<String, V> parent;
    	private Map<String, V> map = new HashMap<String, V>();
    	private Map<String, V> lmap = new HashMap<String, V>();

    	CaseInsensitiveChainMap() { }

    	CaseInsensitiveChainMap(Map<String, V> parent) {
    		this.parent = parent;
    	}
    	
		public void clear() {
			map.clear();
			lmap.clear();
		}

		public boolean containsKey(Object key) {
			if (map.containsKey(key) || lmap.containsKey(key)) {
				return true;
			} else if (parent == null) {
				return false;
			} else {
				return parent.containsKey(key);
			}
		}

		public boolean containsValue(Object value) {
			if (map.containsValue(value)) {
				return true;
			} else if (parent == null) {
				return false;
			} else {
				return parent.containsValue(value);
			}
		}

		public Set<Map.Entry<String, V>> entrySet() {
			if (parent == null) {
				return map.entrySet();
			} else {
				return new JoinedSet<Map.Entry<String, V>>(map.entrySet(), parent.entrySet());
			}
		}

		public V get(Object key) {
			V ret = map.get(key);
			if (ret == null && key instanceof String) ret = lmap.get(((String)key).toLowerCase());
			if (ret == null && parent != null) ret = parent.get(key); 
			return ret;
		}

		public boolean isEmpty() {
			return map.isEmpty() && (parent == null || parent.isEmpty());
		}

		public Set<String> keySet() {
			if (parent == null) {
				return map.keySet();
			} else {
				return new JoinedSet<String>(map.keySet(), parent.keySet());
			}
		}

		public V put(String key, V value) {
			lmap.put(key.toLowerCase(), value);
			return map.put(key, value);
		}

		public void putAll(Map<? extends String, ? extends V> t) {
			map.putAll(t);
			
			for (Map.Entry<? extends String, ? extends V> e : t.entrySet()) {
				lmap.put(e.getKey().toLowerCase(), e.getValue());
			}
		}

		public V remove(Object key) {
			if (key instanceof String) lmap.remove(((String)key).toLowerCase());
			return map.remove(key);
		}

		public int size() {
			if (parent == null) {
				return map.size();
			} else {
				return map.size() + parent.size();
			}
		}

		public Collection<V> values() {
			if (parent == null) {
				return map.values();
			} else {
				return new JoinedSet<V>(map.values(), parent.values());
			}
		}
    }
    
    private static class JoinedSet<T> extends AbstractSet<T> {
    	private Collection<T> c1;
    	private Collection<T> c2;
    	
    	private JoinedSet(Collection<T> c1, Collection<T> c2) {
    		this.c1 = c1;
    		this.c2 = c2;
    	}
    	
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				boolean iteratingSecond;
				Iterator<T> it = c1.iterator();
				
				public boolean hasNext() {
					if (it.hasNext()) {
						return true;
					} else if (iteratingSecond) {
						return false;
					} else {
						iteratingSecond = true;
						it = c2.iterator();
						return it.hasNext();
					}
				}

				public T next() {
					hasNext();
					return it.next();
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		public int size() {
			return c1.size() + c2.size(); 
		}
    }
    
    private String className;
    
    private Converter converter = DEFAULT_CONVERTER;
    private Map<String, PropertyTarget> nameToProperty;
    private Map<String, PropertyTarget> roNameToProperty;
    private Map<String, MethodTarget> nameToMethod;
    private Map<String, MethodTarget> roNameToMethod;

    private Reflector(Class clazz) throws CovariantTypeException {
    	if (clazz.isInterface()) throw new UnsupportedOperationException("reflecting over interfaces is currently unsupported");
    	boolean isLoggable = log.isLoggable(LEVEL);
    	className = clazz.getName();
    	Class[] interfaces = clazz.getInterfaces();
    	Class superClass = clazz.getSuperclass();

    	if (isLoggable) {
    		log.log(LEVEL, "Reflecting over declared methods of " + (clazz.isInterface() ? "interface " : "class ") + clazz.getName());
    		if (superClass != null) log.log(LEVEL, "Extends class " + superClass.getName());
    		log.log(LEVEL, (clazz.isInterface() ? "Extends " : "Implements ") + interfaces.length + " interfaces");
    	}
    	
    	Reflector superReflector = superClass == null ? null : getReflector(superClass); 
    	Map<String, Method> descToMethod = new HashMap<String, Method>();
    	StringBuilder sbDesc = new StringBuilder();
    	
    	//Gather up all interface methods first
    	for (Class inf : interfaces) {
    		for (Method m : inf.getMethods()) {
    			String desc = getDescriptor(sbDesc, m).toString();
    			if (!descToMethod.containsKey(desc)) descToMethod.put(desc, m);
    		}
    	}
    	
    	for (Method m : clazz.getDeclaredMethods()) {
    		String desc = getDescriptor(sbDesc, m).toString();
    		if (!descToMethod.containsKey(desc)) descToMethod.put(desc, m);
    	}
    	
		if (isLoggable) log.log(LEVEL, "Found " + descToMethod.size() + " methods directly declared or implemented from " + interfaces.length + " interfaces for class " + className);
		
    	if (descToMethod.size() == 0) {
    		nameToProperty = superReflector.nameToProperty;
    		nameToMethod = superReflector.nameToMethod;
    	} else {
    		if (superReflector == null) {
	        	nameToProperty = new CaseInsensitiveChainMap<PropertyTarget>();
	        	nameToMethod = new CaseInsensitiveChainMap<MethodTarget>();
    		} else {
	        	nameToProperty = new CaseInsensitiveChainMap<PropertyTarget>(superReflector.nameToProperty);
	        	nameToMethod = new CaseInsensitiveChainMap<MethodTarget>(superReflector.nameToMethod);
    		}

	    	for (Method m : descToMethod.values()) {
	            String name = m.getName();
	        	if (isLoggable) log.log(LEVEL, "Analyzing " + Modifier.toString(m.getModifiers()) + " method " + m.getName() + " with declaring " + Modifier.toString(m.getDeclaringClass().getModifiers()) + " class " + m.getDeclaringClass().getName());
	            int len = name.length();
	            Class retType = m.getReturnType();
	            Class[] argTypes = m.getParameterTypes();
	            MethodTarget method = nameToMethod.get(name);
	            
	            if (method == null) {
		            method = new MethodTarget(this, name, m, retType, argTypes);
		            nameToMethod.put(name, method);
	            } else {
	            	if (retType == method.type) {
	            		method.addSignature(m, argTypes);
	            	} else {
	            		throw new IllegalStateException("illegal covariant return type!");
	            	}
	            }
	            
	            if (name.startsWith("set") && len > 3 && argTypes.length == 1) {
	                String propName = name.substring(3);
	                propName = Character.toLowerCase(propName.charAt(0)) + (propName.length() > 1 ? propName.substring(1) : "");
	                PropertyTarget property = nameToProperty.get(propName);
	                
	                if (property == null) {
	                	property = new PropertyTarget(this, m, propName, argTypes[0], false);
	                	nameToProperty.put(propName, property);
	                } else if (property.type != argTypes[0]) {
	                	throw new CovariantTypeException(className, propName, property.type, argTypes[0]);
	                }
	            } else if (retType != void.class && argTypes.length == 0 && ((name.startsWith("get") && len > 3) || (name.startsWith("is") && len > 2))) {
	                String propName = name.substring(name.charAt(0) == 'i' ? 2 : 3);
	                propName = Character.toLowerCase(propName.charAt(0)) + (propName.length() > 1 ? propName.substring(1) : "");
	                PropertyTarget property = nameToProperty.get(propName);
	                
	                if (property == null) {
	                	property = new PropertyTarget(this, m, propName, retType, true);
	                	nameToProperty.put(propName, property);
	                } else if (property.type != retType) {
	                	throw new CovariantTypeException(className, propName, property.type, retType);
	                }
	            }
	    	}
        }
    }
    
    //NOTE: This excludes the return parameter because we don't use it for matching
    private StringBuilder getDescriptor(StringBuilder sb, Method m) {
		sb.setLength(0);
	    sb.append(m.getName()).append(':');
	    Class[] params = m.getParameterTypes();

	    for (int i = 0, cnt = params.length; i < cnt; i++) {
    		sb.append(params[i].getName()).append(':');
		}
	    
	    return sb;
    }
    
    public Map<String, PropertyTarget> getProperties() {
    	if (roNameToProperty == null) roNameToProperty = Collections.unmodifiableMap(nameToProperty);
    	return roNameToProperty;
    }
    
    public Map<String, MethodTarget> getMethods() {
    	if (roNameToMethod == null) roNameToMethod = Collections.unmodifiableMap(nameToMethod);
    	return roNameToMethod;
    }
    
    public Object call(Object target, String methodName, Object...args) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (methodName == null || methodName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	MethodTarget method = nameToMethod.get(methodName);
		if (method == null) throw new NotFoundException(className, methodName, true);
		return method.call(target, args);
    }
    
    public void set(Object target, String propertyName, Object value) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	PropertyTarget property = nameToProperty.get(propertyName);
    	if (property == null) throw new NotFoundException(className, propertyName, false);
		property.set(target, value);
    }
    
    public Object get(Object target, String propertyName) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	PropertyTarget property = nameToProperty.get(propertyName);
    	if (property == null) throw new NotFoundException(className, propertyName, false);
		return property.get(target);
    }
    
    public static String toString(Object obj) {
    	if (obj == null) throw new IllegalArgumentException("obj == null");
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');
    	
    	for (PropertyTarget prop : getReflector(obj.getClass()).getProperties().values()) {
    		sb.append(prop.getName()).append(':');
			Object value = prop.get(obj);
			
    		if (value == null || value instanceof Boolean || value instanceof Number) {
				sb.append(value);
			} else if (value instanceof Character) {
				sb.append('\'').append(value).append('\'');
			} else {
				sb.append('"').append(value).append('"');
			}
			
			sb.append(',');
    	}
    	
    	if (sb.length() == 1) {
    		sb.append('}');
    	} else {
    		sb.setCharAt(sb.length() - 1, '}');
    	}
    	
    	return sb.toString();
    }
    
    public static boolean isComplexType(Class type) {
    	return !(type == String.class ||
                type == Boolean.class || type == boolean.class ||
                type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Short.class || type == short.class ||
                type == Byte.class || type == byte.class ||
                type == Float.class || type == float.class ||
                type == Double.class || type == double.class ||
                type == Character.class || type == char.class ||
                type == Void.class || type == void.class);
    }
    
    public static RuntimeException throwException(Exception ex) {
    	if (ex instanceof RuntimeException) throw (RuntimeException)ex;
    	
    	try {
    		synchronized (CheckedExceptionThrower.class) {
    			CheckedExceptionThrower.exception = ex;
    			CheckedExceptionThrower.class.newInstance();
    			return null; //unreached
    		}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} 
    }
    
    private static class CheckedExceptionThrower {
    	private static Exception exception;
    	
    	CheckedExceptionThrower() throws Exception {
    		Exception ex = exception;
    		exception = null;
    		throw ex;
    	}
    }
}
