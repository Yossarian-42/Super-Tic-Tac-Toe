import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.io.Serializable;

public class MyButton extends JButton implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image img;
	
	public MyButton(Image img){
		Color c = new Color(32,32,32);
		Border thickBorder = new LineBorder(c, 3);
		this.img=img;
		this.setBorder(thickBorder);
	}
	
	public Image getImg() {
		return img;
	}
	
	public void setImg(Image img) {
		this.img = img;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(),null);
	}
}
