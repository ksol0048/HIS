package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestData{
    private List<String> newValues;
    private int subListIndex;
    private int listIndex;
    //add일경우 true, delete일경우 false
    boolean addOrDelete;

    public void clear(){
        this.newValues = new ArrayList<>();
        this.subListIndex = 0;
        this.listIndex = 0;
    }
}