package levelGenerators.projectx;

import engine.core.MarioLevelModel;
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
    public String projectxdir = "";

    // Constructor
    public LevelGenerator(){
        this.workingdir = System.getProperty("user.dir");
        this.projectxdir = workingdir+"/src/levelGenerators/projectx/";
    }

    /**
     * Generate a playable mario level
     *
     * @param model contain a model of the level
     */
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer){

        // INIT random
        Random r = new Random();

        // Clear the map
        model.clearMap();

        // Get HashMap with SliceDistributions
        HashMap<String, SliceDistribution> SliceDistributions = retrieveSliceDistributions();
        int length = SliceDistributions.size();

        // Get ArrayList with all slices
        ArrayList<String> allSlices = retrieveAllSlices();
        int nSlices = allSlices.size();

        // Initialize an empty level of desired width
        int width = 151;
        String level = initializeEmptyLevel(width);

        // Set first slice - TODO: or get random
        String firstSlice = "--------------XX";
        String marioSlice = "-------------MXX";
        String exitSlice = "-------------FXX";

        level = setVerticalSlice(level, firstSlice, 0, width);
        level = setVerticalSlice(level, firstSlice, 1, width);
        level = setVerticalSlice(level, marioSlice, 2, width);
        level = setVerticalSlice(level, firstSlice, 3, width);
        level = setVerticalSlice(level, exitSlice, width-2, width);
        level = setVerticalSlice(level, firstSlice, width-1, width);

        System.out.println(level);

        // Set firstSlice to generate the first neighbour - via sampling the SliceDistributions
        String currentSlice = firstSlice;
        String nextSlice = "";

        // Boolean to check if 6 same pieces have come in a row - boring
        int boring = 0;

        for(int i = 4; i < 148; i++){
            SliceDistribution temp = SliceDistributions.get(currentSlice);
            nextSlice = temp.sample();

            // check boringness
            if(nextSlice.equals(currentSlice)) boring+=1;
            else boring = 0;

            if(boring == 6){
              while(nextSlice.equals(currentSlice)){
                  nextSlice = allSlices.get(r.nextInt(nSlices));
              }
              boring = 0;
            }


            level = setVerticalSlice(level, nextSlice, i, width);
            currentSlice = nextSlice;

        }
        System.out.println(level);
        return level;
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
            FileInputStream fis = new FileInputStream(projectxdir+"SliceDistributions.ser");
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
            FileInputStream fis = new FileInputStream(projectxdir+"allSlices.ser");
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
                String currentSlice = getVerticalSlice(level, j, levelWidth);
                String nextSlice = getVerticalSlice(level, j+1, levelWidth);

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

        ArrayList<String> allSlices = new ArrayList<>();

        // Make an array with all types of slices
        for (Map.Entry<String, SliceDistribution> entry : SliceDistributions.entrySet()) {
            allSlices.add(entry.getKey());
        }

        // Saving both: SliceDistributions and AllSlices
        try
        {
            FileOutputStream fos = new FileOutputStream(projectxdir+"SliceDistributions.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(SliceDistributions);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in "+projectxdir+"SliceDistributions.ser\n");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        //allslices
        try
        {
            FileOutputStream fos = new FileOutputStream(projectxdir+"allSlices.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allSlices);
            oos.close();
            fos.close();
            System.out.printf("Serialized ArrayList (allSlices data is saved in "+projectxdir+"allSlices.ser");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public String getVerticalSlice(String level, int index, int width){

        // Remove all 'newlines'
        level = level.replace("\n", "").replace("\r", "");

        // Notify if index is bigger than level
        if(index > width){
            System.out.println("XW: Index is out of bounds - getVerticalSlice()");
        }

        // Init vertical slice as a string
        String slice = "";

        // get all tiles in this vertical slice at the given horizontal index
        for(int i = 0; i < 16; i++){
            char tile = level.charAt(index + (i*width));
            slice += Character.toString(tile);
        }
        return slice;
    }

    public String setVerticalSlice(String level, String slice, int index, int width){

        // NOTE: Remember that level has 'newline' characters.

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

    //Return the name of the level generator
    public String getGeneratorName(){return "projectx";}
}
