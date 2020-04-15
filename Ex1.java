/*
* File: DumboController.java
* Created: 17 September 2002, 00:34
* Author: Stephen Jarvis
*/

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1 {

  private int pollRun = 0;  //Incremented after each pass
  private RobotData robotData; // Data store for junctions
  private int explorerMode; //When 1 the robot is in explorermode, else in backtrackmode

  int direction; //What is the robots next move
  int randno; //A random number for when the robot has multiple choices

  /*
  The first method is to reset the junctioncounter in the robotdata class,
  every time the robot goes for a new run,
  and to set explorermode to 0, since in the beginning of each run the robor
  is in explorermode.
  */
  public void reset(){
    robotData.resetJunctionCounter();
    explorerMode = 1;
  }

  /*
  The most significant method is the controlRobot. It is respontsible for
  moving the robot in the right direction after considering a set of rules
  */
  public void controlRobot(IRobot robot) {
   if ((robot.getRuns() == 0) && pollRun == 0){ // creates a new object robotData at the first step of the first run
     robotData = new RobotData();
   }
   pollRun ++; //increase pollRun so that robotData is not created after every step
   if ((walls(robot) == 0 || walls(robot) == 1) && beenbefores(robot) == 1){ //The robot  know that it is new junction if the only beenbefore square ids the one where it came from.
    robotData.recordJunction(robot); //A new junction is recorded in the robotData class
    robotData.printJunction(); //The same junction is printed out
   }
   if(explorerMode == 1) // if the robot is in explorerMode it uses the exploreControl method to find the right direction
    exploreControl(robot);
   else // if it is in backtrackmode, then it uses the backtrackControl method
    backtrackControl(robot);
  }

  /*
  The next 3 methods are to find the number of walls, passages and beenbefores,
  which is often very useful, when looking for the direction
  */
  private int walls(IRobot robot){ // The method to find the walls around the robot
     int walls = 0;
     for(int i = IRobot.AHEAD; i <= IRobot.LEFT; i++){ // looks in every direction and every time it sees a wall the number of walls increases
       if (robot.look(i) == IRobot.WALL)
         walls++;
     }
     return walls;
   }

  private int passages(IRobot robot){ // The method to find the passages around the robot
     int passages = 0;
     for(int i = IRobot.AHEAD; i <= IRobot.LEFT; i++){ // looks in every direction and every time it sees a passage the number of passages increases
       if (robot.look(i) == IRobot.PASSAGE)
         passages++;
     }
     return passages;
   }

  private int beenbefores(IRobot robot){ // The method to find the beenbefores around the robot
    int beenbefores = 0;
    for(int i = IRobot.AHEAD; i <= IRobot.LEFT; i++){ // looks in every direction and every time it sees a beenbefore the number of beenbefores increases
      if (robot.look(i) == IRobot.BEENBEFORE)
        beenbefores++;
    }
    return beenbefores;
  }

  /*
  The following methods are to determine the type of spuare, where the robot is
  at a given time by the blocks around it and decide the direction where the robot should move
  */
  /*
  The first of these is the corridor where there are two walls around import junit.framework.TestCase;
  Important to know that corners are also corridors.
  */
  private int corridor(IRobot robot){
    for (int n=IRobot.AHEAD; n<=IRobot.LEFT; n++){ // The robot checks for every direction
      if (n!=IRobot.BEHIND && robot.look(n)!=IRobot.WALL){ // the two statements that have to be true are that the robot cannot turn back and cant run into a wall
        direction = n;
      }
    }
    return direction;
  }
  /*
  The junction method is called when there is 0 or 1 wall around the robot
  It chooses randomly from the exits which are not walls or beenbefores.
  */
  private int junction(IRobot robot){
        do {
         randno = (int)(Math.random()*3);
         if (randno == 0){
           direction = IRobot.LEFT;
         }
         else if (randno == 1){
           direction = IRobot.RIGHT;
         }
         else{
           direction = IRobot.AHEAD;
         }
       }
      while (robot.look(direction) == IRobot.WALL || robot.look(direction) == IRobot.BEENBEFORE);

    return direction;
  }

  /*
  The backtrackjunction is called when the robot is in backtrackmode and there is 1 or 0 walls around import junit.framework.TestCase;
  It takes the heading that the robot initially had when it first arrived at that junction and turns to its reverse.
  */
  private int backtrackJunction(IRobot robot){
      String junctionHeading = robotData.searchJunction(robot); //it uses the searchJunction method form the robotData clss to find the junction
      if (junctionHeading == "North"){
        direction = IRobot.SOUTH;
      }
      else if (junctionHeading == "East"){
        direction = IRobot.WEST;
      }
      else if (junctionHeading == "South"){
        direction = IRobot.NORTH;
      }
      else{
        direction = IRobot.EAST;
      }
    return direction;
  }

  /*
  The deadend method is called when there is only one non-wall exit.
  It would be more simple if it would just return IRobot.BEHIND, but we need
  to make sure that it doesn't get stuck in the beginning.
  */
  private int deadend(IRobot robot){
    do{ //tries every possible way until it finds a way out
      direction = IRobot.AHEAD+(int)(Math.random()*4);
    }
    while(robot.look(direction) == IRobot.WALL);
    return direction;
  }

  /*
  The exploreControl method decided the type of block where the robot is currently
  in with the help of the walls method and faces the robot in the right direction
  If it is a deadend, then the robot turns into backtrackMode.
  */
  private void exploreControl(IRobot robot){
    switch (walls(robot)){
     case 0:
     case 1: direction = junction(robot);
       break;
     case 2: direction = corridor(robot);
      break;
     case 3: direction = deadend(robot);
      explorerMode = 0; // backtrackMode activated

    }
    robot.face(direction);
  }

  /*
  When the robot is in backtrackMode the only difference is when it arrives in a junction.
  However since it sometimes turns into an absolute direction we need to separate things.
  */
  private void backtrackControl(IRobot robot){
    switch (walls(robot)){
     case 0:
     case 1: if (passages(robot)==0) {robot.setHeading(backtrackJunction(robot));}
     //if there is no passage it means that the robot stays in backtrackMode and goes to a direction from where it originally came from when it first entered the junction
       else { //otherwise it switches to explorerMode.
         direction = junction(robot);
         robot.face(direction);
         explorerMode = 1;
       }
       break;
     case 2: direction = corridor(robot);
      robot.face(direction);
      break;
     case 3: direction = deadend(robot);
      robot.face(direction);
    }
  }
}

/*
The function of the robotData class is to record the junctions and be able to
return them if they are needed later.
*/
class RobotData {

  private static int maxJunctions = 200000; //maximal number of junctions that can occur
  private static int junctionCounter; //counts the number of junctions
  private int[] juncX; //x coordinate for the junctions
  private int[] juncY; //y coordinate for the junctions
  private String[] juncHeading; //heading for the junctions

  RobotData(){
    juncX = new int[maxJunctions] ;
    juncY = new int[maxJunctions] ;
    juncHeading = new String[maxJunctions] ;
  }

  public void resetJunctionCounter(){ // resets the junctioncounter for every run
    junctionCounter = 0;
  }
  /*
  Gets the location and the the heading of the robot and stores them when the robot
  is in a junction. Also increments the junctionCounter.
  */
  public void recordJunction(IRobot robot){
    junctionCounter++; //increments the junctionCounter
    juncX[junctionCounter] = robot.getLocation().x;
    juncY[junctionCounter] = robot.getLocation().y;
    switch (robot.getHeading()){
      case IRobot.NORTH : juncHeading[junctionCounter] = "North";
        break;
      case IRobot.EAST : juncHeading[junctionCounter] = "East";
        break;
      case IRobot.SOUTH : juncHeading[junctionCounter] = "South";
        break;
      case IRobot.WEST : juncHeading[junctionCounter] = "West";
        break;
    }
  }

  public void printJunction(){ // prints the current junction, with coordinates and the heading
    System.out.println("Junction " + junctionCounter + " x=" + juncX[junctionCounter] + " y=" + juncY[junctionCounter] + " heading=" + juncHeading[junctionCounter]);
  }
  /*
  Searches for a specific junction by comparing the the coordiantes of the robot and the junctions stored
  Returns the heading of this junction.
  */
  public String searchJunction(IRobot robot){
    String returnString = "";
    for(int i = 0; i <= junctionCounter; i++){
      if (juncX[i] ==robot.getLocation().x && juncY[i] ==robot.getLocation().y){
        returnString = juncHeading[i];
      }
    }
    return returnString;
  }
}
