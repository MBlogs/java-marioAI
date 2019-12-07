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
    public static final int INITIAL_LEVELS = 30; // levels to create initially
    private final int LEVEL_N = 10; // levels to carry over into new populations. Needs to be < INITIAL LEVELS
    private final String mutationBlocks = "#%@!SCULoyYgkKrR-";
    private final String HEURISTIC = "blocks";
    private final int MUTATION_N = 40; // How many additional mutated levels to generate in each population
    private final int CROSSOVERS = 3; // level crossovers in the mutation stage
    private final double MUTATION_SLICE_RATE = 0.1; //Chances of a slice mutation
    private final double MUTATION_TILE_RATE = 0.2; //Chance of a tile mutation within a slice mutation
    private final int ITERATIONS = 30; // Total iterations
    private final int ISSUE_TWEAK_RANGE = 2;//When trying to fix a level, how many slices either side of problem point to tweak?

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
            if(issueLocation < 150-ISSUE_TWEAK_RANGE){
                System.out.println("selectLevels. Trying to fix the level:"+levelCandidate);
                candidateLevel = tweakLevel(candidateLevel,issueLocation,ISSUE_TWEAK_RANGE,generator);
            }
            // If 150, it is a valid level
            if(issueLocation == 150){
                System.out.println("selectLevels. Level number: "+levelCandidate+" was accepted, with fitness "+fitnesses[levelCandidate]);
                validLevels[numValidLevels] = candidateLevel;
                numValidLevels++;
            } else {
                System.out.println("selectLevels. Level number: " + levelCandidate + " was rejected, with fitness " + fitnesses[levelCandidate]);
            }
            levelCandidate++;
        }

        return validLevels;
    }

    private String tweakLevel(String level, int tweakLocation,int tweakRange, LevelGenerator generator){
        for(int i=tweakLocation-tweakRange;i<tweakLocation+tweakRange;i++){
            String currentSlice = groupxutils.getVerticalSlice(level,i);
            String newSlice = generator.sampleNextSlice();
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

        /*
        Mutation strategy:
        Get neighbouring slices
        Copy a neighbour, if both valid mutation tiles
        */
        for (int i = 3; i < level.length()/16 - 2; i++) {

            if (random.nextDouble() < MUTATION_SLICE_RATE) {
                // Get the previous, current and next slice
                String prevSlice = groupxutils.getVerticalSlice(level,i-1);
                String currentSlice = groupxutils.getVerticalSlice(level,i);
                String nextSlice = groupxutils.getVerticalSlice(level,i+1);

                int sliceLength = 16;

                // For each tile in current slice, decide whether to steal a neighbour tile
                for (int j = 0; j<sliceLength-1;j++)
                    if (random.nextDouble()<MUTATION_TILE_RATE){
                        // Steal the tile in a random direction. Use max and min to not exceed arrays
                        char tileChar = currentSlice.charAt(j);
                        char tileMutationChar = tileChar;
                        int randomDirection = random.nextInt(4);

                        // Get candidate mutation tile from surrounding tiles.
                        if(randomDirection==0) {
                            tileMutationChar = currentSlice.charAt(Math.max(0, j - 1));
                        } else if((randomDirection==1)) {
                            tileMutationChar = nextSlice.charAt(j);
                        } else if(randomDirection==2) {
                            tileMutationChar = prevSlice.charAt(j);;
                        } else {
                            tileMutationChar = currentSlice.charAt(Math.min(j+1,sliceLength-1));
                        }

                        String tileString = Character.toString(tileChar);
                        String tileMutationString = Character.toString(tileMutationChar);

                        // If both the current block and the new block are valid mutation blocks, do it.
                        if(mutationBlocks.contains(tileMutationString) && mutationBlocks.contains(tileString)){
                            if(tileChar != '\n' && tileMutationChar != '\n'){
                                // Update current slice with the new tile
                                currentSlice = currentSlice.substring(0,j) + tileMutationString + currentSlice.substring(j+1);
                                level = groupxutils.setVerticalSlice(level,currentSlice,i);
                                if(tileChar != tileMutationChar){System.out.println("Mutating. " + tileString + " became " + tileMutationString);}
                            }
                        }
                    }
            }

        }
        System.out.println("mutateLevel. Mutated the level");
        return level;
    }

    private void printLevelFitnesses(String[] levels, double[] fitnesses){
        System.out.println("Start of selectLevels. Following list of levels and fitnesses");
        for(int i =0; i<levels.length; i++){
            System.out.println("Level "+i+" has fitness "+fitnesses[i]);
        }

    }
}