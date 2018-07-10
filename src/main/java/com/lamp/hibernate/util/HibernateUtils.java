package com.lamp.hibernate.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtils {

	private static SessionFactory sessionFactory;

	// 在静态块中加载SessionFactory
	static {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		// 关闭：系统停止运行的时候关闭
		// JVM停止通过的时候:为JVM增加一个监听事件， 在JVM停止工作的时候，关闭sessionFactory
		// addShutdownHook：添加关闭事件
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				sessionFactory.close();
				System.out.println("sessionFactory关闭了....");
			}
		});
	}

	// 获得连接工厂
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	// 获得连接
	public static Session openSession() {
		return sessionFactory.openSession();
	}

}
