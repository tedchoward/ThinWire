/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.EventObject;

/**
 * @author David J. Vriend
 */
public final class UserActionEvent extends EventObject {
	private String name;
	private Object value;
	private long delay;

	public UserActionEvent(Integer source, String name, Object value) {
		super(source);
		this.name = name;
		this.value = value;
		this.delay = 0;
	}

	public UserActionEvent(WebComponentEvent evt) {
		super(evt.getSource());
		this.name = evt.getName();
		this.value = evt.getValue();
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
	public long getDelay(){
		return this.delay;
	}
	
	public void setDelay(long delay){
		this.delay = delay;
	}

	public String toString() {
		return "[delay=" + this.delay + ",source=" + this.source + ",name=" + this.name + ",value="
				+ this.valueToString() + "]";
	}
	
	private String valueToString(){
		String result = "";
		
		if (this.value instanceof String[]){
			String[] arr = (String[])this.value;
			
			for (int i = 0; i < arr.length; i++){
				result += arr[i] + "@@@";
			}
		} else {
			result = this.value.toString();
		}
		return result;
	}

	public String toXML() {
		return "<event delay=\"" + this.delay + "\"" 
			+ " source=\"" + this.source + "\"" 
			+ " name=\"" + this.name + "\""
			+ " value=\"" + this.valueToString() + "\""
			+ " type=\"" + this.value.getClass().getName()
			+ "\"/>";
	}
}
