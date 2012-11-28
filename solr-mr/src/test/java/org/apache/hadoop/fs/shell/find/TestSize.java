package org.apache.hadoop.fs.shell.find;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.shell.PathData;
import org.apache.hadoop.fs.shell.find.FindOptions;
import org.apache.hadoop.fs.shell.find.Result;
import org.apache.hadoop.fs.shell.find.Size;
import org.junit.Test;
import org.junit.Before;

public class TestSize extends TestExpression {
  private MockFileSystem fs;
  private PathData fiveBlocks;
  private PathData fourBlocks;
  private PathData sixBlocks;
  private PathData fiveBlocksMinus;
  private PathData fiveBlocksPlus;
  
  @Before
  public void setUp() throws IOException {
    MockFileSystem.reset();
    fs = new MockFileSystem();
    
    FileStatus fileStatus;
    
    fileStatus = mock(FileStatus.class);
    when(fileStatus.getLen()).thenReturn(5l * 512l);
    fs.setFileStatus("fiveBlocks", fileStatus);
    fiveBlocks = new PathData("fiveBlocks", fs.getConf());

    fileStatus = mock(FileStatus.class);
    when(fileStatus.getLen()).thenReturn(6l * 512l);
    fs.setFileStatus("sixBlocks", fileStatus);
    sixBlocks = new PathData("sixBlocks", fs.getConf());

    fileStatus = mock(FileStatus.class);
    when(fileStatus.getLen()).thenReturn(4l * 512l);
    fs.setFileStatus("fourBlocks", fileStatus);
    fourBlocks = new PathData("fourBlocks", fs.getConf());

    fileStatus = mock(FileStatus.class);
    when(fileStatus.getLen()).thenReturn((5l * 512l) + 511);
    fs.setFileStatus("fiveBlocksPlus", fileStatus);
    fiveBlocksPlus = new PathData("fiveBlocksPlus", fs.getConf());

    fileStatus = mock(FileStatus.class);
    when(fileStatus.getLen()).thenReturn((5l * 512l) - 1);
    fs.setFileStatus("fiveBlocksMinus", fileStatus);
    fiveBlocksMinus = new PathData("fiveBlocksMinus", fs.getConf());
  }

  @Test
  public void applyEqualsBlock() throws IOException {
    Size size = new Size();
    addArgument(size, "5");
    size.initialise(new FindOptions());

    assertEquals(Result.PASS, size.apply(fiveBlocks));
    assertEquals(Result.FAIL, size.apply(sixBlocks));
    assertEquals(Result.FAIL, size.apply(fourBlocks));
    assertEquals(Result.PASS, size.apply(fiveBlocksPlus));
    assertEquals(Result.FAIL, size.apply(fiveBlocksMinus));
  }

  @Test
  public void applyGreaterThanBlock() throws IOException {
    Size size = new Size();
    addArgument(size, "+5");
    size.initialise(new FindOptions());

    assertEquals(Result.FAIL, size.apply(fiveBlocks));
    assertEquals(Result.PASS, size.apply(sixBlocks));
    assertEquals(Result.FAIL, size.apply(fourBlocks));
    assertEquals(Result.FAIL, size.apply(fiveBlocksPlus));
    assertEquals(Result.FAIL, size.apply(fiveBlocksMinus));
  }

  @Test
  public void applyLessThanBlock() throws IOException {
    Size size = new Size();
    addArgument(size, "-5");
    size.initialise(new FindOptions());

    assertEquals(Result.FAIL, size.apply(fiveBlocks));
    assertEquals(Result.FAIL, size.apply(sixBlocks));
    assertEquals(Result.PASS, size.apply(fourBlocks));
    assertEquals(Result.FAIL, size.apply(fiveBlocksPlus));
    assertEquals(Result.PASS, size.apply(fiveBlocksMinus));
  }
  @Test
  public void applyEqualsBytes() throws IOException {
    Size size = new Size();
    addArgument(size, (5 * 512) + "c");
    size.initialise(new FindOptions());

    assertEquals(Result.PASS, size.apply(fiveBlocks));
    assertEquals(Result.FAIL, size.apply(sixBlocks));
    assertEquals(Result.FAIL, size.apply(fourBlocks));
    assertEquals(Result.FAIL, size.apply(fiveBlocksPlus));
    assertEquals(Result.FAIL, size.apply(fiveBlocksMinus));
  }

  @Test
  public void applyGreaterThanBytes() throws IOException {
    Size size = new Size();
    addArgument(size, "+" + (5 * 512) + "c");
    size.initialise(new FindOptions());

    assertEquals(Result.FAIL, size.apply(fiveBlocks));
    assertEquals(Result.PASS, size.apply(sixBlocks));
    assertEquals(Result.FAIL, size.apply(fourBlocks));
    assertEquals(Result.PASS, size.apply(fiveBlocksPlus));
    assertEquals(Result.FAIL, size.apply(fiveBlocksMinus));
  }

  @Test
  public void applyLessThanBytes() throws IOException {
    Size size = new Size();
    addArgument(size, "-" + (5 * 512) + "c");
    size.initialise(new FindOptions());

    assertEquals(Result.FAIL, size.apply(fiveBlocks));
    assertEquals(Result.FAIL, size.apply(sixBlocks));
    assertEquals(Result.PASS, size.apply(fourBlocks));
    assertEquals(Result.FAIL, size.apply(fiveBlocksPlus));
    assertEquals(Result.PASS, size.apply(fiveBlocksMinus));
  }
}
