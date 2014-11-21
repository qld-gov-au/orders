package au.gov.qld.bdm.orders.dao.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;


public class FileItemPropertiesDAOTest {
	@Test
	public void returnProperties() {
		FileItemPropertiesDAO dao = new FileItemPropertiesDAO();
		assertThat(dao.find("test").getProperty("productId"), is("test"));
	}
	
	@Test
	public void returnNullWhenCannotFindItemProperties() {
		FileItemPropertiesDAO dao = new FileItemPropertiesDAO();
		assertThat(dao.find("bogus"), nullValue());
	}
}
