package com.aspectran.core.util.wildcard;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

/**
 * <p>Created: 2016. 4. 3.</p>
 */
public class WildcardMatcherTest {

	@Test
	public void antStylePatternTest() {
		// double asterisks
		assertTrue(checkAntPattern("/static/**", "/static/a/b/sssss/"));
		assertTrue(checkAntPattern("/static/**/", "/static/a/"));
		assertTrue(checkAntPattern("/static/**/", "/static/a/b/"));
		assertTrue(checkAntPattern("/static/**/", "/static/a/b/c/d/"));
		assertTrue(checkAntPattern("/static/**/", "/static/a/b/c/d/"));
		assertTrue(checkAntPattern("/static/**", "/static/a/b/c/d/"));
		assertTrue(checkAntPattern("/static/**", "/static/a/a/"));
		assertTrue(checkAntPattern("/static/**", "/static/a/b/c/d/e/f/g/"));

		// single asterisk
		assertTrue(checkAntPattern("/static/*", "/static/a.jpg"));
		assertTrue(checkAntPattern("/static/*", "/static/*.jpg"));
		assertTrue(checkAntPattern("/static/*", "/static/namkyuProfilePicture.jpg"));

		assertFalse(checkAntPattern("/static/*", "/static/a/test.jpg"));
		assertFalse(checkAntPattern("/static/*", "/static/a/b/c/d/test.jpg"));

		assertTrue(checkAntPattern("/static*/*", "/static/test.jpg"));
		assertTrue(checkAntPattern("/static*/*", "/static1/test.jpg"));
		assertTrue(checkAntPattern("/static*/*", "/static123/test.jpg"));
		assertTrue(checkAntPattern("/static*/*", "/static-123/test.jpg"));
		assertTrue(checkAntPattern("/static*/*", "/static~!@#$%^&*()_+}{|/test.jpg"));

		assertFalse(checkAntPattern("/static*/*", "/static12/a/test.jpg"));
		assertFalse(checkAntPattern("/static*/*", "/static12/a/b/test.jpg"));

		// double and single combine
		assertTrue(checkAntPattern("/static*/**/*", "/static/a.jpg"));
		assertTrue(checkAntPattern("/static*/**/b/*", "/static/a/b/c.jpg"));
		assertTrue(checkAntPattern("/static*/**", "/static1/a.jpg"));
		assertTrue(checkAntPattern("/static*/**", "/static/a/a.jpg"));
		assertTrue(checkAntPattern("/static*/**", "/static/a/b/a.jpg"));
		assertTrue(checkAntPattern("/static*/**", "/static/a/b/c/a.jpg"));
		assertTrue(checkAntPattern("/static*/a/b/c/a.jpg**", "/static/a/b/c/a.jpg"));

		assertTrue(checkAntPattern("**/static/**", "a/static/a/b/c/a.jpg"));
		assertTrue(checkAntPattern("**/static/**", "a/b/static/a/b/c/a.jpg"));

		// question-mark
		assertTrue(checkAntPattern("/static-?/**", "/static-a/a.jpg"));
		assertTrue(checkAntPattern("/static-?/**", "/static-a/b/c/a.jpg"));
		assertTrue(checkAntPattern("/static-?/*", "/static-a/abcd.jpg"));
		assertTrue(checkAntPattern("/static-?/???.jpg", "/static-a/abc.jpg"));

	}

	private boolean checkAntPattern(String pattern, String inputStr) {
		return matches(pattern, inputStr);
	}

	private static boolean matches(String patternStr, String inputStr) {
		WildcardPattern pattern = WildcardPattern.compile(patternStr, '/');
		return WildcardMatcher.matches(pattern, inputStr);
	}

}