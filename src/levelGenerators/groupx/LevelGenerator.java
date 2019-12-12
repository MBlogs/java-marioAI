package levelGenerators.groupx;

import agents.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.helper.MarioStats;
import engine.helper.MarioTimer;
import levelGenerators.MarioLevelGenerator;

import java.io.*;
import java.util.*;

import static engine.helper.RunUtils.*;

/**
 * Created by Xavier Weber on 11/25/19.
 * TODO: create hashmap of vertical slice distributions
 */
public class LevelGenerator implements MarioLevelGenerator{

    public String workingdir = "";
    public String groupxdir = "";
    private Utils groupxutils;
    private Optimizer optimizer;
    private HashMap<String, SliceDistribution> SliceDistributions = null;
    private ArrayList<String> allSlices = null;
    public Random r;


    // Constructor
    public LevelGenerator(){
        seed();
        this.workingdir = System.getProperty("user.dir");
        this.groupxdir = workingdir+"/src/levelGenerators/groupx/";
        this.groupxutils = new Utils();
        this.optimizer = new Optimizer(r);
    }

    public void seed(){
        this.r = new Random();
        //Store a random seed
        long range = 123456789L;
        long seed = (long)(r.nextDouble()*range);
        //seed = 58561945;

        // Set the Random object seed
        r.setSeed(seed);

        //Print the seed - this will allow reproduction of the same level.
        System.out.println("Level Seed is: "+seed);
    }

    public String getGeneratedLevel(MarioLevelModel model,MarioTimer timer){
        if(SliceDistributions == null){
            // Step 0: Retrieve the pre-built distributions
            this.SliceDistributions = retrieveSliceDistributions();
            this.allSlices = retrieveAllSlices();
        }

        // Step 1: Initialisation.
        String[] levels = optimizer.initialiseLevels(this);
        // Step 2: Evaluate initial levels and select the best
        levels = optimizer.selectLevels(levels,optimizer.evaluateEveryLevel(levels),this);
        // Step 3: Repeat Evolutionary Optimisation process
        String level = optimizer.runOptimization(levels,this);
        System.out.println(level);
        return level;
    }

    /**
     * Generate a playable mario level
     *
     * @param model contain a model of the level
     */
    public String getSlicedLevel(MarioLevelModel model, MarioTimer timer){
        if(SliceDistributions == null){
            // Step 0: Retrieve the pre-built distributions
            this.SliceDistributions = retrieveSliceDistributions();
            this.allSlices = retrieveAllSlices();
        }


        // Clear the map
        model.clearMap();
        // Get HashMap with SliceDistributions
        int length = SliceDistributions.size();
        // Get ArrayList with all slices
        int nSlices = allSlices.size();

        // Initialize an empty level of desired width
        int width = 151;
        String level = initializeEmptyLevel(width);

        // Set first slice - TODO: or get random
        String firstSlice = "--------------XX";
        String marioSlice = "-------------MXX";
        String exitSlice = "-------------FXX";

        level = groupxutils.setVerticalSlice(level, firstSlice, 0);
        level = groupxutils.setVerticalSlice(level, firstSlice, 1);
        level = groupxutils.setVerticalSlice(level, marioSlice, 2);
        level = groupxutils.setVerticalSlice(level, firstSlice, 3);
        level = groupxutils.setVerticalSlice(level, exitSlice, width-2);
        level = groupxutils.setVerticalSlice(level, firstSlice, width-1);

        // Set firstSlice to generate the first neighbour - via sampling the SliceDistributions
        String currentSlice = firstSlice;
        String nextSlice = "";

        // Boolean to check if 6 same pieces have come in a row - boring
        int boring = 0;

        for(int i = 4; i < 148; i++){
            SliceDistribution temp = SliceDistributions.get(currentSlice);
            nextSlice = temp.sample(r);

            // Make sure level isn't getting boring (many slices same type in a row)
            if(nextSlice.equals(currentSlice)) boring+=1;
            else boring = 0;

            // If it's getting boring, sample a random slice instead.
            if(boring == 6){
              while(nextSlice.equals(currentSlice)){
                  nextSlice = allSlices.get(r.nextInt(nSlices));
              }
              boring = 0;
            }

            level = groupxutils.setVerticalSlice(level, nextSlice, i);
            currentSlice = nextSlice;

        }
        //System.out.println(level);
        return level;
    }

    public String sampleRandomSlice() {
        Random r = new Random();
        int nSlices = allSlices.size();
        return allSlices.get(r.nextInt(nSlices));
    }

    public String sampleNextSlice() {
        int nSlices = allSlices.size();
        return allSlices.get(r.nextInt(nSlices));
    }

    public String sampleNextSlice(String slice){
        // If it's a known slice, return from sample. Else return random.
        if(SliceDistributions.containsKey(slice)){
            return SliceDistributions.get(slice).sample(r);
        } else {
            return sampleRandomSlice();
        }

    }

    public String initializeEmptyLevel(int width){
        // width of width + 1 extra newline character at each line
        String emptyLevel = "";

        for(int i=0; i<16;i++){

            for(int j=0; j<width; j++){
                emptyLevel += "-";
            }
            emptyLevel += "\n";
        }
        return emptyLevel;
    }

    // De-Serializes the SliceDistributions HashMap and returns it
    public HashMap<String, SliceDistribution> retrieveSliceDistributions(){

        HashMap<String, SliceDistribution> map = null;
        try
        {
            FileInputStream fis = new FileInputStream(groupxdir+"SliceDistributions.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        System.out.println("Deserialized HashMap..");

        /* Display content using Iterator
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            System.out.print("key: "+ mentry.getKey() + " & Value: ");
            System.out.println(mentry.getValue());
        }
        */
        return map;
    }

    // De-Serializes the SliceDistributions HashMap and returns it
    public ArrayList<String> retrieveAllSlices(){

        ArrayList<String> array = null;
        try
        {
            FileInputStream fis = new FileInputStream(groupxdir+"allSlices.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            array = (ArrayList<String>) ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        System.out.println("Deserialized ArrayList..");

        return array;
    }

    public void makeDistribution(){

        // Set paths to working directory and mario levels
        String levelFilenamePrefix = workingdir+"/levels/original/lvl-";

        // Create HashMap of SliceDistributions
        HashMap<String, SliceDistribution> SliceDistributions = new HashMap<>();

        // Loop over the levels
        for(int i=1; i<=15; i++){ //15

            // Printing to keep track which level
            System.out.println("Currently doing level "+i);
            // Retrieve the current level string
            String level = retrieveLevel(levelFilenamePrefix+i+".txt");

            // Get information about current level dimensions
            int levelLength = level.length();
            int levelWidth = levelLength/16;

            // For each vertical slice, see it's immediate neighbour (to the right) and document it in the hashmap
            for(int j = 0; j < levelWidth-1; j++){
                //Set current and immediate next slice
                String currentSlice = groupxutils.getVerticalSlice(level, j);
                String nextSlice = groupxutils.getVerticalSlice(level, j+1);

                //if string contains mario start or exit, just skip it
                if(nextSlice.contains("M") || nextSlice.contains("F")){
                    continue;
                }

                //Update HashMap with these slices
                if(SliceDistributions.containsKey(currentSlice)){
                    SliceDistribution temp = SliceDistributions.get(currentSlice);
                    temp.update(nextSlice);
                }else{
                    SliceDistribution temp = new SliceDistribution(nextSlice);
                    SliceDistributions.put(currentSlice, temp);
                }
            }
        }

        // Add normal slice to 1 populated hashmap entries to even out distributions and prevent repetitions
        for (Map.Entry<String, SliceDistribution> entry : SliceDistributions.entrySet()) {
            // Add an escape when there are only a few options
            //if(SliceDistributions.get(entry.getKey()).Size() <= 4) {}
            SliceDistributions.get(entry.getKey()).update("--------------XX");

            //MB: THIS COULD BE REMOVED!!!! Make sure vanilla has all possible
            SliceDistributions.get("--------------XX").update(entry.getKey());

        }

        ArrayList<String> allSlices = new ArrayList<>();

        // Make an array with all types of slices
        for (Map.Entry<String, SliceDistribution> entry : SliceDistributions.entrySet()) {
            allSlices.add(entry.getKey());
        }

        // Saving both: SliceDistributions and AllSlices
        try
        {
            FileOutputStream fos = new FileOutputStream(groupxdir+"SliceDistributions.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(SliceDistributions);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in "+groupxdir+"SliceDistributions.ser\n");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        //allslices
        try
        {
            FileOutputStream fos = new FileOutputStream(groupxdir+"allSlices.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allSlices);
            oos.close();
            fos.close();
            System.out.printf("Serialized ArrayList (allSlices data is saved in "+groupxdir+"allSlices.ser");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    //Return the name of the level generator
    public String getGeneratorName(){return "groupx";}
}
