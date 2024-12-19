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

}