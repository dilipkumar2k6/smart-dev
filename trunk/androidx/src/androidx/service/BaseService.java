package androidx.service; 
import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Base class for any background services. 
 * Use static method {@code checkServiceStatus()} to check any service status.
 * @author 
 *
 */
public abstract class BaseService extends Service {
	public Context context;
	
	public boolean isRunning;
	
	protected String LOG_LABEL = "service";

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
	
	/**
	 * Check whether a service is running.
	 * @param ctx
	 * @param serviceClass
	 * @return true if service is running, false otherwise
	 */
	public static boolean checkServiceStatus(Context ctx, Class serviceClass) {
		ActivityManager actManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : actManager.getRunningServices(Integer.MAX_VALUE)) {
	        if (service.service.getClassName().equals(serviceClass.getName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	protected void debug(Object log) {
		Log.d("service", log.toString());
	}
}
