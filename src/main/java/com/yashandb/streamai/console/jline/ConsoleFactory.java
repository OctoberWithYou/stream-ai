/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import com.yashandb.streamai.console.IConsole;
import com.yashandb.streamai.exception.SaiStartFailedException;

/**
 * {@link CliConsole} 工厂，封装终端和控制台的复杂构建过程。
 *
 * <h2>设计目标</h2>
 *
 * <p>{@code CliConsole} 的构造涉及 JLine {@code Terminal}、{@code LineReader}、补全器注册等多个组件的初始化和配置。
 * 随着功能演进（如自定义提示符、历史记录、高亮器、多补全策略组合等），构建过程会越来越复杂。 将构建逻辑集中在工厂类中，使调用方无需关心底层细节，同时便于统一管理和测试。
 *
 * <h2>使用方式</h2>
 *
 * <pre>{@code
 * CommandCompleter completer = new CommandCompleter("help", "quit");
 * try (IConsole console = ConsoleFactory.create(completer)) { ... }
 * }</pre>
 *
 * <h2>扩展方向</h2>
 *
 * <p>未来可在此类中扩展：
 *
 * <ul>
 *   <li>自定义提示符（prompt）
 *   <li>命令历史记录持久化
 *   <li>语法高亮器（Highlighter）
 *   <li>多补全策略组合（AggregateCompletionProvider）
 *   <li>终端配置选项（颜色模式、编码等）
 * </ul>
 *
 * @see CliConsole
 * @see CompletionProvider
 */
public final class ConsoleFactory {

  private ConsoleFactory() {}

  /**
   * 创建带代码补全的控制台实例。
   *
   * <p>工厂内部负责将 {@link CompletionProvider} 桥接为 JLine 的 {@code Completer}， 并初始化终端、行读取器等组件。
   *
   * @param completionProvider 补全提供者（不能为 {@code null}）
   * @return 新创建的 IConsole 实例，调用方负责关闭
   * @throws SaiStartFailedException 终端初始化失败时抛出
   */
  public static IConsole create(final CompletionProvider completionProvider)
      throws SaiStartFailedException {
    return new CliConsole(completionProvider);
  }
}
