package dev.ikm.tinkar.reasoner.elksnomed.test2;
import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionInternational;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedCompareTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.PrimitiveDataTestUtil;

public class ElkSnomedCompareVersionIntl20240701TestIT extends ElkSnomedCompareTestBase
		implements SnomedVersionInternational {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedCompareVersionIntl20240701TestIT.class);

	static {
		// Note this db is different than the Snomed version
		test_case = "snomed-intl-20250101";
	}

	@Override
	public String getVersion() {
		return "20240701";
	}

	@Override
	protected ViewCalculator getViewCalculator() {
		ViewCalculator viewCalculator = PrimitiveDataTestUtil.getViewCalculator(getVersion());
		long time = ((StampCalculatorWithCache) viewCalculator.stampCalculator()).filter().time();
		LOG.info("View calculator time: " + Instant.ofEpochMilli(time) + " " + time);
		return viewCalculator;
	}

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		PrimitiveDataTestUtil.setupPrimitiveData(test_case + "-sa");
		PrimitiveData.start();
	}

	@AfterAll
	public static void stopPrimitiveData() {
		PrimitiveDataTestUtil.stopPrimitiveData();
	}

}
