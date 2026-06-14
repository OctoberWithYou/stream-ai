/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/** ConsoleFactory 单元测试。 */
public class ConsoleFactoryTest {

  @Test
  public void createWithCompleter() throws Exception {
    final CommandCompleter completer = new CommandCompleter("help", "quit");
    try (final var console = ConsoleFactory.create(completer)) {
      assertNotNull(console);
    }
  }
}
