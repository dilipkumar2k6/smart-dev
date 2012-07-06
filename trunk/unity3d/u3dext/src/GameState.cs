using System;
using System.Timers;

namespace u3dext {
	public class GameState {

		public static bool isGamePausing = true;

		// For level-based game, 0=Playing, 1=Passed, 2=Failed
		public static  int levelPassStatus = 0;

		// Game timer for each level.
		public static Timer gameTimer;

		// Elapse time for level
		public static float levelElapseTime;

		public GameState () {
		}

		public static void startGameTimer(long intevalInSecond) {
			gameTimer = new Timer((double)intevalInSecond * 1000);
			gameTimer.Elapsed += delegate(object source, System.Timers.ElapsedEventArgs e){
				levelPassStatus = 2;
			};
		}

		public static void stopGameTimer() {
			gameTimer.Stop();
		}
	}
}

