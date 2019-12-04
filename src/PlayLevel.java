import agents.MarioAgent;
import engine.core.*;
import engine.helper.MarioStats;
import engine.sprites.Mario;
import levelGenerators.groupx.*;

import static engine.helper.RunUtils.*;

@SuppressWarnings("ConstantConditions")
public class PlayLevel {

    public static void main(String[] args) {
        // Run settings:
        boolean visuals = true;  // Set to false if no visuals required for this run.
        boolean generateDifferentLevels = false;  // If true, each play will be a different generated level.
        String levelFile = "levels/original/lvl-1.txt"; //"levels/notch/lvl-11.txt";  // null;
        MarioAgent agent = new agents.sergeyKarakovskiy.Agent();
        Utils groupxutils = new Utils();
        //LevelGenerator generator = null; //new levelGenerators.groupx.lab2();  // null;

        //XW: init projectXGenerator
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        //generator.makeDistribution();

        //MB: Create Agent stats on all default levels
        MarioStatsX[] defaultLevelAgentStats = groupxutils.makeDefaultAgentStats(40, 5);

        // Note: either levelFile or generator must be non-null. If neither is null, levelFile takes priority.
        if (levelFile == null && generator == null) {
            return;
        }

        // Create a MarioGame instance and game-playing AI
        MarioGame game = new MarioGame();

        // Grab a level from file, found in directory "levels/" or pass null to generate a level automatically.
        //MB: This is a static method. When first is null it just uses the generator.
        String level = getLevel(levelFile, generator);
        String[] defaultLevels = groupxutils.getDefaultLevels();

        // Display the entire level.
        game.buildWorld(level, 1);

        // Repeat the game several times, maybe.
        int playAgain = 0;
        while (playAgain == 0) {  // 0 - play again! 1 - end execution.

            // Play the level, either as a human ...
            //MarioResult result = game.playGame(level, 20, 0);

            // ... Or with an AI agent
            MarioResult result = game.runGame(agent, level, 40, 0, visuals);

            // Print the results of the game
            System.out.println(result.getGameStatus().toString());
            //System.out.println(resultToStats(result).toString());

            if (generateDifferentLevels) {
                level = generateLevel(generator);
            }

            // Check if we should play again.
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }

    // Run 5 levels


    private void recordHumanStatistics(String[] levels, int timer){
        MarioStats humanStats = new MarioStats();
        MarioGame game = new MarioGame();

        for(int i = 0; i< levels.length; i++) {
            MarioResult result = game.playGame(levels[i],timer);
            MarioStats stats = resultToStats(result);
            humanStats = humanStats.merge(stats);
        }
    }
}
