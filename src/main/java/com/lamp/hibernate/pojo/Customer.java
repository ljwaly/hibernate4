package com.lamp.hibernate.pojo;

import java.util.HashSet;
import java.util.Set;

public class Customer {

	private Integer id;
	private String name;
	private String city;

	//集合
	//set:无需不重复
	//也可以用list:有序重复
	//配置hbm.xml的时候，如果类中用的是list集合的话，那边hbm中也可以使用<bag>标签配置集合
    //<bag>:有序不重复，但是效率低下
	private Set<Order> orders = new HashSet<Order>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Set<Order> getOrders() {
		return orders;
	}

	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", city=" + city + "]";
	}

}
