/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.yashandb.streamai.console.IConsole;
import com.yashandb.streamai.console.StyledText;
import com.yashandb.streamai.exception.SaiClosedException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** ConsoleCommandHandler 单元测试。 */
public class ConsoleCommandHandlerTest {

  /** 简单的输出收集器。 */
  private static final class MockConsole implements IConsole {
    private final List<String> outputs = new ArrayList<>();
    private boolean closed = false;

    @Override
    public String readLine() throws SaiClosedException {
      checkClosed();
      return null;
    }

    @Override
    public void print(final String text) throws SaiClosedException {
      checkClosed();
      outputs.add(text);
    }

    @Override
    public void print(final StyledText text) throws SaiClosedException {
      checkClosed();
      outputs.add(text.toPlainText());
    }

    @Override
    public void copy(final String text) throws SaiClosedException {
      checkClosed();
    }

    @Override
    public void close() {
      closed = true;
    }

    private void checkClosed() throws SaiClosedException {
      if (closed) {
        throw new SaiClosedException("Mock console is closed");
      }
    }

    public String getOutput() {
      return String.join("\n", outputs);
    }
  }

  @Test
  public void handleNullInputReturnsTrue() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertTrue(handler.handle(console, null));
    assertTrue(console.getOutput().contains("Bye!"));
  }

  @Test
  public void handleEmptyInputReturnsFalse() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertFalse(handler.handle(console, ""));
    assertFalse(handler.handle(console, "   "));
    assertEquals("", console.getOutput());
  }

  @Test
  public void handleHelpCommand() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertFalse(handler.handle(console, "/help"));
    final String output = console.getOutput();
    assertTrue(output.contains("Available Commands"));
    assertTrue(output.contains("/help"));
    assertTrue(output.contains("/quit"));
  }

  @Test
  public void handleQuitCommand() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertTrue(handler.handle(console, "/quit"));
    assertTrue(console.getOutput().contains("Bye!"));
  }

  @Test
  public void handleQuitCommandCaseInsensitive() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertTrue(handler.handle(console, "/QUIT"));
    assertTrue(handler.handle(console, "/Quit"));
  }

  @Test
  public void handleUnknownSlashCommandIsEchoed() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertFalse(handler.handle(console, "/unknown"));
    assertEquals("/unknown", console.getOutput());
  }

  @Test
  public void handleHelpWithArgsIsEchoed() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertFalse(handler.handle(console, "/help extra"));
    assertEquals("/help extra", console.getOutput());
  }

  @Test
  public void handleNonCommandInputIsEchoed() throws SaiClosedException {
    final MockConsole console = new MockConsole();
    final ConsoleCommandHandler handler = new ConsoleCommandHandler();
    assertFalse(handler.handle(console, "hello world"));
    assertEquals("hello world", console.getOutput());
  }
}
