package alvaromateo.asteroids;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.view.MotionEvent;

public class Game extends Activity {
	
	private Button propButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		// Initialize victory and defeat screens
		GameView gv;
		gv = (GameView) findViewById(R.id.GameView);
		gv.setDefeatView(findViewById(R.id.defeat));
		gv.setFather(this);
		
		propButton = (Button) findViewById(R.id.propulsionButton);
		findViewById(R.id.propulsionButton).bringToFront();
		OnTouchListener o1 = new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch (View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					float a = GameView.getMaxAcceleration();
					float ax = GameView.getAccelerationX();
					float ay = GameView.getAccelerationY();
					int alphaShip = GameView.getAlphaShip();
					ax = (float) (a * Math.cos(Math.toRadians(alphaShip)));
					ay = (float) (a * Math.sin(Math.toRadians(alphaShip)));
					GameView.setAccelerationX(ax);
					GameView.setAccelerationY(ay);
					GameView.pushed = true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float ax = (float) GameView.getPrevVelx();
					float ay = (float) GameView.getPrevVely();
					GameView.setAccelerationX(-ax * 0.05f);
					GameView.setAccelerationY(-ay * 0.05f);
					GameView.pushed = false;
				}
				return true;
			}
		};
		propButton.setOnTouchListener(o1);
	}
	
	@Override
	protected void onDestroy () {
		GameView.setRunning(false);
		try {
			GameView.getThread().join();
		} catch (InterruptedException e) {}
		setResult(RESULT_OK);
		finish();
		super.onDestroy();
	}
	
	@Override
	protected void onPause () {
		GameView.setRunning(false);
		try {
			GameView.getThread().join();
		} catch (InterruptedException e) {}
		setResult(RESULT_OK);
		finish();
		super.onPause();
	}
	
	/*
	@Override
	protected void onResume () {
		super.onResume();
		mp2 = MediaPlayer.create(this, R.raw.audio);
		mp2.setLooping(true);
		mp2.start();
	}
	*/
}
