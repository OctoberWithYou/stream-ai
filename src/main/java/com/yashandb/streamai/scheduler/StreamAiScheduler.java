/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.scheduler;

import com.yashandb.streamai.console.IConsole;
import com.yashandb.streamai.console.StyledText;
import com.yashandb.streamai.console.jline.CommandCompleter;
import com.yashandb.streamai.console.jline.ConsoleFactory;
import com.yashandb.streamai.exception.SaiClosedException;
import com.yashandb.streamai.exception.SaiIllegalArgumentException;
import com.yashandb.streamai.exception.SaiStartFailedException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用调度器，负责初始化流程编排：展示 Banner、创建控制台、驱动命令循环。
 *
 * <h2>职责划分</h2>
 *
 * <ul>
 *   <li>本类：生命周期管理（Banner 展示、控制台创建、资源清理）
 *   <li>{@link ConsoleCommandHandler}：命令解析与执行
 *   <li>{@link ConsoleFactory}：控制台构建
 * </ul>
 */
@Slf4j
public final class StreamAiScheduler {

  private static final String BANNER_RESOURCE = "/banner.txt";
  private static final int ASCII_ART_LINES = 5;

  private final ConsoleCommandHandler commandHandler;

  /** 构造调度器，使用默认命令处理器。 */
  public StreamAiScheduler() {
    this(new ConsoleCommandHandler());
  }

  /**
   * 构造调度器，使用指定的命令处理器。
   *
   * @param commandHandler 命令处理器（不能为 {@code null}）
   */
  public StreamAiScheduler(final ConsoleCommandHandler commandHandler) {
    this.commandHandler = commandHandler;
  }

  /** 启动应用：展示 Banner 并进入控制台交互循环。 */
  public void start() {
    try {
      final CommandCompleter completer = new CommandCompleter("/help", "/quit");
      try (final IConsole console = ConsoleFactory.create(completer)) {
        displayBanner(console);
        console.print("StreamAI Console - type '/help' for commands, '/quit' to exit");
        runConsoleLoop(console);
      }
    } catch (final SaiStartFailedException e) {
      LOG.error("Failed to start console", e);
    } catch (final SaiClosedException e) {
      LOG.warn("Console closed unexpectedly", e);
    } catch (final SaiIllegalArgumentException e) {
      LOG.error("Failed to create command completer", e);
    }
  }

  private void displayBanner(final IConsole console) {
    try (final InputStream is = getClass().getResourceAsStream(BANNER_RESOURCE)) {
      if (is == null) {
        LOG.warn("Banner resource not found: {}", BANNER_RESOURCE);
        return;
      }
      final String banner =
          new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
              .lines()
              .collect(Collectors.joining(System.lineSeparator()));
      printBannerLines(console, banner);
    } catch (final SaiClosedException e) {
      LOG.warn("Console closed during banner display", e);
    } catch (final Exception e) {
      LOG.warn("Failed to display banner", e);
    }
  }

  private static void printBannerLines(final IConsole console, final String banner)
      throws SaiClosedException {
    final String[] lines = banner.split(System.lineSeparator());
    for (int i = 0; i < lines.length; i++) {
      if (i < ASCII_ART_LINES) {
        console.print(StyledText.builder().bold().cyan().append(lines[i]).build());
      } else {
        console.print(StyledText.builder().yellow().append(lines[i]).build());
      }
    }
  }

  private void runConsoleLoop(final IConsole console) throws SaiClosedException {
    while (true) {
      final String line = console.readLine();
      if (commandHandler.handle(console, line)) {
        break;
      }
    }
  }
}
