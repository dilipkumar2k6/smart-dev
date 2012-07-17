using System;

namespace u3dext {
	public class Theme480x320 :Theme {
	
		public Theme480x320 ():base() {
			zoomingRatioWidth = 320f / STANDARD_WIDTH;
			zoomingRatioHeight = 480f / STANDARD_HEIGHT;
		}
	
		public override int W (int oWidth) {
			return (int)(zoomingRatioWidth * oWidth);
		}
	
		public override int H (int oHeight) {
			return (int)(zoomingRatioHeight * oHeight);
		}
	}
}
