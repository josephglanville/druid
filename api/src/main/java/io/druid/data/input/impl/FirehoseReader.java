package io.druid.data.input.impl;

public interface FirehoseReader<T>
{
  boolean hasNext();
  T next();
  void close();
}
