using System;

namespace u3dext {
	public class Theme960x640:Theme {

		public Theme960x640 ():base() {
			zoomingRatioWidth = 640f / STANDARD_WIDTH;
			zoomingRatioHeight = 960f / STANDARD_HEIGHT;
		}
	
		public override int W (int oWidth) {
			return (int)(zoomingRatioWidth * oWidth);
		}
	
		public override int H (int oHeight) {
			return (int)(zoomingRatioHeight * oHeight);
		}
	}
}
