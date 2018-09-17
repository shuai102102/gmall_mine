package com.atguigu.cart.cart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallCartWebApplicationTests {

	@Test
	public void contextLoads() throws InterruptedException {
		for(int i = 1;i<i+1;i++){
			System.err.println("    * *             * *");
			Thread.sleep(200);
			System.err.println(" *        *     *        *");
			Thread.sleep(200);
			System.err.println("*            *            *");
			Thread.sleep(200);Thread.sleep(200);
			System.err.println("*                         *");
			Thread.sleep(200);
			System.err.println(" *                       *");
			Thread.sleep(200);
			System.err.println("  *                     *");
			Thread.sleep(200);
			System.err.println("    *                 *");
			Thread.sleep(200);
			System.err.println("        *          *");
			Thread.sleep(200);
			System.err.println("             *");

			Thread.sleep(1000);
			System.out.println();System.out.println();System.out.println();


		}

	}
}

class sb extends Thread{

	@Override
	public void run() {

	}
}
