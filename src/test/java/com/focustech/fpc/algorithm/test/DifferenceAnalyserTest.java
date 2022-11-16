package com.focustech.fpc.algorithm.test;

import com.focustech.fpc.algorithm.DemoDifferenceAnalyser;
import com.focustech.fpc.algorithm.IDifferenceAnalyser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author MOCO
 **/
public class DifferenceAnalyserTest {

    @Test
    public void valid() {
        long startTime = System.currentTimeMillis();
        IDifferenceAnalyser analyser = new DemoDifferenceAnalyser();
        int[] differenceLines = analyser.diff();
        long endTime = System.currentTimeMillis();
        long timeSpent = endTime - startTime;
        Assertions.assertTrue(timeSpent < 600000, "运行时长超限");
        Assertions.assertArrayEquals(new int[] { 1, 2, 4 }, differenceLines, "分析结果错误");
    }
}
