package com.peterfile.fbtest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int SETTINGS = 2;
	private static final int FRAGMENT_COUNT = SETTINGS + 1;
	private boolean isResumed = false;
	private UiLifecycleHelper uiHelper;
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private MenuItem settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w(TAG, "onCreate");
		printKeyHash();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		
		FragmentManager fm = getSupportFragmentManager();
		fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
		fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
		fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
		
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			Log.w(TAG, "Fragemnt: " + i);
			transaction.hide(fragments[i]);
		}
		transaction.commit();
	}
	
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		Log.w(TAG, "showFragment: " + fragmentIndex);
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < FRAGMENT_COUNT; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
			if (addToBackStack) {
				transaction.addToBackStack(null);
			}
		}
		transaction.commit();
	}
	
	private void onSessionStateChange (Session session, SessionState state, Exception exception) {
	    // Only make changes if the activity is visible
	    if (isResumed) {
	        FragmentManager fm = getSupportFragmentManager();
	        // Get the number of entries in the back stack
	        int backStackSize = fm.getBackStackEntryCount();
	        // Clear the back stack
	        for (int i = 0; i < backStackSize; i++) {
	        	fm.popBackStack();
	        }
	        if (state.isOpened()) {
	            // If the session state is open:
	            // Show the authenticated fragment
	            showFragment(SELECTION, false);
	        } else if (state.isClosed()) {
	            // If the session state is closed:
	            // Show the login fragment
	            showFragment(SPLASH, false);
	        }
	    }
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		    @Override
		    public void call(Session session, SessionState state, Exception exception) {
		        onSessionStateChange(session, state, exception);
		    }
		};
	
	@Override
	protected void onResumeFragments() {
		Log.w(TAG, "onResumeFragments");
		super.onResumeFragments();
		Session session = Session.getActiveSession();
		
	    if (session != null && session.isOpened()) {
	        // if the session is already open,
	        // try to show the selection fragment
	        showFragment(SELECTION, false);
	    } else {
	        // otherwise present the splash screen
	        // and ask the person to login.
	        showFragment(SPLASH, false);
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onResume() {
		Log.w(TAG, "onResume");
		uiHelper.onResume();
		isResumed = true;
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.w(TAG, "onPause");
		uiHelper.onPause();
		isResumed = false;
		super.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (fragments[SELECTION].isVisible()) {
			if (menu.size() == 0) {
				settings = menu.add(R.string.settings);
			}
			return true;
		} else {
			menu.clear();
			settings = null;
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.equals(settings)) {
			showFragment(SETTINGS, true);
			return true;
		}
		return false;
	}

	private void printKeyHash(){
	    // Add code to print out the key hash
	    try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.peterfile.fbtest", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {
	        Log.d("KeyHash:", e.toString());
	    } catch (NoSuchAlgorithmException e) {
	        Log.d("KeyHash:", e.toString());
	    }
	}
}
