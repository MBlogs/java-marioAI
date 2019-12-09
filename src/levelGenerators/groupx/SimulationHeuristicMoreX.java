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
public class SimulationHeuristicMoreX {

    // Constructor
    Utils groupxutils;

    public SimulationHeuristicMoreX(){
        groupxutils = new Utils();
    }

    // The evaluation function
    public int getScore(String level){

        // Create a MarioGame instance and game-playing AI
        MarioGame game = new MarioGame(); boolean visuals = true;
        MarioAgent agent = new agents.robinBaumgarten.Agent();

        // Let the agent play
        MarioStatsX stats = groupxutils.getAgentStats(agent,level,30,1);

        // The stats/features we are interested in.
        int enemieskilled = stats.killsTotal;
        int jumps = stats.numJumps;
        int airtime = stats.maxJumpAirTime;
        int Xjump = (int) stats.maxXjump; // what is this lol
        int mushroom = stats.mushroomsCollected;
        int coins = stats.coins;

        // Absolute difference with our target for each feature
        //int fitness = abs(enemieskilled - 5) + abs(jumps-18) + abs(airtime-18) + abs(Xjump-160) + abs(mushroom-1) +
        //        abs(coins-3);

        int df = stats.flowersCollected;
        int dm = stats.mushroomsCollected;

        int k = stats.killsTotal;
        int kst = stats.stompKills;
        int ksh = stats.shellKills;
        int kf = stats.fireKills;
        int s = (int) stats.winRate;
        int m = stats.marioState;
        int bh = stats.bricksDestroyed;
        int c = stats.coins;
        int t = stats.remainingTime;

        int fitness = 64*df + 58*dm + 42*k + 12*kst + 17*ksh + 4*kf + 1024*s + 32*m + 24*bh + 16*c + 8*t;

        // NOTE: higher is better now
        return fitness;
    }
}