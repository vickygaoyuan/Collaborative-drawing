/*It is a collaborative drawing app. The user can only draw once (press and release once) and then pass 
the image to the next to continue. They can pass the image through different social media app and also 
share the final work to social media.

The structure of the program:
1. Layout setting
2. Define drawing function
3. Detect whether the desired area is touched
4. Set functions for each button 

Copyright Yuan Gao (Vicky)*/

package com.tted.palette;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ShareActionProvider;

public class PaletteView extends SurfaceView implements Runnable,
		SurfaceHolder.Callback {
    boolean drawtime = true;
	public Paint mPaint = null;
	// background setting
	private int bgBitmapX = 40;
	private int bgBitmapY = 40;
	private int bgBitmapHeight = 1000;
	private int bgBitmapWidth = 1000;
	// pen setting
	private int toolsX = 2000;
	private int toolsY = 1800;
	private int toolsSide = 100;
	private int toolsHeightNum = 1;
	private int toolsWidthNum = 7;
	// size of pen setting
	private int sizeX = 9000;
	private int sizeY = bgBitmapHeight + 300;
	private int sizeWidth = toolsSide;
	private int sizeHeight = toolsSide;
	private int sizeNum = 3;
	// chosen color
	private int colorSelectedX = 40;
	private int colorSelectedY = bgBitmapHeight + 350;
	private int colorSelectedSide = 300;
	// color board
	private int colorX = 440;
	private int colorY = colorSelectedY;
	private int colorSide = 150;
	private int colorWidthNum = 4;
	private int colorHeightNum = 2;
	// back
	private int backX = 340;
	private int backY = bgBitmapHeight + 80;
	private int backWidth = colorSide;
	private int backHeight = colorSide;
	// forward
	private int forwardX = backX + 300;
	private int forwardY = backY;
	private int forwardWidth = backWidth;
	private int forwardHeight = backHeight;
	// current
	private int currentPaintTool = 0; // 0~6
	private int currentColor = Color.LTGRAY;	
	private int currentSize = 10; // 1,3,5
	private int currentPaintIndex = -1;
	private boolean isBackPressed = false;
	private boolean isForwardPressed = false;
	// background color
	private int selectedCellColor = 0xff78B8BF;
	// save action
	private ArrayList<Action> actionList = null;
	// current action
	private Action curAction = null;
	// action ends
	boolean mLoop = true;
	SurfaceHolder mSurfaceHolder = null;
	// background pic
	Bitmap bgBitmap = null;
	// temporary canvas
	Bitmap newbit=null;
	
	Bitmap savebit = null;
	
	
	private Canvas canvas = null;

	public PaletteView(Context context, AttributeSet arr) {
		super(context, arr);

		mPaint = new Paint();
		actionList = new ArrayList<Action>();
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		this.setFocusable(true);
		mLoop = true;

		bgBitmap = ((BitmapDrawable) (getResources()
				.getDrawable(R.drawable.pic1))).getBitmap();
		newbit = Bitmap.createBitmap(bgBitmapWidth, bgBitmapHeight,
				Config.ARGB_4444);
		new Thread(this).start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int antion = event.getAction();
		if (antion == MotionEvent.ACTION_CANCEL) {
			return false;
		}

		float touchX = event.getX();
		float touchY = event.getY();

		// tap
		if (antion == MotionEvent.ACTION_DOWN) {
			// whether in main drawing area
			if (testTouchMainPallent(touchX, touchY)) {
				setCurAction(getRealX(touchX), getRealY(touchY));
				clearSpareAction();
			}
		    //whether on desired place
			testTouchToolsPanel(touchX, touchY);
			
			testTouchSizePanel(touchX, touchY);
			
			testTouchColorPanel(touchX, touchY);
	
			testTouchButton(touchX, touchY);
		}
		// draw
		if (antion == MotionEvent.ACTION_MOVE) {
			if (curAction != null) {
				curAction.move(getRealX(touchX), getRealY(touchY));
			}
		}
		// release
		if (antion == MotionEvent.ACTION_UP) {
			if (curAction != null) {
				curAction.move(getRealX(touchX), getRealY(touchY));
				if (actionList.size() < 1){
					actionList.add(curAction);
					currentPaintIndex++;
				}
				curAction = null;	
			}
			
			isBackPressed = false;
			isForwardPressed = false;
			System.out.print("Actionlist's size = " + actionList.size());
			//tell true or false of drawtime variable  
			if (testTouchMainPallent(touchX, touchY)){
				drawtime = false;
			}
		}
		return super.onTouchEvent(event);
	}

	// when drawing
	protected void Draw() {
		canvas = mSurfaceHolder.lockCanvas();
		if (mSurfaceHolder == null || canvas == null) {
			return;
		}
        
		
		// background color
		canvas.drawColor(Color.WHITE);
		// draw main pallent
		drawMainPallent();
		// draw tools panel
		drawToolsPanel();

		mSurfaceHolder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void run() {
		while (mLoop) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (mSurfaceHolder) {
				Draw();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		new Thread(this).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mLoop = false;
	}

	// whether on button
	public boolean testTouchButton(float x, float y) {
		if (x > backX + 2 && y > backY + 2 && x < backX + backWidth - 2
				&& y < backY + backHeight - 2) {
			if (isBackPressed) {
				return false;
			}
			if (currentPaintIndex >= 0) {
				currentPaintIndex--;
			}
			isBackPressed = true;
			return true;
		}
		if (x > forwardX + 2 && y > forwardY + 2
				&& x < forwardX + forwardWidth - 2
				&& y < forwardY + forwardHeight - 2) {
			if (isForwardPressed) {
				return false;
			}
			if ((currentPaintIndex + 1) < actionList.size()) {
				currentPaintIndex++;
			}
			isForwardPressed = true;
			return true;
		}

		return false;
	}

	//whether on main pallent
	public boolean testTouchMainPallent(float x, float y) {
		if (x > bgBitmapX + 2 && y > bgBitmapY + 2
				&& x < bgBitmapX + bgBitmapWidth - 2
				&& y < bgBitmapY + bgBitmapHeight - 2) {
			return true;
		}

		return false;
	}

	// whether on tools panel
	public boolean testTouchToolsPanel(float x, float y) {
		if (x > toolsX && y > toolsY && x < toolsX + toolsSide * toolsWidthNum
				&& y < toolsY + toolsSide * toolsHeightNum) {

			// xy of pen
			int tx = (int) ((x - toolsX) / toolsSide);
			int ty = (int) ((y - toolsY) / toolsSide);
			// current paint tool
			currentPaintTool = tx + ty * toolsWidthNum;
			return true;
		}

		return false;
	}

	// whether on size panel
	public boolean testTouchSizePanel(float x, float y) {
		if (x > sizeX && y > sizeY && x < sizeX + sizeWidth
				&& y < sizeY + sizeHeight * sizeNum) {

			// current size
			int index = (int) ((y - sizeY) / sizeHeight);
			// size setting
			switch (index) {
			case 0:
				currentSize = 5;
				break;
			case 1:
				currentSize = 10;
				break;
			case 2:
				currentSize = 15;
				break;
			default:
				currentSize = 1;
			}
			return true;
		}

		return false;
	}
	
	
	// whether on color panel
	public boolean testTouchColorPanel(float x, float y) {
		if (x > colorX && y > colorY && x < colorX + colorSide * colorWidthNum
				&& y < colorY + colorSide * colorHeightNum) {

			// current color
			int tx = (int) ((x - colorX) / colorSide);
			int ty = (int) ((y - colorY) / colorSide);
			int index = ty * colorWidthNum + tx;

			switch (index) {
			case 0:
				currentColor = Color.LTGRAY;
				break;
			case 1:
				currentColor = 0xffCAFCD8;
				break;
			case 2:
				currentColor = 0xffF7E967;
				break;
			case 3:
				currentColor = 0xffA9CF54;
				break;
			case 4:
				currentColor = 0xff588F27;
				break;
			case 5:
				currentColor = 0xff04bfbf;
				break;
			case 6:
				currentColor = 0xffFF9B93;
				break;
			case 7:
				currentColor = 0xffA49A87;
				break;
			case 8:
				currentColor = 0xffB0E0E6;
				break;
			case 9:
				currentColor = 0xffADFF2F;
				break;
			default:
				currentColor = Color.LTGRAY;
			}

			return true;
		}

		return false;
	}

	// current paint
	public void setCurAction(float x, float y) {
		switch (currentPaintTool) {
		case 0:
			curAction = new MyPath(x, y, currentSize, currentColor);
			break;
		case 1:
			curAction = new MyLine(x, y, currentSize, currentColor);
			break;
		case 2:
			curAction = new MyRect(x, y, currentSize, currentColor);
			break;
		case 3:
			curAction = new MyCircle(x, y, currentSize, currentColor);
			break;
		case 4:
			curAction = new MyFillRect(x, y, currentSize, currentColor);
			break;
		case 5:
			curAction = new MyFillCircle(x, y, currentSize, currentColor);
			break;
		
		}
	}

	// draw tools
	private void drawToolsPanel() {

		
		drawPaintToolsPanel(canvas);


		drawPaintSizePanel(canvas);


		drawPaintColorPanel(canvas);


		drawBackForwardPanel();
	}

	// back and forward button
	private void drawBackForwardPanel() {
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setColor(Color.LTGRAY);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);

		int cellX = backX;
		int cellY = backY;
		int cellBX = backX + backWidth;
		int cellBY = backY + backHeight;
		// strokes
		canvas.drawCircle(cellX + backWidth/2, cellY + backHeight/2, 80, paint);
		// background when touched
		if (isBackPressed) {
			paint.setColor(selectedCellColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(cellX + backWidth/2, cellY + backHeight/2, 80, paint);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
            drawtime = true;
		}
		// content inside the stroke
		drawCellText(canvas, cellX+15, cellY, cellBX, cellBY-20, "<");

		cellX = forwardX;
		cellY = forwardY;
		cellBX = forwardX + forwardWidth;
		cellBY = forwardY + forwardHeight;

		paint.setAntiAlias(true);
		paint.setColor(Color.LTGRAY);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		// stroke
		canvas.drawCircle(cellX + forwardWidth/2, cellY + forwardHeight/2, 80, paint);
		//  background when touched
		if (isForwardPressed) {
			paint.setColor(selectedCellColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(cellX + forwardWidth/2, cellY + forwardHeight/2, 80, paint);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
		}
		// content inside the stroke
		drawCellText(canvas, cellX + 25, cellY, cellBX, cellBY-20, ">");

	}

	// paint tool
	private void drawPaintToolsPanel(Canvas canvas) {
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);

		// strokes
		for (int i = 0; i < toolsWidthNum; i++)
			for (int j = 0; j < toolsHeightNum; j++) {
				int cellX = toolsX + i * toolsSide;
				int cellY = toolsY + j * toolsSide;
				int cellBX = toolsX + (i + 1) * toolsSide;
				int cellBY = toolsY + (j + 1) * toolsSide;
				int paintTool = j * toolsWidthNum + i;
	
				canvas.drawRect(cellX, cellY, cellBX, cellBY, paint);
				// background of paint tool
				if (paintTool == currentPaintTool) {
					paint.setColor(selectedCellColor);
					paint.setStyle(Paint.Style.FILL);
					canvas.drawRect(cellX + 2, cellY + 2, cellBX - 2,
							cellBY - 2, paint);
					paint.setColor(Color.BLACK);
					paint.setStyle(Paint.Style.STROKE);
				}
				// content in paint tool
				drawToolsText(canvas, cellX, cellY, cellBX, cellBY, paintTool);
			}
	}

	// paint size
	private void drawPaintSizePanel(Canvas canvas) {
		
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		// settings
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		for (int i = 0; i < sizeNum; i++) {
			int cellX = sizeX;
			int cellY = sizeY + i * sizeHeight;
			int cellBX = sizeX + sizeWidth;
			int cellBY = sizeY + (i + 1) * sizeHeight;
			int toolSize = i * 2 + 1;
			// stroke
			canvas.drawRect(cellX, cellY, cellBX, cellBY, paint);
			// background
			if (toolSize == currentSize) {
				paint.setColor(selectedCellColor);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawRect(cellX + 2, cellY + 2, cellBX - 2, cellBY - 2,
						paint);
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.STROKE);
			}
			// content 
			drawSizeText(canvas, cellX, cellY, cellBX, cellBY, toolSize);
		}
		
	}

	// draw paint color panel
	private void drawPaintColorPanel(Canvas canvas) {
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setColor(Color.GRAY);
		//paint.setStyle(Paint.Style.STROKE);
		//paint.setStrokeWidth(2);

		// draw rectangular
		canvas.drawRect(colorSelectedX, colorSelectedY, colorSelectedX
				+ colorSelectedSide, colorSelectedY + colorSelectedSide, paint);
		// paint color
		paint.setColor(currentColor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(colorSelectedX, colorSelectedY,
				colorSelectedX + colorSelectedSide, colorSelectedY
						+ colorSelectedSide, paint);
		// stroke of color board
		for (int i = 0; i < colorWidthNum; i++)
			for (int j = 0; j < colorHeightNum; j++) {
				int cellX = colorX + i * colorSide;
				int cellY = colorY + j * colorSide;
				int cellBX = colorX + (i + 1) * colorSide;
				int cellBY = colorY + (j + 1) * colorSide;
				int paintColor = i + j * colorWidthNum;
				// stroke
				canvas.drawRect(cellX, cellY, cellBX, cellBY, paint);
				// color
				drawColorText(canvas, cellX, cellY, cellBX, cellBY, paintColor);
			}
	}

	// pen tools
	private void drawToolsText(Canvas canvas, int cellX, int cellY, int cellBX,
			int cellBY, int paintTool) {
		
		
		switch (paintTool) {
		case 0:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "铅");
			break;
		case 1:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "直");
			break;
		case 2:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "方");
			break;
		case 3:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "圆");
			break;
		case 4:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "块");
			break;
		case 5:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "饼");
			break;
		case 6:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "橡");
			break;
		}
		
		
	}

	// draw size text
	private void drawSizeText(Canvas canvas, int cellX, int cellY, int cellBX,
			int cellBY, int toolSize) {
		
		
		switch (toolSize) {
		case 1:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "1dip");
			break;
		case 3:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "3dip");
			break;
		case 5:
			drawCellText(canvas, cellX, cellY, cellBX, cellBY, "5dip");
			break;
		}
		
	}

	// draw color text
	private void drawColorText(Canvas canvas, int cellX, int cellY, int cellBX,
			int cellBY, int paintColor) {
		switch (paintColor) {
		case 0:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, Color.LTGRAY);
			break;
		case 1:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffCAFCD8);
			break;
		case 2:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffF7E967);
			break;
		case 3:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffA9CF54);
			break;
		case 4:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xff588F27);
			break;
		case 5:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xff04BFBF);
			break;
		case 6:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffFF9B93);
			break;
		case 7:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffA49A87);
			break;
		case 8:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffB0E0E6);
			break;
		case 9:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, 0xffADFF2F);
			break;
		default:
			drawCellColor(canvas, cellX, cellY, cellBX, cellBY, Color.LTGRAY);
			break;
		}
	}

	// text
	private void drawCellText(Canvas canvas, int cellX, int cellY, int cellBX,
			int cellBY, String text) {
		Paint paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.LTGRAY);
		paint.setTextSize((cellBY - cellY) / 4 * 3);
		int textX = cellX + (cellBX - cellX) / 5;
		int textY = cellBY - (cellBY - cellY) / 5;
		canvas.drawText(text, textX, textY, paint);
	}

	// cell color
	private void drawCellColor(Canvas canvas, int cellX, int cellY, int cellBX,
			int cellBY, int color) {
		Paint paint = new Paint();
		
		paint.setColor(color);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(cellX, cellY, cellBX, cellBY, paint);
	}
	
	
	// save picture
	public boolean save(){

		FileOutputStream fos = null;

		savebit = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);

		System.out.print("hello");
		
	        Canvas canvas = new Canvas(newbit);  
	
	        
	        canvas.drawBitmap(bgBitmap, 0,0, null);
	        canvas.drawBitmap(newbit, 0, 0, null); 

	       
	       
	        //saving path  
	        File file = new File("/sdcard/");  
	        if(!file.exists())  
	            file.mkdirs();  
	            try {  
	                FileOutputStream fileOutputStream = new FileOutputStream(file.getPath() + "/save.png");  
	                newbit.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);  
	                fileOutputStream.close();  
	                System.out.println("saveBmp is here");  
	            } catch (Exception e) {  
	                        e.printStackTrace();  
	        }  
	 
		
		
		return true;

	}
	public boolean open(){
		//open picture
				File file = new File("/sdcard/Download/save.png");
				if (file.exists()) {
		            bgBitmap=BitmapFactory.decodeFile("/sdcard/Download/save.png");
		        }
				
		return true;
	}
	
	// draw main pallent
	private Bitmap drawMainPallent() {
		
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		          
		// background pic

		if (bgBitmap != null){
			canvas.drawBitmap(bgBitmap, bgBitmapX, bgBitmapY, null);
		}
		if(drawtime == true){
		newbit=Bitmap.createBitmap(bgBitmapWidth, bgBitmapHeight,
				Config.ARGB_4444);
		Canvas canvasTemp = new Canvas(newbit);
		canvasTemp.drawColor(Color.TRANSPARENT);
		
		for(int i=0;i<=currentPaintIndex;i++){
			actionList.get(i).draw(canvasTemp);
		}
		
		// current action
		if (curAction != null) {
				curAction.draw(canvasTemp);
		}
		
	}
		// draw temporary canvas
		
			canvas.drawBitmap(newbit, bgBitmapX, bgBitmapY, null);
		
		// layout settings for main canvas
		mPaint.setColor(Color.LTGRAY);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(5);
		canvas.drawRect(bgBitmapX - 2, bgBitmapY - 2, bgBitmapX + bgBitmapWidth
				+ 2, bgBitmapY + bgBitmapHeight + 2, mPaint);
		return bgBitmap;
	}

	// x
	public float getRealX(float x) {
		
		return x-bgBitmapX;
	}

	// y
	public float getRealY(float y) {

		return y-bgBitmapY;
	}

	// clear all actions out of main canvas
	private void clearSpareAction() {
		for (int i = actionList.size() - 1; i > currentPaintIndex; i--) {
			actionList.remove(i);

		}
	}
}
