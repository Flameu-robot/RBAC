package org.example.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    void add(T item);
    boolean remove(T item);
    Optional findById(String id);
    List findAll();
    int count();
    void clear();
}