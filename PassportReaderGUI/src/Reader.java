import javax.swing.*;

public class Reader {
    private JTextField document_number;
    private JButton read_button;
    private JTextField name;
    private JTextField personal_number;
    private JTextField birth_date;
    private JTextField expiry_date;
    private JTextField gender;
    private JTextField nationality;
    private JTextField issuing_state;
    private JPanel Panel2;
    private JPanel image;
    private JPanel mrzinfo;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Reader");
        frame.setContentPane(new Reader().Panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
