package levelGenerators.groupx;

import agents.MarioAgent;
import com.sun.xml.internal.ws.util.StringUtils;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.MarioStats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import static engine.helper.RunUtils.getLevel;
import static engine.helper.RunUtils.resultToStats;

/**
 * Created by wweb on 11/26/19.
 */
public class Utils {

    private ObjectInputStream ois;

    // getAllAgentStats: Return Array of Stats for multiple agents over multiple levels.
    public MarioStatsX[] getAllAgentStats(MarioAgent agents[], String[] levels, int timer, int repsPerLevel){
        // Create and populate empty stats in the stats array
        MarioStatsX[] agentsStats = new MarioStatsX[agents.length];
        for(int i = 0; i<agentsStats.length;i++){
            agentsStats[i] = new MarioStatsX();
        }

        // Iterate through every Agent
        for(int ag = 0; ag < agents.length; ag++){
            // Iterate through all levels and update the Agent's stats each time
            for(int lev = 0; lev< levels.length; lev++) {
                System.out.println(agents[ag].getAgentName()+" is playing Level "+ lev);
                MarioStatsX stats = getAgentStats(agents[ag],levels[lev],timer,repsPerLevel);
                agentsStats[ag] = agentsStats[ag].merge(stats);
            }
            System.out.println("Stats for: "+agents[ag].getAgentName());
            System.out.println(agentsStats[ag].toString());
        }
        return agentsStats;
    }

    // getAgentStats: Return Stats for single agent on a single level.
    public MarioStatsX getAgentStats(MarioAgent agent, String level, int timer, int repsPerLevel){
        //ToDo: Merging needs to be an average in the end. I've made MarioStatsX so we can edit this.
        MarioGame game = new MarioGame();
        MarioStatsX statstotal = new MarioStatsX();
        for (int j = 0; j < repsPerLevel; j++) {
            MarioResult result = game.runGame(agent, level, timer, 0, false);
            MarioStatsX stats = resultToStatsX(result);
            statstotal = statstotal.merge(stats);
        }
        return statstotal;
    }

    // Take level String. Return whether it meets minimum validation requirements. Add to this criteria.
    public boolean validLevel(String level,int timer,int repsPerLevel){
        // Level needs to be winnable. Run a good agent on the level 2 times and see if it is:
        MarioAgent agent = new agents.robinBaumgarten.Agent();
        MarioStatsX stats = getAgentStats(agent,level,40,2);

        if(stats.winRate == 0) {return false; }

        // If none of the above the level is valid
        return true;
    }

    public int evaluateLevel(String level, int evaluationMode){
        // ToDo: Heuristics here needs to well thought through. Ones there currently are just for testing. Unlikely that
        // win rate is a good metric because for any level it will always be 0 or 1.

        // If level isn't valid, it scores 0% Similarity
        if(validLevel(level,40,5) == false){ return 0; };
        // If level is valid, score it according to whatever evaluationMode is being used
        if(evaluationMode == 0){
            // Optimise to put as many blocks in the level as possible, but keeping it winnable.
            return level.length() - level.replace("X", "").length();
        } else if(evaluationMode == 1){
            // Fall back to % complete for a fairly poor agent. optimise the level to be easier.
            MarioAgent agent = new agents.spencerSchumann.Agent();
            MarioStatsX stats = getAgentStats(agent,level,40,2);
            return Math.round(stats.percentageComplete);
        } else {
            System.out.println("MB Error: Mode "+ evaluationMode + " is not valid. Default to many blocks");
            return 0;
        }
    }

    public void evaluateLevelBlocks(String level){
        // Input level string, output
    }

    // Run all agents against the default levels. Save the results for use in evaluation metrics.
    public MarioStatsX[] makeDefaultAgentStats(int timer, int repsPerLevel) {
        MarioAgent[] agents = getAgents();
        String[] defaultLevels = getDefaultLevels();
        return getAllAgentStats(agents,defaultLevels,timer,repsPerLevel);
    }

    public String[] getDefaultLevels(){
        String[] defaultLevels = new String[15];
        for(int i=1;i<=15;i++){
            String s = "levels/original/lvl-"+i+".txt";
            defaultLevels[i-1] = getLevel(s, null);
        }
        return defaultLevels;
    }

    // Dictates the order of results returned in stats array.
    // These are the best agents
    public MarioAgent[] getAgents(){
        return new MarioAgent[]{
                //new agents.andySloane.Agent(),
                //new agents.glennHartmann.Agent(),
                //new agents.michal.Agent(),
                new agents.robinBaumgarten.Agent(),
                //new agents.sergeyKarakovskiy.Agent(),
                //new agents.sergeyPolikarpov.Agent(),
                new agents.spencerSchumann.Agent(),
                //new agents.trondEllingsen.Agent()
        };
    }

    public static MarioStatsX resultToStatsX(MarioResult result) {
        return new MarioStatsX(result.getGameStatus(), result.getCompletionPercentage(),
                result.getCurrentLives(), result.getCurrentCoins(), (int) Math.ceil(result.getRemainingTime() / 1000f),
                result.getMarioMode(), result.getNumCollectedMushrooms(), result.getNumCollectedFireflower(),
                result.getKillsTotal(), result.getKillsByStomp(), result.getKillsByFire(), result.getKillsByShell(),
                result.getKillsByFall(), result.getNumDestroyedBricks(), result.getNumJumps(), result.getMaxXJump(),
                result.getMaxJumpAirTime(), result.getNumBumpBrick(), result.getNumBumpQuestionBlock(),
                result.getMarioNumHurts());
    }
}
