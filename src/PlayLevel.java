import agents.MarioAgent;
import engine.core.*;
import engine.sprites.Mario;
import levelGenerators.MarioLevelGenerator;
import levelGenerators.groupx.*;

import static engine.helper.RunUtils.*;

public class PlayLevel {

    public static void main(String[] args) {
        // Run settings:
        boolean visuals = true;
        boolean generateDifferentLevels = true;
        String levelFile = "levels/original/lvl-1.txt";
        //levelFile = null;
        lab2 generator = new levelGenerators.groupx.lab2();
        //MB:Set the parameters
        generator.setParameters(new int[]{0,0,1,1,1,0,0,1});

        // Note: either levelFile or generator must be non-null. If neither is null, levelFile takes priority.
        if (levelFile == null && generator == null) {
            return;
        }

        MarioGame game = new MarioGame();
        String level = getLevel(levelFile, generator);

        // Display the entire level (useful).
        game.buildWorld(level, 1);

        int playAgain = 0;
        while (playAgain == 0) {

            // Play the level, either as a human ...
            //MarioResult result = game.playGame(level, 200, 0);

            // ... Or with an AI agent
            visuals = true;
            MarioResult result_1 = game.runGame(new agents.human.Agent(), level, 20, 0, visuals);

            // Print the results of the game
            System.out.println(result_1.getGameStatus().toString());

            if (generateDifferentLevels) {
                level = generateLevel(generator);
            }

            // Check if we should play again.
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }
}
