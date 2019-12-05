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

    public String getVerticalSlice(String level, int index){
        //int width = level.length()/16;
        int width = 151;
        // Remove all 'newlines'
        level = level.replace("\n", "").replace("\r", "");

        // Notify if index is bigger than level
        if(index > width){ System.out.println("XW: Index is out of bounds - getVerticalSlice()"); }

        // Init vertical slice as a string
        String slice = "";

        // get all tiles in this vertical slice at the given horizontal index
        for(int i = 0; i < 16; i++){
            char tile = level.charAt(index + (i*width));
            slice += Character.toString(tile);
        }
        return slice;
    }

    public String setVerticalSlice(String level, String slice, int index){

        // NOTE: Remember that level has 'newline' characters.
        //int width = (level.length())/16;
        int width = 151;

        // Notify if index is bigger than level
        if(index > width){
            System.out.println("XW: Index is out of bounds - setVerticalSlice()");
        }

        // get all tiles in this vertical slice at the given horizontal index
        for(int i = 0; i < 16; i++){
            char c = slice.charAt(i);
            String sliceTile = Character.toString(c);
            int levelIndex = index + (i* (width+1));

            level = level.substring(0, levelIndex) + sliceTile + level.substring(levelIndex+1);
        }
        return level;
    }

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
    public int validateLevel(String level){
        // Return 150 if valid. Otherwise return issue location.
        // Run agent througj t
        if(level == null){
            System.out.println("MB ERROR: Utils.validateLevel was passed a null level");
        }

        MarioAgent agent = new agents.robinBaumgarten.Agent();
        MarioStatsX stats = getAgentStats(agent,level,30,1);
        if (stats.winRate == 1) { return 150; }

        // Return the index where it failed
        int issueLocation = (int) ((double)stats.percentageComplete * 150);
        return issueLocation;
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

    public MarioStatsX resultToStatsX(MarioResult result) {
        return new MarioStatsX(result.getGameStatus(), result.getCompletionPercentage(),
                result.getCurrentLives(), result.getCurrentCoins(), (int) Math.ceil(result.getRemainingTime() / 1000f),
                result.getMarioMode(), result.getNumCollectedMushrooms(), result.getNumCollectedFireflower(),
                result.getKillsTotal(), result.getKillsByStomp(), result.getKillsByFire(), result.getKillsByShell(),
                result.getKillsByFall(), result.getNumDestroyedBricks(), result.getNumJumps(), result.getMaxXJump(),
                result.getMaxJumpAirTime(), result.getNumBumpBrick(), result.getNumBumpQuestionBlock(),
                result.getMarioNumHurts());
    }

}
