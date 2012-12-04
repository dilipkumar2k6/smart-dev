package androidx.view; 
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * 自定义的TabView，禁止滑动
 * 
 * @author 
 * 
 */
public class TabView extends GridView {

	public TabView(Context context) {
		super(context);
		this.init();
	}

	public TabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public TabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	private void init() {
//		this.setScrollbarFadingEnabled(false); // Android 1.6不支持
		this.setFadingEdgeLength(0);
		this.setVerticalSpacing(0);
		this.setHorizontalSpacing(5);
		this.setVerticalFadingEdgeEnabled(false);
		this.setHorizontalFadingEdgeEnabled(false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 按下
			return super.onTouchEvent(event);
		case MotionEvent.ACTION_MOVE:// 滑动
			break;
		case MotionEvent.ACTION_UP:// 离开
			return super.onTouchEvent(event);
		}
		return false;
	}

}
