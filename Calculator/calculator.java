import java.awt.*; 
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
/** Calculator GUI. Creates the GUI elements */
public class calculator extends JFrame{
  calcLogic logic = new calcLogic(); //Calculator logic class that does all mathematical logic for the program
  //All JButtons
  private JButton changeSign; 
  private JButton period;
  private JButton addition;
  private JButton subtract; 
  private JButton multiply;
  private JButton divide;
  private JButton equals;
  private JButton clear;
  private JButton mc; //Memory Clear
  private JButton mremove; //Memory Subtract
  private JButton madd; //Memory Add
  private JButton mr; //Memory Retrieval
  private JButton backspace;
  //All the visible text fields
  private JTextField entry;
  private JTextField operation;
  private ArrayList<Double> memory = new ArrayList<Double>(); //Array that stores all the inputs
  private JButton [] numbers = new JButton[10]; //Array for the numerical JButtons
  private String display = ""; //The text entered in the entry JTextField
  private Double m; //The stored Double in the memory
  private Double answers; //The answer of the calculation
  private String operator = ""; //The last operator entered i.e. "+", "-", "Ö", "x"
  private Boolean operating = true; //Whether or not an operator is active. If so entering a number will cause the JTextField to Clear and enter the new number.
  private Boolean text = false; //Whether or not the entry JTextField currently contains text
  
  /** Calculator constructor sets all the data fields and creates GUI elements */
  public calculator(){
    //All listeners for the buttons
    EventListener listener = new EventListener(); 
    NumberListener listener2 = new NumberListener();
    //Instantiating all the buttons and and adding listeners to them
    for(int i = 0; i < numbers.length; i++){
      numbers[i] = new JButton(Integer.toString(i));
      numbers[i].addActionListener(listener2);
    }
    backspace = new JButton("DEL");
    backspace.addActionListener(listener2);
    mc = new JButton("MC");
    mc.addActionListener(listener2);
    madd = new JButton("M+");
    madd.addActionListener(listener2);
    mremove = new JButton("M-");
    mremove.addActionListener(listener2);
    mr = new JButton("MR");
    mr.addActionListener(listener2);
    changeSign = new JButton("±");
    changeSign.addActionListener(listener2);
    clear = new JButton("C");
    clear.addActionListener(listener2);
    period = new JButton(".");
    period.addActionListener(listener2);
    addition = new JButton("+");
    addition.addActionListener(listener);
    subtract = new JButton("-");
    subtract.addActionListener(listener);
    multiply = new JButton("X");
    multiply.addActionListener(listener);
    divide = new JButton("Ö");
    divide.addActionListener(listener);
    equals = new JButton("=");
    equals.addActionListener(listener);
    
    //Instantiating the Text Fields and maaking their horizontal alignment on the right and making them uneditable
    entry = new JTextField();
    entry.setHorizontalAlignment(JTextField.RIGHT);
    entry.setEditable(false);
    operation = new JTextField();
    operation.setHorizontalAlignment(JTextField.RIGHT);
    operation.setEditable(false);
    
    //Instantiating the JPanel that will hold the text fields, giving it a default size and adding the text fields to it
    JPanel screens = new JPanel();
    screens.setLayout(new BorderLayout());
    screens.setPreferredSize(new Dimension(250, 50));
    screens.add(entry, BorderLayout.NORTH);
    screens.add(operation, BorderLayout.CENTER);
    
    //Making the JPanel that does the buttons and giving it a default size, setting a grid layout.
    //Adding all the buttons to the JPanel that will hold them
    JPanel buttons = new JPanel();
    buttons.setPreferredSize(new Dimension(250, 250));
    buttons.setLayout(new GridLayout(6,4));
    buttons.add(mc);
    buttons.add(madd);
    buttons.add(mremove);
    buttons.add(mr);
    buttons.add(clear);
    buttons.add(backspace);
    buttons.add(divide);
    buttons.add(multiply);
    buttons.add(numbers[7]);
    buttons.add(numbers[8]);
    buttons.add(numbers[9]);
    buttons.add(subtract);
    buttons.add(numbers[4]);
    buttons.add(numbers[5]);
    buttons.add(numbers[6]);
    buttons.add(addition);
    buttons.add(numbers[1]);
    buttons.add(numbers[2]);
    buttons.add(numbers[3]);
    buttons.add(equals);
    buttons.add(numbers[0]);
    buttons.add(period);
    buttons.add(changeSign);
    //Adding the JPanels to a border layout, making it focused so it can register keyboard input and adds the keyboard listener
    add(screens, BorderLayout.NORTH);
    add(buttons, BorderLayout.CENTER);
    setFocusable(true); 
    requestFocus();
    addKeyListener(new KeyboardListener());
  }

  /** EventListener class to register all other button inputs
    */
  private class EventListener implements ActionListener{ 
    public void actionPerformed(ActionEvent e){
      try { //Try catch loop so any errors are caught
        //If a button is pressed, the method relating to that button is started
        if(e.getSource() == addition){
          add();
        }
        if(e.getSource() == subtract){
          subtract();
        }
        if(e.getSource() == multiply){
          multiply();
        }
        if(e.getSource() == divide){
          divide();
        }
        if(e.getSource() == equals){
          equal();
          operator = "="; //Setting the operator to equal
        }
      }catch(Exception error) { //If an error occurs the JTextFields are cleared and the the text variable is set to true as ERROR is displayed
        clear();
        entry.setText("ERROR"); text = true;
      }
      requestFocus(); //Requesting focus for keyboard input
    }
  }
  
    /** NumberListener class to register inputs from the numbers, memory buttons and clear button
    * @param input The value from the button pressed
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param m Double that holds the value that is currently stored in the memory
    */
  private class NumberListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      if(e.getSource() == period){
        period();
      }
      String input = e.getActionCommand();
      if(Character.isDigit(input.charAt(0))){
        if(operating || text == true || operator == "=" || operator == "mr"){
          entry.setText(input); operating = false; text = false;
          if(operator == "=" || operator == "mr"){
            operator = "";
          }
        }
        else{entry.setText(entry.getText() + input);}
        operating = false;
      }
      else{
        if(e.getSource() == clear){
          clear();
        }
        if(e.getSource() == mc){
          m = 0;
        }
        try{
          if(e.getSource() == madd){
            m = m + Double.parseDouble(entry.getText());
          }
          if(e.getSource() == mremove){
            m = m - Double.parseDouble(entry.getText());
          }
        }catch(Exception error) { entry.setText("No value Entered"); text = true;}
        if(e.getSource() == mr){
          operator = "mr";
          entry.setText(Double.toString(m));
        }
        if(e.getSource() == backspace){
          backspace();
        }
        if(e.getSource() == changeSign){
          if(entry.getText().indexOf("-") == -1){
            entry.setText("-" + entry.getText());
            operating = false;
          }
          else{
            entry.setText(entry.getText().substring(1));
          }
        }
      }
     requestFocus(); 
    }
  }
  
    /** KeyboardListener class to use keyboard inputs to interact with the calculator
    * @param key The key code of the key that has been pressed
    * @param keyNum The key that has been pressed (Used for number and period input)
    * @param display The current values in the entry textfield
    */ 
  private class KeyboardListener implements KeyListener{
    public void keyPressed(KeyEvent event){
      int key = event.getKeyCode();
      try{
        if(key == KeyEvent.VK_BACK_SPACE){
          backspace();
        }
        if(key == 61){
          add();
        }
        if(key == 45){
          subtract();
        }
        if(event.getKeyChar() == '*'){
          multiply();
        }
        if(key == KeyEvent.VK_SLASH){
          divide();
        }
        if(key == KeyEvent.VK_ENTER){
          equal();
          operator = "=";
        }
        if(event.getKeyChar() == 'c'){
          clear();
        }
      }catch(Exception e) {       
        clear();
        entry.setText("ERROR"); text = true;
      }
      char keyNum = event.getKeyChar();
      if(Character.isDigit(keyNum)){      
        if(operating || text == true || operator == "=" || operator == "mr"){entry.setText("" + keyNum); operating = false; text = false;
         if(operator == "=" || operator == "mr"){
            operator = "";
          }
        }
        else if(operation.getText().indexOf("") == 0){ entry.setText(entry.getText() + keyNum);}
      }
      
      if(String.valueOf(keyNum).equals(".")){
        period();
      }
    }
    public void keyReleased(KeyEvent event){} 
    public void keyTyped(KeyEvent input){
    }
  }
  
  /** Backspace method to backspace the entered digits
    * @param text Boolean variable that displays true if there is text on the screen
    */
  public void backspace(){
    try{ if(Character.isDigit(entry.getText().charAt(0)) || (entry.getText()).substring(0,1).equals("-")){entry.setText(entry.getText().substring(0, entry.getText().length() -1)); }}
    catch(Exception error){ entry.setText("Need a value entered"); text = true;}
  }
  
  /** Period method to add a period to the entered data
    * @param display The data entered on the display
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    */ 
  public void period(){
    display = entry.getText();
    if(display.indexOf(".") == -1 || operating == true){
      if(display.isEmpty() || operating == true){
        entry.setText("0.");
      }
      else{
        entry.setText(display + ".");
      }
    }
    operating = false;
  }
  
  /** Add method to allow addition to occur and stores variables
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param operator String variable that holds the value of the last operator that was pressed
    */
  public void add(){
    if(!operation.getText().isEmpty() && operating == false){
      equal();
    }
    memory.add(Double.parseDouble(entry.getText()));
    operator = "+"; operating = true;
    operation.setText("+"); addition.setBackground(Color.blue);
  }
  
  /** Subtract method to allow subtraction to occur and stores variables
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param operator String variable that holds the value of the last operator that was pressed
    */
  public void subtract(){
    if(!operation.getText().isEmpty() && operating == false){
      equal();
    }
    memory.add(Double.parseDouble(entry.getText()));
    operator = "-";    operating = true;
    operation.setText("-");
  }
  
  /** Multiply method to allow multiplication to occur and stores variables
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param operator String variable that holds the value of the last operator that was pressed
    */
  public void multiply(){
    if(!operation.getText().isEmpty() && operating == false){
      equal();
    }
    memory.add(Double.parseDouble(entry.getText())); 
    operator = "X";     operating = true;
    operation.setText("X");
  }
  
  /** Divide method to allow division to occur and stores variables
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param operator String variable that holds the value of the last operator that was pressed
    */
  public void divide(){
    if(!operation.getText().isEmpty() && operating == false){
      equal();
    }
    memory.add(Double.parseDouble(entry.getText()));
    operator = "Ö";     operating = true;
    operation.setText("Ö");
  }
  
  /** Clear method to clear all the textfields 
    * @param operating Boolean variable that is set so numbers will be replaced after a function is pressed
    * @param text Boolean variable that displays true if there is text on the screen
    */
  public void clear(){
    entry.setText("");  
    operation.setText("");
    operating = true;
    text = false;
  }
  
  /** Equal method that acts as the logic processor for the calculator and does all the functions and displays the answer
    * @param answers The answer to the function
    * @param operator String variable that holds the value of the last operator that was pressed
    */ 
  public void equal(){
    try{
    memory.add(Double.parseDouble(entry.getText()));
    if(operator.equals("")){
      answers = memory.get(memory.size() - 1);
    }
    if(operator.equals("+")){
      answers = logic.add(((memory.get(memory.size() - 2))), memory.get(memory.size() - 1));
    }
    if(operator.equals("-")){
      answers = logic.subtract((memory.get(memory.size() - 2)), memory.get(memory.size() - 1));
    }
    if(operator.equals("Ö")){
      answers = logic.divide((memory.get(memory.size() - 2)), memory.get(memory.size() - 1));
    }
    if(operator.equals("X")){
      answers = logic.multiply((memory.get(memory.size() - 2)), memory.get(memory.size() - 1));
    }
    Boolean dividebyzero = false;
    if((operator.equals("Ö") && (memory.get(memory.size() - 1) == 0))){
      dividebyzero = true;
    }
    if(answers == Math.rint(answers) && !dividebyzero){
      entry.setText(Integer.toString(answers.intValue()));
    }else{entry.setText(Double.toString(answers));}
    operation.setText("");
    memory.add(answers);
    operator = "";
    }catch(Exception e){}
  }
}