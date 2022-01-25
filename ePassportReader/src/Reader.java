import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.protocol.BACResult;
import sun.rmi.runtime.Log;
import util.IconUtil;
import util.ImageUtil;

import static com.sun.javafx.iio.common.ImageTools.scaleImage;

public class Reader {
    public JTextField document_number;
    public JButton read_button;
    private JTextField fname;
    private JTextField personal_number;
    private JTextField birth_date;
    private JTextField expiry_date;
    private JTextField gender;
    private JTextField nationality;
    private JTextField issuing_state;
    private JPanel Panel2;
    public JPanel passport_image;
    private JTextArea mrztext;
    private JLabel face;
    private JLabel tip;
    private JTextField BAC_doc;
    private JTextField BAC_birth;
    private JTextField BAC_expiry;
    private JButton verify_button;
    private JTextField lname;
    static Map<String, String> country_dict = new HashMap<String, String>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("ePassport Reader");
        frame.setContentPane(new Reader().Panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        country_dict.put("AUS", "Australia");
        country_dict.put("BRA", "Brazil");
        country_dict.put("CHN", "China");
        country_dict.put("DEU", "Germany");
        country_dict.put("ESP", "Spain");
        country_dict.put("FRA", "France");
        country_dict.put("GBR", "United Kingdom");
        country_dict.put("HKG", "Hong Kong");
        country_dict.put("IRL", "Ireland");
        country_dict.put("JPN", "Japan");
        country_dict.put("KEN", "Kenya");
        country_dict.put("LTU", "Lithuania");
        country_dict.put("MAC", "Macao");
        country_dict.put("NLD", "Netherlands");
        country_dict.put("POL", "Poland");
        country_dict.put("REU", "Reunion");
        country_dict.put("SGP", "Singapore");
        country_dict.put("SWE", "Sweden");
        country_dict.put("TWN", "Taiwan");
        country_dict.put("USA", "United States");
        country_dict.put("ZAF", "South Africa");
    }

    public class ReadMRZ extends JPanel {
        static final int MAX_TRANSCEIVE_LENGTH = PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;
        static final int MAX_BLOCK_SIZE = PassportService.DEFAULT_MAX_BLOCKSIZE;
        static final boolean IS_SFI_ENABLED = false;
        static final boolean SHOULD_CHECK_MAC = false;
        private int width, height;

        public MRZInfo ReadMRZ(String[] args) {
            // do bac
            MRZInfo mrzInfo = null;
            try {
                BACKey bacKey = new BACKey(BAC_doc.getText(),BAC_birth.getText(),BAC_expiry.getText());
                CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
                CardService cs = CardService.getInstance(terminal);
                PassportService ps = new PassportService(cs,MAX_TRANSCEIVE_LENGTH,
                        MAX_BLOCK_SIZE,IS_SFI_ENABLED,SHOULD_CHECK_MAC);
                ps.open();

                ps.sendSelectApplet(false);
                BACResult bacResult = ps.doBAC(bacKey);

                // read mrzinfo
                InputStream is = ps.getInputStream(PassportService.EF_DG1);
                DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
                mrzInfo = dg1.getMRZInfo();

                // read face
                InputStream is2 = ps.getInputStream(PassportService.EF_DG2);
                DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, is2);
                List<FaceInfo> faceInfos = dg2.getFaceInfos();
                for (FaceInfo faceInfo: faceInfos) {
                    List<FaceImageInfo> faceImageInfos = faceInfo.getFaceImageInfos();
                    for (FaceImageInfo faceImageInfo: faceImageInfos) {
                        String mimeType = faceImageInfo.getMimeType();
                        int length = faceImageInfo.getImageLength();
                        InputStream inputStream = faceImageInfo.getImageInputStream();
                        BufferedImage image = ImageUtil.read(inputStream, length, mimeType);
                        Image face_image = image.getScaledInstance(150, 200, Image.SCALE_DEFAULT);
                        face.setIcon(new ImageIcon(face_image));
                    }
                }
                ps.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return mrzInfo;
        }
    }

    public void readCard(JButton button) {
        String[] args = {};
        ReadMRZ Reader = new ReadMRZ();
        MRZInfo mrzInfo = null;
        try {
            mrzInfo = Reader.ReadMRZ(args);
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            document_number.setText(mrzInfo.getDocumentNumber());
            fname.setText(mrzInfo.getPrimaryIdentifier());
            lname.setText(mrzInfo.getSecondaryIdentifier().replace("<",""));
            personal_number.setText(mrzInfo.getPersonalNumber());
            birth_date.setText(mrzInfo.getDateOfBirth());
            expiry_date.setText(mrzInfo.getDateOfExpiry());
            gender.setText(String.valueOf(mrzInfo.getGender()));
            nationality.setText(country_dict.get(mrzInfo.getNationality()));
            issuing_state.setText(country_dict.get(mrzInfo.getIssuingState()));
            mrztext.setText(String.valueOf(mrzInfo));
            JOptionPane.showMessageDialog(null,"讀取成功!");
            verify_button.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"讀取失敗! 請確認 BAC 是否正確或重新感應卡片!","Error",JOptionPane.WARNING_MESSAGE);
        }
    }

    public void verifyCard(JButton button) {
        final String sql = "SELECT doc_id, fname, lname, gender, birthday, expire FROM User WHERE fname='" + fname.getText() +
                "' and lname='" + lname.getText() + "';";
        try (	Connection connection = DBConnection.getConnection();
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql);
        ){
            int i = 0;
            while(rs.next()) {
                i += 1;
                break;
            }
            if (i < 1) {
                JOptionPane.showMessageDialog(null, "驗卡失敗! 該使用者不存在於合法電子護照資料庫中!", "Error", JOptionPane.WARNING_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null,"驗卡成功!");
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null,"驗卡失敗! 該使用者不存在於合法電子護照資料庫中!","Error",JOptionPane.WARNING_MESSAGE);
        }
    }

    public Reader() {
        read_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {readCard(read_button);}
        });
        verify_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { verifyCard(verify_button);}
        });
    }
}
