/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.yashandb.streamai.console.jline.CommandCompleter;
import com.yashandb.streamai.console.jline.CompletionCandidate;
import com.yashandb.streamai.exception.SaiIllegalArgumentException;
import java.util.List;
import org.junit.Test;

/** CommandCompleter 单元测试。 */
public class CommandCompleterTest {

  @Test
  public void completeEmptyBuffer() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit", "echo");
    final List<CompletionCandidate> candidates = completer.complete("", 0);
    assertEquals(3, candidates.size());
    assertEquals("help", candidates.get(0).getValue());
    assertEquals("exit", candidates.get(1).getValue());
    assertEquals("echo", candidates.get(2).getValue());
  }

  @Test
  public void completePartialCommand() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit", "echo", "print");
    final List<CompletionCandidate> candidates = completer.complete("e", 1);
    assertEquals(2, candidates.size());
    assertEquals("exit", candidates.get(0).getValue());
    assertEquals("echo", candidates.get(1).getValue());
  }

  @Test
  public void completeFullCommand() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit");
    final List<CompletionCandidate> candidates = completer.complete("help", 4);
    assertEquals(1, candidates.size());
    assertEquals("help", candidates.get(0).getValue());
  }

  @Test
  public void completeNoMatch() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit");
    final List<CompletionCandidate> candidates = completer.complete("xyz", 3);
    assertTrue(candidates.isEmpty());
  }

  @Test
  public void completeAfterSpaceReturnsEmpty() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit");
    final List<CompletionCandidate> candidates = completer.complete("help ", 5);
    assertTrue(candidates.isEmpty());
  }

  @Test
  public void completeWithCursorInMiddle() throws SaiIllegalArgumentException {
    final CommandCompleter completer = new CommandCompleter("help", "exit", "echo");
    final List<CompletionCandidate> candidates = completer.complete("hexit", 1);
    assertEquals(1, candidates.size());
    assertEquals("help", candidates.get(0).getValue());
  }

  @Test(expected = SaiIllegalArgumentException.class)
  public void constructorWithNullThrows() throws SaiIllegalArgumentException {
    new CommandCompleter((String[]) null);
  }
}
