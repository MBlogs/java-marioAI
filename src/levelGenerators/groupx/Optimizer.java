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
import java.util.logging.Level;

import static engine.helper.RunUtils.generateLevel;

/**
 * Created by Michael Brooks on 05/12/19.
 */

public class Optimizer {
    private Random random;
    private Utils groupxutils;
    private SimulationHeuristicX simulationHeuristicX;

    // Fixed configuration Parameters for Evolutionary Algorithm
    private final int ITERATIONS = 10; // Total iterations
    public static final int INITIAL_LEVELS = 20; // levels to create initially
    public final int LEVEL_N = 5; // levels to carry over into new populations. Needs to be < INITIAL LEVELS
    private final String MUTATION_BLOCKS = "#%@!SCULoyYgkKrR-"; //eligible blocks to mutate
    private final String MUTATION_ENEMIES = "yYgkKrR"; //elligible enemies to mutate
    private final String HEURISTIC = "simulation";//"type of evaluation function to use (note: only simulation is valid)
    public final int MUTATION_N = 15; // How many additional mutated levels to generate in each population
    private final int CROSSOVERS = 4; // level crossovers in the mutation stage
    private final double MUTATION_SLICE_RATE = 0.25; //Chances of a slice mutation
    private final double MUTATION_TILE_RATE = 0.25; //Chance of a tile mutation within a slice mutation
    private final int ISSUE_TWEAK_RANGE = 4;//When trying to fix a level, how many slices either side of problem point to tweak?


    public Optimizer(Random r) {
        this.random = r;
        groupxutils = new Utils();
        simulationHeuristicX = new SimulationHeuristicX();
    }

    public String runOptimization(String[] levels, LevelGenerator generator){
        // Give a set of starting levels. Run optimisation process and return best level.
        for(int i = 0; i<ITERATIONS;i++){
            System.out.println("Generating levels for Iteration: ("+i+" /"+ITERATIONS+")");
            String[] candidateLevels = generateCandidateLevels(levels);
            System.out.println("Computing fitness for Iteration: ("+i+" /"+ITERATIONS+")");
            double[] fitnesses = evaluateEveryLevel(candidateLevels);
            System.out.println("Selecting levels for Iteration: ("+i+" /"+ITERATIONS+")");
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
        // Evaluate level according to current evaluation mode
        double fitness = 0.0;
        if(HEURISTIC.equals("blocks")){ fitness = evaluateLevelByBlocks(level); }
        else if(HEURISTIC.equals("simulation")){fitness = simulationHeuristicX.getScore(level); }
        else { System.out.println("The following isn't a valid evaluation function: "+HEURISTIC); }

        return fitness;
    }

    public String[] initialiseLevels(LevelGenerator generator){
        // Intitialise a set of levels through slice distributions.

        System.out.println("Initializing starting levels...");
        String[] levels = new String[Optimizer.INITIAL_LEVELS];
        String level = "";
        MarioLevelModel levelModel = new MarioLevelModel(150, 16);
        MarioTimer timer = new MarioTimer(5 * 60 * 60 * 1000);

        for (int i = 0; i<Optimizer.INITIAL_LEVELS; i++){
            int validLevel = 0;

            while(validLevel != 150){
                level = generator.getSlicedLevel(levelModel,timer);
                validLevel = groupxutils.validateLevel(level);
                if(validLevel != 150){
                    //Attempt fix
                    level = tweakLevel(level,validLevel,ISSUE_TWEAK_RANGE,generator);
                    validLevel = groupxutils.validateLevel(level);
                }
            }
            levels[i] = level;
        }
        return levels;
    }

    public int evaluateLevelByBlocks(String level){
        // Count number of blocks
        return level.length() - level.replace("#", "").length();
    }

    public String[] selectLevels(String[] candidateLevels,double[] fitnesses, LevelGenerator generator){
        String[] validLevels = new String[LEVEL_N];
        int numValidLevels = 0;
        int levelCandidate = 0;
        sortLevelsByFitness(candidateLevels,fitnesses);

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
        // Tweak the level around the issue location,
        for(int i=Math.max(tweakLocation-tweakRange, 2);i<Math.min(tweakLocation+tweakRange,149);i++){
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
        while(randLevel1==randLevel2){
            randLevel2 = random.nextInt(levels.length);
        }

        String level1 = levels[randLevel1];
        String level2 = levels[randLevel2];

        //System.out.println("getCrossLevel chose levels "+ randLevel1+ " and "+randLevel2);

        // Determine crossover points
        int[] crossoverPoints = new int[CROSSOVERS];
        for(int i=0;i<CROSSOVERS;i++){
            crossoverPoints[i] = random.nextInt(level1.length()/16);
        }
        // Sort cross over points
        Arrays.sort(crossoverPoints);
        // Cross into level1 sections of level2
        for (int c=0;c<crossoverPoints.length;c+=2){
            // From the first cross over point to the next, get the slice from level2 and put it in level1.
            for(int i = crossoverPoints[c]; i < crossoverPoints[c+1]; i++){
                String slice = groupxutils.getVerticalSlice(level2,i);
                level1 = groupxutils.setVerticalSlice(level1,slice,i);
            }
        }
        return mutate(level1);

    }

    private String mutate(String level) {
        int fullWidth = level.length();
        char[] levelBlocks = level.toCharArray();

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

                        // If mutating enemy, choose a random enemy
                        if(MUTATION_ENEMIES.contains(tileMutationString)){
                            int randomEnemy = random.nextInt(MUTATION_ENEMIES.length()-1);
                            tileMutationString = MUTATION_ENEMIES.substring(randomEnemy, randomEnemy+1);
                        }

                        // If both the current block and the new block are valid mutation blocks, mutate.
                        if(MUTATION_BLOCKS.contains(tileMutationString) && MUTATION_BLOCKS.contains(tileString)){
                            if(tileChar != '\n' && tileMutationChar != '\n'){
                                // Update current slice with the new tile
                                currentSlice = currentSlice.substring(0,j) + tileMutationString + currentSlice.substring(j+1);
                                level = groupxutils.setVerticalSlice(level,currentSlice,i);
                            }
                        }
                    }
            }

        }
        return level;
    }

    private void printLevelFitnesses(String[] levels, double[] fitnesses){
        System.out.println("Start of selectLevels. Following list of levels and fitnesses");
        for(int i =0; i<levels.length; i++){
            System.out.println("Level "+i+" has fitness "+fitnesses[i]);
        }

    }
}