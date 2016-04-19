package alvaromateo.asteroids;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import android.content.Context;
import android.util.Log;

public class StoreScore {
	
	private static String FILE = "scores.txt";
	private Context context;
	
	public StoreScore (Context context) {
		this.context = context;
	}
	
	public void storePoints (int points, String name) {
		try {
			FileOutputStream fos = context.openFileOutput(FILE, Context.MODE_APPEND);
			String text = Integer.toString(points) + " " + name + "\n";
			fos.write(text.getBytes());
			fos.close();
		} catch (Exception e) {
			Log.e("Asteroids", e.getMessage(), e);
		}
	}
	
	public Vector<String> scoresList (int n) {
		Vector<String> result = new Vector<String>();
		try {
			FileInputStream fis = context.openFileInput(FILE);
			String text = "";
			int i;
			int j = 0;
			do {
				i = fis.read();
				if (i != '\n') {
					text = text + (char) i;
				}
				else {
					result.add(text);
					text = "";
					j++;
				}
			} while (i > 0 && j < n);
			fis.close();
		} catch (Exception e) {
			Log.e("Asteroids", e.getMessage(), e);
		}
		return result;
	}
}
