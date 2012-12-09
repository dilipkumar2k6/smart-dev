package androidx.view;

import android.view.View;
import androidx.Callback;

public class TabsController {

	public int currentTabIndex;

	public int[] tabNames;
	
	public View[] tabView;
	
	public Callback[] callbacks;

	public TabsController(int[] tabNames, View[] tabView) {
		super();
		this.tabNames = tabNames;
		this.tabView = tabView;
	}

	public int previousTabIndex() {
		currentTabIndex = currentTabIndex == 0 ? tabNames.length - 1 : currentTabIndex - 1;
		return currentTabIndex;
	}

	public int nextTabIndex() {
		currentTabIndex = currentTabIndex == tabNames.length - 1 ? 0 : currentTabIndex + 1;
		return currentTabIndex;
	}
}
