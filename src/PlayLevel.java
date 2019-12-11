import agents.MarioAgent;
import engine.core.*;
import engine.helper.MarioTimer;
import levelGenerators.MarioLevelGenerator;
import levelGenerators.groupx.*;
import levelGenerators.projectx.*;

import static engine.helper.RunUtils.*;

@SuppressWarnings("ConstantConditions")
public class PlayLevel {

    public static void main(String[] args) {
        // Run settings:
        boolean visuals = true;  // Set to false if no visuals required for this run.
        boolean generateDifferentLevels = false;  // If true, each play will be a different generated level.
        String levelFile = null; //"levels/notch/lvl-11.txt";  // null;
        //levelFile = null;
        //LevelGenerator generator = null; //new levelGenerators.groupx.lab2();  // null;


        //XW: init projectXGenerator
        LevelGenerator generator = new levelGenerators.projectx.LevelGenerator();
        //generator.makeDistribution();

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

        //SimulationHeuristicX x = new SimulationHeuristicX();
        //System.out.println(x.getScore(level));

        // Repeat the game several times, maybe.
        int playAgain = 0;
        while (playAgain == 0) {  // 0 - play again! 1 - end execution.

            // Play the level, either as a human ...
            MarioResult result = game.playGame(level, 200, 0);

            // ... Or with an AI agent
            //MarioResult result = game.runGame(agent, level, 20, 0, visuals);

            // Print the results of the game
            System.out.println(result.getGameStatus().toString());
            System.out.println(resultToStats(result).toString());

            if (generateDifferentLevels) {
                level = generateLevel(generator);
            }


            // Check if we should play again.
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }
}
