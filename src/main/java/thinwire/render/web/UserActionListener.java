package thinwire.render.web;

/**
 * This interface will be called when thinwire starts processing a session request from the 
 * thread at does the actual rendering and event processing. Note this is not the 
 * servlet request/responce thread.<br/>
 * Very usefull if you are implementing the "open sesion in view" pattern
 * 
 * @author richard.schmidt
 */ 
public interface UserActionListener {
	
	public void startProcessing();
	public void finishedProcessing();

}
