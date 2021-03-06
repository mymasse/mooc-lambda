/**
 * Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * JDK 8 MOOC Lesson 3 homework
 */
package ca.mymasse.mooc.lambda.lesson03;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Simon Ritter (@speakjava)
 * @author Stuart Marks
 */
public class Lesson03 {
	/* How many times to repeat the test. 5 seems to give reasonable results */
	private static final int RUN_COUNT = 5;

	/**
	 * Used by the measure method to determine how long a Supplier takes to
	 * return a result.
	 *
	 * @param <T>
	 *            The type of the result provided by the Supplier
	 * @param label
	 *            Description of what's being measured
	 * @param supplier
	 *            The Supplier to measure execution time of
	 * @return
	 */
	static <T> T measureOneRun(String label, Supplier<T> supplier) {
		long startTime = System.nanoTime();
		T result = supplier.get();
		long endTime = System.nanoTime();
		System.out.printf("%s took %dms%n", label, (endTime - startTime + 500_000L) / 1_000_000L);
		return result;
	}

	/**
	 * Repeatedly generate results using a Supplier to eliminate some of the
	 * issues of running a micro-benchmark.
	 *
	 * @param <T>
	 *            The type of result generated by the Supplier
	 * @param label
	 *            Description of what's being measured
	 * @param supplier
	 *            The Supplier to measure execution time of
	 * @return The last execution time of the Supplier code
	 */
	static <T> T measure(String label, Supplier<T> supplier) {
		T result = null;

		for (int i = 0; i < RUN_COUNT; i++)
			result = measureOneRun(label, supplier);

		return result;
	}

	/**
	 * Computes the Levenshtein distance between every pair of words in the
	 * subset, and returns a matrix of distances. This actually computes twice
	 * as much as it needs to, since for every word a, b it should be the case
	 * that lev(a,b) == lev(b,a) i.e., Levenshtein distance is commutative.
	 *
	 * @param wordList
	 *            The subset of words whose distances to compute
	 * @param parallel
	 *            Whether to run in parallel
	 * @return Matrix of Levenshtein distances
	 */
	static int[][] computeLevenshtein(List<String> wordList, boolean parallel) {
		final int LIST_SIZE = wordList.size();
		int[][] distances = new int[LIST_SIZE][LIST_SIZE];

		if (parallel) {
			IntStream.range(0, LIST_SIZE).parallel().forEach(i -> IntStream.range(0, LIST_SIZE).parallel()
					.forEach(j -> distances[i][j] = Levenshtein.lev(wordList.get(i), wordList.get(j))));
		} else {
			IntStream.range(0, LIST_SIZE).forEach(i -> IntStream.range(0, LIST_SIZE)
					.forEach(j -> distances[i][j] = Levenshtein.lev(wordList.get(i), wordList.get(j))));
		}

		return distances;
	}

	/**
	 * Process a list of random strings and return a modified list
	 * 
	 * @param wordList
	 *            The subset of words whose distances to compute
	 * @param parallel
	 *            Whether to run in parallel
	 * @return The list processed in whatever way you want
	 */
	static List<String> processWords(List<String> wordList, boolean parallel) {
		Stream<String> stream = wordList.stream();

		if (parallel) {
			stream.parallel();
		}

		return stream.filter(word -> word.length() > 2).distinct().map(String::toUpperCase)
				.collect(Collectors.toList());
	}

	/**
	 * Main entry point for application
	 *
	 * @param args
	 *            the command line arguments
	 * @throws IOException
	 *             If word file cannot be read
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		RandomWords fullWordList = new RandomWords();
		List<String> wordList = fullWordList.createList(1000);

		measure("Sequential computeLevenshtein", () -> computeLevenshtein(wordList, false));
		measure("Parallel computeLevenshtein", () -> computeLevenshtein(wordList, true));

		measure("Sequential processWords", () -> processWords(wordList, false));
		measure("Parallel processWords", () -> processWords(wordList, true));
	}
}