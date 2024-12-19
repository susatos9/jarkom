import java.util.ArrayList;

public class Question {
  
  public String question, answer;
  public ArrayList<String> options = new ArrayList<>();

  public Question(String question){
    this.question = question;
  }
  public void add_option(String option){
    options.add(option);
  }
  public void set_answer(String answer){
    this.answer = answer;
  }
  public void print_question(){
    System.out.println(question);
    char opt = 'a';
    for(String s : options){
      System.out.println("" + opt + ". " + s);
      opt++;
    }
    System.out.println("");
  }
  public String wrap(){ // join question and all options into one string
    String res = question + "|";
    for(String s : options) {
      res += s;
      res += "|";
    }
    return res;
  }
  public static Question unWrap(String msg) { 
    Question res = new Question("");

    int last = 0;
    for(int i = 0; i < msg.length(); i++){
      if(msg.charAt(i) == '|'){
        // question part
        if(last == 0){
          res.question = msg.substring(0, i);
          last = i + 1;
        }
        // answer part
        else {
          res.add_option(msg.substring(last, i));
          last = i + 1;
        }
      }
    }
    return res;
  }

}