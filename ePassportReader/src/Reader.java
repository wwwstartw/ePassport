import javax.imageio.ImageIO;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class Reader {
    public JTextField document_number;
    public JButton read_button;
    private JTextField name;
    private JTextField personal_number;
    private JTextField birth_date;
    private JTextField expiry_date;
    private JTextField gender;
    private JTextField nationality;
    private JTextField issuing_state;
    private JPanel Panel2;
    public JPanel image;
    private JTextArea mrztext;
    private JLabel face;
    private JLabel tip;
    private JTextField BAC_doc;
    private JTextField BAC_birth;
    private JTextField BAC_expiry;
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

    public class ReadMRZ {
        static final int MAX_TRANSCEIVE_LENGTH = PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;
        static final int MAX_BLOCK_SIZE = PassportService.DEFAULT_MAX_BLOCKSIZE;
        static final boolean IS_SFI_ENABLED = false;
        static final boolean SHOULD_CHECK_MAC = false;

        public MRZInfo ReadMRZ(String[] args)throws CardServiceException, CardException, IOException {
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

                // read face image
                InputStream is2 = ps.getInputStream(PassportService.EF_DG2);
                DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, is2);

                List<FaceInfo> faceInfos = dg2.getFaceInfos();
                for (FaceInfo faceInfo: faceInfos) {
                    List<FaceImageInfo> faceImageInfos = faceInfo.getFaceImageInfos();
                    for (FaceImageInfo faceImageInfo: faceImageInfos) {
                        InputStream inputStream = faceImageInfo.getImageInputStream();
                        BufferedImage faceimage = ImageIO.read(inputStream);
                        ImageIcon image = new ImageIcon(faceimage);
                        image.setImage(image.getImage().getScaledInstance(150, 200, Image.SCALE_DEFAULT ));
                        face.setIcon(image);
                    }
                }
                ps.close();
            } catch (Exception e) {
                if (e.getMessage().contains("6982")) {
                    JOptionPane.showMessageDialog(null,"讀取失敗! 請確認BAC key是否正確或重新感應卡片!","Error",JOptionPane.WARNING_MESSAGE);
                }else{
                    JOptionPane.showMessageDialog(null,"讀取成功!");
                }
            }
            return mrzInfo;
        }
    }

    public Reader() {
        read_button.addActionListener(new ActionListener() {
            String[] args = {};
            public void actionPerformed(ActionEvent e) {
                ReadMRZ Reader = new ReadMRZ();
                MRZInfo mrzInfo = null;
                try {
                    mrzInfo = Reader.ReadMRZ(args);
                } catch (CardServiceException ex) {
                    ex.printStackTrace();
                } catch (CardException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                document_number.setText(mrzInfo.getDocumentNumber());
                name.setText(mrzInfo.getPrimaryIdentifier() + " " + mrzInfo.getSecondaryIdentifier().replace("<",""));
                personal_number.setText(mrzInfo.getPersonalNumber());
                birth_date.setText(mrzInfo.getDateOfBirth());
                expiry_date.setText(mrzInfo.getDateOfExpiry());
                gender.setText(String.valueOf(mrzInfo.getGender()));
                nationality.setText(country_dict.get(mrzInfo.getNationality()));
                issuing_state.setText(country_dict.get(mrzInfo.getIssuingState()));
                mrztext.setText(String.valueOf(mrzInfo));
            }
        });
    }
}
