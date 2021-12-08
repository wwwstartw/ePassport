package star;

import net.sf.scuba.util.Hex;
import org.jmrtd.app.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.smartcardio.CardException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.util.List;
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
    public byte[] image_byte;

    public static void main(String[] args) throws CardException, IOException {
        JFrame frame = new JFrame("ePassport Writer");
        frame.setContentPane(new WriteCard().panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        String file = "C:\\Users\\star.li\\Desktop\\277088.jpg";
        

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
        cmd = "00e00000066304005D010100";
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

        if (face.getIcon() != null) {
            System.out.println("==========Create File DG2==========");
            cmd = "00e000000663045ACF010200";
            System.out.println("Command: " + cmd);
            cmdArray = hexStringToByteArray(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========Select File DG2==========");
            cmd = "00a4000002010200";
            System.out.println("Command: " + cmd);
            cmdArray = hexStringToByteArray(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========Update Binary DG2==========");
            // header + file block
            String image_apdu = Hex.bytesToHexString(image_byte).replace("\n","");
            cmd = "00D60000FF758245977F618245920201017F6082458AA10E81010282010087020101880200085F2E824575464143003031300000004575000100004567000000000000000000000000000000000000018C0200000000000000" + image_apdu.substring(0,342);
            System.out.println(image_apdu);
            System.out.println("Command: " + cmd);
            cmdArray = hexStringToByteArray(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);
            // file
            int count = 0;
            boolean flag = false;
            int idx = 342;
            for(int i = 255; i > 100; i--) {
                String num = "";
                if (Integer.toHexString(count).length() < 2) {
                    num = "0" + Integer.toHexString(count);
                }else{
                    num = Integer.toHexString(count);
                }
                if (idx + 32*15 + 30 > image_apdu.length() ){
                    cmd = "00D6" + num + Integer.toHexString(i) + Integer.toHexString(image_apdu.substring(idx).length()/2);
                    // 補零要做
                    cmd += image_apdu.substring(idx);
                    flag = true;
                }else{
                    cmd = "00D6" + num + Integer.toHexString(i) + "FF";
                    cmd += image_apdu.substring(idx, idx + 32*15 + 30);
                }
                System.out.println("Command: " + cmd);
                cmdArray = hexStringToByteArray(cmd);
                resp = cardChannel.transmit(new CommandAPDU(cmdArray));
                hex = DatatypeConverter.printHexBinary(resp.getBytes());
                System.out.println("Response: " + hex);
                idx += 32*15 + 30;
                count += 1;
                if (flag == true){
                    break;
                }
            }
        }
        connection.disconnect(true);
    }
    public void addPicture(JButton button) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".jpg/.jpeg","jpg","jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(button);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] arrfiles = chooser.getSelectedFiles();
            if (arrfiles == null || arrfiles.length == 0) {
                return;
            }
            try {
                File ff = chooser.getSelectedFile();
                BufferedImage origImage = ImageIO.read(ff);
                Image image = origImage.getScaledInstance(150, 200, Image.SCALE_DEFAULT);
                face.setIcon(new ImageIcon(image));
                ByteArrayOutputStream encodedImageOut = new ByteArrayOutputStream();
                ImageUtil.write(origImage, "image/jpeg", encodedImageOut);
                image_byte = encodedImageOut.toByteArray();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public WriteCard() {
        upload_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPicture(upload_btn);
            }
        });
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
                dg1_binary = "00D600005D615B5F1F58503C" + issue_hex + fname_hex + new String(new char[(20-fname_hex.length())/2]).replace("\0", "3C") + lname_hex + new String(new char[(62-name.getText().length())/2]).replace("\0", "3C") + doc_number_hex + "37" + nationality_hex + birth_hex + "34" + gender_hex + expire_hex + "37" + personal_hex + "3C3C3C3C3738";

                try {
                    sendAPDU(mrztext,dg1_binary);
                } catch (CardException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
