package thinwire.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

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
    
	private static abstract class CallTarget {
		final Reflector reflector;
        final boolean complexType;
        final String name;
        Class type;
        
        private CallTarget(Reflector reflector, String name, Class type) {
        	this.reflector = reflector;
        	this.type = type;
        	this.name = name;
        	complexType = !(type == String.class ||
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
        
        abstract Object call(Object obj, Object...args) throws CallException;
	}
	
    private static class PropertyTarget extends CallTarget {
        private final boolean readable;
        private final boolean writable;
        
        private Method getter;
        private Method setter;

        private PropertyTarget(Reflector reflector, Method m, String name, Class type, boolean isGetter) {
        	super(reflector, name, type);
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

        void setValue(Object obj, Object value) throws CallException {
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
        
        Object getValue(Object obj) throws CallException {
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
    	
    	Object call(Object obj, Object...args) throws CallException {
    		if (args == null || args.length == 0) {
    			return getValue(obj);
    		} else if (args.length == 1) {
    			setValue(obj, args[0]);
                return null;
    		} else {
    			throw new CallException(reflector.className, name, false, "number of arguments '" + args.length + "' not appropriate for a setter or getter.");
    		}
    	}
    }
    
    private static class MethodTarget extends CallTarget {
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
    		super(reflector, name, retType);
    		addSignature(method, argTypes);
    	}
    	
    	@SuppressWarnings("unchecked")
    	void addSignature(Method method, Class[] argTypes) {
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
    	Object call(Object obj, Object...args) throws CallException {
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
    
    private String className;
    private Reflector superClass;
    private Converter converter = DEFAULT_CONVERTER;
    private Map<String, PropertyTarget> nameToProperty = new HashMap<String, PropertyTarget>();
    private Map<String, PropertyTarget> lowerNameToProperty = new HashMap<String, PropertyTarget>();
    private Map<String, MethodTarget> nameToMethod = new HashMap<String, MethodTarget>();
    private Map<String, MethodTarget> lowerNameToMethod = new HashMap<String, MethodTarget>();

    private Reflector(Class clazz) throws CovariantTypeException {
    	if (clazz.isInterface()) throw new UnsupportedOperationException("reflecting over interfaces is currently unsupported");
    	className = clazz.getName();
    	Class[] interfaces = clazz.getInterfaces();
    	Class superClass = clazz.getSuperclass();

    	if (log.isLoggable(LEVEL)) {
    		log.log(LEVEL, "Reflecting over declared methods of " + (clazz.isInterface() ? "interface " : "class ") + clazz.getName());
    		if (superClass != null) log.log(LEVEL, "Extends class " + superClass.getName());
    		log.log(LEVEL, (clazz.isInterface() ? "Extends " : "Implements ") + interfaces.length + " interfaces");
    	}
    	
    	if (superClass != null) this.superClass = getReflector(superClass); 

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
    	
		if (log.isLoggable(LEVEL)) log.log(LEVEL, "Found " + descToMethod.size() + " methods directly declared or implemented from " + interfaces.length + " interfaces for class " + className);

    	for (Method m : descToMethod.values()) {
            String name = m.getName();
        	if (log.isLoggable(LEVEL)) log.log(LEVEL, "Analyzing " + Modifier.toString(m.getModifiers()) + " method " + m.getName() + " with declaring " + Modifier.toString(m.getDeclaringClass().getModifiers()) + " class " + m.getDeclaringClass().getName());
            int len = name.length();
            Class retType = m.getReturnType();
            Class[] argTypes = m.getParameterTypes();
            MethodTarget method = nameToMethod.get(name);
            
            if (method == null) {
	            method = new MethodTarget(this, name, m, retType, argTypes);
	            nameToMethod.put(name, method);
	            String lower = name.toLowerCase();
	            if (!lowerNameToMethod.containsKey(lower)) lowerNameToMethod.put(lower, method);
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
                	String lowerPropName = propName.toLowerCase();
                	if (!lowerNameToProperty.containsKey(lowerPropName)) lowerNameToProperty.put(lowerPropName, property);
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
                	String lowerPropName = propName.toLowerCase();
                	if (!lowerNameToProperty.containsKey(lowerPropName)) lowerNameToProperty.put(lowerPropName, property);
                } else if (property.type != retType) {
                	throw new CovariantTypeException(className, propName, property.type, retType);
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
    
    public Object call(Object target, String methodName, Object...args) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (methodName == null || methodName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	MethodTarget method = nameToMethod.get(methodName);
    	if (method == null) method = lowerNameToMethod.get(methodName.toLowerCase());    	
    	
    	if (method == null) {
    		if (superClass == null) throw new NotFoundException(className, methodName, true);
    		return superClass.call(target, methodName, args);
    	} else {
    		return method.call(target, args);
    	}
    }
    
    public void set(Object target, String propertyName, Object value) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	PropertyTarget property = nameToProperty.get(propertyName);
    	if (property == null) property = lowerNameToProperty.get(propertyName.toLowerCase());
    	
    	if (property == null) {
    		if (superClass == null) throw new NotFoundException(className, propertyName, false);
    		superClass.set(target, propertyName, value);
    	} else {
    		property.setValue(target, value);
    	}
    }
    
    public Object get(Object target, String propertyName) throws NotFoundException, CallException {
    	if (target == null) throw new IllegalArgumentException("target == null");
    	if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
    	PropertyTarget property = nameToProperty.get(propertyName);
    	if (property == null) property = lowerNameToProperty.get(propertyName.toLowerCase());
    	
    	if (property == null) {
    		if (superClass == null) throw new NotFoundException(className, propertyName, false);
    		return superClass.get(target, propertyName);
    	} else {
    		return property.getValue(target);
    	}
    }
    
    public static void throwException(Exception ex) {
    	if (ex instanceof RuntimeException) throw (RuntimeException)ex;
    	
    	try {
    		synchronized (CheckedExceptionThrower.class) {
    			CheckedExceptionThrower.exception = ex;
    			CheckedExceptionThrower.class.newInstance();
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
