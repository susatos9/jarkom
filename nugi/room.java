//import utils.*;
import java.util.*;
import java.io.*;
import java.net.*;

class quiz{
  question[] questions;
  user[] users;
}

class user{
  int id,score;
  String name;
  String[] answer;
}

class question{
  int id;
  String question;
  char answer;
}


public class room{
  int id;
  quiz quiz;
}

