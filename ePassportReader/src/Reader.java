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
import java.util.List;

import com.sun.imageio.plugins.common.ImageUtil;
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

    public class ePassportReader {
        static final int MAX_TRANSCEIVE_LENGTH = PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;
        static final int MAX_BLOCK_SIZE = PassportService.DEFAULT_MAX_BLOCKSIZE;
        static final boolean IS_SFI_ENABLED = false;
        static final boolean SHOULD_CHECK_MAC = false;

        public MRZInfo ReadCard(String[] args)throws CardServiceException, CardException, IOException {
            // do bac
            BACKey bacKey = new BACKey("123456789","211026","221026");
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
            MRZInfo mrzInfo = dg1.getMRZInfo();

            // read face image
            InputStream is2 = ps.getInputStream(PassportService.EF_DG2);
            DG2File dg2 = (DG2File) LDSFileUtil.getLDSFile(PassportService.EF_DG2, is2);
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            ps.close();
            return mrzInfo;
        }
    }

    public Reader() {
        read_button.addActionListener(new ActionListener() {
            String[] args = {};
            public void actionPerformed(ActionEvent e) {
                ePassportReader Reader = new ePassportReader();
                MRZInfo mrzInfo = null;
                try {
                    mrzInfo = Reader.ReadCard(args);
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
                nationality.setText(mrzInfo.getNationality());
                issuing_state.setText(mrzInfo.getIssuingState());
                mrztext.setText(String.valueOf(mrzInfo));
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ePassport Reader");
        frame.setContentPane(new Reader().Panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
