/*
* File: DumboController.java
* Created: 17 September 2002, 00:34
* Author: Stephen Jarvis
*/

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex3 {

  private int pollRun = 0;  //Incremented after each pass
  private RobotData robotData; // Data store for junctions

  int direction; //What is the robots next move
  int randno; //A random number for when the robot has multiple choices

  /*
  The reset method is activated at every run and resets the junctioncounter.
  */
  public void reset(){
    robotData.resetJunctionCounter();
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

   switch (walls(robot)){ // the robot faces according to the type of block where it is currently staying
    case 0:
    case 1: direction = junction(robot);
      break;
    case 2: direction = corridor(robot);
     break;
    case 3: direction = deadend(robot);
   }
   robot.face(direction);
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


  private int junction(IRobot robot) { //when the number of walls is 0 or 1
      if (robotData.searchJunction(robot) == -1) { //if there is no junction store in the robotData class with the robots current coordinates
        robotData.recordJunction(robot); //A new junction is recorded in the robotData class
      }
      if (passages(robot)!=0){ //if the passages around the robot is not 0
        do { // chooses a randomly from the available passages
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

        robotData.addBeenOnce(robot, converter(robot, direction)); //converts the robots direction into an absolute direction and stores it in an array in the robotData class
        if (robotData.searchBeenOnce(robot, converter(robot, IRobot.BEHIND))){ // if the robot already entered or left the junction from IRobot.BEHIND then the beentwice of the junction is set to 1
          robotData.addBeenTwice(robot, converter(robot, IRobot.BEHIND));
        }
        else robotData.addBeenOnce(robot, converter(robot, IRobot.BEHIND)); // otherwise the beenonce is set to one
      }
      else{ // if there is no more passages, then it gets a little more complicated
        boolean tremaux = false; //we use the tremaux algorithm so I name this booleanvariable after him
        if (robotData.searchBeenOnce(robot, converter(robot, IRobot.BEHIND))==false){  //if the robot uses that direction for the first time to enter or leave the junction
          robotData.addBeenOnce(robot, converter(robot, IRobot.BEHIND)); //beenonce is set to 1
        }
        for (int i=IRobot.AHEAD; i<=IRobot.LEFT; i++){ //the robot looks around
          if (robotData.searchBeenTwice(robot, converter(robot,i)) != true & robot.look(i)!=IRobot.WALL) tremaux=true; //if there is a way where the robot hasnt been so far and there is no wall
        }
        if (tremaux){ //then the robot can choose between these directions randomly
          do {
           randno = (int)(Math.random()*4);
           if (randno == 0){
             direction = IRobot.LEFT;
           }
           else if (randno == 1){
             direction = IRobot.RIGHT;
           }
           else if (randno == 2){
             direction = IRobot.BEHIND;
           }
           else{
             direction = IRobot.AHEAD;
           }
          }
          while ((robot.look(direction) == IRobot.WALL) || (robotData.searchBeenTwice(robot,converter(robot, direction))));
          robotData.addBeenTwice(robot, converter(robot, direction));
        }
        else { //otherwise it has to choose a random direction
          do {
           randno = (int)(Math.random()*4);
           if (randno == 0){
             direction = IRobot.LEFT;
           }
           else if (randno == 1){
             direction = IRobot.RIGHT;
           }
           else if (randno == 2){
             direction = IRobot.BEHIND;
           }
           else{
             direction = IRobot.AHEAD;
           }
         }
      while (robot.look(direction) == IRobot.WALL);
      }
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
  This very useful method converts the robot's relative into an absolute direction
  */
  private int converter(IRobot robot, int direction){
    direction=direction-IRobot.AHEAD; //we get a number between 0 and 3
    if (robot.getHeading()+direction <=IRobot.WEST) return robot.getHeading()+direction; //we need two case to avoid runtime errors
    else return robot.getHeading()-4+direction; //this has to be done since the absolute directions have only one value and does not go in cirecles
  }
}
/*
The function of the robotData class is to record the junctions and be able to
return them if they are needed later.
*/
class RobotData {

  private static int maxJunctions = 200000; //maximal number of junctions that can occur, turns out 10000 was not enough
  private static int junctionCounter; //counts the number of junctions
  private int[] juncX; //x coordinate for the junctions
  private int[] juncY; //y coordinate for the junctions
  private int[][][] tremaux; //a 3 dimensional junction
  private int junctionNow; //refers to the current junction

  RobotData(){
    juncX = new int[maxJunctions] ;
    juncY = new int[maxJunctions] ;
    tremaux = new int[maxJunctions][2][4]; // 200000x2x4 size junction 1. dimension: all the junctions, 2. dimension: beenonce and beentwice, 3. dimension: 4 surroundings
  }

  public void resetJunctionCounter(){ // resets the junctioncounter and junctionnow for every run
    junctionCounter = 0;
    junctionNow = 0;
  }
  /*
  Gets the location and the the sorrundings of the robot and stores them when the robot
  is in a junction. Also increments the junctionCounter.
  */
  public void recordJunction(IRobot robot){
    junctionCounter++; //increments the junctionCounter
    junctionNow = junctionCounter;
    juncX[junctionCounter] = robot.getLocation().x;
    juncY[junctionCounter] = robot.getLocation().y;
    for(int i=0 ;i<2; i++){
      for(int j =0; j<4; j++){
        tremaux[junctionCounter][i][j]=0; //fills it up with 0s
      }
    }
  }
  /*
  The next two methods set the appropriate valus of the tremaux array to one
  so that we can use the later
  */
  public void addBeenOnce(IRobot robot, int heading){
    tremaux[junctionNow][0][heading-IRobot.NORTH]=1;
  }

  public void addBeenTwice(IRobot robot, int heading){
    tremaux[junctionNow][1][heading-IRobot.NORTH]=1;
  }
  /*
  The next two methods get the vales from the tremaux array to decide the
  direction where the robot will be heading
  */
  public boolean searchBeenOnce(IRobot robot, int heading){
    if(tremaux[junctionNow][0][heading-IRobot.NORTH]==0) return (false);
    else return (true);
  }

  public boolean searchBeenTwice(IRobot robot, int heading){
    if(tremaux[junctionNow][1][heading-IRobot.NORTH]==0) return (false);
    else return (true);
  }

  /*
  Searches for a specific junction by comparing the the coordiantes of the robot and the junctions stored
  Returns the heading of this junction.
  */
  public int searchJunction(IRobot robot){
    int returnint = -1;
    for(int i = 0; i <= junctionCounter; i++){
      if (juncX[i] ==robot.getLocation().x && juncY[i] ==robot.getLocation().y){
        returnint = i;
      }
    }
    junctionNow = returnint; //sets the junctionnow to the current junction
    return returnint;
  }
}
