/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface Window extends Container<Component> {
    public static final String PROPERTY_MENU = "menu";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_WAIT_FOR_WINDOW = "waitForWindow";

    /**
     * Gets the title of the window.
     * @return the title of the window.
     */
    String getTitle();

    /**
     * Sets the title of the window.
     * @param title
     */
    void setTitle(String title);

    Menu getMenu();

    /**
     * Sets the main menubar for the window.
     * @param menu
     */
    void setMenu(Menu menu);

    /**
     * Makes the window visible.
     * @param visible (Default = false)
     */
    void setVisible(boolean visible);

    boolean isWaitForWindow();

    /**
     * Sets whether the script execution pauses until the window is closed or not.
     * @param waitForWindow (Default = true)
     */
    void setWaitForWindow(boolean waitForWindow);

}