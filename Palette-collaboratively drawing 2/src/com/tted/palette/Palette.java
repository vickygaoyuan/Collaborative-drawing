package com.tted.palette;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Palette extends Activity {
	PaletteView paletteView=null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
       requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        setContentView(R.layout.main);
        
        paletteView=(PaletteView)findViewById(R.id.palette);
        
        ArrayList<HashMap<String, Object>> users = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 4; i++) {
            HashMap<String, Object> user = new HashMap<String, Object>();
            int picId=getResources().getIdentifier("pic"+(i+1), "drawable", "com.tted.palette");
            user.put("img", picId);
            user.put("imgname", "picture_"+(i+1));
            users.add(user);
        }

  
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return paletteView.onTouchEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	
	public boolean save(){
	
		System.out.print("save");
		paletteView.save();
		return true;
	}
	
	public boolean open(){
		
		System.out.print("open");
		paletteView.open();
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.open:
        	Toast.makeText(this,"open", Toast.LENGTH_SHORT).show();
        	open();
        	return true;
        
        case R.id.save:
        	Toast.makeText(this,"save", Toast.LENGTH_SHORT).show();
        	save();
        	return true;
        	
        case R.id.close:
        	this.finish();
        	return true;
        	
        case R.id.share:
        	//Toast.makeText(this,"share", Toast.LENGTH_SHORT).show();
        	shareImage();
        	return true;
        	
        
        default:
        	return super.onOptionsItemSelected(item);
        }
    }

	private void shareImage() {
		// TODO Auto-generated method stub
		Intent share = new Intent(Intent.ACTION_SEND);
		 
        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("image/png");
 
        // Make sure you put example png image named myImage.png in your
        // directory
        String imagePath = Environment.getExternalStorageDirectory()
                + "/save.png";
 
        File imageFileToShare = new File(imagePath);
 
        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);
 
        startActivity(Intent.createChooser(share, "Share Image!"));
		
	}




}