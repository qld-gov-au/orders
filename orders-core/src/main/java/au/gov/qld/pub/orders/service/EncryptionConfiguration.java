package au.gov.qld.pub.orders.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;

/**
 * @deprecated for use of jasypt-spring-boot-starter springboot wiring
 *
 * jasypt:
 *   encryptor:
 *     #provider needs to be bouncycastel for this algorithm
 *     provider-name: "BC"
 *    provider-class-name: org.bouncycastle.jce.provider.BouncyCastleProvider
 *    algorithm: "PBEWITHSHA256AND128BITAES-CBC-BC"
 *    password: "mysecretpassword" #keyfilefallbackvalue
 *    iv-generator-classname: org.jasypt.iv.NoIvGenerator #CBC-BC does not need iv
 */
@Deprecated
public class EncryptionConfiguration extends SimplePBEConfig {
    private static final String ALGORITHM = "PBEWITHSHA256AND128BITAES-CBC-BC";

    public EncryptionConfiguration(String keyfilename,
                                   String keyfilefallbackvalue) throws IOException {
        super();
        setProvider(new BouncyCastleProvider());
        setAlgorithm(ALGORITHM);

        final char[] password;
        File keyfile = new File(keyfilename);
		if (keyfile.exists()) {
        	password = IOUtils.toCharArray(new FileReader(keyfile));
        } else {
        	password = keyfilefallbackvalue.toCharArray();
        }

        int end = password.length;
        for (int i=0; i < password.length; i++) {
            if (password[i] == '\n' || password[i] == '\r' || password[i] == ' ') {
                end = i;
                break;
            }
        }
        char[] trimmedPassword = Arrays.copyOfRange(password, 0, end);
        Arrays.fill(password, ' ');

        setProvider(new BouncyCastleProvider());
        setPasswordCharArray(trimmedPassword);
        Arrays.fill(trimmedPassword, ' ');
    }

}
