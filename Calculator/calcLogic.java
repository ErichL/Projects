/** Calculator logic class. Does the calculations when inputs are made. */
public class calcLogic{
  private double answer;
  
  /** Add method to do addition of the input variables from the GUI class 
    * @param n The first Double value entered in from the calculator
    * @param n2 The second Double value entered in from the calculator
    * @param answer the answer to the function
    */
  public Double add(Double n, Double n2){
    answer = n + n2;
    return answer;
  }
  /** Subtract method to do subtraction of the input variables from the GUI class 
    * @param n The first Double value entered in from the calculator
    * @param n2 The second Double value entered in from the calculator
    * @param answer the answer to the function
    */
  public Double subtract(Double n, Double n2){
    answer = n - n2;
    return answer;
  }
  /** Multiply method to do multiplication of the input variables from the GUI class 
    * @param n The first Double value entered in from the calculator
    * @param n2 The second Double value entered in from the calculator
    * @param answer the answer to the function
    */
  public Double multiply(Double n, Double n2){
    answer = n * n2;
    return answer;
  }
  /** Divide method to do division of the input variables from the GUI class 
    * @param n The first Double value entered in from the calculator
    * @param n2 The second Double value entered in from the calculator
    * @param answer the answer to the function
    */
  public Double divide(Double n, Double n2){
    answer = n / n2;
    return answer;
  }
}