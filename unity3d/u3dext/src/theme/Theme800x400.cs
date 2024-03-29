using System;

namespace u3dext {
	public class Theme800x400:Theme {
		public Theme800x400 ():base() {
			zoomingRatioWidth = 400f / STANDARD_WIDTH;
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
