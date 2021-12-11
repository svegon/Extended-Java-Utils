package com.github.svegon.utils.collections;

import com.google.common.collect.Iterators;

import java.util.*;


public abstract class AbstractDeque<E> extends AbstractQueue<E> implements Deque<E> {
    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerFirst(E e) {
        try {
            addFirst(e);
            return true;
        } catch (OutOfMemoryError ignored) {
            return false;
        }
    }

    @Override
    public boolean offerLast(E e) {
        try {
            addLast(e);
            return true;
        } catch (OutOfMemoryError ignored) {
            return false;
        }
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return pollFirst();
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return pollLast();
    }

    @Override
    public E pollFirst() {
        try {
            Iterator<E> itr = iterator();
            E e = itr.next();
            itr.remove();
            return e;
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }

    @Override
    public E pollLast() {
        try {
            Iterator<E> itr = descendingIterator();
            E e = itr.next();
            itr.remove();
            return e;
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return peekFirst();
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return peekLast();
    }

    @Override
    public E peekFirst() {
        try {
            return iterator().next();
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }

    @Override
    public E peekLast() {
        try {
            return descendingIterator().next();
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        Iterator<E> itr = descendingIterator();

        if (!Iterators.tryFind(itr, o == null ? Objects::isNull : o::equals).isPresent()) {
            return false;
        }

        itr.remove();
        return true;
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }
}
