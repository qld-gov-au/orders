package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.EnumerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import au.gov.qld.pub.orders.service.FileService;
import au.gov.qld.pub.orders.service.ValidationException;

@Controller
public class OrderWithFileController {
	private static final int MAXIMUM_GROUP_LENGTH = 50;
	private static final Logger LOG = LoggerFactory.getLogger(OrderWithFileController.class);
	private static final String FILE_ID = "fileIds";
	private static final int MAX_UPLOADS = 10;
	private final FileService fileService;
	private final Map<String, OrderValidator> productGroupValidators;

	@Autowired
	public OrderWithFileController(FileService fileService, Collection<OrderValidator> validators) {
		this.fileService = fileService;
		this.productGroupValidators = new HashMap<>();
		for (OrderValidator validator : validators) {
			productGroupValidators.put(validator.getProductGroup(), validator);
		}
	}
	
	@RequestMapping(value = "/confirmwithfile", method = RequestMethod.POST)
    public ModelAndView confirmWithFile(@RequestParam String group, @RequestParam("upload") List<MultipartFile> upload,
    		MultipartFile upload1, MultipartFile upload2, MultipartFile upload3, MultipartFile upload4, 
            HttpServletRequest request) throws IOException, ValidationException {
		if (isBlank(group) || group.length() > MAXIMUM_GROUP_LENGTH) {
			throw new ValidationException("Invalid group provided");
		}
		
		Map<String, String> fieldsAndValues = getFieldValues(request);
		// Prefill with list of uploads if they are provided.
		List<MultipartFile> uploads = new ArrayList<>(upload == null ? Collections.<MultipartFile>emptyList() : upload);
		addIfNotNull(upload1, uploads);
		addIfNotNull(upload2, uploads);
		addIfNotNull(upload3, uploads);
		addIfNotNull(upload4, uploads);
		if (uploads.size() > MAX_UPLOADS) {
			throw new ValidationException("Too many uploads");
		}
		
		OrderValidator orderValidator = productGroupValidators.get(group);
		if (orderValidator != null) {
			orderValidator.validate(uploads, fieldsAndValues);	
		} else {
			LOG.info("No server side validation for: {}", group);
		}
		
		
		ModelAndView mav = confirm(group, fieldsAndValues);
        List<String> fileIds = fileService.save(uploads);
        mav.addObject(FILE_ID, fileIds);
        LOG.info("Adding fileId {} with ids {}", FILE_ID, fileIds);
		return mav;
    }

	private void addIfNotNull(MultipartFile upload, List<MultipartFile> uploads) {
		if (upload != null) {
			uploads.add(upload);
		}
	}

	private ModelAndView confirm(@RequestParam String group, Map<String, String> fields) {
        ModelAndView mav = new ModelAndView("confirm." + group);
        mav.getModel().put("fields", fields);
        return mav;
    }

	@SuppressWarnings("unchecked")
	private Map<String, String> getFieldValues(HttpServletRequest request) {
		Map<String, String> fields = new HashMap<>();
        List<String> fieldNames = EnumerationUtils.toList(request.getParameterNames());
        for (String fieldName : fieldNames) {
            if (isNotBlank(request.getParameter(fieldName))) {
                fields.put(fieldName, request.getParameter(fieldName).trim());
            }
        }
		return fields;
	}
	
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver createMultipartResolver() {
	    CommonsMultipartResolver resolver = new CommonsMultipartResolver();
	    resolver.setDefaultEncoding("utf-8");
	    return resolver;
	}
}