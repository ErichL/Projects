/** Calculator. This draws the JFrame 
  * @author Erich Lee
  * @version 1.0 17/03/2013
  */
import javax.swing.JFrame; 
public class WindowApp{
  public static void main(String[]args){
    calculator calc = new calculator();
    calc.pack();
    calc.setLocationRelativeTo(null);
    calc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    calc.setVisible(true);
  }
}