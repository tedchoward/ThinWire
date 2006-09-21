/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface ImageComponent extends Component {
    public static final String PROPERTY_IMAGE = "image";
    public String getImage();
    public void setImage(String image);
}