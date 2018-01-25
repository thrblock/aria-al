package com.thrblock.aria.source;

/**
 * AL command in queue
 * <p>
 * OpenAL 队列指令
 * 
 * @author zepu.li
 *
 */
@FunctionalInterface
interface IALCmd {
    /**
     * 运行指令
     */
    public void exec();
}
