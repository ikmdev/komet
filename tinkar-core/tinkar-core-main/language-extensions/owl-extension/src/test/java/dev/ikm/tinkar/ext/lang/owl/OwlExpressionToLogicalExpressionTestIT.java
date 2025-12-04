package dev.ikm.tinkar.ext.lang.owl;

import dev.ikm.tinkar.common.service.PrimitiveData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OwlExpressionToLogicalExpressionTestIT {

	private static final Logger LOG = LoggerFactory.getLogger(OwlExpressionToLogicalExpressionTestIT.class);

	private static String test_case = "snomed-intl-20250101";

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		PrimitiveDataTestUtil.setupPrimitiveData(test_case + "-sa");
		PrimitiveData.start();
	}

	@AfterAll
	public static void stopPrimitiveData() {
		LOG.info("stopPrimitiveData");
		PrimitiveData.stop();
		LOG.info("Stopped");
	}

	@Test
	public void uuidToNid() {
		UUID sub = UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c");
		UUID sup = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
		String expr = "SubClassOf(:[" + sub + "] :[" + sup + "])";
		String nid_expr = OwlElExpressionToLogicalExpression.uuidToNid(expr);
		LOG.info(nid_expr);
		assertEquals("SubClassOf(:" + PrimitiveData.nid(sub) + " :" + PrimitiveData.nid(sup) + ")", nid_expr);
	}

}
