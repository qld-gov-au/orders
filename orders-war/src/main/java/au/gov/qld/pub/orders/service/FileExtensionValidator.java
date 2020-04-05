package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileExtensionValidator implements FileValidator {
	private static final Logger LOG = LoggerFactory.getLogger(FileExtensionValidator.class);
	private final List<String> supportedTypes;

	@Autowired
	public FileExtensionValidator(@Value("${upload.supported}") String supportedTypes) {
		this.supportedTypes = asList(supportedTypes.split(";"));
	}
	
	@Override
	public void validate(String filename, long filesize, InputStream is) throws ValidationException {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault()).trim();
        if (!supportedTypes.contains(ext)) {
        	LOG.info("Rejected filetype was: {}", ext);
        	throw new ValidationException("File upload was an invalid type: " + ext);
        }	
	}

}
