package alvaromateo.asteroids;

import java.util.Vector;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

	// ASTEROIDS
	private Vector<Graphic> asteroids;		// vector with the asteroids to be painted
	private int numAsteroids = 2;					// number of initial asteroids
	private int numFragments = 2;					// number of fragments when destroyed
	private Drawable dAsteroid[] = new Drawable[3];
	
	// SPACESHIP
	private Graphic ship;
	private int spinShip;												// direction of the ship
	// ship acceleration when boost activated
	private static float accelerationX;
	private static float accelerationY;
	private static final float ship_acc = 0.5f;	// ship maximum acceleration
	private static final int inc_spin = 5;			// base direction increment
	private static int alphaShip;
	public static int lifes = 3;
	private int shield;
	
	// LASER
	private Vector<Graphic> lasers;
	private static int vel_laser = Graphic.MAX_VEL;
	private Drawable dLaser;
	private boolean shoot;
	public static final int maxShots = 5;
	
	// THREAD AND TIME
	private static GameThread thread;
	private static int PROCESS_PERIOD = 50;
	private long lastProcess;
	private static boolean running = false;
	
	// Control variables
	private int close;
	private int rotation;
	private int cx, cy;
	private double dx, dy;
	protected static double prevXvel, prevYvel;
	protected static boolean pushed;
	private Paint textPaint;
	private int screenWidth, screenHeight;
	public static final int BIG_ASTEROID_POINTS = 50;
	public static final int MEDIUM_ASTEROID_POINTS = 100;
	public static final int LITTLE_ASTEROID_POINTS = 200;
	private Context context;
	private Activity father;
	
	// Game states
	public static final int PLAYING = 0;
	public static final int VICTORY = 1;
	public static final int DEFEAT = 2;
	private int state;
	private View defeatView;
	private int level = 1;
	private int score = 0;
	
	// Methods
	
	public GameView (Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		dAsteroid[0] = context.getResources().getDrawable(R.drawable.asteroid);
		dAsteroid[1] = context.getResources().getDrawable(R.drawable.asteroid2);
		dAsteroid[2] = context.getResources().getDrawable(R.drawable.asteroid3);
		asteroids = new Vector<Graphic>();
		lasers = new Vector<Graphic>();
		
		paintlevel();

		dLaser = context.getResources().getDrawable(R.drawable.laser);
		
		state = 0;
		textPaint = new Paint();
	  textPaint.setColor(Color.LTGRAY);
	  textPaint.setTextSize(25);
	  
		// Thread
		thread = new GameThread();
		thread.start();
		running = true;
	}
	
	public void setFather (Activity father) {
		this.father = father;
	}
	
	private void paintlevel() {
		Drawable dShip;
		
		if (level == 1) putLevel0Asteroids();
		else if (level == 2) putLevel1Asteroids();
		else {
			for (int i = 0; i < numAsteroids; i++) {
				Graphic asteroid = new Graphic(this, dAsteroid[0]);
				asteroid.setVelx(Math.random() * 10 - 5);
				asteroid.setVely(Math.random() * 10 - 5);
				asteroid.setAlpha((int) Math.random() * 360);
				asteroid.setW((int) Math.random() * 10 - 5);
				asteroids.add(asteroid);
			}
			
			int i = 0;
			for (Graphic asteroid: asteroids) {
				boolean same_place = false;
				do {
					same_place = false;
					asteroid.setPosx(Math.random() * (screenWidth - asteroid.getWidth()));
					asteroid.setPosy(Math.random() * (screenHeight - asteroid.getHeight()));
					if (checkInitialShipPos(asteroid, screenWidth, screenHeight)) same_place = true;
					int j = 0;
					while (j < i && !same_place) {
						Graphic prev_asteroid = asteroids.get(j);
						if (asteroid.checkCollision(prev_asteroid)) same_place = true;
						j++;
					}
				} while (same_place);
				i++;
			}
		}
		
		dShip = context.getResources().getDrawable(R.drawable.mini_spaceship);
		ship = new Graphic(this, dShip);
		ship.setVelx(0.0);
		ship.setVely(0.0);
		ship.setAlpha(270);
		ship.setW(0);
		shield = 60;
		alphaShip = ship.getAlpha();
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenWidth = w;
		screenHeight = h;
		cx = (w/2) - ship.getWidth()/2;
		cy = (h/2) - ship.getHeight()/2;
		// Once we know our height and width
		if (level == 1) locateLevel0Asteroids();
		else if (level == 2) locateLevel1Asteroids();
		else {
			int i = 0;
			for (Graphic asteroid: asteroids) {
				boolean same_place = false;
				do {
					same_place = false;
					asteroid.setPosx(Math.random() * (w - asteroid.getWidth()));
					asteroid.setPosy(Math.random() * (h - asteroid.getHeight()));
					if (checkInitialShipPos(asteroid, w, h)) same_place = true;
					int j = 0;
					while (j < i && !same_place) {
						Graphic prev_asteroid = asteroids.get(j);
						if (asteroid.checkCollision(prev_asteroid)) same_place = true;
						j++;
					}
				} while (same_place);
				i++;
			}
		}
		
		ship.setPosx(cx);
		ship.setPosy(cy);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	synchronized protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		for (Graphic asteroid: asteroids) {
			asteroid.drawGraphic(canvas);
		}
		for (Graphic laser: lasers) {
			laser.drawGraphic(canvas);
		}
		ship.drawGraphic(canvas);
		
		draw(canvas, screenWidth, screenHeight);
	  
		if (state == VICTORY) {
			state = 0;
			level++;
			numAsteroids = level;
			if (level > 9) numAsteroids = 10;

			running = false;
			paintlevel();
			
			ship.setPosx(cx);
			ship.setPosy(cy);
			ship.setVelx(0.0);
			ship.setVely(0.0);
			ship.setAlpha(270);
			ship.setW(0);
			alphaShip = ship.getAlpha();
			shield = 60;
			accelerationX = 0;
			accelerationY = 0;
			
			running = true;
		}
		if (state == DEFEAT) {
			defeatView.setVisibility(VISIBLE);
			lifes = 3;
			running = false;

			if (father != null) {
				Bundle bundle = new Bundle();
				bundle.putInt("score", score);
				Intent intent = new Intent();
				intent.putExtras(bundle);
				father.setResult(Activity.RESULT_OK, intent);
			}
		}
	}
	
	public void draw(Canvas canvas, float screenWidth, float screenHeight) {  
    textPaint.setTextAlign(Paint.Align.LEFT);
    canvas.drawText("Score: " + score, 1, 19, textPaint);
  
    textPaint.setTextAlign(Paint.Align.RIGHT);
    canvas.drawText("Lifes: " + lifes, screenWidth-1, 19, textPaint);
    canvas.drawText("Level: " + level, screenWidth-1, screenHeight-2, textPaint);
  }
	
	private boolean checkInitialShipPos (Graphic g, int width, int height) {
		double x = g.getPosx();
		double y = g.getPosy();
		int asteroid_width = g.getWidth();
		int asteroid_height = g.getHeight();
		double centerx = width / 2.0d;
		double centery = height / 2.0d;
		if (x > (centerx - asteroid_width * 2) && x < (centerx + asteroid_width * 2) && 
				y > (centery - asteroid_height * 2) && y < (centery + asteroid_height * 2))
					return true;
		return false;
	}
	
	private boolean checkBreak (double nIncX, double nIncY) {
		int sx = (int) Math.signum(nIncX);
		int prev_sx = (int) Math.signum(prevXvel);
		int sy = (int) Math.signum(nIncY);
		int prev_sy = (int) Math.signum(prevYvel);
		if (!pushed) {
			if (sx != prev_sx || sy != prev_sy) {
				return true;
			}
		}
		return false;
	}
	
	protected void updatePhysics() {
		long now = System.currentTimeMillis();
		if (lastProcess + PROCESS_PERIOD > now) return;
		double delay = (now - lastProcess) / PROCESS_PERIOD;
		
		// Ship updates
		if (shield > 0) shield--;
		else shield = 0;
		ship.setAlpha((int) (ship.getAlpha() + spinShip * delay));
		if (ship.getAlpha() > 360) ship.setAlpha(ship.getAlpha() - 360);
		else if (ship.getAlpha() < 0) ship.setAlpha(360 + ship.getAlpha());
		alphaShip = ship.getAlpha();
		
		double nIncX, nIncY;
		nIncX = ship.getVelx() + accelerationX * delay;
		nIncY = ship.getVely() + accelerationY * delay;
		
		if (Graphic.distanceE(0, 0, nIncX, nIncY) <= Graphic.MAX_VEL - 6) {
			if (checkBreak (nIncX, nIncY)) {
				ship.setVelx(0);
				ship.setVely(0);
				accelerationX = 0;
				accelerationY = 0;
			} else {
				ship.setVelx(nIncX);
				ship.setVely(nIncY);
			}
			prevXvel = ship.getVelx();
			prevYvel = ship.getVely();
		}
		ship.changePos();
		
		if (rotation != 0) {
			if (close == 0) {
				spinShip = 0;
				rotation = 0;
			}
			else if (close < 5) {
				spinShip = rotation;
				close -= Math.abs(spinShip);
			}
			else close -= Math.abs(spinShip);
		} else {
			spinShip = 0;
		}
		
		// Asteroid updates
		Vector<Graphic> toRemoveAsteroid = new Vector<Graphic>();
		Vector<Graphic> toAddAsteroid = new Vector<Graphic>();
		for (Graphic asteroid: asteroids) {
			if (ship.checkCollision(asteroid) && shield == 0) {
				lifes--;
				if (lifes <= 0) state = DEFEAT;
				ship.setPosx(cx);
				ship.setPosy(cy);
				ship.setVelx(0.0);
				ship.setVely(0.0);
				ship.setAlpha(270);
				ship.setW(0);
				alphaShip = ship.getAlpha();
				shield = 60;
				accelerationX = 0;
				accelerationY = 0;
				toRemoveAsteroid.add(asteroid);
				toAddAsteroid = destroyAsteroid(asteroid);
			}
			asteroid.changePos();
		}		
		
		// Missile updates
		Vector<Graphic> toRemove = new Vector<Graphic>();
		Vector<Graphic> auxAsteroids = new Vector<Graphic>();
		for (Graphic laser: lasers) {
			laser.changePos();
			laser.decrementLaserDistance();
			if (laser.getLaserDistance() < 0) toRemove.add(laser);
			else {
				for (Graphic asteroid: asteroids) {
					if (laser.checkCollision(asteroid)) {
						toRemoveAsteroid.add(asteroid);
						toRemove.add(laser);
						auxAsteroids = destroyAsteroid(asteroid);
						toAddAsteroid.addAll(auxAsteroids);
					}
				}
			}
		}
		lasers.removeAll(toRemove);
		asteroids.removeAll(toRemoveAsteroid);
		asteroids.addAll(toAddAsteroid);
		
		if (asteroids.isEmpty()) state = VICTORY;
		lastProcess = now;
	}
	
	synchronized private Vector<Graphic> destroyAsteroid (Graphic asteroid) {
		int s;
		Vector<Graphic> aux = new Vector<Graphic>();
		if (asteroid.getD() != dAsteroid[2]) {
			if (asteroid.getD() == dAsteroid[1]) {
				s = 2;
				score += MEDIUM_ASTEROID_POINTS;
			} else {
				s = 1;
				score += BIG_ASTEROID_POINTS;
			}
			for (int n = 0; n < numFragments; n++) {
				Graphic a = new Graphic (this, dAsteroid[s]);
				a.setPosx(asteroid.getPosx());
				a.setPosy(asteroid.getPosy());
				a.setVelx(Math.random() * 10 - 5);
				a.setVely(Math.random() * 10 - 5);
				a.setAlpha((int) Math.random() * 360);
				a.setW((int) Math.random() * 10 - 5);
				aux.add(a);
			}
		} else score += LITTLE_ASTEROID_POINTS;
		return aux;
	}
	
	private void shootLaser () {
		if (lasers.size() < 5) {
			Graphic laser = new Graphic(this, dLaser);
			laser.setPosx(ship.getPosx() + ship.getWidth()/2 - laser.getWidth()/2);
			laser.setPosy(ship.getPosy() + ship.getHeight()/2 - laser.getHeight()/2);
			laser.setAlpha(ship.getAlpha());
			laser.setVelx(Math.cos(Math.toRadians(laser.getAlpha())) * vel_laser);
			laser.setVely(Math.sin(Math.toRadians(laser.getAlpha())) * vel_laser);
			int laserDist = (int) Math.min(this.getWidth() / Math.abs(laser.getVelx()), this.getHeight() / Math.abs(laser.getVely())) - 15;
			laser.setLaserDistance(laserDist);
			lasers.add(laser);
		}
	}
	
	// Ship control
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent (MotionEvent event) {
		super.onTouchEvent(event);
		double x = event.getX();
		double y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			shoot = true;
			rotateShip(x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			if ((x - dx) < 8 && (y - dy) < 8) {
				rotateShip(x, y);
			}
			shoot = false;
			break;
		case MotionEvent.ACTION_UP:
			spinShip = 0;
			rotation = 0;
			close = 0;
			if (shoot) shootLaser();
			shoot = false;
			break;
		}
		dx = x;
		dy = y;
		return true;
	}
	
	private int finger_angle (double rx, double ry) {
		double module = Math.sqrt(rx*rx + ry*ry);
		rx = rx / module;
		ry = ry / module;
		int beta = (int) Math.toDegrees(Math.atan2(ry, rx));
		if (beta < 0) beta = 360 + beta;
		return beta;
	}
	
	/* x, y are the coordinates of the point were the finger is pressing the screen */
	private void rotateShip (double x, double y) {
		double rx, ry, aux;
		rx = x - (ship.getPosx() + (double) ship.getWidth()/2.0);
		ry = y - (ship.getPosy() + (double) ship.getHeight()/2.0);
		aux = (Math.cos(Math.toRadians(ship.getAlpha())) * ry) - (Math.sin(Math.toRadians(ship.getAlpha())) * rx);
		
		rotation = (int) Math.signum(aux);
		int beta = finger_angle(rx,ry);
		close = Math.abs(ship.getAlpha() - beta);
		
		// close can't be bigger than 180 because we have to rotate following the shortest path
		if (close > 180) {
			if (ship.getAlpha() > 180) close = (360 - ship.getAlpha()) + beta;
			else close = (360 - beta) + ship.getAlpha();
		}
		/* if close == 0 we don't rotate because we are already in the good direction
			 if close < 5 it means we are near to the right direction and we rotate more slowly to be more accurate
			 else we rotate faster to get closer to the direction we want 
		*/
		if (close == 0) spinShip = 0;
		else if (close < 5) spinShip = rotation;
		else spinShip = ((int) rotation) * inc_spin;
	}
	
	// Levels
	
	// Vertical level
	private void putLevel0Asteroids() {
		for (int i = 0; i < 5; i++) {
			Graphic asteroid = new Graphic(this, dAsteroid[0]);
			asteroid.setVelx(0.0);
			asteroid.setVely(5.0);
			asteroid.setAlpha((int) Math.random() * 360);
			asteroid.setW((int) Math.random() * 10 - 5);
			asteroids.add(asteroid);
		}
		locateLevel0Asteroids();
	}
	
	private void locateLevel0Asteroids() {
		double pos = screenWidth / 5;
		int j = 1;
		for (Graphic asteroid: asteroids) {
			asteroid.setPosx(j * pos);
			asteroid.setPosy(asteroid.getWidth()/2);
			j++;
		}
	}
	
	// Centered level
	private void putLevel1Asteroids() {
		double velsx[] = new double[4];
		double velsy[] = new double[4];
		velsx[0] = velsx[3] = 4.0;
		velsx[1] = velsx[2] = -4.0;
		velsy[0] = velsy[1] = -4.0;
		velsy[2] = velsy[3] = 4.0;
		
		for (int i = 0; i < 4; i++) {
			Graphic asteroid = new Graphic(this, dAsteroid[0]);
			asteroid.setVelx(velsx[i]);
			asteroid.setVely(velsy[i]);
			asteroid.setAlpha((int) Math.random() * 360);
			asteroid.setW((int) Math.random() * 10 - 5);
			asteroids.add(asteroid);
		}
		locateLevel1Asteroids();
	}
	
	private void locateLevel1Asteroids() {
		double posx[] = new double[4];
		double posy[] = new double[4];
		posx[0] = posx[3] = 0.0;
		posx[1] = posx[2] = screenWidth;
		posy[0] = posy[1] = 0.0;
		posy[2] = posy[3] = screenHeight;
		
		int j = 0;
		for (Graphic asteroid: asteroids) {
			asteroid.setPosx(posx[j]);
			asteroid.setPosy(posy[j]);
			j++;
		}
	}
	
	// Setters & Getters
	
	public static float getMaxAcceleration () {
		return ship_acc;
	}
	
	public static float getAccelerationX () {
		return accelerationX;
	}
	
	public static float getAccelerationY () {
		return accelerationY;
	}
	
	public static void setAccelerationX (float ax) {
		accelerationX = ax;
	}
	
	public static void setAccelerationY (float ay) {
		accelerationY = ay;
	}
	
	public static int getAlphaShip () {
		return alphaShip;
	}
	
	public static double getPrevVelx () {
		return prevXvel;
	}
	
	public static double getPrevVely () {
		return prevYvel;
	}
	
	public static GameThread getThread () {
		return thread;
	}
	
	public static boolean getRunning () {
		return running;
	}
	
	public static void setRunning (boolean run) {
		running = run;
	}
	
	public void setDefeatView (View v) {
		defeatView = v;
	}

	
	class GameThread extends Thread {	
		@Override
		public void run () {
			while (running) {
				updatePhysics();
			}
		}
	}

}
