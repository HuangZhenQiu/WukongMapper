package edu.uci.eecs.wukong.energy.mapper;

import junit.framework.TestCase;
import org.junit.Test;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.Iterator;
import java.util.PriorityQueue;

public class PriorityQueueTest extends TestCase{
	
	@Test
	public void testWuDevicPriorityQueueUpdate() {
		
		PriorityQueue<WuDevice> queue = new PriorityQueue<WuDevice>();
		
		WuDevice device1 = new WuDevice(1, 0.0, null);
		device1.setCurrentConsumption(3.0);
		queue.add(device1);
		
		WuDevice device2 = new WuDevice(2, 0.0, null);
		device2.setCurrentConsumption(4.0);
		queue.add(device2);
		
		WuDevice device3 = new WuDevice(3, 0.0, null);
		device3.setCurrentConsumption(5.0);
		queue.add(device3);
		
		Iterator<WuDevice> iter = queue.iterator();
		
		while(iter.hasNext()) {
			WuDevice device = iter.next();
			
			if(2 == device.getWuDeviceId()) {
				device.setCurrentConsumption(100.0);
				queue.remove(device);
				queue.add(device);
				break;
			}
			
		}
		
		iter = queue.iterator();
		while(iter.hasNext()) {
			WuDevice device = iter.next();
			System.out.println(device.getCurrentConsumption());
		}
	}
	
	@Test
	public void testEdgePriorityQueueUpdate() {
		PriorityQueue<FlowBasedProcess.Edge> queue = new PriorityQueue<FlowBasedProcess.Edge>();
		
		FlowBasedProcess.Edge edge1 = new FlowBasedProcess.Edge(null, null, 2, 5);
		FlowBasedProcess.Edge edge2 = new FlowBasedProcess.Edge(null, null, 10, 8);
		
		queue.add(edge1);
		queue.add(edge2);
		
		
		Iterator<FlowBasedProcess.Edge> iter = queue.iterator();
		while(iter.hasNext()) {
			FlowBasedProcess.Edge edge = iter.next();
			System.out.println(edge.getWeight());
		}
	}

}
