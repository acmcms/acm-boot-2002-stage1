/*
 * Created on 30.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ae2core;

import java.io.InputStream;

import ru.myx.ae3.flow.ObjectBuffer;
import ru.myx.ae3.flow.ObjectBufferArray;
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Search;

/**
 * @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
class Tests {
	
	static boolean tests() {
		
		System.out.println("AE2-CORE-TEST: Tests requested - starting tests.");
		{
			System.out.print("AE2-CORE-TEST: Boot loader resource test: url=");
			System.out.print(Tests.class.getClassLoader().getResource("ae2core/test-resource.txt"));
			System.out.print(", stream=");
			final InputStream stream = Tests.class.getClassLoader().getResourceAsStream("ae2core/test-resource.txt");
			System.out.print(stream == null
				? "null"
				: stream.getClass().getName());
			System.out.print(", relative=");
			System.out.print(Tests.class.getResource("test-resource.txt"));
			assert stream != null : "resources are not accessible!";
			System.out.println(" OK");
		}
		{
			System.out.print("AE2-CORE-TEST: Common class search: ");
			final Class<?>[][] pattern = {
					{
							Integer.class, Integer.class, Integer.class
					}, {
							Comparable.class, Integer.class, String.class
					}, {
							Number.class, Integer.class, Double.class
					}, {
							ObjectBuffer.class, ObjectBufferArray.class, ObjectBuffer.NUL_BUFFER.getClass()
					}, {
							ObjectBuffer.class, ObjectBufferArray.class, ObjectBuffer.NUL_BUFFER.getClass()
					},
			};
			int ok = 0;
			for (final Class<?>[] line : pattern) {
				final Class<?> result = Search.Reflection.commonSuperclass(line[1], line[2]);
				if (line[0].isAssignableFrom(result)) {
					ok++;
				}
			}
			if (ok == pattern.length) {
				System.out.println("OK [" + ok + "/" + ok + "]");
			} else {
				System.out.println("FAILED [ok=" + ok + "/" + pattern.length + "]");
				System.out.println("AE2-TEST: TESTS FAILED!");
				return true;
			}
		}
		{
			System.out.print("AE2-CORE-TEST: Any-To-Int conversion: ");
			final class Test {
				
				public final int result;

				public final Object source;

				Test(final int result, final Object source) {
					this.result = result;
					this.source = source;
				}
			}
			final Test[] pattern = {
					new Test(14, new Integer(14)), new Test(15, new Double(15.7)), new Test(-255, "-255"), new Test(255, "255"), new Test(256, "0x100"), new Test(1, "true"),
					new Test(1, "TRUE"), new Test(0, "false"), new Test(0, "FaLsE"), new Test(1024, "1k"), new Test(1024 + 512, "1.5k"),
					new Test(1024 * 1024 * 1024 + 2 * 1024 + 6, "1g 2K 6"), new Test(1030 + 2 * 1024 * 1024, "2m1K6"),
			};
			int ok = 0;
			for (final Test test : pattern) {
				final int result = Convert.Any.toInt(test.source, -1);
				if (result == test.result) {
					ok++;
				} else {
					System.out.print("{" + result + "!=" + test.result + "}");
				}
			}
			if (ok == pattern.length) {
				System.out.println("OK [" + ok + "/" + ok + "]");
			} else {
				System.out.println("FAILED [ok=" + ok + "/" + pattern.length + "]");
				System.out.println("AE2-TEST: TESTS FAILED!");
				return true;
			}
		}
		{
			System.out.print("AE2-CORE-TEST: Any-To-Long conversion: ");
			final class Test {
				
				public final long result;

				public final Object source;

				Test(final long result, final Object source) {
					this.result = result;
					this.source = source;
				}
			}
			final Test[] pattern = {
					new Test(14, new Integer(14)), new Test(15, new Double(15.7)), new Test(-255, "-255"), new Test(255, "255"), new Test(256, "0x100"), new Test(1, "true"),
					new Test(1, "TRUE"), new Test(0, "false"), new Test(0, "FaLsE"), new Test(1024, "1k"), new Test(1024 + 512, "1.5k"),
					new Test(ru.myx.ae3.help.Convert.MUL_GIGA + 2 * 1024 + 6, "1g 2K 6"), new Test(1030 + 2 * 1024 * 1024, "2m1K6"),
			};
			int ok = 0;
			for (final Test test : pattern) {
				final long result = Convert.Any.toInt(test.source, -1);
				if (result == test.result) {
					ok++;
				} else {
					System.out.print("{" + result + "!=" + test.result + "}");
				}
			}
			if (ok == pattern.length) {
				System.out.println("OK [" + ok + "/" + ok + "]");
			} else {
				System.out.println("FAILED [ok=" + ok + "/" + pattern.length + "]");
				System.out.println("AE2-TEST: TESTS FAILED!");
				return true;
			}
		}
		{
			System.out.print("AE2-CORE-TEST: Any-To-Period conversion: ");
			final class Test {
				
				public final long result;

				public final Object source;

				Test(final long result, final Object source) {
					this.result = result;
					this.source = source;
				}
			}
			final Test[] pattern = {
					new Test(1000, "1s"), new Test(1500, "1s 500"), new Test(1500, "1.5s"), new Test(2000, "1.5s 500"), new Test(60000, "1m"), new Test(60000, "60s"),
					new Test(60000, "60000"), new Test(60000, "0.5m30s"), new Test(60000, "0.5m25s5000"),
			};
			int ok = 0;
			for (final Test test : pattern) {
				final long result = Convert.Any.toPeriod(test.source, -1);
				if (result == test.result) {
					ok++;
				} else {
					System.out.print("{" + result + "!=" + test.result + "}");
				}
			}
			if (ok == pattern.length) {
				System.out.println("OK [" + ok + "/" + ok + "]");
			} else {
				System.out.println("FAILED [ok=" + ok + "/" + pattern.length + "]");
				System.out.println("AE2-TEST: TESTS FAILED!");
				return true;
			}
		}
		System.out.println("AE2-CORE-TEST: Tests finished.");
		return false;
	}

}
