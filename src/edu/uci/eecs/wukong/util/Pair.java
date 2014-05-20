package edu.uci.eecs.wukong.util;

public class Pair<L, R> {
	private L l;
	private R r;

	public Pair(L l, R r) {
		this.l = l;
		this.r = r;
	}

	public L getFirst() {
		return l;
	}

	public R getSecond() {
		return r;
	}

	public void setFirst(L l) {
		this.l = l;
	}

	public void setSecond(R r) {
		this.r = r;
	}
	
	public String toString(){
		return l.toString() + ", " + r.toString() + "\n";
	}
}
