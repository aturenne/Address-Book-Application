import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Lab2 extends JFrame implements ActionListener {
    JButton open = new JButton("Next Program");
    JTextArea result = new JTextArea(20,40);
    JLabel errors = new JLabel();
    JScrollPane scroller = new JScrollPane();

    public Lab2() {
        setLayout(new java.awt.FlowLayout());
        setSize(500,430);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(open); open.addActionListener(this);
        scroller.getViewport().add(result);
        add(scroller);
        add(errors);
    }

    public void actionPerformed(ActionEvent evt) {
        result.setText("");	//clear TextArea for next program
        errors.setText("");
        processProgram();
    }

    public static void main(String[] args) {
        Lab2 display = new Lab2();
        display.setVisible(true);
    }

    String getFileName() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
            return fc.getSelectedFile().getPath();
        else
            return null;
    }


//create arraylist of lines in the program
//create empty hashmap to store variables and their values
HashMap<String,Double> hm = new HashMap<String,Double>();
ArrayList<String> lines = new ArrayList<String>();
int lineNum = 1;
    private void processProgram(){
        //read the file and create ArrayList containing the lines of the program
        String fileName = getFileName();
        if (fileName == null)
            return;
        try {
                BufferedReader in = new BufferedReader(new FileReader(fileName));
                String line;
                lines.add("");
                while ((line=in.readLine()) != null) {
                    lines.add(line);
                }

                in.close();
        } catch (IOException e) {
                errors.setText("ERROR: " + e);
        }
        System.out.println(lines);

        int x = lines.size()-1;
        if(!(lines.get(x).equals("END"))) {
            errors.setText("ERROR: Must contain an END statement.");
            return;
        }

        //deal with lineNum
        //create ArrayList of the statements to pass into methods
        while(true){
            if(lines.get(lineNum).toUpperCase().contains("END")) {
                return;
            }
            String line = lines.get(lineNum);
            lineNum++;
            ArrayList<String> command = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(line, " ");
            while(tok.hasMoreTokens()){
                command.add(tok.nextToken());
            }
            processStatement(command);
            if(!errors.getText().equals(""))
                break;
        }
    }
    private void processStatement(ArrayList<String> command){
        //which key word is being used
        String statement = command.get(0);
        if(statement.equals("PRINT"))
            print(command);
        else if(statement.equals("GOTO"))
            goTo(command);
        else if(statement.equals("IF"))
            conditional(command);
        else if(command.contains("="))
            equalsOperator(command);
        else{
            errors.setText("ERROR: No proper key word detected.");
        }
    }
    private void print(ArrayList<String> command){
        //check if the variable to be printed is in the hashmap, then print if true
        String variable = command.get(1);
        double value = 0;
        if(hm.containsKey(variable)){
            value = hm.get(variable);
        }
        else{
            errors.setText("ERROR: Variable does not exist");
        }
        result.append(String.format("%.2f\n",value));
    }
    private void goTo(ArrayList<String> command){
        int line = Integer.parseInt(command.get(1));
        int y = lines.size()-1;
        if(line > y){
            errors.setText("ERROR: That line number does not exist.");
        }
        lineNum = line;
    }
    private void conditional(ArrayList<String> command){
        if(!command.contains("THEN") || !command.contains("IS")){
            errors.setText("ERROR: Illegal Format. Must be 'IF variable IS value THEN simple statement' format");
            return;
        }
        //Check if the variable in the conditional is in the hashmap
        String variable = command.get(1);
        double value = 0;
        if(hm.containsKey(variable))
            value = hm.get(variable);
        else {
            errors.setText("ERROR: The variable does not exist.");
            return;
        }
        double num = 0;
        try{
            num = Double.parseDouble(command.get(3));
            } catch(NumberFormatException e){
                errors.setText("ERROR: Illegal Format.");
                return;
            }
        //create new ArrayList of just the simple statement to be passed into the appropriate method if the conditional is satisfied
        ArrayList<String> statement = new ArrayList<String>();
        if(num == value){
            for(int i=5; i<command.size(); i++) {
                statement.add(command.get(i));
            }
            if(statement.contains("PRINT"))
                print(statement);
            else if(statement.contains("GOTO"))
                goTo(statement);
            else if (statement.contains("="))
                equalsOperator(statement);
            else{
                errors.setText("ERROR: No proper key word detected");
            }
        }
    }
    public void equalsOperator(ArrayList<String> command){
        String variableName = command.get(0);
        double value = evaluate(command.get(2));
        if(!errors.getText().equals(""))
            return;
        //loop through the operators (odd indexes) in the command statement to save proper value in the variable
        for (int i = 3; i < command.size() - 1; i += 2) {
            String op = command.get(i);
            if (op.equals("+")) {
                value = value + evaluate(command.get(i + 1));
            } else if (op.equals("-")) {
                value = value - evaluate(command.get(i + 1));
            } else if (op.equals("*")) {
                value = value * evaluate(command.get(i + 1));
            } else if (op.equals("/")) {
                value = value / evaluate(command.get(i + 1));
            } else {
                errors.setText("ERROR: Invalid operator.");
                return;
                }
            }
            //add appropriate values to the corresponding variable in the hashmap
            hm.put(variableName, value);
        }
    private double evaluate(String str) {
        if (hm.containsKey(str)) {
            return hm.get(str);
        }
        else{
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e){
                errors.setText("ERROR: Illegal Format");
                return 0.0;
            }
        }
    }
}