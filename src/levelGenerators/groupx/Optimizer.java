package levelGenerators.groupx;

import agents.MarioAgent;
import com.sun.java.accessibility.util.TopLevelWindowListener;
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.helper.MarioStats;
import engine.helper.MarioTimer;
import engine.helper.RunUtils;
import levelGenerators.ParamMarioLevelGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static engine.helper.RunUtils.generateLevel;

/**
 * Created by Michael Brooks on 05/12/19.
 */

public class Optimizer {
    private Random random;
    private Utils groupxutils;

    //ToDo Decide on which blocks are allowed to be a mutation (eg. some of these will be bad).
    private static final String mutationBlocks = "X#%|!12SCULoYyGgKkRr";
    private static final String HEURISTIC = "blocks";
    private static final int CROSSOVERS = 3;
    public static final int LEVEL_N = 2;
    private static final int MUTATION_N = 1;
    private static final double MUTATION_RATE = 0.01;
    private static final int ITERATIONS = 5;
    private int TWEAK_RANGE = 2;//Slices either side of this one to adjust.

    public Optimizer() {
        random = new Random();
        groupxutils = new Utils();
    }

    public String runOptimization(String[] levels, LevelGenerator generator){
        for(int i = 0; i<ITERATIONS;i++){
            String[] candidateLevels = generateCandidateLevels(levels);
            double[] fitnesses = evaluateEveryLevel(candidateLevels);
            levels = selectLevels(candidateLevels,fitnesses,generator);
            System.out.println("Finished Full Optimization Iteration: "+i);
        }
        return levels[0];
    }

    public double[] evaluateEveryLevel(String[] levels){
        // Return fitnesses of all levels
        double[] fitnesses = new double[levels.length];
        for(int i = 0; i<levels.length;i++){
            fitnesses[i] = evaluateLevel(levels[i]);
        }
        return fitnesses;
    }

    public double evaluateLevel(String level){
        // ToDo: Heuristics here needs to well thought through. Ones there currently are just for testing. Unlikely that
        double fitness = 0.0;
        if(HEURISTIC.equals("blocks")){ fitness = evaluateLevelByBlocks(level); }
        else { System.out.println("MB: The following isn't a valid evaluation function: "+HEURISTIC); }

        return fitness;
    }

    public int evaluateLevelByBlocks(String level){
        // Count number of blocks
        return level.length() - level.replace("X", "").length();
    }

    public String[] selectLevels(String[] candidateLevels,double[] fitnesses, LevelGenerator generator){
        // Check that levels are valid. If it isn't, try a minor fix at a promising point (where the agent dies).
        String[] validLevels = new String[LEVEL_N];
        int numValidLevels = 0;
        int levelCandidate = 0;
        sortLevelsByFitness(candidateLevels,fitnesses);

        while(numValidLevels<LEVEL_N){
            String candidateLevel = candidateLevels[levelCandidate];
            System.out.println("selectLevels. Selection Phase. Following level is being validated:"+numValidLevels+" and has size "+candidateLevel.length());
            System.out.println(candidateLevel);
            int issueLocation = groupxutils.validateLevel(candidateLevel);

            // If there is an issue try to fix it.
            if(issueLocation < 150-TWEAK_RANGE){
                System.out.println("selectLevels. Trying to fix the level:"+numValidLevels);
                candidateLevel = tweakLevel(candidateLevel,issueLocation,generator);
            }

            // If 150, it is a valid level
            if(issueLocation == 150){
                validLevels[numValidLevels] = candidateLevel;
                numValidLevels++;
            }

            levelCandidate++;
        }
        return validLevels;
    }

    private String tweakLevel(String level, int issueLocation, LevelGenerator generator){
        //ToDO: What is the best way of tweaking the level?
        for(int i=issueLocation-TWEAK_RANGE;i<issueLocation+TWEAK_RANGE;i++){
            String currentSlice = groupxutils.getVerticalSlice(level,i);
            String newSlice = generator.sampleNextSlice(currentSlice);
            level = groupxutils.setVerticalSlice(level,newSlice,i);
        }

        return level;
    }


    private void sortLevelsByFitness(String[] levels,double[] fitnesses){
        for (int i = 0; i < fitnesses.length; i++) {
            for (int j = i + 1; j < fitnesses.length; j++) {
                if (fitnesses[i] > fitnesses[j]) {
                    double tempFit = fitnesses[i];
                    String tempLevel = levels[i];
                    fitnesses[i] = fitnesses[j];
                    levels[i] = levels[j];
                    fitnesses[j] = tempFit;
                    levels[j] = tempLevel;
                }
            }
        }
    }

    public String[] generateCandidateLevels(String[] levels) {
        // Create new population of old solutions + crossover/mutated solutions.
        String[] candidateLevels = new String[levels.length + MUTATION_N];
        for(int i = 0; i<levels.length;i++){
            candidateLevels[i] = levels[i];
        }

        for(int i=levels.length;i<MUTATION_N+levels.length;i++){
            candidateLevels[i] = getCrossOverLevel(levels);
        }
        return candidateLevels;
    }

    public String getCrossOverLevel(String[] levels) {
        // Select two random levels to crossover
        String level1 = levels[random.nextInt(levels.length)];
        String level2 = levels[random.nextInt(levels.length)];

        // Determine crossover points
        int[] crossoverPoints = new int[CROSSOVERS];
        for(int i=0;i<CROSSOVERS;i++){
            if(level1 == null){
                System.out.println("Level is null");
            }
            System.out.println("getCrossLevel. Level1 size is: "+level1.length());
            System.out.println("getCrossLevel. Level2 size is: "+level2.length());
            System.out.println("Level1 is:");
            System.out.println(level1);
            System.out.println("Level2 is:");
            System.out.println(level2);
            crossoverPoints[i] = random.nextInt(level1.length()/16);
            System.out.println("getCrossLevel. Crossover point is: "+crossoverPoints[i]);
        }
        System.out.println("That was all crossover points");
        // Sort cross over points
        Arrays.sort(crossoverPoints);
        // Cross into level1 sections of level2
        for (int c=0;c<crossoverPoints.length-1;c+=2){
            for(int i = crossoverPoints[c]; i < crossoverPoints[c]-1; i++){
                String slice = groupxutils.getVerticalSlice(level2,i);
                level1 = groupxutils.setVerticalSlice(level1,slice,i);
            }
        }

        return mutate(level1);
    }

    private String mutate(String level) {
        //ToDo: Decision: Should we or shouldn't we mutate within a slice here?
        // Mutate with probability 1/n
        char[] levelBlocks = level.toCharArray();

        for (int i = 0; i < level.length(); i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                levelBlocks[i] = mutationBlocks.charAt(random.nextInt(mutationBlocks.length()));
            }
        }
        return String.valueOf(levelBlocks);
    }


}