/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.EventListener;

/**
 * @author David J. Vriend
 */
public interface UserActionListener extends EventListener {
	void actionReceived(UserActionEvent event);
	void stop();
}
