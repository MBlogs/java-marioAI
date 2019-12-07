package levelGenerators.groupx;

import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.MarioStats;
import java.lang.Math;

import static engine.helper.RunUtils.resultToStats;
import static java.lang.Math.abs;

/**
 * Created by Xavier Weber on 12/7/19.
 */
public class SimulationHeuristicX {
    // Constructor
    public SimulationHeuristicX(){}

    // The evaluation function
    public int getScore(String level){

        // Create a MarioGame instance and game-playing AI
        MarioGame game = new MarioGame(); boolean visuals = true;
        MarioAgent agent = new agents.robinBaumgarten.Agent();

        // Let the agent play
        MarioResult result = game.runGame(agent, level, 20, 0, visuals);
        MarioStats stats = resultToStats(result);

        // The stats/features we are interested in.
        int enemieskilled = stats.killsTotal;
        int jumps = stats.numJumps;
        int airtime = stats.maxJumpAirTime;
        int Xjump = (int) stats.maxXjump; // what is this lol
        int mushroom = stats.mushroomsCollected;
        int coins = stats.coins;

        // Absolute difference with our target for each feature
        int fitness = abs(enemieskilled - 5) + abs(jumps-18) + abs(airtime-18) + abs(Xjump-160) + abs(mushroom-1) +
                abs(coins-3);

        // NOTE: lower is better
        return fitness;
    }
}