/*
 * Created on Jul 5, 2006
  */
package thinwire.ui.style;

interface StyleGroup<T> {
    void copy(T style);
    Object getValue(String propertyName);
    Object getDefaultValue(String propertyName);
    Object getParent();
}
