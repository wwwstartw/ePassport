import net.sf.scuba.data.Gender;
import net.sf.scuba.util.Hex;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.FaceImageInfo;
import org.jmrtd.lds.FaceInfo;

import javax.imageio.ImageIO;
import javax.smartcardio.CardException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class WriteCard {

    public JTextField document_number;
    public JButton write_btn;
    private JTextField name;
    private JTextField personal_number;
    private JTextField birth_date;
    private JTextField expiry_date;
    private JComboBox gender;
    private JComboBox nationality;
    private JComboBox issuing_state;
    private JPanel panel2;
    private JLabel face;
    private JButton upload_btn;
    private JLabel tip;
    public String image_apdu, mrztext, dg1_binary;


    public static void main(String[] args) throws CardException, IOException {
        JFrame frame = new JFrame("ePassport Writer");
        frame.setContentPane(new WriteCard().panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static String toHexadecimal(String text) throws UnsupportedEncodingException
    {
        byte[] myBytes = text.getBytes("UTF-8");

        return DatatypeConverter.printHexBinary(myBytes);
    }

    public WriteCard() {
        upload_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFace(upload_btn);
            }
        });
        write_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { addMRZInfo(write_btn);}
        });
    }

    public void addFace(JButton button) {
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
                image_apdu = createFace(origImage);
                Image image = origImage.getScaledInstance(150, 200, Image.SCALE_DEFAULT);
                face.setIcon(new ImageIcon(image));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String createFace(BufferedImage faceimage) {
        try {
            List<FaceImageInfo> FaceImageInfos = new LinkedList<FaceImageInfo>();
            FaceImageInfo FaceImageInfo = createFaceImageObject(faceimage);
            FaceImageInfos.add(FaceImageInfo);
            FaceInfo faceInfo = new FaceInfo(FaceImageInfos);
            DG2File dg2 = new DG2File(Arrays.asList(new FaceInfo[] { faceInfo }));
            return Hex.bytesToHexString(dg2.getEncoded()).replace("\n","");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FaceImageInfo createFaceImageObject(BufferedImage faceinfo) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(faceinfo, "jpg", out);
            out.flush();
            byte[] bytes = out.toByteArray();

            byte[] imageBytes = bytes;
            Gender gender = Gender.UNSPECIFIED;
            FaceImageInfo.EyeColor eyeColor = FaceImageInfo.EyeColor.UNSPECIFIED;
            int hairColor = FaceImageInfo.HAIR_COLOR_UNSPECIFIED;
            int featureMask = 0;
            short expression = FaceImageInfo.EXPRESSION_UNSPECIFIED;
            int[] poseAngle = { 0, 0, 0 };
            int[] poseAngleUncertainty = { 0, 0, 0 };
            int faceImageType = FaceImageInfo.FACE_IMAGE_TYPE_FULL_FRONTAL;
            int colorSpace = 0x00;
            int sourceType = FaceImageInfo.SOURCE_TYPE_UNSPECIFIED;
            int deviceType = 0x0000;
            int quality = 0x0000;
            int imageDataType = FaceImageInfo.IMAGE_DATA_TYPE_JPEG;
            FaceImageInfo.FeaturePoint[] featurePoints = new FaceImageInfo.FeaturePoint[0];
            FaceImageInfo imageInfo = new FaceImageInfo(
                    gender,  eyeColor, hairColor,
                    featureMask,
                    expression,
                    poseAngle, poseAngleUncertainty,
                    faceImageType,
                    colorSpace,
                    sourceType,
                    deviceType,
                    quality,
                    featurePoints,
                    150, 200,
                    new ByteArrayInputStream(imageBytes), imageBytes.length, imageDataType);
            return imageInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addMRZInfo(JButton button) {
        String doc_number_hex, birth_hex, expire_hex, nationality_hex, issue_hex, fname_hex, lname_hex, gender_hex, personal_hex;
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

            mrztext = "00da00621d401bc009" + doc_number_hex + "c106" + birth_hex + "c206" + expire_hex;
            dg1_binary = "00D600005D615B5F1F58503C" + issue_hex +
                    fname_hex + new String(new char[(20-fname_hex.length())/2]).replace("\0", "3C") +
                    lname_hex + new String(new char[(62-name.getText().length())/2]).replace("\0", "3C") +
                    doc_number_hex + "37" + nationality_hex + birth_hex + "34" + gender_hex + expire_hex + "37" + personal_hex + "3C3C3C3C3738";

            sendAPDU();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    public void sendAPDU()  {
        try {
            TerminalFactory tf = TerminalFactory.getDefault();
            List< CardTerminal> terminals = tf.terminals().list();

            System.out.println("Available Readers:");
            System.out.println(terminals + "\n");

            CardTerminal cardTerminal = terminals.get(0);
            Card connection = cardTerminal.connect("*");
            CardChannel cardChannel = connection.getBasicChannel();

            System.out.println("==========Select Applet==========");
            String cmd = "00a4040007a0000002471001";
            System.out.println("Command: " + cmd);
            byte[] cmdArray = Hex.hexStringToBytes(cmd);
            ResponseAPDU resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            String hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========Create File DG1==========");
            cmd = "00e00000066304005D0101";
            System.out.println("Command: " + cmd);
            cmdArray = Hex.hexStringToBytes(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========PUT MRZ==========");
            cmd = mrztext;
            System.out.println("Command: " + cmd);
            cmdArray = Hex.hexStringToBytes(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========Select File DG1==========");
            cmd = "00a4000002010100";
            System.out.println("Command: " + cmd);
            cmdArray = Hex.hexStringToBytes(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            System.out.println("==========Update Binary DG1==========");
            cmd = dg1_binary;
            System.out.println("Command: " + cmd);
            cmdArray = Hex.hexStringToBytes(cmd);
            resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            hex = DatatypeConverter.printHexBinary(resp.getBytes());
            System.out.println("Response: " + hex);

            boolean flag = false;
            if (face.getIcon() != null) {
                System.out.println("==========Create File DG2==========");
                String dg2_length = String.format("%02X", (Integer.valueOf(image_apdu.substring(6, 8),16) + 4) & 0xFFFFF);
                cmd = "00e00000066304" + image_apdu.substring(4, 6) + dg2_length + "0102";
                System.out.println("Command: " + cmd);
                cmdArray = Hex.hexStringToBytes(cmd);
                resp = cardChannel.transmit(new CommandAPDU(cmdArray));
                hex = DatatypeConverter.printHexBinary(resp.getBytes());
                System.out.println("Response: " + hex);

                System.out.println("==========Select File DG2==========");
                cmd = "00a4000002010200";
                System.out.println("Command: " + cmd);
                cmdArray = Hex.hexStringToBytes(cmd);
                resp = cardChannel.transmit(new CommandAPDU(cmdArray));
                hex = DatatypeConverter.printHexBinary(resp.getBytes());
                System.out.println("Response: " + hex);

                System.out.println("==========Update Binary DG2==========");
                int idx = 510, count = 0;
                // file header + file first block
                cmd = "00D60000FF" + image_apdu.substring(0, idx);
                System.out.println("Command: " + cmd);
                cmdArray = Hex.hexStringToBytes(cmd);
                resp = cardChannel.transmit(new CommandAPDU(cmdArray));
                hex = DatatypeConverter.printHexBinary(resp.getBytes());
                System.out.println("Response: " + hex);
                // file block
                for(int i = 255; i > 1; i--) {
                    String num = String.format("%02X", count & 0xFFFFF);
                    if (idx + 510 > image_apdu.length() ){
                        cmd = "00D6" + num + Integer.toHexString(i) + Integer.toHexString(image_apdu.substring(idx).length()/2);
                        cmd += image_apdu.substring(idx);
                        flag = true;
                    }else{
                        cmd = "00D6" + num + Integer.toHexString(i) + "FF";
                        cmd += image_apdu.substring(idx, idx + 510);
                    }
                    System.out.println("Command: " + cmd);
                    cmdArray = Hex.hexStringToBytes(cmd);
                    resp = cardChannel.transmit(new CommandAPDU(cmdArray));
                    hex = DatatypeConverter.printHexBinary(resp.getBytes());
                    System.out.println("Response: " + hex);
                    idx += 510;
                    count += 1;
                    if (flag == true){
                        break;
                    }
                }
            }
            connection.disconnect(true);
            if (hex.contains("9000") == false && flag == true) {
                JOptionPane.showMessageDialog(null,"寫入失敗! 圖片解析錯誤，請上傳其他圖片!","Error",JOptionPane.WARNING_MESSAGE);
            }else if (hex.contains("9000") == false && flag == false) {
                JOptionPane.showMessageDialog(null, "寫入失敗! 資料格式錯誤，請重新輸入資料!", "Error", JOptionPane.WARNING_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null,"寫入成功!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"寫入失敗!","Error",JOptionPane.WARNING_MESSAGE);
        }
    }
}
