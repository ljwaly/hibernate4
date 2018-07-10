package com.lamp.hibernate.test;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.junit.Test;

import com.lamp.hibernate.pojo.Customer;
import com.lamp.hibernate.pojo.Order;
import com.lamp.hibernate.util.HibernateUtils;

@SuppressWarnings("all")
public class TestDeno {

	@Test
	public void prepareData() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// 一个客户对应多个订单，一个客户对应10个订单
		Customer customer = new Customer();
		customer.setName("irving");
		customer.setCity("北京");

		session.save(customer);

		for (int i = 1; i <= 10; i++) {
			Order o = new Order();
			o.setName(customer.getName() + "的订单".getBytes() + i);
			o.setPrice(i * 10d);

			o.setCustomer(customer);
			session.save(o);
		}

		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testNavigate() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();
		// 1）．查询某客户信息,并且打印其下订单；
		// Customer customer = (Customer) session.get(Customer.class, 1);
		// System.out.println(customer);
		// 遍历所有的订单
		// for(Order o:customer.getOrders())
		// {
		// System.out.println(o);
		// }

		// 2）．查询某订单的信息，并打印其所属客户的信息。
		Order order = (Order) session.get(Order.class, 3);
		System.out.println(order);
		System.out.println(order.getCustomer());

		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testBaseQuery() {
		// 查询出所有客户信息。
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// hql
		List<Customer> list1 = session.createQuery("from Customer").list();
		System.out.println(list1);
		
		// sql:sql查询出来的对象放入的是Object[]数组中，所以我们通过addEntity装入实体
		List<Customer> list2 = session.createSQLQuery("select * from t_customer").addEntity(Customer.class).list();
		System.out.println(list2);
		
		// qbc
		List<Customer> list3 = session.createCriteria(Customer.class).list();

		System.out.println(list3);
		session.getTransaction().commit();
		session.close();
	}

	// 查询姓名是rose的客户
	@Test
	public void testQueryByCondition() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// hql
		// 方式一
		List<Customer> list1 = session.createQuery("from Customer where name = ?").setParameter(0, "rose").list();
		// 方式二
		List<Customer> list2 = session.createQuery("from Customer where name = ?").setString(0, "rose").list();

		// 方式三: 命名参数注入
		List<Customer> list3 = session.createQuery("from Customer where name = :name").setParameter("name", "rose")
				.list();
		// 方式四：
		List<Customer> list4 = session.createQuery("from Customer where name = :name").setString("name", "rose").list();

		// sql,除了sql语句不一样之外，参数注入方式跟上面一致，也是四种方式
		List<Customer> list5 = session.createSQLQuery("select * from t_customer where name = ?")
				.addEntity(Customer.class)// 采用SQLQuery先封装实体，在设置参数
				.setString(0, "rose").list();
		// 其余三种方式略，思路同上

		List<Customer> list7 = session.createSQLQuery("select * from t_customer where name = ?")
				.addEntity(Customer.class).setParameter(0, "rose").list();

		List<Customer> list8 = session.createSQLQuery("select * from t_customer where name = :name")
				.addEntity(Customer.class).setParameter("name", "rose").list();

		List<Customer> list9 = session.createSQLQuery("select * from t_customer where name = :name")
				.addEntity(Customer.class).setString("name", "rose").list();

		// qbc
		Criteria criteria = session.createCriteria(Customer.class);
		// 添加条件:criteria对象的add方法就是添加条件,
		// 方法中的参数是Criterion类型
		criteria.add(Restrictions.eq("name", "rose"));
		// 玩命的加条件
		criteria.add(Restrictions.like("city", "%上%"));

		// 查询
		List<Customer> list6 = criteria.list();

		System.out.println(list6);

		session.getTransaction().commit();
		session.close();
	}

	// 按照id对客户信息进行排序
	@Test
	public void testQueryByOrder() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// hql:直接采用order by关键字进行排序
		// asc：升序，默认值
		// desc:降序
		List<Customer> list1 = session.createQuery("from Customer order by id desc").list();

		// sql
		List<Customer> list2 = session.createSQLQuery("select * from t_customer order by id desc")
				.addEntity(Customer.class).list();

		// qbc
		List<Customer> list3 = session.createCriteria(Customer.class)
				// Criteria的排序方式：借助addOrder方法
				// 参数使用是org.hibernate.criterion.Order排序对象，它里面有两个方法
				// 一个是asc(排序的属性名):升序
				// 一个是desc(排序的属性名)：降序
				.addOrder(org.hibernate.criterion.Order.desc("id")).list();

		System.out.println(list3);
		session.getTransaction().commit();
		session.close();
	}

	// 将订单进行分页查询，每页10条记录，现在需要显示第二页的数据。
	@Test
	public void testQueryByPage() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();
		// 准备两个变量
		int page = 2;
		int pageCount = 10;
		// 起始数:hibernate也是从0开始计数,所以起始条数不需要+1
		int fromIndex = (page - 1) * 10;

		// hql:分页查询方式，适用所有的数据库
		List<Customer> list1 = session.createQuery("from Order")
				// //设置起始索引
				.setFirstResult(fromIndex)
				// //设置每页查询的条数
				.setMaxResults(pageCount).list();

		// sql:注意区分数据库：mysql的分页使用limit关键，oracle的分页相当复杂
		List list2 = session.createSQLQuery("select * from t_order limit ?,?").addEntity(Order.class)
				.setInteger(0, fromIndex).setInteger(1, pageCount).list();

		// qbc
		List<Order> list3 = session.createCriteria(Order.class)
				// 起始索引
				.setFirstResult(fromIndex)
				// 每页的条数
				.setMaxResults(pageCount).list();

		System.out.println(list3);
		session.getTransaction().commit();
		session.close();
	}

	// 查询客户的id和姓名。
	@Test
	public void testQueryByProjection() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// hql：投影查询返回是一个数组，不在一个是封装好的对象,
		// 在hibernate中，如果返回的是Object[]的话，那么这个对象是不会存在于一级缓存的,
		// 是一个非受管对象(不受session管理)
		// List集合的长度是3：
		// 0 [1,'rose']
		// 1 [2,'lucy']
		// 2 [3,'jack']
		// List<Object[]> list = session.createQuery("select c.id,c.name from
		// Customer c").list();
		// 适用hql投影查询的结果可以封装成一个对象，但是还是一个非受管对象
		// 步奏
		// 1 去po中添加构造方法:空参构造+带参构造
		// 2 重新编写hql语句
		// List<Customer> list = session.createQuery("select new
		// Customer(c.id,c.name) from Customer c").list();
		// sql
		// List<Object[]> list = session.createSQLQuery("select id,name from
		// t_customer").list();
		// qbc

		List<Object[]> list = session.createCriteria(Customer.class)
				// 设置投影,参数就是需要投影的属性
				.setProjection(
						// 投影可能需要投影多个列，所以将多个列加入list集合，list集合是有序的
						Projections.projectionList()
								// 向projectionList中添加需要查询的列
								.add(Property.forName("id")).add(Property.forName("name"))
				// 疯狂的追加投影的列
				).list();

		for (Object[] obj : list) {
			System.out.println(obj[0] + ":" + obj[1]);
		}

		System.out.println(list);
		session.getTransaction().commit();
		session.close();
	}

	// 查询用户的id和姓名。
	@Test
	// 投影查询：只查询部分属性的值
	public void queryByProjection() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();
		// hql
		// 结果集是根据返回的数据，自动封装为Object[]，没有封装为实体对象
		// List<Object[]> list = session.createQuery("select id,name from
		// Customer").list();
		// 如果要封装为实体对象，需要提供一个投影属性的构造方法,不会再调用默认的构造器
		// 尽管被封装为实体对象，但该对象，是个非受管对象。不是被session管理
		// List list = session.createQuery("select new Customer(id,name) from
		// Customer").list();
		// System.out.println(list);
		// for (Object[] obj : list) {
		// System.out.println(obj[1]);
		// }

		// sql
		// 结果集也是根据返回的数据的结果自动封装为Object[]
		List list2 = session.createSQLQuery("select id,name from t_customer")
				// 设置结果集封装策略
				// 类似于dbutil中的beanhandler，自动通过反射机制，自动将结果集封装到指定的类型中
				// .setResultTransformer(new
				// AliasToBeanResultTransformer(Customer.class))
				// 官方提供了一个工具类，简化代码编写
				.setResultTransformer(Transformers.aliasToBean(Customer.class)).list();
		// ResultTransformer

		System.out.println(list2);

		// qbc
		List list3 = session.createCriteria(Customer.class)
				// 设置投影列表
				.setProjection(Projections.projectionList()
						// 给属性起别名
						.add(Property.forName("id").as("id")).add(Property.forName("name").as("name")))
				// 添加结果集的封装策略
				// 发现了，该结果集封装策略，是根据字段的别名来自动封装
				// 解决方案：增加别名
				.setResultTransformer(Transformers.aliasToBean(Customer.class)).list();
		// Projection
		// Property
		System.out.println(list3);

		session.getTransaction().commit();
		session.close();

	}

	// 查询客户的总数
	@Test
	public void testQueryByCount() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// hql:hql返回的结果集类型是Long
		Object result1 = session.createQuery("select count(c) from Customer c").uniqueResult();
		long result2 = (Long) session.createQuery("select count(c) from Customer c").uniqueResult();

		// sql:返回是BigInteger
		Object result3 = session.createSQLQuery("select count(*) from t_customer").uniqueResult();
		BigInteger result4 = (BigInteger) session.createSQLQuery("select count(*) from t_customer").uniqueResult();
		// qbc:返回Long类型
		Object result5 = session.createCriteria(Customer.class)
				// rowCount：读取所有的行数
				.setProjection(Projections.rowCount())
				// 读取指定列的行数 ,这种读取方式，当city为null的时候，就不算一条记录
				.setProjection(Projections.count("city")).uniqueResult();

		System.out.println(result5);
		session.getTransaction().commit();
		session.close();
	}

	// 命名查询：只针对于HQL和SQL而言,QBC根本不需要我们写Query 语句
	@Test
	public void testNameQuery() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		
		List<Customer> list1 = session.getNamedQuery("com.lamp.hibernate.pojo.Customer.query1").list();
		System.out.println(list1);
		
		// 写在class标签内的，得结合类对应的完整的包路径的访问方法来访问
		List<Customer> list2 = session.getNamedQuery("customer.hql.queryAll").list();
		System.out.println(list2);

		// sql：调用sql的时候，对返回结果必须进行强制转换SQLQuery
		SQLQuery sqlQuery = (SQLQuery) session.getNamedQuery("query4");
		// 因此需要装载实体
		List<Customer> list3 = sqlQuery.addEntity(Customer.class).list();

		System.out.println(list3);
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testDetachedCriteria() {
		// 查询id值大等于2且城市是杭州的客户信息。
		// 模拟service层
		// DetachedCriteria detachedCriteria =
		// DetachedCriteria.forClass(Customer.class);
		// //拼装条件
		// detachedCriteria.add(Restrictions.ge("id", 2));
		// //继续拼装条件
		// detachedCriteria.add(Restrictions.eq("city", "杭州"));
		// //。。。拼命的添加条件

		// 查询id小于等于2的名字中包含l的客户信息
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Customer.class);
		// 拼装条件
		detachedCriteria.add(Restrictions.le("id", 2));
		detachedCriteria.add(Restrictions.like("name", "%l%"));

		// DAO层的代码,是固定的
		Session session = HibernateUtils.openSession();
		session.beginTransaction();
		// 传入session，返回在线的Criteria
		Criteria criteria = detachedCriteria.getExecutableCriteria(session);
		// 查询
		List<Customer> list = criteria.list();

		System.out.println(list);
		session.getTransaction().commit();
		session.close();
	}

	// HQL的方式：查询所有的客户及其对应的订单
	@Test
	public void testQueryForManyTable() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// 原始的方式:通过对执行结果的观察，需要使用的时候才会查询
		// 会发出很多次的sql语句，那我们能不能一次性查询出多表中的内容，可以的
		List<Customer> list = session.createQuery("from Customer").list();
		System.out.println(list);

		for (Customer c : list) {
			System.out.println(c.getOrders());
		}

		// HQL：使用内连接:一次性查处多表中的数据
		// hql写内连接，不需要写条件,因为在hbm中配置了关系
		// 通过断点观察数据，我们发现，查询出来的结果都是Object[]数组，是三张对象
		// /是不会存入session缓存中的
		List list1 = session.createQuery("from Customer c inner join c.orders").list();
		//
		// 可以通过迫切内连接解决散装对象的问题：也就是返回的不再是数组，而是我们想要的对象
		List list2 = session.createQuery("from Customer c inner join fetch c.orders").list();

		// 采用迫切内连接之后，发现数据重复，需要滤重
		List list3 = session.createQuery("select distinct(c) from Customer c inner join fetch c.orders").list();

		// 左外连接：返回的结果是一个Object[]的数组对象
		List list4 = session.createQuery("from Customer c left join c.orders").list();
		// 迫切左外连接：返回的结果是一个封装好的Customer对象
		List list5 = session.createQuery("select distinct c from Customer c left join fetch c.orders").list();
		System.out.println(list5);

		session.getTransaction().commit();
		session.close();
	}

	// qbc:进行多表关联
	// 一次性查询出所有用户及其所下的订单信息。
	@Test
	public void testQueryForManyTableByCriteria1() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// 主查询
		Criteria criteria = session.createCriteria(Customer.class);
		// 子查询:默认情况下：就是内连接
		criteria.createCriteria("orders");
		// 滤重:一定要在调用list方法之前过滤
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// 查询
		List list = criteria.list();

		session.getTransaction().commit();
		session.close();
	}

	// 一次性查询出某用户所下订单的信息，并且要求订单价格大于50元。
	@Test
	public void testQueryForManyTableByCriteria2() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		Criteria criteria = session.createCriteria(Order.class);
		// 添加条件
		criteria.add(Restrictions.gt("price", 50d));
		// 查询客户
		Criteria childCriteria = criteria.createCriteria("customer");
		// 如果childCriteria也需要添加条件的话，就在这个位置添加
		childCriteria.add(Restrictions.le("id", 2));

		// 滤重
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// 查询
		List list = criteria.list();

		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testQuery() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		// 加载customer信息
		// get：默认立即加载
		Customer c1 = (Customer) session.get(Customer.class, 1);
		System.out.println(c1);
		// load：默认延迟加载
		// Customer c2 = (Customer) session.load(Customer.class, 3);

		// load：默认延迟加载，何时被初始化呢？
		Customer c3 = (Customer) session.load(Customer.class, 2);
		// System.out.println(c3.getId());// 访问id的时候不会初始化
		// System.out.println(c3);// 当访问其他属性的时候，自动初始化

		// Customer c2 = (Customer)session.load(Customer.class, 2);
		// Hibernate.initialize(c2);//强制初始化

		Customer c4 = (Customer) session.createQuery("from Customer where id =2").uniqueResult();
		System.out.println(c4);

		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testFetchAndLazy() {
		Session session = HibernateUtils.openSession();
		session.beginTransaction();

		Customer customer = (Customer) session.get(Customer.class, 1);

		System.out.println(customer.getOrders().size());

		session.getTransaction().commit();
		session.close();
	}

}
