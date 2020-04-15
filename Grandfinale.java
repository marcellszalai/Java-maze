/*
* File: DumboController.java
* Created: 17 September 2002, 00:34
* Author: Stephen Jarvis
*/

import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Arrays; //i think i need it for arrays

public class Grandfinale {

  private int pollRun = 0;  //Incremented after each pass
  private RobotData robotData; // Data store for junctions
  private int explorerMode; //When 1 the robot is in explorermode, else in backtrackmode

  int direction; //What is the robots next move
  int randno; //A random number for when the robot has multiple choices
  int updateJunction = 0; //The heading out of the junction is reset every time the robot enters

  /*
  The first method is to reset the junctioncounter in the robotdata class,
  every time the robot goes for a new run,
  and to set explorermode to 0, since in the beginning of each run the robor
  is in explorermode. Also prints out all the junctions for convenience.
  */
  public void reset(){
    robotData.printAllJunctions();
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
   if (robot.getRuns()==0){ //only in the first run
     if (updateJunction==1){ //any time the robot steps into a junction
      robotData.junctionHeadingTo(robot); //the heading out is updated
      robotData.printJunction();
      updateJunction = 0; //the robot is no longer in the junction
     }

     if (walls(robot) == 0 || walls(robot) == 1){
      if (beenbefores(robot) == 1){
        robotData.recordJunction(robot);
      }
      updateJunction = 1; //regardless of the beenbefores
     }
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
  This method is for the second run where the robot knows the path from the
  junctions saved in the first run. It gets the heading out from the junctions
  and turns ito the right direction.
  */
  private int secondRunJunction(IRobot robot){
    String junctionHeading = robotData.secondSearchJunction(robot);
    //uses the secondSearchJunction for the robotData class
    if (junctionHeading == "North"){
      direction = IRobot.NORTH;
    }
    else if (junctionHeading == "East"){
      direction = IRobot.EAST;
    }
    else if (junctionHeading == "South"){
      direction = IRobot.SOUTH;
    }
    else{
      direction = IRobot.WEST;
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
         case 0: //if it is a junction the robot separates the two cases by checking the number of runs so far
         case 1: if (robot.getRuns() == 0){ // if it is the first run then everything is the same
                  direction = junction(robot);
                  robot.face(direction);}
                 else { //otherwise the direction is set according to the secondRunJunction method
                   direction = secondRunJunction(robot);
                   robot.setHeading(direction);}
           break;
         case 2: direction = corridor(robot);
          robot.face(direction);
          break;
         case 3: direction = deadend(robot);
          robot.face(direction);
          explorerMode = 0;
        }
    }

  /*
  When the robot is in backtrackMode the only difference is when it arrives in a junction.
  However since it sometimes turns into an absolute direction we need to separate things.
  */
  private void backtrackControl(IRobot robot){
    switch (walls(robot)){
     case 0:
     case 1: if (robot.getRuns() == 0){
               if (passages(robot)==0) {
                 /*if there is no passage it means that the robot stays in backtrackMode
                 and goes to a direction from where it originally came from when it first
                 entered the junction. */
                 robot.setHeading(backtrackJunction(robot));
                 /* This junction is always the one that was added the last time so it
                 can be deleted since  in a non-loopy maze the robot can never return there */
                 robotData.removeJunction(robot);
               }
               else{ //otherwise it switches to explorerMode.
                 direction = junction(robot);
                 robot.face(direction);
                 explorerMode = 1;
               }
             }
             else{ //if it is the second run the heading is set to the heading out of the junction stored in the previous run
               direction = secondRunJunction(robot);
               robot.setHeading(direction);
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

  private static int maxJunctions = 10000; //maximal number of junctions that can occur
  private static int junctionCounter; //counts the number of junctions
  private String[] juncHeadingFrom; //heading of the robot when it first arrived in the junction
  private String[] juncHeadingTo; //headign of the robot when it last left the junction

  RobotData(){ //we need the heading out too now for the second run
    juncHeadingFrom = new String[maxJunctions] ;
    juncHeadingTo = new String[maxJunctions] ;
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
    switch (robot.getHeading()){
      case IRobot.NORTH : juncHeadingFrom[junctionCounter] = "North";
        break;
      case IRobot.EAST : juncHeadingFrom[junctionCounter] = "East";
        break;
      case IRobot.SOUTH : juncHeadingFrom[junctionCounter] = "South";
        break;
      case IRobot.WEST : juncHeadingFrom[junctionCounter] = "West";
        break;
    }
  }

  public void printJunction(){ // prints the junction with the 2 headings every time the robot enters a junction
    System.out.println("Junction " + junctionCounter + " heading from=" + juncHeadingFrom[junctionCounter] +  " heading to=" + juncHeadingTo[junctionCounter]);
  }

  public String searchJunction(IRobot robot){ //Searches for the last function in the array and returns the heading
    return juncHeadingFrom[junctionCounter];
  }

  public String secondSearchJunction(IRobot robot){ //returns the heading out of the current junction to set the direction of the robot
    junctionCounter++; //increments the junctioncounter
    return juncHeadingTo[junctionCounter];
  }

  public void removeJunction(IRobot robot){ //removes the last junction beacuse we dont need it anymore
    juncHeadingFrom[junctionCounter] =juncHeadingFrom[junctionCounter+1];
    juncHeadingTo[junctionCounter] =juncHeadingTo[junctionCounter+1];
    junctionCounter--; //decrements the junctioncounter
  }
  /*
  This method updates the junctions heading out every time the robot enters a junction.
  Also saves it in an array
  */
  public void junctionHeadingTo(IRobot robot){
    switch (robot.getHeading()){
      case IRobot.NORTH : juncHeadingTo[junctionCounter] = "North";
        break;
      case IRobot.EAST : juncHeadingTo[junctionCounter] = "East";
        break;
      case IRobot.SOUTH : juncHeadingTo[junctionCounter] = "South";
        break;
      case IRobot.WEST : juncHeadingTo[junctionCounter] = "West";
        break;
    }
  }

  public void printAllJunctions(){ //prints all of the junctions
    for(int n=1;n<=junctionCounter;n++){
      System.out.println("Junction " + n + " heading from=" + juncHeadingFrom[n] +  " heading to=" + juncHeadingTo[n]);
    }
  }
}
