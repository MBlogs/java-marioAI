package levelGenerators.groupx;

import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.helper.MarioTimer;
import static engine.helper.RunUtils.*;

import static engine.helper.RunUtils.getLevel;

/**
 * This class holds all the experiments.
 */
public class Experiments {

    // Constructor
    public Experiments(){ }

    /**
     * This experiments tests how many percentage  of the levels generated - purely by the SliceDistribution/Unigram
     * method - are playable by the robinBaumgarten.
     *
     * @param nLevels :   How many levels to test
     *
     * @param nIters  :   Times the agent will play the same level
     *
     */
    public void playabilityExperiment(int nLevels, int nIters){

        // Init levelGenerator
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();

        // Init necessary objects
        MarioGame game = new MarioGame();
        MarioAgent agent = new agents.robinBaumgarten.Agent();
        MarioLevelModel levelModel = new MarioLevelModel(150, 16);
        MarioTimer timer = new MarioTimer(5 * 60 * 60 * 1000);

        // Init win counter
        int wincounter = 0;

        // Create levels for the agent to play on and record wins
        for(int i = 1; i <= nLevels; i++){

            System.out.print("Running level number "+i);

            // generate and build level
            String level = generator.getSlicedLevel(levelModel, timer);
            //game.buildWorld(level, 1);

            // let the agent play
            for(int j = 1; j <= nIters; j++){
                MarioResult result = game.runGame(agent, level, 40, 0, true);

                // get result
                String winorloss = result.getGameStatus().toString();
                if (j == 1)
                    System.out.print(": "+ winorloss);
                else
                    System.out.print(", "+ winorloss);

                // update counter if the agent managed to finish the game
                if(winorloss.equals("WIN")) {
                    wincounter++;
                    break; //exit current loop because we got a win
                }
            }
            System.out.println();
        }
        double percentage = ((double) wincounter/ (double) nLevels)*100;
        System.out.println("The agent completed "+wincounter+" out of "+nLevels+" games: "+percentage+"%");
    }
}
