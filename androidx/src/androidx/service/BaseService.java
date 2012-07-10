package androidx.service; 
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * @author yxwang
 *
 */
public abstract class BaseService extends Service {
	public Context context;
	
	public boolean isRunning;

	public BaseService() {
		super();
		this.context = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new BaseServiceBinder(this);
	}
	
	/**
	 * 
	 */
	public abstract void stopService();
	
	/**
	 * 
	 */
	public abstract void restartService();

}
