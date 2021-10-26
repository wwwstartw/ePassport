package star.ePassportReader;

import net.sf.scuba.smartcards.*;

import net.sf.scuba.smartcards.CommandAPDU;
import org.jmrtd.BACKey;
import org.jmrtd.PassportService;

import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.protocol.BACResult;
import org.jmrtd.protocol.SecureMessagingAPDUSender;

import javax.smartcardio.*;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class ePassportReader {
    static final int MAX_TRANSCEIVE_LENGTH = PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;
    static final int MAX_BLOCK_SIZE = PassportService.DEFAULT_MAX_BLOCKSIZE;
    static final boolean IS_SFI_ENABLED = false;
    static final boolean SHOULD_CHECK_MAC = false;

    public static void main(String[] args)throws CardServiceException, CardException, IOException {
        BACKey bacKey = new BACKey("A12345678","980928","990928");

        //get terminal (card reader)
        CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
        System.out.println(terminal.getName());

        CardService cs = CardService.getInstance(terminal);
        PassportService ps = new PassportService(cs,MAX_TRANSCEIVE_LENGTH,
                MAX_BLOCK_SIZE,IS_SFI_ENABLED,SHOULD_CHECK_MAC);
        ps.open();

        //select passport applet
        ps.sendSelectApplet(false);

        //doBAC (basic access control)
        System.out.println(bacKey);
        BACResult bacResult = ps.doBAC(bacKey);
        System.out.println(bacResult);

        // read file
        InputStream is = ps.getInputStream(PassportService.EF_DG1);
        DG1File dg1 = (DG1File) LDSFileUtil.getLDSFile(PassportService.EF_DG1, is);
        MRZInfo mrzInfo = dg1.getMRZInfo();
        System.out.println(mrzInfo);
        ps.close();
    }
}


