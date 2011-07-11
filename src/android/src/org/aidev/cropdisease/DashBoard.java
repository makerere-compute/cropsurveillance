package org.aidev.cropdisease;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DashBoard extends Activity {
	Button mosaicdetect,whiteflycount,Snapwhiteflycount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		mosaicdetect=(Button)findViewById(R.id.MosaicDetect);
		whiteflycount=(Button)findViewById(R.id.WhiteFlyCount);
		Snapwhiteflycount=(Button)findViewById(R.id.SnapWhiteFlyCount);
		
		mosaicdetect.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
			
				startActivity(new Intent("MosaicDetection"));
			}
		});
		
		whiteflycount.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
			
				startActivity(new Intent("WhiteFlyCount"));
			}
		});
		
Snapwhiteflycount.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
			
				startActivity(new Intent("SnapWhiteFlyCount"));
			}
		});

	}

	
}
