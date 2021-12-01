import javax.smartcardio.CardException;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class WriteCard {

    public JTextField document_number;
    public JButton write_button;
    private JTextField name;
    private JTextField personal_number;
    private JTextField birth_date;
    private JTextField expiry_date;
    private JComboBox gender;
    private JComboBox nationality;
    private JComboBox issuing_state;
    private JPanel panel2;
    public JPanel image;
    private JTextArea mrztext;
    private JLabel face;
    private JLabel tip;
    private JButton upload_btn;

    public static void main(String[] args) throws CardException {
        JFrame frame = new JFrame("ePassport Writer");
        frame.setContentPane(new WriteCard().panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String toHexadecimal(String text) throws UnsupportedEncodingException
    {
        byte[] myBytes = text.getBytes("UTF-8");

        return DatatypeConverter.printHexBinary(myBytes);
    }

    public void sendAPDU(String mrz, String dg1_binary) throws CardException {

        TerminalFactory tf = TerminalFactory.getDefault();
        List< CardTerminal> terminals = tf.terminals().list();

        System.out.println("Available Readers:");
        System.out.println(terminals + "\n");

        CardTerminal cardTerminal = (CardTerminal) terminals.get(0);
        Card connection = cardTerminal.connect("*");
        CardChannel cardChannel = connection.getBasicChannel();

        System.out.println("==========Select Applet==========");
        String cmd = "00a4040007a0000002471001";
        System.out.println("Command: " + cmd);
        byte[] cmdArray = hexStringToByteArray(cmd);
        ResponseAPDU resp = cardChannel.transmit(new CommandAPDU(cmdArray));
        String hex = DatatypeConverter.printHexBinary(resp.getBytes());
        System.out.println("Response: " + hex);

        System.out.println("==========Create File DG1==========");
        cmd = "00e000000663040070010100";
        System.out.println("Command: " + cmd);
        cmdArray = hexStringToByteArray(cmd);
        resp = cardChannel.transmit(new CommandAPDU(cmdArray));
        hex = DatatypeConverter.printHexBinary(resp.getBytes());
        System.out.println("Response: " + hex);

        System.out.println("==========PUT MRZ==========");
        cmd = mrz;
        System.out.println("Command: " + cmd);
        cmdArray = hexStringToByteArray(cmd);
        resp = cardChannel.transmit(new CommandAPDU(cmdArray));
        hex = DatatypeConverter.printHexBinary(resp.getBytes());
        System.out.println("Response: " + hex);

        System.out.println("==========Select File DG1==========");
        cmd = "00a4000002010100";
        System.out.println("Command: " + cmd);
        cmdArray = hexStringToByteArray(cmd);
        resp = cardChannel.transmit(new CommandAPDU(cmdArray));
        hex = DatatypeConverter.printHexBinary(resp.getBytes());
        System.out.println("Response: " + hex);

        System.out.println("==========Update Binary DG1==========");
        cmd = dg1_binary;
        System.out.println("Command: " + cmd);
        cmdArray = hexStringToByteArray(cmd);
        resp = cardChannel.transmit(new CommandAPDU(cmdArray));
        hex = DatatypeConverter.printHexBinary(resp.getBytes());
        System.out.println("Response: " + hex);

        connection.disconnect(true);
    }

    public WriteCard() {
        write_button.addActionListener(new ActionListener() {
            String[] args = {};
            String mrztext, doc_number_hex, birth_hex, expire_hex, dg1_binary, nationality_hex, issue_hex, fname_hex, lname_hex, gender_hex, personal_hex;

            public void actionPerformed(ActionEvent e) {
                try {
                    doc_number_hex = toHexadecimal(document_number.getText());
                    birth_hex = toHexadecimal(birth_date.getText());
                    expire_hex = toHexadecimal(expiry_date.getText());
                    fname_hex = toHexadecimal((name.getText().split(" ")[0]));
                    lname_hex = toHexadecimal((name.getText().split(" ")[1]));
                    nationality_hex = toHexadecimal(nationality.getSelectedItem().toString().split(" ")[0]);
                    issue_hex = toHexadecimal(issuing_state.getSelectedItem().toString().split(" ")[0]);
                    if (gender.getSelectedItem().toString().substring(0,1) == "U") {
                        gender_hex = "3C";
                    }else{
                        gender_hex = toHexadecimal(gender.getSelectedItem().toString().substring(0,1));
                    }
                    personal_hex = toHexadecimal(personal_number.getText());
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                mrztext = "00da00621d401bc009" + doc_number_hex + "c106" + birth_hex + "c206" + expire_hex;
                dg1_binary = "00d600005f615d5f1f5a503C" + issue_hex + doc_number_hex + "34" + personal_hex + "3C3C3C3C3C" + birth_hex + "31" + gender_hex + expire_hex + "35" + nationality_hex + "3C3C3C3C3C3C3C3C3C3C3C3C" + fname_hex + "3C" + lname_hex + new String(new char[30-name.getText().length()]).replace("\0", "3C");;

                try {
                    sendAPDU(mrztext,dg1_binary);
                } catch (CardException ex) {
                    ex.printStackTrace();
                }

            }
        });
    }
}
