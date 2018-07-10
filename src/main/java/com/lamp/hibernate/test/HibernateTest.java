package com.lamp.hibernate.test;

import org.junit.Test;

import com.lamp.hibernate.util.HibernateUtils;

public class HibernateTest {

	@Test
	public void createTable() {
		HibernateUtils.getSessionFactory();
	}
}
