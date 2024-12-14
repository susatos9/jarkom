import utils.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class quiz{
  question[] questions;
  user[] users;
}

class user{
  int id,score;
  string name;
  String[] answer;
}

class question{
  int id;
  string question;
  char answer;
}


public class room{
  int id;
  quiz quiz;
  static void main(String[] args){
    System.out.println("Connected to room " + id);
  } 
}

