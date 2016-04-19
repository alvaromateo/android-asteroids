package alvaromateo.asteroids;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class Graphic {
	
	private Drawable d;						// image to draw
	private double posx, posy; 		// position
	private double velx, vely;		// speed
	private int alpha, w;					// angle and speed of rotation
	private int width, height;		// image dimensions
	private double col;						// collision radius
	
	private int laser_distance;

	private View v;
	public static final int MAX_VEL = 20;
	
	public Graphic(View v, Drawable d) {
		this.v = v;
		this.d = d;
		width = d.getIntrinsicWidth();
		height = d.getIntrinsicHeight();
		if (width > height) col = width / 2.0;
		else col = height / 2.0;
	}
	
	public void drawGraphic(Canvas canvas) {
		canvas.save();
		int x = (int) (posx + (width / 2.0));
		int y = (int) (posy + (height / 2.0));
		canvas.rotate((float) alpha, (float) x, (float) y);
		d.setBounds((int) posx, (int) posy, (int) posx + width, (int) posy + height);
		d.draw(canvas);
		canvas.restore();
		
		int inval = (int) (distanceE(0, 0, width, height) / 2.0) + MAX_VEL;
		// send to paint the graphic
		v.invalidate(x - inval, y - inval, x + inval, y + inval);
	}
	
	public void changePos() {
		posx += velx;
		// we check if we are out of the screen
		if (posx < - (width / 2.0)) posx = v.getWidth() - width / 2.0;
		if (posx > (v.getWidth() - (width / 2.0))) posx = - width / 2.0;
		
		posy += vely;
		// we check if we are out of screen
		if (posy < - (height / 2.0)) posy = v.getHeight() - height / 2.0;
		if (posy > (v.getHeight() - (height / 2.0))) posy = - height / 2.0;
		
		// we update the angle
		alpha += w;
	}
	
	public double distance(Graphic g) {
		return distanceE(posx, posy, g.posx, g.posy);
	}
	
	public boolean checkCollision(Graphic g) {
		return (distance(g) < (col + g.col));
	}
	
	public static double distanceE(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}

	public int getLaserDistance() {
		return laser_distance;
	}
	
	public void setLaserDistance(int l) {
		this.laser_distance = l;
	}
	
	public void decrementLaserDistance() {
		this.laser_distance--;
	}
	
	public void incrementLaserDistance() {
		this.laser_distance++;
	}
	
	public Drawable getD() {
		return d;
	}

	public void setD(Drawable d) {
		this.d = d;
	}

	public double getPosx() {
		return posx;
	}

	public void setPosx(double posx) {
		this.posx = posx;
	}

	public double getPosy() {
		return posy;
	}

	public void setPosy(double posy) {
		this.posy = posy;
	}

	public double getVelx() {
		return velx;
	}

	public void setVelx(double velx) {
		this.velx = velx;
	}

	public double getVely() {
		return vely;
	}

	public void setVely(double vely) {
		this.vely = vely;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double getCol() {
		return col;
	}

	public void setCol(double col) {
		this.col = col;
	}

	public View getV() {
		return v;
	}

	public void setV(View v) {
		this.v = v;
	}

}
