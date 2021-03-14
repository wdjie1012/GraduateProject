package model;

import java.util.Arrays;

public class ConflictArea {
    public final Region[] regions;
    private int mode;

    public ConflictArea(int mode, Region[] regions){
        this.mode=mode;
        if(mode==2)
            regions =new Region[3];
        else if(mode==4)
            regions =new Region[5];
        this.regions = regions;
    }

    public void clear(){
        Arrays.fill(regions, null);
        mode=-1;
    }
}




