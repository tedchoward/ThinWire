/**
 * 
 */
package thinwire.ui.layout;

/**
 * @author tsmith6
 * 
 */
public class Rectangle
{
	int x, y, width, height = 0;

	public Rectangle(int x, int y, int width, int height)
	{
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rectangle(Rectangle from)
	{
		this.x = from.x;
		this.y = from.y;
		this.width = from.width;
		this.height = from.height;
	}

	public int getHeight()
	{
		return height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

}
