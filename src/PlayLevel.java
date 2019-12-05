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
        boolean visuals = true;
        boolean generateDifferentLevels = true;  // Each play through will be different
        String levelFile = null; //"levels/original/lvl-1.txt"
        MarioAgent agent = new agents.robinBaumgarten.Agent();
        Utils groupxutils = new Utils();

        //XW: init projectXGenerator
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        //generator.makeDistribution();

        //MB: Create Agent stats on all default levels
        // String[] defaultLevels = groupxutils.getDefaultLevels();
        //MarioStatsX[] defaultLevelAgentStats = groupxutils.makeDefaultAgentStats(40, 5);

        if (levelFile == null && generator == null) { return; }

        MarioGame game = new MarioGame();
        String level = getLevel(levelFile, generator);
        game.buildWorld(level, 1);

        int playAgain = 0;
        while (playAgain == 0) {

            // Play the level, either as a human ...
            //MarioResult result = game.playGame(level, 20, 0);
            // ... Or with an AI agent
            MarioResult result = game.runGame(agent, level, 40, 0, visuals);

            System.out.println(result.getGameStatus().toString());
            //System.out.println(resultToStats(result).toString());

            if (generateDifferentLevels) { level = generateLevel(generator); }
            playAgain = (game.playAgain == 0 && visuals) ? 0 : 1;  // If visuals are not on, only play 1 time
        }
    }
}
