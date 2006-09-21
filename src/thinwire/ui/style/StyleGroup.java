/*
 * Created on Jul 5, 2006
  */
package thinwire.ui.style;

interface StyleGroup<T> {
    void copy(T style);
    Object getProperty(String propertyName);
    void setProperty(String propertyName, Object value);
    Object getPropertyDefault(String propertyName);
    Object getParent();
}
