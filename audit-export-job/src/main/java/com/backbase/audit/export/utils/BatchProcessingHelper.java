package com.backbase.audit.export.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@UtilityClass
public class BatchProcessingHelper {
    public <T> void processBatches(List<T> list, Function<List<T>, CompletableFuture<Void>> futureFunction, int batchSize) {
        var futures = partitionList(list, batchSize).stream()
                .map(futureFunction)
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private  <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        AtomicInteger counter = new AtomicInteger(0);
        return new ArrayList<>(list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize))
                .values());
    }
}
