using System;

namespace u3dext {
	public class Theme480x320 :Theme {
	
		public Theme480x320 () {
			zoomingRatioWidth = 320f / STANDARD_WIDTH;
			zoomingRatioHeight = 480f / STANDARD_HEIGHT;
		}
	
		public override int makeWidth (int oWidth) {
			return (int)(zoomingRatioWidth * oWidth);
		}
	
		public override int makeHeight (int oHeight) {
			return (int)(zoomingRatioHeight * oHeight);
		}
	}
}
