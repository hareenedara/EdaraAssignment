package com.samples;

public interface TraceFilter<S, I extends Number, M> {

    M filter(S str, I num);
}
