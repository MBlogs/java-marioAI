package levelGenerators.groupx;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Xavier Weber on 11/26/19.
 */
public class SliceDistribution implements Serializable  {

    private HashMap<String, Integer> slicedistribution;
    private int totalSize = 0;
    
    // constructor
    public SliceDistribution() {
        slicedistribution = new HashMap<>();
    }

    public SliceDistribution(String firstSlice) {
        slicedistribution = new HashMap<>();
        slicedistribution.put(firstSlice, 1);
        totalSize += 1;
    }

    public HashMap<String, Integer> getDistribution(){
        return slicedistribution;
    }

    public String toString(){
        String s = "";
        for(HashMap.Entry<String, Integer> entry : slicedistribution.entrySet()){
            s += "Slice: "+entry.getKey()+ " has count "+entry.getValue()+"\n";
        }
       return s;
    }

    // Updates the distribution with the new slice
    public void update(String slice){
        // Key was already here, so add 1 to its value
        if(slicedistribution.containsKey(slice)){
            slicedistribution.put(slice, slicedistribution.get(slice)+1);
        }else{ // Key was not here, add it with a value of 1
            slicedistribution.put(slice, 1);
        }
        totalSize += 1;
    }
    
    public String sample(Random r){
        int rand = r.nextInt(totalSize);
        
        int currentSum = 0;

        for (Map.Entry<String, Integer> entry : slicedistribution.entrySet()) {
            String slice = entry.getKey();
            int value = entry.getValue();

            currentSum += value;

            if (rand <= currentSum){
                return slice;
            }
        }
        return "";
    }

    public int Size(){
        return this.totalSize;
    }
}