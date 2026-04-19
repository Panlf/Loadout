package com.loadout.segment;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author panlf
 * @date 2026/4/15
 */
@Slf4j
public class HanLpTest {

    @Test
    public void testSegment(){
        String text = "近日，兵团十四师二二四团塔克拉玛干沙漠南缘，8000亩冬小麦进入拔节期，兵团人织出巨型“同心圆”。指针式喷灌机如同圆规，380米长的机械手臂匀速缓慢旋转，180多个喷头同步喷洒，将一株株麦苗润得青翠。";
        List<String> segment = HanLpUtils.extractKeywords(text,5);
        segment.forEach(log::info);
    }
}
