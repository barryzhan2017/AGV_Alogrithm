package org.spring.springboot.algorithmn.conflict_free_routing;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.spring.springboot.algorithmn.exception.NoPathFeasibleException;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GetRouteTest {

    private double[][] graph;


    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        graph = CommonTestConstant.initializeGraph();
    }

    //Start from source node 9, and try to add free time window in node 1. it should fail because no link exists.
    @Test
    public void shouldNextPossibleTimeWindowNotBeAddedWhenItIsNotReachable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 0;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 1, -1);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(0, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(0).add(endTimeWindow);
        freeTimeWindowList.get(9).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> occupiedTimeWindow = new ArrayList<>();
        occupiedTimeWindow.add(currentTimeWindow);
        List<TimeWindow> possibleTimeWindow = new ArrayList<>();
        routing.findPossibleNextTimeWindow(endTimeWindow, occupiedTimeWindow, possibleTimeWindow);
        assertEquals(0, possibleTimeWindow.size());
        assertEquals(1, occupiedTimeWindow.size());
        assertEquals(currentTimeWindow, occupiedTimeWindow.get(0));
        assertEquals(CommonConstant.INFINITE, endTimeWindow.getLeastTimeReachHere(), 0.000000001);
        assertNull(endTimeWindow.getLastTimeWindow());
        assertEquals(-1, (int) endTimeWindow.getPath()[0]);
    }

    //Start from source node 9, and try to add free time window in node 4. it should succeed because there is a link and he time window is available.
    @Test
    public void shouldNextPossibleTimeWindowBeAddedAndTimeWindowStatusChangesCorrectlyWhenItIsReachable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 0;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, -1, -1);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(3, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(0).add(endTimeWindow);
        freeTimeWindowList.get(9).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> occupiedTimeWindow = new ArrayList<>();
        occupiedTimeWindow.add(currentTimeWindow);
        List<TimeWindow> possibleTimeWindow = new ArrayList<>();
        routing.findPossibleNextTimeWindow(endTimeWindow, occupiedTimeWindow, possibleTimeWindow);
        assertEquals(1, possibleTimeWindow.size());
        assertEquals(endTimeWindow, possibleTimeWindow.get(0));
        assertEquals(1, occupiedTimeWindow.size());
        assertEquals(currentTimeWindow, occupiedTimeWindow.get(0));
        assertEquals((2 - 1) / 2.0, endTimeWindow.getLeastTimeReachHere(), 0.000000001);
        assertEquals(currentTimeWindow, endTimeWindow.getLastTimeWindow());
        assertEquals(9, (int) endTimeWindow.getPath()[0]);
        assertEquals(3, (int) endTimeWindow.getPath()[1]);
        assertEquals(-1, (int) endTimeWindow.getPath()[2]);
    }

    //Given just one AGV and task started from node 10(start from the first buffer(right one)) to node 6, check if the path is one of the best one and the time calculation is correct.
    //Check if the free time window and reserved time window is changed correspondingly.
    @Test
    public void shouldAGVGoCorrectlyFrom1To6WhenThereIsNoPreviousRouting() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 5;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        currentTimeWindow.setFirstStep(true);
        TimeWindow reservedTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        reservedTimeWindowList.get(9).add(reservedTimeWindow);
        //Initialize all the free time windows
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE, -1, -1);
            freeTimeWindowList.get(i).add(freeTimeWindow);
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> path = routing.getRoute();
        //The path should be 10->4->9->6 or 10->4->5->6
        assertEquals(4, path.size());
        //Test for the first time window(node 10)
        TimeWindow timeWindow0 = path.get(0);
        assertEquals(9, timeWindow0.getNodeNumber());
        assertEquals(null, timeWindow0.getLastTimeWindow());
        assertEquals(0, timeWindow0.getLeastTimeReachHere(), 0.000000001);
         
        assertEquals(CommonConstant.INFINITE, timeWindow0.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow0.getStartTime(), 0.000000001);
        assertEquals(3, timeWindow0.getNextNodeNumber());
        assertEquals(-1, (int) timeWindow0.getPath()[0]);
        assertEquals(-1, (int) timeWindow0.getPath()[1]);
        assertEquals(-1, (int) timeWindow0.getPath()[2]);

        //Test for the second time window(node 4)
        double timeToReachNode4 = 0 + CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow1 = path.get(1);
        assertEquals(3, timeWindow1.getNodeNumber());
        assertEquals(timeWindow0, timeWindow1.getLastTimeWindow());
        assertEquals(timeToReachNode4, timeWindow1.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow1.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow1.getStartTime(), 0.000000001);
        assertTrue(timeWindow1.getNextNodeNumber() == 8 || timeWindow1.getNextNodeNumber() == 4);
        assertEquals(9, (int) timeWindow1.getPath()[0]);
        assertEquals(3, (int) timeWindow1.getPath()[1]);
        assertEquals(-1, (int) timeWindow1.getPath()[2]);

        //Test for third time window(node 9 or node 5)
        double timeToReachNode9 = timeToReachNode4 + (10 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        double timeToReachNode5 = timeToReachNode4 + (8 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow2 = path.get(2);
        assertTrue((timeWindow2.getNodeNumber() == 4 && timeWindow2.getLeastTimeReachHere() == timeToReachNode5)
                || (timeWindow2.getNodeNumber() == 8 && timeWindow2.getLeastTimeReachHere() == timeToReachNode9));
        assertEquals(timeWindow1, timeWindow2.getLastTimeWindow());
        assertEquals(CommonConstant.INFINITE, timeWindow2.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow2.getStartTime(), 0.000000001);
        assertTrue(timeWindow2.getNextNodeNumber() == 5);
        assertEquals(3, (int) timeWindow2.getPath()[0]);
        assertTrue(timeWindow2.getPath()[1] == 4 || timeWindow2.getPath()[1] == 8);
        assertEquals(-1, (int) timeWindow2.getPath()[2]);


        //Test for last time window(node 6)
        double timeToReachNode6 = timeToReachNode9 + (8 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow3 = path.get(3);
        assertEquals(5, timeWindow3.getNodeNumber());
        assertEquals(timeWindow2, timeWindow3.getLastTimeWindow());
        assertEquals(timeToReachNode6, timeWindow3.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow3.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow3.getStartTime(), 0.000000001);
        assertTrue(timeWindow3.getNextNodeNumber() == -1);
        assertTrue(timeWindow3.getPath()[0] == 4 || timeWindow3.getPath()[0] == 8);
        assertEquals(5, (int) timeWindow3.getPath()[1]);
        assertEquals(-1, (int) timeWindow3.getPath()[2]);

        //Test for free time window list
        Queue<TimeWindow> freeTimeWindowListForNode10 = freeTimeWindowList.get(9);
        assertEquals(freeTimeWindowListForNode10.poll(),new TimeWindow(9, CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0));
        Queue<TimeWindow> freeTimeWindowListForNode4 = freeTimeWindowList.get(3);
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3, 0,
                timeToReachNode4, -1, 0));
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3,
                timeToReachNode4 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0));
        Queue<TimeWindow> freeTimeWindowListForNode5 = freeTimeWindowList.get(4);
        Queue<TimeWindow> freeTimeWindowListForNode9 = freeTimeWindowList.get(8);
        assertTrue((freeTimeWindowListForNode9.size() == 1 && freeTimeWindowListForNode9.peek().equals(new TimeWindow(8,
                0, CommonConstant.INFINITE, -1, 0)) &&
                freeTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                0, timeToReachNode5, -1, 0)) && freeTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                timeToReachNode5 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0))) || (freeTimeWindowListForNode5.size() == 1
                && freeTimeWindowListForNode5.peek().equals(new TimeWindow(4,
                0, CommonConstant.INFINITE, -1, 0)) &&
                freeTimeWindowListForNode9.poll().equals(new TimeWindow(8,
                        0, timeToReachNode9, -1, 0)) && freeTimeWindowListForNode9.poll().equals(new TimeWindow(8,
                timeToReachNode9 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0))));
        Queue<TimeWindow> freeTimeWindowListForNode6 = freeTimeWindowList.get(5);
        assertEquals(freeTimeWindowListForNode6.poll(), new TimeWindow(5,
                0, timeToReachNode6, -1, 0));
        
        //other free time window should be available all the time
        assertTrue(freeTimeWindowList.size() == 11);
        assertEquals(freeTimeWindowList.get(0).poll(), new TimeWindow(0, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(1).poll(), new TimeWindow(1, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(2).poll(), new TimeWindow(2, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(6).poll(), new TimeWindow(6, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(7).poll(), new TimeWindow(7, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(10).poll(), new TimeWindow(10, 0,
                CommonConstant.INFINITE, -1, 0));


        //Test for reserved time window list
        //reserved time window needs to check the next node
        Queue<TimeWindow> reservedTimeWindowListForNode10 = reservedTimeWindowList.get(9);
        assertEquals(3, reservedTimeWindowListForNode10.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode10.poll(), new TimeWindow(9, 0,CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                 0, 4));
        Queue<TimeWindow> reservedTimeWindowListForNode4 = reservedTimeWindowList.get(3);
        assertTrue(reservedTimeWindowListForNode4.peek().getNextNodeNumber() == 8 || reservedTimeWindowListForNode4.peek().getNextNodeNumber() == 4);
        assertEquals(reservedTimeWindowListForNode4.poll(), new TimeWindow(3, 
                timeToReachNode4, timeToReachNode4 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED,
                0, 0));
        Queue<TimeWindow> reservedTimeWindowListForNode5 = reservedTimeWindowList.get(4);
        Queue<TimeWindow> reservedTimeWindowListForNode9 = reservedTimeWindowList.get(8);
        if (!reservedTimeWindowListForNode5.isEmpty()) {
            assertEquals(5, reservedTimeWindowListForNode5.peek().getNextNodeNumber());
        }
        if (!reservedTimeWindowListForNode9.isEmpty()) {
            assertEquals(5, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        }
        assertTrue((reservedTimeWindowListForNode9.isEmpty()  &&
                reservedTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                        timeToReachNode5, timeToReachNode5 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED,0, 0)))
                 || (reservedTimeWindowListForNode5.isEmpty() && reservedTimeWindowListForNode9.poll().equals(new TimeWindow(8,
                timeToReachNode9, timeToReachNode9 + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED, 0, 0))));
        Queue<TimeWindow> reservedTimeWindowListForNode6 = reservedTimeWindowList.get(5);
        assertEquals(-1, reservedTimeWindowListForNode6.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode6.poll(), new TimeWindow(5,
                 timeToReachNode6, CommonConstant.INFINITE,0, -1));
        //other reserved time window should be empty
        assertTrue(reservedTimeWindowList.size() == 11);
        assertTrue(reservedTimeWindowList.get(0).isEmpty());
        assertTrue(reservedTimeWindowList.get(1).isEmpty());
        assertTrue(reservedTimeWindowList.get(2).isEmpty());
        assertTrue(reservedTimeWindowList.get(6).isEmpty());
        assertTrue(reservedTimeWindowList.get(7).isEmpty());
        assertTrue(reservedTimeWindowList.get(10).isEmpty());
    }


}
