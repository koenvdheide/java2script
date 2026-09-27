package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test_RegexLookahead extends Test_ {

	public static void main(String[] args) {
		Pattern quoted = Pattern.compile(".*='[^']*(?!')");
		for (String input : new String[] { "a='", "a='hello", "a='\n" })
			assert quoted.matcher(input).matches() : input;
		for (String input : new String[] { "", "a=''", "a='hello'" })
			assert !quoted.matcher(input).matches() : input;
		assert !quoted.matcher("a=''").find();
		assert !Pattern.compile("a(?!b)").matcher("ab").find();
		assert !Pattern.compile("a(?=b)").matcher("ac").find();

		check("a(?=b)", "xab", new int[][] { { 1, 2 } });
		check("a(?!b)", "xac", new int[][] { { 1, 2 } });
		check("a(?=b)(b)", "xab", new int[][] { { 1, 3 }, { 2, 3 } });
		check("(a)(?!b)(c)", "xac", new int[][] { { 1, 3 }, { 1, 2 }, { 2, 3 } });
		check("(?=(a))(a)", "xa", new int[][] { { 1, 2 }, { 1, 2 }, { 1, 2 } });
		check("(?=a(b))(ab)", "xab", new int[][] { { 1, 3 }, { 2, 3 }, { 1, 3 } });
		check("(?!(a))(b)", "xb", new int[][] { { 1, 2 }, { -1, -1 }, { 1, 2 } });
		check("(a)?b", "b", new int[][] { { 0, 1 }, { -1, -1 } });
		check("(?=(a(?=b)))(ab)", "xab", new int[][] { { 1, 3 }, { 1, 2 }, { 1, 3 } });
		check("(?=(?:a)(b))(ab)", "xab", new int[][] { { 1, 3 }, { 2, 3 }, { 1, 3 } });
		check("(?=(?:a(?:b))(c))(abc)", "xabc", new int[][] { { 1, 4 }, { 3, 4 }, { 1, 4 } });
		Matcher named = check("(?=(?<ahead>a))(?<after>a)", "xa",
				new int[][] { { 1, 2 }, { 1, 2 }, { 1, 2 } });
		assert named.group("ahead").equals("a");
		assert named.start("after") == 1;
		assert named.end("after") == 2;
		named = check("(?<first>a)(?<second>b)", "xab",
				new int[][] { { 1, 3 }, { 1, 2 }, { 2, 3 } });
		assert named.group("second").equals("b");
		check("(?=\\()([(])", "x(", new int[][] { { 1, 2 }, { 1, 2 } });
		check("(?=[)])([)])", "x)", new int[][] { { 1, 2 }, { 1, 2 } });
		check("(a)(b)", "xab", new int[][] { { 1, 3 }, { 1, 2 }, { 2, 3 } });
		check("(?=)", "a", new int[][] { { 0, 0 } });
		check("a", "a", new int[][] { { 0, 1 } });
		check("", "a", new int[][] { { 0, 0 } });

		Matcher successive = Pattern.compile("(?=(a))(a)").matcher("aa");
		assert successive.find();
		assert successive.start(2) == 0;
		assert successive.find();
		assert successive.start(2) == 1;
		assert !successive.find();
		check("x(?:ab)+(c)", "xababc", new int[][] { { 0, 6 }, { 5, 6 } });
		check("(?=(?:ab)+(c))(ababc)", "xababc", new int[][] { { 1, 6 }, { 5, 6 }, { 1, 6 } });
		check("(?:a)(b)\\1", "abb", new int[][] { { 0, 3 }, { 1, 2 } });
		check("x(a)\\1", "xaa", new int[][] { { 0, 3 }, { 1, 2 } });
		check("(a)|(b)", "b", new int[][] { { 0, 1 }, { -1, -1 }, { 0, 1 } });
		check("(a)+(b)", "aab", new int[][] { { 0, 3 }, { 1, 2 }, { 2, 3 } });
		check("(a){2}", "aa", new int[][] { { 0, 2 }, { 1, 2 } });
		Matcher advancing = check("(a)", "aa", new int[][] { { 0, 1 }, { 0, 1 } });
		java.util.regex.MatchResult snapshot = advancing.toMatchResult();
		assert advancing.find();
		assert advancing.start(1) == 1;
		assert snapshot.group(1).equals("a");
		assert snapshot.start(1) == 0;
		assert snapshot.end(1) == 1;
		Matcher region = Pattern.compile("(b)").matcher("xbxb").region(1, 3);
		assert region.find();
		assert region.start(1) == 1;
		assert region.end(1) == 2;
		assert !region.find();
		System.out.println("Test_RegexLookahead OK");
	}

	private static Matcher check(String regex, String input, int[][] bounds) {
		Matcher matcher = Pattern.compile(regex).matcher(input);
		assert matcher.find() : regex;
		assert matcher.groupCount() == bounds.length - 1 : regex;
		for (int i = 0; i < bounds.length; i++) {
			assert matcher.start(i) == bounds[i][0] : regex + " start " + i;
			assert matcher.end(i) == bounds[i][1] : regex + " end " + i;
			assert bounds[i][0] < 0 ? matcher.group(i) == null
					: matcher.group(i).equals(input.substring(bounds[i][0], bounds[i][1])) : regex + " group " + i;
		}
		return matcher;
	}
}
