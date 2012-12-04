package androidx.event;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import androidx.AndroidUtils;


/**
 * 
 * @author 
 * 
 */
public abstract class DragAndMoveListener implements OnTouchListener {
	Context context;
	boolean isDockable;
	float width;// dock对象宽度
	float startx = 0;
	float starty = 0;

	public DragAndMoveListener(Context context, boolean isDockable, float width) {
		super();
		this.context = context;
		this.isDockable = isDockable;
		this.width = width;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startx = event.getX();
			starty = event.getY() + 30;
			// Log.d("WM", "onTouch().ACTION_DOWN " + startx + "," + starty);
			return false;
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (isDockable) {
				autoDock(event);
			}
			Log.d("WM", "onTouch().ACTION_UP " + event.getX() + "," + event.getY());
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float x = event.getRawX();
			float y = event.getRawY();
			// Log.d("WM", "onTouch().ACTION_MOVE " + x + "," + y);
			this.updateView((int) (x - startx), (int) (y - starty));
//			params.x = (int) (x - startx);
//			params.y = (int) (y - starty);
//			windowManager.updateViewLayout(floatWndLayout, params);
			return true;
		}
		else {
			return false;
		}
	}

	public abstract void updateView(int newx, int newy);

	/**
	 * 
	 * @param event
	 */
	private void autoDock(MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();
//		float width = floatWndLayout.getWidth();
		Log.d("WM", "onTouch().ACTION_MOVE " + x + "," + y);
		int screenWidth = AndroidUtils.getScreenWidth(context);
		if ((x - startx + width / 2) < screenWidth / 2) {
			// params.x = 0;
			this.updateView(0, (int) (y - starty));
		}
		else {
			// params.x = (int) (screenWidth - width);
			this.updateView((int) (screenWidth - width), (int) (y - starty));
		}
		// params.y = (int) (y - starty);
		// windowManager.updateViewLayout(floatWndLayout, params);
	}
}