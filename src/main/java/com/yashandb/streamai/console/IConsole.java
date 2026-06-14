/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import com.yashandb.streamai.exception.SaiClosedException;

/** 控制台交互接口，提供线程安全的阻塞读取和输出方法。 */
public interface IConsole extends AutoCloseable {

  /**
   * 阻塞读取一行用户输入。支持粘贴多行内容（通过 BRACKETED_PASTE），粘贴的换行符会保留在返回文本中。
   *
   * @return 用户输入的一行文本（不含换行符），输入结束时返回 {@code null}
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  String readLine() throws SaiClosedException;

  /**
   * 向控制台输出一行纯文本并立即刷新。等价于 {@code print(StyledText.of(text))}。
   *
   * @param text 要输出的纯文本
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  void print(String text) throws SaiClosedException;

  /**
   * 向控制台输出一行带样式的文本并立即刷新。输出会插入到当前输入提示行上方，不干扰用户正在输入的提示行。
   *
   * @param text 要输出的带样式文本
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  void print(StyledText text) throws SaiClosedException;

  /**
   * 将文本复制到系统剪贴板。
   *
   * @param text 要复制的文本（支持包含换行符的多行文本）
   * @throws SaiClosedException 控制台已关闭时抛出
   */
  void copy(String text) throws SaiClosedException;

  @Override
  void close();
}
