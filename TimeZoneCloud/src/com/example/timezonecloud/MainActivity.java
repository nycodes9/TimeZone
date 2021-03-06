package com.example.timezonecloud;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

	private static final String TAG = MainActivity.class.getSimpleName();
	Map<String, ?> savedPrefsMap;
	ListView dateTimeLV;
	DataListAdapter dataAdapter;
	private static final String SPFileName = "com.example.FILE_KEY"; 
	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerReceiver(mTZChangedReceiver, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
		
		dateTimeLV = (ListView) findViewById(R.id.dateTimeLV);
		dataAdapter = new DataListAdapter();
		dateTimeLV.setAdapter(dataAdapter);
		
		getSharedPreferences(SPFileName, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
		
		updateContent(getSharedPreferences(SPFileName, Context.MODE_PRIVATE));
		
		/*if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	private void updateContent(SharedPreferences sharedPref) {
		savedPrefsMap = sharedPref.getAll();
		
		dataAdapter.notifyDataSetChanged();
		dateTimeLV.invalidateViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		
		if (id == R.id.action_add) {
			Editor spEditor = getSharedPreferences(SPFileName, Context.MODE_PRIVATE).edit();
			
			Time t = new Time();
			t.setToNow();
			
			sdf.setTimeZone(TimeZone.getDefault());
			spEditor.putLong(sdf.format(new Date(t.toMillis(false))), t.toMillis(false))
					.apply();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mTZChangedReceiver);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}
	
	class DataListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return savedPrefsMap == null ? 0 : savedPrefsMap.size();
		}

		@Override
		public Entry<String, ?> getItem(int position) {
			return (Entry<String, ?>) savedPrefsMap.entrySet().toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_list_item, null);
			}
			
			TextView savedTimeTV = (TextView) convertView.findViewById(R.id.savedTimeTV);
			TextView deviceTimeTV = (TextView) convertView.findViewById(R.id.deviceTimeTV);
			TextView backendTimeTV = (TextView) convertView.findViewById(R.id.bkgTimeTV);
			
			
			Entry<String, ?> entry = getItem(position);
			
			savedTimeTV.setText(entry.getKey()); 
			
//			TimeZone.getTimeZone(Time.getCurrentTimezone());
			sdf.setTimeZone(TimeZone.getDefault());
			deviceTimeTV.setText(sdf.format(new Date((Long) entry.getValue())));
			
			backendTimeTV.setText(" " + entry.getValue());
			
			return convertView;
		}
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateContent(sharedPreferences);
	}
	
	private final BroadcastReceiver mTZChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				updateContent(getSharedPreferences(SPFileName, Context.MODE_PRIVATE));
			}
		}
	};
	
	
	// full screen code programatically
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
			}
		}
	}
	
	public void videotest() {
		if (mShowVideo 
				&& android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {	
			
			setContentView(R.layout.layout_fullscreen_video);

			mOnboardingVV = (VideoView) findViewById(R.id.fullscreenVideoVV);
			Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.onboarding6);
			mOnboardingVV.setVideoURI(video);
			mOnboardingVV.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					
					mIsVideoComplete = true;
					preInit();
				}
			});
			mOnboardingVV.start();
			
			/**
			 * Skip imageview click stops video playback and in turn calls OnCompletionListener.onCompletion()
			 */
			mskipVideoIV = (ImageView) findViewById(R.id.fullscreenVideoSkipIV);
			mskipVideoIV.setVisibility(View.INVISIBLE);
			mskipVideoIV.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					// stop playback and go on with initFinished
					mOnboardingVV.stopPlayback();
					mIsVideoComplete = true;
					preInit();
					
				}
			});
			
//			showVideoSkipControl(true);
			mShowSkipTimer.start();
	}
	
	private CountDownTimer mShowSkipTimer = new CountDownTimer(mVideoPlaybackThreshold, 1000) {
		
		private boolean canShowSkip = false;
		
		@Override
		public void onTick(long millisUntilFinished) {
			/**
			 * if task is complete and show skip is true then end timer and show skip UI
			 */
			if (canShowSkip && mIsInitTaskComplete) {
				mShowSkipTimer.cancel();
				mskipVideoIV.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onFinish() {
			// can show skip is true iff mVideoPlaybackThreshold has elapsed
			canShowSkip = true;
			// if init task not complete then start the timer again
			if (!mIsInitTaskComplete) {
				mShowSkipTimer.start();
			} else {
				mskipVideoIV.setVisibility(View.VISIBLE);
			}
		}
	};
	
}
