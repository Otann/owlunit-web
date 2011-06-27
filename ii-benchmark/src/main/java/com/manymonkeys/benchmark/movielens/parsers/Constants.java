package com.manymonkeys.benchmark.movielens.parsers;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class Constants {

    // MovieLens

    public static double INITIAL_TAG_TO_USER_WEIGHT = 0.01d;
    public static double ADDITIONAL_TAG_TO_USER_WEIGHT = 0.005d;

    public static double INITIAL_TAG_TO_MOVIE_WEIGHT = 1d;
    public static double ADDITIONAL_TAG_TO_MOVIE_WEIGHT = 0.5d;

    public static double INITIAL_DIFFUSE_MUTLIPLIER = 0.1d;
    public static double ADDITIONAL_DIFFUSE_MULTIPLIER = 1d;

    public static final double INITIAL_PERSON_WEIGHT = 5d;

    public static final double MIN_KEYWORD_WEIGHT = 5d;
    public static final double MAX_KEYWORD_WEIGHT = 15d;
    public static final int MAX_KEYWORD_COUNT = 150;
    public static final int KEYWORD_THRESHOLD = 50;
}
