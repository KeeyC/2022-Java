package com.focustech.fpc.algorithm;

/**
 * 差异分析器接口
 * @author MOCO
 */
@FunctionalInterface
public interface IDifferenceAnalyser {

    /**
     * 比较 origin 和 dest 两个文件的差异
     * @return 差异行行号数组
     */
    int[] diff();
}
