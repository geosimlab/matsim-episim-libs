package org.matsim.run.modules;

public class JlmParamsHolder {
	public static int first_id = 1;
	public static int second_id = 1;
	public static long rand_seed = 1;
	public static int iterations= 1;
	public static int getFirst_id() {
		return first_id;
	}
	public static void setFirst_id(int first_id) {
		JlmParamsHolder.first_id = first_id;
	}
	public static int getSecond_id() {
		return second_id;
	}
	public static void setSecond_id(int second_id) {
		JlmParamsHolder.second_id = second_id;
	}
	public static long getRand_seed() {
		return rand_seed;
	}
	public static void setRand_seed(long rand_seed) {
		JlmParamsHolder.rand_seed = rand_seed;
	}
	public static int getIterations() {
		return iterations;
	}
	public static void setIterations(int iterations) {
		JlmParamsHolder.iterations = iterations;
	}
}
