package au.gov.qld.pub.orders.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	private final int deleteUploadDays;
	private final List<FileValidator> fileValidators;

	@Autowired
	public FileService(FormFileDAO dao, 
			@Value("${scheduler.cleanup.deleteUploadsDays}") int deleteUploadDays, List<FileValidator> fileValidators) {
		this.dao = dao;
		this.deleteUploadDays = deleteUploadDays;
		this.fileValidators = fileValidators;
		LOG.info("Loaded file validators: " + fileValidators);
		if (fileValidators.size() < 2) {
			throw new IllegalStateException("Need at least the built in validators");
		}
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
        for (MultipartFile upload : uploads) {
            if (upload == null || upload.isEmpty()) {
                continue;
            }
            
            for (FileValidator fileValidator : fileValidators) {
            	fileValidator.validate(upload.getOriginalFilename(), upload.getSize());
            }
            
            verified.add(upload);
        }
	    return verified;
    }

	public FormFile find(String fileId) {
		return dao.findOne(fileId);
	}
	
}