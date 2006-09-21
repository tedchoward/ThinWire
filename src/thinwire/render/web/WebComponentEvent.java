/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.EventObject;

/**
 * @author Joshua J. Gertzen
 */
public final class WebComponentEvent extends EventObject {
	private String name;
	private Object value;
	
	public WebComponentEvent(Integer source, String name, Object value) {
	    super(source);
		this.name = name;
		this.value = value;
	}	
	
	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
}
