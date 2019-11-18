import agents.MarioAgent;
import engine.core.*;
import engine.sprites.Mario;
import levelGenerators.MarioLevelGenerator;
import levelGenerators.groupx.*;

import static engine.helper.RunUtils.*;

@SuppressWarnings("ConstantConditions")
public class PlayLevel {

    public static void main(String[] args) {
        // Run settings:
        boolean visuals = true;  // Set to false if no visuals required for this run.
        boolean generateDifferentLevels = true;  // If true, each play will be a different generated level.
        String levelFile = "levels/original/lvl-1.txt";  // null;
        levelFile = null;
        lab2 generator = new levelGenerators.groupx.lab2();   // null;
        //MB:Set the parameters
        generator.setParameters(new int[]{0,0,1,1,1,0,0,1});

        // Note: either levelFile or generator must be non-null. If neither is null, levelFile takes priority.
        if (levelFile == null && generator == null) {
            return;
        }

        // Create a MarioGame instance and game-playing AI
        MarioGame game = new MarioGame();
        MarioAgent agent = new agents.robinBaumgarten.Agent();

        // Grab a level from file, found in directory "levels/" or pass null to generate a level automatically.
        String level = getLevel(levelFile, generator);

        // Display the entire level.
//        game.buildWorld(level, 1);

        // Repeat the game several times, maybe.
        int playAgain = 0;
        while (playAgain == 0) {  // 0 - play again! 1 - end execution.

            // Play the level, either as a human ...
            //MarioResult result = game.playGame(level, 200, 0);

            // ... Or with an AI agent
            visuals = false;
            MarioResult result_1 = game.runGame(new agents.robinBaumgarten.Agent(), level, 20, 0, visuals);
            MarioResult result_2 = game.runGame(new agents.random.Agent(), level, 20, 0, visuals);
            MarioResult result_3 = game.runGame(new agents.andySloane.Agent(), level, 20, 0, visuals);
            // Print the results of the game
            System.out.println(result_1.getGameStatus().toString());
            System.out.println(result_2.getGameStatus().toString());
            System.out.println(result_3.getGameStatus().toString());
//            System.out.println(resultToStats(result).toString());

            if (generateDifferentLevels) {
                level = generateLevel(generator);
            }

            // Check if we should play again.
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }
}
