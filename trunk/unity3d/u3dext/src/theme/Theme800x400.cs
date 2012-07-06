using System;

namespace u3dext {
	public class Theme800x400:Theme {
		public Theme800x400 () {
			zoomingRatioWidth = 400 / STANDARD_WIDTH;
			zoomingRatioHeight = 800 / STANDARD_HEIGHT;
		}
	
		public override int makeWidth (int oWidth) {
			return (int)zoomingRatioWidth * oWidth;
		}
	
		public override int makeHeight (int oHeight) {
			return (int)zoomingRatioHeight * oHeight;
		}
	}
}
