package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import au.gov.qld.pub.orders.dao.FormFileDAO;
import au.gov.qld.pub.orders.entity.FormFile;

@Service
public class FileService {
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
	private final FormFileDAO dao;
	private final List<String> supportedTypes;
	private final long maxUploadSize;
	private final int deleteUploadDays;

	@Autowired
	public FileService(FormFileDAO dao, @Value("${upload.supported}") String supportedTypes, @Value("${upload.max}") long maxUploadSize,
			@Value("${scheduler.cleanup.deleteUploadsDays}") int deleteUploadDays) {
		this.dao = dao;
		this.deleteUploadDays = deleteUploadDays;
		this.supportedTypes = asList(supportedTypes.split(";"));
		this.maxUploadSize = maxUploadSize;
	}
	
	@Scheduled(fixedDelay = 60 * 60 * 1000)
	public void cleanUp() {
		LOG.debug("Cleaning up files older than: {} days", deleteUploadDays);
		Collection<String> ids = dao.findIdByCreatedAtBefore(new LocalDate().minusDays(deleteUploadDays).toDate());
		for (String id : ids) {
			LOG.info("Cleaning up form file with id: {}", id);
			dao.delete(id);
		}
	}

	public List<String> save(List<MultipartFile> uploads) throws IOException, ValidationException {
	    List<MultipartFile> verified = getVerifiedUploads(uploads);
	    List<String> ids = new ArrayList<>();
	    for (MultipartFile upload : verified) {
	        FormFile formFile = dao.save(new FormFile(upload.getOriginalFilename(), upload.getBytes(), upload.getContentType()));
	        ids.add(formFile.getId());
        }
	    return ids;
	}

    private List<MultipartFile> getVerifiedUploads(List<MultipartFile> uploads) throws ValidationException {
        
        List<MultipartFile> verified = new ArrayList<MultipartFile>();
        
        long totalUploadSize = 0;
        
        for (MultipartFile upload : uploads) {
            if (upload == null || upload.isEmpty()) {
                continue;
            }
            
            totalUploadSize += upload.getSize();
            if (totalUploadSize  > maxUploadSize) {
                throw new ValidationException("File upload too big");
            }
            String filename = upload.getOriginalFilename();
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase(Locale.getDefault()).trim();
            if (!supportedTypes.contains(ext)) {
            	LOG.info("Rejected filetype was: {}", ext);
            	throw new ValidationException("File upload was an invalid type: " + ext);
            }
            
            verified.add(upload);
        }
	    return verified;
    }

	public FormFile find(String fileId) {
		return dao.findOne(fileId);
	}
	
}