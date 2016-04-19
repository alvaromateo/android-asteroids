package alvaromateo.asteroids;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;

public class Asteroids extends Activity {
	
	private Button aboutButton1;
	private Button aboutButton2;
	private Button aboutButton3;
	private Button aboutButton4;
	private MediaPlayer mp;
	
	public static StoreScore storage = new StoreScore(null);
	
	public void launchGame() {
		Intent i = new Intent(this, Game.class);
		startActivityForResult(i, 1234);
	}
	
	public void launchScores(){
		Intent i = new Intent(this, Scores.class);
		startActivity(i);
	}
	
	public void launchAbout(){
		Intent i = new Intent(this,About.class);
		startActivity(i);
	}
	
	/*
	public void showPreferences(){
		SharedPreferences pref = getSharedPreferences("alvaromateo.asteroids_preferences", MODE_PRIVATE);
		String s1 = "Music: " + pref.getBoolean("musica", true) + "\n";
		String s2 = "Graphics: " + pref.getString("graphics", "?");
		String s = s1 + s2;
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_asteroids);
		
		storage = new StoreScore(this);
		
		// Change title font
		Typeface type1 = Typeface.createFromAsset(getAssets(),"fonts/Starjedi.ttf");
		TextView tv1 = (TextView) findViewById(R.id.titol1);
		tv1.setTypeface(type1);
		// Change buttons fonts
		Typeface type2 = Typeface.createFromAsset(getAssets(), "fonts/TELE2.TTF");
		TextView b1 = (TextView) findViewById(R.id.button1);
		b1.setTypeface(type2);
		TextView b2 = (TextView) findViewById(R.id.button2);
		b2.setTypeface(type2);
		TextView b3 = (TextView) findViewById(R.id.button3);
		b3.setTypeface(type2);
		TextView b4 = (TextView) findViewById(R.id.button4);
		b4.setTypeface(type2);
		
		aboutButton1 = (Button) b1;
		OnClickListener l1 = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchGame();
			}
		};
		aboutButton1.setOnClickListener(l1);
		
		aboutButton2 = (Button) b2;
		OnClickListener l2 = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchScores();
			}
		};
		aboutButton2.setOnClickListener(l2);
		
		aboutButton3 = (Button) b3;
		OnClickListener l3 = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchAbout();
			}
		};
		aboutButton3.setOnClickListener(l3);
		
		aboutButton4 = (Button) b4;
		OnClickListener l4 = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				System.exit(0);
			}
		};
		aboutButton4.setOnClickListener(l4);
		
		mp = MediaPlayer.create(this, R.raw.audio2);
		mp.setLooping(true);
		mp.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.asteroids, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case R.id.about:
			launchAbout();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		mp = MediaPlayer.create(this, R.raw.audio2);
		mp.setLooping(true);
		mp.start();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
			int score = data.getExtras().getInt("score");
			if (score > 0) {
				String name = "Me";
				storage.storePoints(score, name);
				launchScores();
			}
		}
	}
}
