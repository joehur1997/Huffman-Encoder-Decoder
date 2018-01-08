import java.util.PriorityQueue;

public class Test {

	public static void main(String[] args) {
		PriorityQueue<Integer> pq= new PriorityQueue<Integer>();
		pq.add(5);
		pq.add(4);
		pq.add(3);
		pq.add(6);
		pq.add(7);
		
		System.out.println(pq.peek());
		System.out.println(pq.poll());
		System.out.println(pq.peek());

	}

}
