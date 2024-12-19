import java.util.*;


// buat nyoba-nyoba kode java
public class Test{
  public static void main(String[]args){
    Question q = new Question("contoh nanya");
    q.add_option("opsi a");
    q.add_option("opsi b");

    System.out.println(q.wrap());
    System.out.println("bisa bos");
    Question.unWrap(q.wrap()).print_question();

  }

}