package io.activej.cube.http;

import io.activej.aggregation.AggregationPredicate;
import io.activej.common.exception.MalformedDataException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.activej.aggregation.AggregationPredicates.*;
import static io.activej.cube.Utils.fromJson;
import static io.activej.cube.Utils.toJson;

public class AggregationPredicateCodecTest {
	private static AggregationPredicateCodec CODEC;

	@BeforeClass
	public static void beforeClass() {
		Map<String, Type> attributeTypes = new HashMap<>();
		attributeTypes.put("campaign", int.class);
		attributeTypes.put("site", String.class);
		attributeTypes.put("hourOfDay", int.class);

		Map<String, Type> measureTypes = new HashMap<>();
		measureTypes.put("conversions", long.class);
		measureTypes.put("eventCount", int.class);
		measureTypes.put("ctr", double.class);

		CODEC = AggregationPredicateCodec.create(attributeTypes, measureTypes);
	}

	@Test
	public void testAlwaysTrue() {
		doTest(alwaysTrue());
	}

	@Test
	public void testAlwaysFalse() {
		doTest(alwaysFalse());
	}

	@Test
	public void testNot() {
		doTest(not(alwaysTrue()));
		doTest(not(alwaysFalse()));
		doTest(not(eq("site", "test")));
	}

	@Test
	public void testAnd() {
		doTest(and(alwaysFalse(), alwaysTrue()));
		doTest(and(eq("site", "test"), ge("campaign", 123)));
	}

	@Test
	public void testOr() {
		doTest(or(alwaysFalse(), alwaysTrue()));
		doTest(or(eq("site", "test"), ge("campaign", 123)));
	}

	@Test
	public void testEq() {
		doTest(eq("site", "test"));
		doTest(eq("site", null));
		doTest(eq("campaign", 1234));
	}

	@Test
	public void testNotEq() {
		doTest(notEq("site", "test"));
		doTest(notEq("campaign", 1234));
		doTest(notEq("campaign", null));
	}

	@Test
	public void testGe() {
		doTest(ge("campaign", 1234));
		doTest(ge("campaign", null));
	}

	@Test
	public void testLe() {
		doTest(le("ctr", 12.34));
	}

	@Test
	public void testGt() {
		doTest(gt("eventCount", 1234));
	}

	@Test
	public void testLt() {
		doTest(lt("campaign", 1234));
	}

	@Test
	public void testIn() {
		doTest(in("campaign", 12, 23, 43, 54, 65));
		doTest(in("campaign", 12));
		doTest(in("hourOfDay", 0, 23));
		doTest(in("hourOfDay"));
	}

	@Test
	public void testRegexp() {
		doTest(regexp("regexp", "pattern"));
	}

	@Test
	public void testBetween() {
		doTest(between("campaign", -12, 444));
	}

	private static void doTest(AggregationPredicate predicate) {
		String json = toJson(CODEC, predicate);
		AggregationPredicate decoded;
		try {
			decoded = fromJson(CODEC, json);
		} catch (MalformedDataException e) {
			throw new AssertionError(e);
		}
		if (!Objects.equals(predicate, decoded)) {
			throw new AssertionError();
		}
	}
}
