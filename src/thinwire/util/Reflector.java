package thinwire.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Reflector {
    private interface Converter {
        Object toType(Class type, Object value);
    }    

    private static class DefaultConverter implements Converter {
        public Object toType(Class type, Object value) {
            if (value == null) return null;
            if (type.equals(value.getClass())) return value;        
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
                        if (m.getReturnType() != type) throw new NoSuchMethodException("public static " + type + " valueOf(String value)");
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
    
	public static abstract class CallTarget {
		private final Reflector reflector;
        private final boolean complexType;
        private final String name;
        final Class type;
        final Class[] argTypes;
        
        private CallTarget(Reflector reflector, String name, Class type, Class[] argTypes) {
        	this.reflector = reflector;
        	this.type = type;
        	this.name = name;
        	this.argTypes = argTypes;
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
        
        public Reflector getReflector() {
        	return reflector;
        }
        
        public String getName() {
        	return name;
        }
        
        public boolean isComplexType() {
        	return complexType;
        }
        
    	final Object callMethod(Method method, Object obj, Object...args) {
    		for (int i = args.length; --i >=0;) {
    			args[i] = reflector.converter.toType(argTypes[i], args[i]);
    		}

    		try {
    			return method.invoke(obj, args);
    		} catch (Exception e) {
    			throwException(e);
    			return null; //Unreachable code
    		}
    	}
        
        abstract Object call(Object obj, Object...args);
	}
	
    public static class PropertyTarget extends CallTarget {
        private final boolean readable;
        private final boolean writable;
        
        private Method getter;
        private Method setter;

        private PropertyTarget(Reflector reflector, Method m, String name, Class type, boolean isGetter) {
        	super(reflector, name, type, new Class[]{type});
            name = name.length() == 1 ? String.valueOf(Character.toUpperCase(name.charAt(0))) : Character.toUpperCase(name.charAt(0)) + name.substring(1);
            
            if (isGetter) {
            	getter = m;
            	readable = true;
            	
                try {
                    setter = getter.getDeclaringClass().getMethod("set" + name, type);
                    setter.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    setter = null;
                }

                writable = setter != null;
            } else {
            	setter = m;
            	writable = true;
            	
                try {
                    getter = setter.getDeclaringClass().getMethod((type == boolean.class || type == Boolean.class ? "is" : "get") + name);
                    getter.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    getter = null;
                }
                
                readable = getter != null;
            }
        }
        
        public Method getSetter() {
        	return setter;
        }
        
        public Method getGetter() {
        	return getter;
        }
        
        public Class getType() {
        	return type;
        }
        
        public boolean isReadable() {
        	return readable;
        }

        public boolean isWritable() {
        	return writable;
        }

        public void setValue(Object obj, Object value) {
            if (!writable) throw new IllegalArgumentException("property '" + getName() + "' is not writable");
            callMethod(setter, obj, value);
        }
        
        public Object getValue(Object obj) {
            if (!readable) throw new IllegalArgumentException("property '" + getName() + "' is not readable");
            return callMethod(getter, obj);
        }
    	
    	public Object call(Object obj, Object...args) {
    		if (args == null || args.length == 0) {
                if (!readable) throw new IllegalStateException("property '" + getName() + "' is not readable");
                return callMethod(getter, obj);
    		} else if (args.length == 1) {
                if (!writable) throw new IllegalStateException("property '" + getName() + "' is not writable");
                callMethod(setter, obj, args);
                return null;
    		} else {
    			throw new IllegalStateException("number of arguments '" + args.length + "' not appropriate for a setter or getter.");
    		}
    	}
    }
    
    public static class MethodTarget extends CallTarget {
        private Method method;
        private List<Class> paramTypes;

    	private MethodTarget(Reflector reflector, String name, Method method, Class retType, Class[] argTypes) {
    		super(reflector, name, retType, argTypes);
    		this.method = method;
    	}
    	
    	public Method getMethod() {
    		return method;
    	}
    	
    	public Class getReturnType() {
    		return type;
    	}
    	
    	public List<Class> getParameterTypes() {
    		if (paramTypes == null) paramTypes = Collections.unmodifiableList(Arrays.asList(argTypes));
    		return paramTypes;
    	}
    	
    	public Object call(Object obj, Object...args) {
    		callMethod(method, obj, args);
    		return void.class;
    	}
    }
    
    public static void main(String[] args) {
		test();
    }
    
    public static void test() {
    	throwException(new java.io.IOException("Hello"));
    }
    
    private static String getPropertyName(String name) {
        String propName = name.substring(name.charAt(0) == 'i' ? 2 : 3);
        propName = Character.toLowerCase(propName.charAt(0)) + (propName.length() > 1 ? propName.substring(1) : "");
        return propName;
    }
    
    private static ConcurrentMap<Class, Reflector> REFLECTORS = new ConcurrentHashMap<Class, Reflector>(50);
    
    public static Reflector getReflector(Class clazz) {
    	Reflector reflector = REFLECTORS.get(clazz);

    	if (reflector == null) {
    		if (REFLECTORS.size() > 512) REFLECTORS.clear();
    		reflector = new Reflector(clazz);
    		REFLECTORS.putIfAbsent(clazz, reflector);
    	}
    	
    	return reflector;
    }
    
    private Converter converter = DEFAULT_CONVERTER;
    private Map<String, PropertyTarget> propertyTargets = new HashMap<String, PropertyTarget>();
    private Map<String, MethodTarget> methodTargets = new HashMap<String, MethodTarget>();

    private Reflector(Class clazz) {
        for (Method m : clazz.getMethods()) {
            String name = m.getName();
            if (!Modifier.isPublic(m.getModifiers())) continue;
            int len = name.length();
            Class retType = m.getReturnType();
            Class[] argTypes = m.getParameterTypes();
            
            if (((name.startsWith("get") && len > 3) || (name.startsWith("is") && len > 2)) && retType != void.class && argTypes.length == 0) {
                String propName = getPropertyName(name);
                
                if (propertyTargets.get(propName) == null) {
                	propertyTargets.put(propName, new PropertyTarget(this, m, propName, retType, true));
                }
            } else if (name.startsWith("set") && len > 3 && argTypes.length == 1) {
                String propName = getPropertyName(name);
                if (propertyTargets.get(propName) == null) propertyTargets.put(propName, new PropertyTarget(this, m, propName, argTypes[0], false));
            }
            
            methodTargets.put(name, new MethodTarget(this, name, m, retType, argTypes));
        }
    }
    
    public MethodTarget getMethodTarget(String name) {
    	return methodTargets.get(name);
    }
    
    public PropertyTarget getPropertyTarget(String name) {
    	return propertyTargets.get(name);
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
