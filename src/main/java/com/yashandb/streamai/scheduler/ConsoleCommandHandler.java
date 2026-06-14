/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.scheduler;

import com.yashandb.streamai.console.IConsole;
import com.yashandb.streamai.console.StyledText;
import com.yashandb.streamai.exception.SaiClosedException;

/**
 * 控制台命令处理器，负责解析用户输入并执行对应命令。
 *
 * <h2>设计目标</h2>
 *
 * <p>将命令解析和命令执行从调度器中分离，使调度器专注于生命周期管理（Banner 展示、控制台创建、资源清理）， 而本类专注于命令逻辑。这种分离使两者可以独立演进和测试。
 *
 * <h2>命令格式</h2>
 *
 * <p>命令必须以 {@code /} 开头，且严格匹配（不允许额外参数）。例如：
 *
 * <ul>
 *   <li>{@code /help} - 显示帮助信息
 *   <li>{@code /quit} - 退出应用
 *   <li>{@code /help xxx} - 不以 {@code /help} 精确匹配，当作普通文本回显
 *   <li>{@code hello} - 不以 {@code /} 开头，当作普通文本回显
 * </ul>
 *
 * <h2>扩展方式</h2>
 *
 * <p>子类可覆盖 {@link #handleCommand(IConsole, String)} 方法添加新命令， 并在 {@link #printHelp(IConsole)} 中补充说明。
 */
public class ConsoleCommandHandler {

  private static final String CMD_PREFIX = "/";
  private static final String CMD_HELP = "/help";
  private static final String CMD_QUIT = "/quit";

  /**
   * 处理一行用户输入。
   *
   * <p>以 {@code /} 开头的输入尝试匹配命令，其余输入原样回显。空行会被静默忽略。
   *
   * @param console 控制台实例，用于输出结果
   * @param input 用户输入的原始文本
   * @return {@code true} 表示应退出循环（quit 命令或 EOF）；{@code false} 表示继续
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  public boolean handle(final IConsole console, final String input) throws SaiClosedException {
    if (input == null) {
      console.print("Bye!");
      return true;
    }
    final String trimmed = input.trim();
    if (trimmed.isEmpty()) {
      return false;
    }
    if (!trimmed.startsWith(CMD_PREFIX)) {
      echoInput(console, input);
      return false;
    }
    return handleCommand(console, trimmed);
  }

  /**
   * 分发命令到对应的处理方法。
   *
   * <p>子类可覆盖此方法添加新命令。对于未识别的命令，原样回显。
   *
   * @param console 控制台实例
   * @param cmd 已 trim 的命令（以 {@code /} 开头）
   * @return {@code true} 表示应退出循环
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  protected boolean handleCommand(final IConsole console, final String cmd)
      throws SaiClosedException {
    return switch (cmd.toLowerCase()) {
      case CMD_HELP -> {
        printHelp(console);
        yield false;
      }
      case CMD_QUIT -> {
        console.print("Bye!");
        yield true;
      }
      default -> {
        echoInput(console, cmd);
        yield false;
      }
    };
  }

  /**
   * 回显用户输入。
   *
   * <p>子类可覆盖此方法自定义回显格式。
   *
   * @param console 控制台实例
   * @param input 原始输入文本
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  protected void echoInput(final IConsole console, final String input) throws SaiClosedException {
    console.print(input);
  }

  /**
   * 打印帮助信息。
   *
   * <p>子类可覆盖此方法以补充自定义命令的说明。
   *
   * @param console 控制台实例
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  protected void printHelp(final IConsole console) throws SaiClosedException {
    console.print(StyledText.builder().cyan().append("=== Available Commands ===").build());
    console.print(
        StyledText.builder()
            .append("  ")
            .bold()
            .append("/help")
            .normal()
            .append("  - Show this help message")
            .build());
    console.print(
        StyledText.builder()
            .append("  ")
            .bold()
            .append("/quit")
            .normal()
            .append("  - Exit the application")
            .build());
  }
}
