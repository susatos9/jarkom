import java.io.FileNotFoundException;
import java.io.File;
import java.util.*;


// buat nyoba-nyoba kode java
public class Test{
  
  public static void main(String[]args){

    ArrayList<Question> question_list = new ArrayList<>();
    String filePath = "./question.txt";

    Question tmp = new Question("");
    try (Scanner sc = new Scanner(new File(filePath))) {
      while(sc.hasNextLine()){
        String now = sc.nextLine();
        if(now.substring(0, 2).equals("Q:")) { // pertanyaan
          tmp = new Question(now.substring(2));
        }
        else if(now.substring(0, 4).equals("OPT-")){   // opsi
          tmp.add_option(now.substring(4));
        }
        else if(now.substring(0, 4).equals("KEY:")){   // kunci jawaban
          tmp.set_answer(now.substring(4));
        }
        else { // akhir pertanyaan, now == END_QUESTION
          question_list.add(tmp);
        }
      }
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }

    System.out.println("udah baca file bang\n");

    for(Question qq : question_list){
      qq.print_question();
    }
  
  }
}