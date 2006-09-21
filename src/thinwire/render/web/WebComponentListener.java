/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.EventListener;

/**
 * @author Joshua J. Gertzen
 */
interface WebComponentListener extends EventListener {
	void componentChange(WebComponentEvent event);
}
