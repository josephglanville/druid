package io.druid.data.input.impl;

import com.google.common.base.Throwables;
import io.druid.java.util.common.logger.Logger;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.InputStream;

public class LineOrientedFirehoseReader implements FirehoseReader<String>
{
  private static final Logger LOG = new Logger(LineOrientedFirehoseReader.class);
  private LineIterator lineIterator;

  public LineOrientedFirehoseReader(InputStream inputStream) {
    try {
      lineIterator = IOUtils.lineIterator(inputStream, Charsets.UTF_8);
    }
    catch (Exception e) {
      LOG.error(
          e,
          "Exception reading from input stream"
      );
      throw Throwables.propagate(e);
    }
  }

  @Override
  public boolean hasNext()
  {
    return lineIterator.hasNext();
  }

  @Override
  public String next()
  {
    return lineIterator.next();
  }

  @Override
  public void close()
  {
    lineIterator.close();
  }
}
