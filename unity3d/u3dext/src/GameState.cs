using System;

namespace u3dext {
	public class GameState {

		public static bool isGamePausing = true;

		// For level-based game, 0=Playing, 1=Passed, 2=Failed
		public static  int levelPassStatus = 0;

		public GameState () {
		}
	}
}

