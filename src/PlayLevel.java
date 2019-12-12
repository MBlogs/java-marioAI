import agents.MarioAgent;
import engine.core.*;
import engine.helper.MarioTimer;
import levelGenerators.MarioLevelGenerator;
import levelGenerators.groupx.*;

import static engine.helper.RunUtils.*;

@SuppressWarnings("ConstantConditions")
public class PlayLevel {

    public static void main(String[] args) {
        // Run settings:
        boolean visuals = false;
        boolean generateDifferentLevels = false;  // Each play through will be different
        String levelFile = null; //"levels/original/lvl-1.txt"
        Utils groupxutils = new Utils();

        //XW: init projectXGenerator
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        //generator.makeDistribution();

        Experiments e = new Experiments();
        e.fitnessExperiment(2, 3);

        if (levelFile == null && generator == null) { return; }

        MarioAgent agent = new agents.robinBaumgarten.Agent();

        // Grab a level from file, found in directory "levels/" or pass null to generate a level automatically.
        String level = getLevel(levelFile, generator);

        // Display the entire level.
        MarioGame game = new MarioGame();
        //game.buildWorld(level, 1);

        // Repeat the game several times, maybe.
        int playAgain = 0;
        while (playAgain == 0) {
            // Play the level, either as a human ...
            MarioResult result = game.playGame(level, 0, 0);

            // ... Or with an AI agent
            //MarioResult result = game.runGame(agent, level, 40, 0, visuals);

            // Print the results of the game
            System.out.println(result.getGameStatus().toString());
            System.out.println(resultToStats(result).toString());

            if (generateDifferentLevels) { level = generateLevel(generator); }
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }
}
