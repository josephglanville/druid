/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.data.input.impl;

import io.druid.data.input.Firehose;
import io.druid.data.input.InputRow;
import io.druid.utils.Runnables;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 */
public class FileIteratingFirehose<T> implements Firehose
{
  private final Iterator<FirehoseReader<T>> readers;

  private FirehoseReader<T> reader = null;
  private InputRowParser<T> parser;

  private final Closeable closer;

  public FileIteratingFirehose(Iterator<FirehoseReader<T>> readers, InputRowParser<T> parser) {
    this(readers, parser, null);
  }

  public FileIteratingFirehose(
      Iterator<FirehoseReader<T>> readers,
      InputRowParser<T> parser,
      Closeable closer
  )
  {
    this.readers = readers;
    this.parser = parser;
    this.closer = closer;
  }

  @Override
  public boolean hasMore()
  {
    while ((reader == null || !reader.hasNext()) && readers.hasNext()) {
      reader = getNextReader();
    }

    return reader != null && reader.hasNext();
  }

  @Nullable
  @Override
  public InputRow nextRow()
  {
    if (!hasMore()) {
      throw new NoSuchElementException();
    }

    // TODO(jpg) should be using parseBatch
    return parser.parse(reader.next());
  }

  private FirehoseReader<T> getNextReader()
  {
    if (reader != null) {
      reader.close();
    }

    return readers.next();
  }

  @Override
  public Runnable commit()
  {
    return Runnables.getNoopRunnable();
  }

  @Override
  public void close() throws IOException
  {
    try {
      if (reader != null) {
        reader.close();
      }
    }
    catch (Throwable t) {
      try {
        if (closer != null) {
          closer.close();
        }
      }
      catch (Exception e) {
        t.addSuppressed(e);
      }
      throw t;
    }
    if (closer != null) {
      closer.close();
    }
  }
}
