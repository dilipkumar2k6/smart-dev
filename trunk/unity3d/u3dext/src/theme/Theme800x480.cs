using System;

namespace u3dext {
	public class Theme800x480:Theme {
		public Theme800x480 ():base() {
			zoomingRatioWidth = 480f / STANDARD_WIDTH;
			zoomingRatioHeight = 800f / STANDARD_HEIGHT;
		}
	
		public override int W (int oWidth) {
			return (int)(zoomingRatioWidth * oWidth);
		}
	
		public override int H (int oHeight) {
			return (int)(zoomingRatioHeight * oHeight);
		}
	}
}
