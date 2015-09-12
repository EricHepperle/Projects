package AstarAlgorithm;

import javafx.scene.control.Button;

public class GridButton extends Button {

    private int x, y, gCost, hCost;
    private boolean walkable, start, goal;
    public GridButton parent;

    public GridButton(){
        this.setStyle("-fx-background-color: #FFFFFF");
        x = 0;
        y = 0;
        gCost = 0;
        hCost = 0;
        walkable = true;
        start = false;
        goal = false;
    } // end AstarAlgorithm.GridButton() default constructor

    public GridButton(int _x, int _y){
        this.setStyle("-fx-background-color: #FFFFFF");
        x = _x;
        y = _y;
        gCost = 0;
        hCost = 0;
        walkable = true;
        start = false;
        goal = false;
    } // end AstarAlgorithm.GridButton() 2 argument constructor

    public void reset(){
        start = false;
        goal = false;
        walkable = true;
        gCost = 0;
        hCost = 0;
        this.setStyle("-fx-background-color: #FFFFFF;");
    } // end reset()

    public void setWalkable(boolean b){
        walkable = b;
    }
    public void setStart(boolean b){
        start = b;
    }
    public void setGoal(boolean b){
        goal = b;
    }

    public boolean isWalkable(){
        return walkable;
    }
    public boolean isStart(){ return start; }
    public boolean isGoal(){ return goal; }
    public int fCost(){ return (hCost + gCost); }

    public void setRow(int _y){
        y = _y;
    }
    public int getRow(){
        return(y);
    }
    public void setColumn(int _x){
        x = _x;
    }
    public int getColumn(){
        return x;
    }

    public void setgCost(int g){
        gCost = g;
    }
    public int getgCost(){
        return gCost;
    }
    public void sethCost(int h){hCost = h;}
    public int gethCost(){return hCost;}

    @Override public String toString(){
        return String.format("(%d,%d)", x, y);
    }
} // end class AstarAlgorithm.GridButton
