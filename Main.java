package AstarAlgorithm;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {


    BorderPane borderLayout;
    Button b_close, b_randomize, b_reset, b_clear, b_findPath, b_save, b_load;
    CheckBox cb_showOpen, cb_showClosed;
    FileChooser fileChooser = new FileChooser();
    GridButton map[][], start, goal;
    GridPane leftMenu, centerGrid;
    HBox topMenu;
    int windowSize = 815, mapSize = 49, currentOption = 0, openNodes = 0, pathNodes = 0, pathCost = 0;
    Label l_stats;
    long startTime, endTime, duration;
    Random rand = new Random();
    Scene scene1;
    Stage window;
    String s_map;
    StringProperty opened, traveled, cost, time, stats;
    ToggleButton tb_walkable, tb_start, tb_goal;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        window.setTitle("A* Simulator");
        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        // initialization
        map = new GridButton[mapSize][mapSize];
        s_map = new String();
        opened = new SimpleStringProperty("Nodes Opened: \n");
        traveled = new SimpleStringProperty("Nodes Traveled: \n");
        cost = new SimpleStringProperty("Path Cost: \n");
        time = new SimpleStringProperty("Rune Time: \n");
        stats = new SimpleStringProperty();
        stats.bind(Bindings.concat(opened).concat(traveled).concat(cost).concat(time));
        l_stats = new Label("Stats\n");
        l_stats.textProperty().bind(stats);
        l_stats.setStyle("-fx-font-color: #FFFFFF");

        // Buttons
        b_close = new Button("Close Program");
        b_close.setOnAction(e -> closeProgram());

        b_randomize = new Button("Random");
        b_randomize.setOnAction(e ->{
            for(int i = 0; i < mapSize*3; i++){
                int j = rand.nextInt(mapSize);
                int k = rand.nextInt(mapSize);
                map[j][k].setWalkable(false);
                map[j][k].setStyle("-fx-background-color: #000000");
            }
        });

        b_reset = new Button("Reset");
        b_reset.setOnAction(e -> {
            for (int x = 0; x < mapSize; x++) {
                for (int y = 0; y < mapSize; y++) {
                    map[x][y].reset();
                }
            }
            start = null;
            goal = null;
            openNodes = 0;
            pathNodes = 0;
            pathCost = 0;
            duration = 0;
            opened.setValue("Opened: " + openNodes + "\n");
            traveled.setValue("Traveled: " + pathNodes + "\n");
            cost.setValue("Path Cost: " + pathCost + "\n");
            time.setValue("Rune Time: " + duration + "\n");
        });

        b_clear = new Button("Clear");
        b_clear.setOnAction(e -> {
            for (int x = 0; x < mapSize; x++) {
                for (int y = 0; y < mapSize; y++) {
                    if (map[x][y].isWalkable() & !map[x][y].isGoal() & !map[x][y].isStart())
                        map[x][y].reset();
                }
            }
            openNodes=0;
            pathNodes=0;
            pathCost=0;
            duration = 0;
            opened.setValue("Opened: " + openNodes + "\n");
            traveled.setValue("Traveled: " + pathNodes + "\n");
            cost.setValue("Path Cost: " + pathCost + "\n");
            time.setValue("Rune Time: " + duration + "\n");
        });

        b_findPath = new Button("Find Path");
        b_findPath.setOnAction(e -> {
            openNodes=0;
            pathNodes=0;
            pathCost=0;
            duration=0;
            opened.setValue("Opened: " + openNodes + "\n");
            traveled.setValue("Traveled: " + pathNodes + "\n");
            cost.setValue("Path Cost: " + pathCost + "\n");
            time.setValue("Rune Time: " + duration/1000000 + "ms\n");
            for(int x = 0; x < mapSize; x++){
                for(int y = 0; y < mapSize; y++){
                    if(map[x][y].isWalkable() && !map[x][y].isStart() && !map[x][y].isGoal()){
                        map[x][y].reset();
                    }
                }
            }
            if(start != null && goal != null) {
                startTime = System.nanoTime();
                Pathfind(start, goal);
                endTime = System.nanoTime();
                duration = (endTime - startTime);
                time.setValue("Run Time: " + duration/1000000 + "ms\n");

            } else{
                // trying to find path with no start or goal
                AlertBox.display("Invalid Parameters","Please set a start and goal before pathfinding!");
            }
        });

        b_save = new Button("Save Map");
        b_save.setOnAction(event -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showSaveDialog(window);

            if(file != null){
                saveFile(file);
            }
        });

        b_load = new Button("Load Map");
        b_load.setOnAction(event -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(window);
            if(file != null){
                openFile(file);
            }
        });

        // Toggle Buttons----------------------------------------------------
        final ToggleGroup toggleButtonGroup = new ToggleGroup();
        tb_walkable = new ToggleButton("Walkable");
        tb_walkable.setToggleGroup(toggleButtonGroup);
        tb_walkable.setSelected(true);
        tb_start = new ToggleButton("Start");
        tb_start.setToggleGroup(toggleButtonGroup);
        tb_goal = new ToggleButton("Goal");
        tb_goal.setToggleGroup(toggleButtonGroup);

        toggleButtonGroup.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue == tb_walkable) {
                currentOption = 0;
            }
            if (newValue == tb_start) {
                currentOption = 1;
            }
            if (newValue == tb_goal) {
                currentOption = 2;
            }
        });

        VBox toggleBox = new VBox();
        toggleBox.getChildren().addAll(tb_walkable, tb_start, tb_goal);

        // Checkboxes
        cb_showOpen = new CheckBox("Show Open");
        cb_showClosed = new CheckBox("Show Closed");
        cb_showOpen.setSelected(true);
        cb_showClosed.setSelected(true);

        // Layouts
        topMenu = new HBox();
        topMenu.setPadding(new Insets(5, 5, 5, 5));
        topMenu.setSpacing(20);
        topMenu.setStyle(" -fx-background-color: #335599; -fx-border-color: black; -fx-border-width: 1;");

        leftMenu = new GridPane();
        leftMenu.setMinWidth(120);
        leftMenu.setMaxWidth(200);
        leftMenu.setPadding(new Insets(10, 10, 10, 10));
        leftMenu.setHgap(10);
        leftMenu.setVgap(10);
        leftMenu.setStyle(" -fx-background-color: #335599; -fx-border-color: black; -fx-border-width: 1;");

        centerGrid = new GridPane();
        centerGrid.setPadding(new Insets(10, 10, 10, 10));
        centerGrid.setHgap(0);
        centerGrid.setVgap(0);
        centerGrid.setStyle(" -fx-background-color: #353535;");

        // Add items to each menu
        topMenu.getChildren().addAll(b_close, b_save, b_load);
        leftMenu.setConstraints(b_randomize, 0, 1);
        leftMenu.setConstraints(b_reset, 0, 2);
        leftMenu.setConstraints(b_clear, 0, 3);
        leftMenu.setConstraints(toggleBox, 0, 6);
        leftMenu.setConstraints(cb_showOpen, 0, 8);
        leftMenu.setConstraints(cb_showClosed, 0, 9);
        leftMenu.setConstraints(b_findPath, 0, 11);
        leftMenu.setConstraints(l_stats, 0, 16);
        leftMenu.getChildren().addAll(b_randomize, b_reset, b_clear, toggleBox, cb_showOpen, cb_showClosed, b_findPath, l_stats);

        // initialize AstarAlgorithm.GridButton map
        for(int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                map[x][y] = new GridButton(x,y);
                centerGrid.add(map[x][y], x, y);
                map[x][y].setOnMousePressed(mouseHandler);
                map[x][y].setStyle(" -fx-background-color: #FFFFFF;");
                map[x][y].setText(" ");
                map[x][y].setAlignment(Pos.CENTER);
                map[x][y].setMinWidth(12);
                map[x][y].setMaxWidth(30);
                map[x][y].setMaxHeight(30);
                map[x][y].setMinHeight(12);
            }
        }

        // Set up border layout
        borderLayout = new BorderPane();
        borderLayout.setTop(topMenu);
        borderLayout.setLeft(leftMenu);
        borderLayout.setCenter(centerGrid);

        // Set up scene and window
        scene1 = new Scene(borderLayout, windowSize + mapSize + 70, windowSize + mapSize - 20);

        window.setScene(scene1);
        window.setTitle("A* Simulator");
        window.setMinHeight(540);
        window.setMinWidth(600);
        // window.setResizable(false);
        window.show();
    } // end start()

    EventHandler<MouseEvent> mouseHandler = new EventHandler< MouseEvent>() {
        public void handle( MouseEvent mouseEvent) {
            for (int x = 0; x < mapSize; x++) {
                for (int y = 0; y < mapSize; y++) {
                    if (mouseEvent.getSource() == map[x][y]) {
                        // clicked on map
                        if (mouseEvent.isPrimaryButtonDown()) {
                            if (currentOption == 0) {
                                map[x][y].setStyle(" -fx-background-color: #000000");
                                map[x][y].setStart(false);
                                map[x][y].setGoal(false);
                                map[x][y].setWalkable(false);
                                if(start == map[x][y])
                                    start = null;
                                if(goal == map[x][y])
                                    goal = null;
                            } // TOGGLE WALKABLE
                            if (currentOption == 1) {
                                if(start != null) {
                                    start.setStart(false);
                                    start.setStart(false);
                                    start.setStyle(" -fx-background-color: #FFFFFF");
                                }
                                map[x][y].setStyle("-fx-background-color: #ffff66");
                                map[x][y].setStart(true);
                                start = map[x][y];
                                map[x][y].setGoal(false);
                                map[x][y].setWalkable(true);
                            } // TOGGLE START
                            if (currentOption == 2) {
                                if(goal != null) {
                                    goal.setWalkable(true);
                                    goal.setGoal(false);
                                    goal.setStyle(" -fx-background-color: #FFFFFF");
                                }
                                map[x][y].setStyle(" -fx-background-color: #FF0000");
                                map[x][y].setStart(false);
                                map[x][y].setGoal(true);
                                goal = map[x][y];
                                map[x][y].setWalkable(true);
                            } // TOGGLE GOAL
                        } // end isPrimaryButtonDown
                        if (mouseEvent.isSecondaryButtonDown()) {
                            if(map[x][y] == start){
                                start = null;
                            }
                            if(map[x][y] == goal){
                                goal = null;
                            }
                            map[x][y].setStyle(" -fx-background-color: #FFFFFF");
                            map[x][y].setStart(false);
                            map[x][y].setGoal(false);
                            map[x][y].setWalkable(true);
                        } // end isSecondaryButtonDown
                    }// end CLICK is a button on map[][]
                }
            }
        } // end handle
    }; // end Mouse Event Handler

    public void Pathfind(GridButton start, GridButton goal) {
        ArrayList<GridButton> openSet = new ArrayList<>();
        ArrayList<GridButton> closedSet = new ArrayList<>();
        openSet.add(start);
        start.setgCost(0);
        start.sethCost(getDistance(start, goal));

        while(!openSet.isEmpty()){
            GridButton current = openSet.get(0);
            for(int i = 0; i < openSet.size(); i++){
                if(openSet.get(i).fCost() < current.fCost()) {
                    current = openSet.get(i);
                }
            }

            if(current == goal){
                retracePath(start, goal);
                opened.setValue("Opened: " + openNodes + "\n");
                traveled.setValue("Traveled: " + pathNodes + "\n");
                cost.setValue("Path Cost: " + pathCost + "\n");
                return;}

            openSet.remove(current);
            closedSet.add(current);
            if(current != start & cb_showClosed.isSelected())
                current.setStyle("-fx-background-color: #99FFFF;");

            GridButton neighbor;
            ArrayList<GridButton> neighbors = new ArrayList<>(this.getNeighbors(current));
            for(int i = 0; i < neighbors.size(); i++){
                neighbor = neighbors.get(i);
                if(!neighbor.isWalkable() || closedSet.contains(neighbor)){
                    continue;
                }

                int newMovementCostToNeighbor = current.getgCost() + getDistance(current, neighbor);
                if(newMovementCostToNeighbor < neighbor.getgCost() || !openSet.contains(neighbor)) {
                    neighbor.setgCost(newMovementCostToNeighbor);
                    neighbor.sethCost(getDistance(neighbor, goal));
                    neighbor.parent = current;

                    if(!openSet.contains(neighbor)){
                        openSet.add(neighbor);
                        openNodes += 1;
                        if(neighbor != goal & cb_showOpen.isSelected())
                            neighbor.setStyle("-fx-background-color: #99FF99;");
                    }
                }
            } // end checking neighbors
        } // end while
        AlertBox.display("Error", "No Path found.");
    }// end pathfind

    public int getDistance(GridButton a, GridButton b){
        int distanceY = Math.abs(a.getRow() - b.getRow());
        int distanceX = Math.abs(a.getColumn() - b.getColumn());

        if(distanceX > distanceY){
            return (14*distanceY + 10*(distanceX - distanceY));
        }
        return (14*distanceX + 10*(distanceY - distanceX));
    }

    public void retracePath(GridButton start, GridButton goal){
        ArrayList<GridButton> path = new ArrayList<>();
        GridButton current = goal;

        while(current != start){
            path.add(current);
            pathCost += getDistance(current, current.parent);
            current = current.parent;
            pathNodes += 1;
            if(current != start)
                current.setStyle("-fx-background-color: #3300ff");
        }
        Collections.reverse(path);
    }

    public ArrayList<GridButton> getNeighbors(GridButton node){
        ArrayList<GridButton> neighbors = new ArrayList<>();
        int row = node.getRow();
        int column = node.getColumn();
        // up
        if(row-1 >= 0 && map[column][row-1].isWalkable()) {
            neighbors.add(map[column][row-1]);
        }
        // right
        if(column+1 < mapSize && map[column+1][row].isWalkable()) {
            neighbors.add(map[column+1][row]);
        }
        //down
        if(row +1 < mapSize && map[column][row+1].isWalkable()) {
            neighbors.add(map[column][row+1]);
        }
        //left
        if(column-1 >= 0 && map[column-1][row].isWalkable()) {
            neighbors.add(map[column-1][row]);
        }
        // up right
        if(row-1 >= 0  && column+1 < mapSize && (map[column][row-1].isWalkable() || map[column+1][row].isWalkable())){
            neighbors.add(map[column+1][row-1]);
        }
        // down right
        if(row +1 < mapSize && column+1 < mapSize && (map[column][row+1].isWalkable() || map[column+1][row].isWalkable())){
            neighbors.add(map[column+1][row+1]);
        }
        // down left
        if(row +1 < mapSize && column-1 >= 0 && (map[column][row+1].isWalkable() || map[column-1][row].isWalkable())){
            neighbors.add(map[column-1][row+1]);
        }
        // up left
        if(row-1 >= 0 && column-1 >= 0 && (map[column][row-1].isWalkable() || map[column-1][row].isWalkable())){
            neighbors.add(map[column-1][row-1]);
        }
        return neighbors;
    }

    private void saveFile(File file){
        s_map = "";
        for(int i = 0; i < mapSize; i++){
            for(int j = 0; j < mapSize; j++){
                if(map[j][i].isWalkable()){
                    s_map += 0;
                } else{
                    s_map += 1;
                }
            }
        }
        try{
            FileWriter outputWriter = null;
            outputWriter = new FileWriter(file);
            outputWriter.write(s_map);
            outputWriter.close();
        }catch(IOException ex){
            Logger.getLogger(
                    Main.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    private void openFile(File file){
        Scanner input;
        int i = 0;
        try {
            input = new Scanner(file);
            s_map = input.next();
            for (int x = 0; x < mapSize; x++) {
                for (int y = 0; y < mapSize; y++) {
                    if (s_map.charAt(i) == '0') {
                        map[y][x].setWalkable(true);
                        map[y][x].setStart(false);
                        map[y][x].setGoal(false);
                        map[y][x].setStyle(" -fx-background-color: #FFFFFF");
                    }
                    if(s_map.charAt(i) == '1'){
                        map[y][x].setWalkable(false);
                        map[y][x].setStart(false);
                        map[y][x].setGoal(false);
                        map[y][x].setStyle(" -fx-background-color: #000000");
                    } i++;
                }
            }
            input.close();
        } catch(FileNotFoundException fileNotFoundException){
            System.err.println("Error Opening file.");
            System.exit(1);
        }
    } // end openFile

    private void closeProgram(){
        boolean answer = ConfirmBox.display("Title", "Are you sure you want to exit?");
        if(answer){
            window.close();
        }
    } // end closeProgram

}// end AstarAlgorithm.Main()

