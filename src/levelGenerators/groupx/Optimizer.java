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

    /* Mutation blocks:
    -:AIR
    X:FLOOR
    #:PYRAMID_BLOCK
    %:JUMP_THROUGH_BLOCK
    @: MUSHROOM_BLOCK
    !:COIN_QUESTION_BLOCK
    S: NORMAL_BLOCK
    C: COIN_BLOCK
    U: MUSHROOM_BLOCK
    L: 1UP_BLOCK
    o: COIN
    y:SPIKY
    Y:SPIKY_WINGED
    g:GOOMBA
    k:GREEN_KOOPA
    K:GREEN_KOOPA_WINGED
    r:RED_KOOPA
    R:RED_KOOPA_WINGED
    */
    public static final int INITIAL_LEVELS = 15; // levels to create initially
    private static final int LEVEL_N = 5; // levels to carry over into new populations
    private static final String mutationBlocks = "#%@!SCULoyYgkKrR";
    private static final String HEURISTIC = "blocks";
    private static final int CROSSOVERS = 3; // crossovers in the mutation stage
    private static final int MUTATION_N = 15; // How many additional mutated levels to generate in each population
    private static final double MUTATION_RATE = 0.01; //Chances of a mutation (at a block level).
    private static final int ITERATIONS = 5; // Total iterations

    private int TWEAK_RANGE = 2;//When trying to fix a level, how many slices either side of problem point to tweak?

    public Optimizer() {
        random = new Random();
        groupxutils = new Utils();
    }

    public String runOptimization(String[] levels, LevelGenerator generator){
        // Give a set of starting levels. Run optimisation process and return best level.
        for(int i = 0; i<ITERATIONS;i++){
            String[] candidateLevels = generateCandidateLevels(levels);
            double[] fitnesses = evaluateEveryLevel(candidateLevels);
            levels = selectLevels(candidateLevels,fitnesses,generator);
            System.out.println(" Finished Optimization Iteration: ("+i+" /"+ITERATIONS+")");
            System.out.println("\n");
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
        // ToDo: Heuristics here needs to well thought through. Ones there currently are just for testing.
        double fitness = 0.0;
        if(HEURISTIC.equals("blocks")){ fitness = evaluateLevelByBlocks(level); }
        else { System.out.println("MB: The following isn't a valid evaluation function: "+HEURISTIC); }

        return fitness;
    }

    public int evaluateLevelByBlocks(String level){
        // Count number of blocks
        return level.length() - level.replace("#", "").length();
    }

    public String[] selectLevels(String[] candidateLevels,double[] fitnesses, LevelGenerator generator){
        // Check that levels are valid. If it isn't, try a minor fix at a promising point (where the agent dies).
        String[] validLevels = new String[LEVEL_N];
        int numValidLevels = 0;
        int levelCandidate = 0;
        sortLevelsByFitness(candidateLevels,fitnesses);
        printLevelFitnesses(candidateLevels, fitnesses);

        while(numValidLevels<LEVEL_N){
            String candidateLevel = candidateLevels[levelCandidate];
            int issueLocation = groupxutils.validateLevel(candidateLevel);

            // If there is an issue try to fix it.
            if(issueLocation < 150-TWEAK_RANGE){
                System.out.println("selectLevels. Trying to fix the level:"+levelCandidate);
                candidateLevel = tweakLevel(candidateLevel,issueLocation,generator);
            }

            // If 150, it is a valid level
            if(issueLocation == 150){
                System.out.println("selectLevels. Level number: "+levelCandidate+" was accepted, with fitness "+fitnesses[levelCandidate]);
                validLevels[numValidLevels] = candidateLevel;
                numValidLevels++;
            }

            levelCandidate++;
        }



        return validLevels;
    }

    private String tweakLevel(String level, int issueLocation, LevelGenerator generator){
        for(int i=issueLocation-TWEAK_RANGE;i<issueLocation+TWEAK_RANGE;i++){
            String currentSlice = groupxutils.getVerticalSlice(level,i);
            String newSlice = generator.sampleNextSlice(currentSlice);
            level = groupxutils.setVerticalSlice(level,newSlice,i);
        }

        return level;
    }


    private void sortLevelsByFitness(String[] levels,double[] fitnesses){
        // Sort both arrays by bubble sorting.
        for (int i = 0; i < fitnesses.length; i++) {
            for (int j = i + 1; j < fitnesses.length; j++) {
                // Ascending
                if (fitnesses[i] < fitnesses[j]) {
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
        int randLevel1 = random.nextInt(levels.length);
        int randLevel2 = random.nextInt(levels.length);

        String level1 = levels[randLevel1];
        String level2 = levels[randLevel2];

        System.out.println("getCrossLevel chose levels "+ randLevel1+ " and "+randLevel2);

        // Determine crossover points
        int[] crossoverPoints = new int[CROSSOVERS];
        for(int i=0;i<CROSSOVERS;i++){
            if(level1 == null){
                System.out.println("getCrossLevel. Level is null");
            }
            //System.out.println("getCrossLevel. Level1 is:");
            //System.out.println(level1);
            //System.out.println("getCrossLevel. Level2 is:");
            //System.out.println(level2);
            crossoverPoints[i] = random.nextInt(level1.length()/16);

        }
        // Sort cross over points
        Arrays.sort(crossoverPoints);
        // Cross into level1 sections of level2
        for (int c=0;c<crossoverPoints.length-1;c+=2){
            System.out.println("getCrossLevel. Crossover point is: "+crossoverPoints[c]);
            for(int i = crossoverPoints[c]; i < crossoverPoints[c]-1; i++){
                String slice = groupxutils.getVerticalSlice(level2,i);
                level1 = groupxutils.setVerticalSlice(level1,slice,i);
            }
        }
        return mutate(level1);

    }

    private String mutate(String level) {
        //ToDo: Mutation should be a bit better here
        int fullWidth = level.length();
        char[] levelBlocks = level.toCharArray();

        for (int i = 0; i < level.length(); i++) {
            //Mutation strategy: If this block, one above, or one below is an acceptable one to mutate
            String thisBlock = Character.toString(levelBlocks[i]);
            String belowBlock = "";
            if(i+fullWidth > 0 && i+fullWidth < level.length()){ belowBlock = Character.toString(levelBlocks[i+fullWidth]); }

            if (mutationBlocks.contains(thisBlock)) {
                if(!thisBlock.equals("\n") && !belowBlock.equals("\n")) {
                    if (random.nextDouble() < MUTATION_RATE) {
                        levelBlocks[i] = mutationBlocks.charAt(random.nextInt(mutationBlocks.length()));
                        //System.out.println("Mutating. " + thisBlock + " became " + Character.toString(levelBlocks[i]));
                    }
                }
            }

        }
        System.out.println("mutateLevel. Mutated the level");
        return String.valueOf(levelBlocks);
    }

    private void printLevelFitnesses(String[] levels, double[] fitnesses){
        System.out.println("Start of selectLevels. Following list of levels and fitnesses");
        for(int i =0; i<levels.length; i++){
            System.out.println("Level "+i+" has fitness "+fitnesses[i]);
        }

    }


}