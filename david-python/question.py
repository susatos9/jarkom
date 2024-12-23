class Question:
  def __init__(self, question):
    self.question = question
    self.options = []
    self.answer = ""
  
  def add_option(self, opt):
    self.options.append(opt)
  
  def print_question(self):
    print("Question: " + self.question)
    opsi = 97
    for o in self.options:
      print(chr(opsi) + ". " + o)
      opsi += 1 
    print("")

  def wrap(self):
    res = self.question
    for o in self.options:
      res += "|" + o
    res += "|"
    return res
  
  def unwrap(qq):
    last = 0
    ret = Question("")
    for i in range(len(qq)):
      if qq[i] != '|': continue
      # question
      if last == 0:
        ret.question = qq[0:i]
        last = i + 1
      # option
      else :
        ret.add_option(qq[last:i])
        last = i + 1
    return ret

# q = Question("Test")
# q.add_option("david ganten")
# q.add_option("bisa ggak")
# q.print_question()  

# tmp = Question.unwrap(q.wrap())
# tmp.print_question()


