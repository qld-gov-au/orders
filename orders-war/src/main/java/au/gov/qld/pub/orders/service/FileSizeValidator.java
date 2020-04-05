package au.gov.qld.pub.orders.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileSizeValidator implements FileValidator {
	private final long maxUploadSize;

	@Autowired
	public FileSizeValidator(@Value("${upload.max}") long maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}
	
	@Override
	public void validate(String filename, long filesize, InputStream is) throws ValidationException {
		if (filesize  > maxUploadSize) {
            throw new ValidationException("File upload too big");
        }
	}

}
