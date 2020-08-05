package com.spring.demo.batch;

import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleItemReader implements ItemReader<String> {

    private List<String> dataSet = new ArrayList<>();

    private Iterator<String> iterator;

    public SimpleItemReader() {
        this.dataSet.add("1");
        this.dataSet.add("2");
        this.dataSet.add("3");
        this.dataSet.add("4");
        this.dataSet.add("5");
        this.iterator = this.dataSet.iterator();
    }

    @Override // This will be invoke for each item on the list
    public String read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
