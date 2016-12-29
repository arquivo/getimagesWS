package getimagesWS;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = { TestContext.class , WebAppContext.class } )
@WebAppConfiguration
public class ControllerTest {
	
}
