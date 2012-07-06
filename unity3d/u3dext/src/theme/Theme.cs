using System;

namespace u3dext {
	public abstract class Theme {

		// Standard image's size, change it if you want.
		public int STANDARD_WIDTH = 640;
		public int STANDARD_HEIGHT = 960;

		// Ratio to calculate actual width and height for specific devices.
		protected float zoomingRatioWidth;
		protected float zoomingRatioHeight;
	
		/// <summary>
		/// Makes the width.
		/// </summary>
		/// <returns>
		/// The width.
		/// </returns>
		/// <param name='oWidth'>
		/// O width.
		/// </param>
		public abstract int makeWidth (int oWidth);
	
		/// <summary>
		/// Makes the height.
		/// </summary>
		/// <returns>
		/// The height.
		/// </returns>
		/// <param name='oHeight'>
		/// O height.
		/// </param>
		public abstract int makeHeight (int oHeight);
	
	}

}