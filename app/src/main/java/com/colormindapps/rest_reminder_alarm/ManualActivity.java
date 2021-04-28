package com.colormindapps.rest_reminder_alarm;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;


public class ManualActivity extends AppCompatActivity {

	WebView manual;
	RelativeLayout rootLayout;
	float progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual);
		
		manual = (WebView) findViewById(R.id.manual);
		manual.loadUrl("file:///android_asset/html/manual.html");
		
		if(savedInstanceState!=null){
			progress = savedInstanceState.getFloat("currentProgress");

		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.manualToolbar);
		setSupportActionBar(toolbar);
		if(getSupportActionBar()!=null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		rootLayout = (RelativeLayout) findViewById(R.id.manual_root_layout);
		toolbar.setVisibility(View.GONE);
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

					view.postDelayed(new Runnable() {
						@Override
						public void run() {
							float webviewsize = manual.getContentHeight() - manual.getTop();
							float positionInWV = webviewsize * progress;

							int positionY =  (int)positionInWV;
							manual.scrollTo(0, positionY);
						}
						// Delay the scrollTo to make it work
					}, 300);
					loadingFinished = true;
				}

		            
			    }
			});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_help, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		if (item.getItemId()==R.id.menu_settings_x){
				Intent i = new Intent(this, PreferenceXActivity.class);
				startActivity(i);
				return true;
		} else if (item.getItemId() == R.id.menu_feedback){
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "colormindapps@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			startActivity(Intent.createChooser(Email, "Send Feedback:"));
			return true;
		}
		return super.onOptionsItemSelected(item);
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
