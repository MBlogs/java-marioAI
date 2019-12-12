package levelGenerators.groupx;

import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.helper.MarioTimer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import static engine.helper.RunUtils.*;

import static engine.helper.RunUtils.getLevel;

/**
 * This class holds all the experiments.
 */
public class Experiments {
    Optimizer optimizer;

    // Constructor
    public Experiments(){
    }

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

    /**
     * This experiment runs iterations of the LevelGeneration process, and records the fitnesses at each iteration
     * When complete, the data generated is saved to file
     *
     * @param nLevels :   How many levels to test
     *
     * @param nIters  :   Iterations per level
     *
     */
    public void fitnessExperiment(int nLevels, int nIters){
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        Random r = new Random();
        //Store a random seed
        long range = 123456789L;
        long seed = (long)(r.nextDouble()*range);
        seed = 58561945;
        r.setSeed(seed);

        optimizer = new Optimizer(r);

        // Initialise array of correct size to store all fitnesses for all iterations for all levels
        String[] records = new String[nIters*nLevels*(optimizer.MUTATION_N+optimizer.MUTATION_N)];
        int recordNumber = 0;

        // Run the optimization process, but this time record fitnesses at each iteration
        for(int level = 0; level<nLevels; level++){
            String[] levels = optimizer.initialiseLevels(generator);
            for(int iter=0;iter<nIters;iter++){
                String[] candidateLevels = optimizer.generateCandidateLevels(levels);
                double[] fitnesses = optimizer.evaluateEveryLevel(candidateLevels);
                // Write the fitness information at this point to file.
                // File is of the format: levelN, iterN, fitness
                for(double f:fitnesses){
                    records[recordNumber] = ""+level+","+iter+","+f;
                    recordNumber += 1;
                }
                levels = optimizer.selectLevels(candidateLevels,fitnesses,generator);
                System.out.println("End of Level:"+level+" , Iteration: "+iter);
            }
        }
        // Write the results to file to perform data analysis
        saveArray(records,"fitnessExperiment.txt");
    }

    /**
     * This experiment outputs 2 starting levels and the result after several mutations.
     * It is to be run with special Optimizer config: LEVEL_N=2, INITIAL_LEVELS=5, ITERATIONS = 1
     * When complete, the data generated is saved to file
     */
    public void visualMutationExperiment(){
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        Random r = new Random();
        //Store a random seed
        long range = 123456789L;
        long seed = (long)(r.nextDouble()*range);
        seed = 123;
        r.setSeed(seed);

        optimizer = new Optimizer(r);
        MarioGame game = new MarioGame();

        // Initialise array of correct size to store all fitnesses for all iterations for all levels
        String[] levels = optimizer.initialiseLevels(generator);
        game.buildWorld(levels[0],1);
        game.buildWorld(levels[1],1);
        String[] candidateLevels = optimizer.generateCandidateLevels(levels);
        double[] fitnesses = optimizer.evaluateEveryLevel(candidateLevels);
        levels = optimizer.selectLevels(candidateLevels,fitnesses,generator);
        // Print the levels
        String level = optimizer.runOptimization(levels,generator);
        //Print final level
        game.buildWorld(level,1);
    }

    /**
     * This experiment created the 5 levels to use for Human Evaluation
     * Levels are output to source folder in the format xlvl-n.txt
     *
     * @param nLevel : How many levels to create
     */
    public void createLevelsExperiment(int nLevel){
        LevelGenerator generator = new levelGenerators.groupx.LevelGenerator();
        Random r = new Random();
        //Store a random seed
        long range = 123456789L;
        long seed = (long)(r.nextDouble()*range);
        seed = 123;
        r.setSeed(seed);

        optimizer = new Optimizer(r);
        MarioGame game = new MarioGame();

        for (int i = 0; i<nLevel;i++){
            // Initialise array of correct size to store all fitnesses for all iterations for all levels
            String[] levels = optimizer.initialiseLevels(generator);
            // Run full optimization
            String level = optimizer.runOptimization(levels,generator);
            saveString(level,"xlvl-"+(i+2)+".txt");
        }
    }

    public void saveString(String stringToSave, String filename){
        // Write string to file
        try(FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(stringToSave);
            out.close();
        } catch (IOException e) {
        }
    }


    public void saveArray(String[] arrayToSave, String filename){
        // Write array to file
        try(FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            for (int i = 0; i< arrayToSave.length; i++) {
                if(arrayToSave[i] != null) {
                    out.println(arrayToSave[i]);
                }
            }
            out.close();
        } catch (IOException e) {
        }
    }
}
