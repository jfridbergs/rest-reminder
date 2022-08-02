package com.colormindapps.rest_reminder_alarm;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;


public class ManualActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

	WebView manual;
	ConstraintLayout rootLayout;
	float progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual);
		
		manual = findViewById(R.id.manual);
		manual.loadUrl("file:///android_asset/html/manual.html");
		
		if(savedInstanceState!=null){
			progress = savedInstanceState.getFloat("currentProgress");

		}

		NavigationView navigationView =findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.manual_activity_title));
		setSupportActionBar(toolbar);

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		rootLayout = findViewById(R.id.frameLayout);
		rootLayout.setPadding(0,0,0,0);

		
		manual.setWebViewClient(new WebViewClient() {

			Boolean loadingFinished = false;

			public void onPageStarted(WebView view, String url, Bitmap favicon){
				super.onPageStarted(view,url,favicon);

				loadingFinished = false;
			}


			public void onPageFinished(WebView view, String url) {
				   super.onPageFinished(view, url);
				if(!loadingFinished){

					// Delay the scrollTo to make it work
					view.postDelayed(() -> {
						float webviewsize = manual.getContentHeight() - manual.getTop();
						float positionInWV = webviewsize * progress;

						int positionY =  (int)positionInWV;
						manual.scrollTo(0, positionY);
					}, 300);
					loadingFinished = true;
				}

		            
			    }
			});
	}


	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.timer) {
			Intent ih = new Intent(this, MainActivity.class);
			startActivity(ih);
		} else if (item.getItemId() == R.id.menu_settings_x) {
			Intent i = new Intent(this, PreferenceXActivity.class);
			startActivity(i);
		}
		else if (item.getItemId() == R.id.menu_session_list){
			Intent i = new Intent(this, CalendarActivity.class);
			startActivity(i);
		}
		else if (item.getItemId() == R.id.menu_open_stats){
			Intent i = new Intent(this, StatsActivity.class);
			startActivity(i);
		}
		else if (item.getItemId() == R.id.menu_feedback){
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "colormindapps@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			startActivity(Intent.createChooser(Email, "Send Feedback:"));
		}
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
	

	
	public void backToTop(View v){
		manual.scrollTo(0,0);
	}
	
	private float calculateProgression(WebView content) {
	    float contentHeight = content.getContentHeight();
	    float currentScrollPosition = content.getScrollY();
		return (currentScrollPosition) / contentHeight;

	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat("currentProgress", calculateProgression(manual));
        super.onSaveInstanceState(outState);
    }

}
