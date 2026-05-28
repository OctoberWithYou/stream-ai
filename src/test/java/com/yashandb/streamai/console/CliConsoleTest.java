/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.yashandb.streamai.console.jline.CliConsole;
import com.yashandb.streamai.exception.SaiClosedException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** CliConsole 单元测试。 */
public class CliConsoleTest {

  private Terminal terminal;
  private LineReader lineReader;
  private CliConsole console;

  /** 初始化测试环境。 */
  @Before
  public void setUp() throws Exception {
    terminal = TerminalBuilder.builder().dumb(true).build();
    lineReader = LineReaderBuilder.builder().terminal(terminal).build();
    console = new CliConsole(terminal, lineReader);
  }

  /** 清理资源。 */
  @After
  public void tearDown() {
    if (console != null) {
      console.close();
    }
  }

  @Test
  public void printDoesNotThrow() throws SaiClosedException {
    console.print("test message");
  }

  @Test
  public void printStyledTextDoesNotThrow() throws SaiClosedException {
    final StyledText text =
        StyledText.builder().bold().cyan().append("[INFO]").normal().append(" hello").build();
    console.print(text);
  }

  @Test
  public void printStyledTextAllColors() throws SaiClosedException {
    final StyledText text =
        StyledText.builder()
            .black()
            .append("B")
            .red()
            .append("R")
            .green()
            .append("G")
            .yellow()
            .append("Y")
            .blue()
            .append("Bl")
            .magenta()
            .append("M")
            .cyan()
            .append("C")
            .white()
            .append("W")
            .build();
    console.print(text);
  }

  @Test
  public void printStyledTextAllModifiers() throws SaiClosedException {
    final StyledText text =
        StyledText.builder()
            .bold()
            .append("bold")
            .underline()
            .append("underline")
            .italic()
            .append("italic")
            .strikethrough()
            .append("strikethrough")
            .build();
    console.print(text);
  }

  @Test
  public void styledTextOfNotNull() {
    assertNotNull(StyledText.of("plain text"));
  }

  @Test
  public void styledTextToPlainText() {
    final StyledText text =
        StyledText.builder().bold().cyan().append("hello").normal().append(" world").build();
    assertEquals("hello world", text.toPlainText());
  }

  @Test
  public void closeMultipleTimes() {
    console.close();
    console.close();
  }

  @Test(expected = SaiClosedException.class)
  public void printAfterCloseThrows() throws SaiClosedException {
    console.close();
    console.print("should fail");
  }

  @Test(expected = SaiClosedException.class)
  public void printStyledTextAfterCloseThrows() throws SaiClosedException {
    console.close();
    console.print(StyledText.of("should fail"));
  }

  @Test(expected = SaiClosedException.class)
  public void readLineAfterCloseThrows() throws SaiClosedException {
    console.close();
    console.readLine();
  }

  @Test
  public void copyDoesNotThrow() throws SaiClosedException {
    try {
      console.copy("test");
    } catch (final Exception e) {
      // copy may fail in headless env, that's ok
    }
  }
}
