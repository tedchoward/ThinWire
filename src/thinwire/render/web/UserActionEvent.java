/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
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
