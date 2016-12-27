package com.frankun.nutzbook.module;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.filter.CheckSession;

import com.frankun.nutzbook.bean.User;
import com.frankun.nutzbook.bean.UserProfile;

@IocBean
@At("/user")
@Ok("json:{locked:'password|salt',ignoreNull:true}")
@Fail("http:500")
@Filters(@By(type=CheckSession.class, args={"me", "/"})) //检查当前session是否带me这个属性
public class UserModule extends BaseModule{

	@At
	public int count() {
		return dao.count(User.class);
	}

	/**
	 * 登录
	 * @param name
	 * @param password
	 * @param session
	 * @return
	 */
	@At
	@Filters
	public Object login(@Param("username") String name, @Param("password") String password, HttpSession session){
		User user = dao.fetch(User.class, Cnd.where("name","=",name).and("password","=",password));
		if (user == null) {
			return false;
		}else{
			session.setAttribute("me", user.getId());
			return true;
		}
	}
	
	/**
	 * 退出
	 * @param session
	 */
	@At 
	@Ok(">>:/")
	public void logout(HttpSession session){
		session.invalidate();
	}
	
	/**
	 * 增加用户
	 * @param user
	 * @return
	 */
	@At
	public Object add(@Param("..")User user){
		NutMap nutMap = new NutMap();
		String msg = checkUser(user, true);
		if (msg != null) {
			return nutMap.setv("result", "fail").setv("msg", msg);
		}
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());
		user = dao.insert(user);
		return nutMap.setv("result", "success").setv("data", user);
	}
	
	/**
	 * 更新用户
	 * @param user
	 * @return
	 */
	@At
	public Object update(@Param("..") User user){
		NutMap nutMap = new NutMap();
		String msg = checkUser(user, false);
		if (msg != null) {
			return nutMap.setv("result", "fail").setv("msg", msg);
		}
		user.setName(null); //不允许更新用户名
		user.setCreateTime(null); //不允许更新创建时间
		user.setUpdateTime(new Date()); 
		dao.updateIgnoreNull(user); //更新操作
		return nutMap.setv("result", "success");
	}
	
	/**
	 * 删除用户
	 * @param id
	 * @param me
	 * @return
	 */
	@At
	@Aop(TransAop.READ_COMMITTED)
	public Object delete(@Param("id") int id, @Attr("me") int me){
		if (me == id) {
			return new NutMap().setv("result", "fail").setv("msg", "不能删除当前用户!!");
		}
		dao.delete(User.class, id);
		dao.clear(UserProfile.class, Cnd.where("userId", "=", me));
		return new NutMap().setv("result", "success");
	}
	
	
	/**
	 * 查询用户
	 * @param name
	 * @param pager
	 * @return
	 */
	@At
	public Object query(@Param("name")String name, @Param("..")Pager pager){
		Cnd cnd = Strings.isBlank(name)?null:Cnd.where("name", "like", "%" + name + "%");
		QueryResult qr = new QueryResult();
		qr.setList(dao.query(User.class, cnd,pager));
		pager.setRecordCount(dao.count(User.class, cnd));
		qr.setPager(pager);
		return qr; //默认分页第一页，每页20条
	}
	
	@At("/")
	@Ok("jsp:jsp.user.list")  // 真实路径是 /WEB-INF/jsp/user/list.jsp
	public void index(){
	}
	
	/**
	 * 验证用户
	 * @param user
	 * @param create
	 * @return
	 */
	protected String checkUser(User user, boolean create) {
		if (user == null) {
			return "空对象";
		}
		if (create) {
			if (Strings.isBlank(user.getName()) || Strings.isBlank(user.getPassword())) {
				return "用户名或密码不能为空";
			}
		}else{
			if (Strings.isBlank(user.getPassword())) {
				return "密码不能为空";
			}
		}
		
		String password = user.getPassword().trim();
		if (6 > password.length() || password.length() >12) {
			return "密码长度错误";
		}
		user.setPassword(password);
		
		if (create) {
			 int count = dao.count(User.class, Cnd.where("name", "=", user.getName()));
			if (count != 0) {
				return "用户名已经存在";
			}
		}else{
			if (user.getId() < 1) {
				return "用户id非法";
			}
		}
		if (user.getName() != null) {
			user.setName(user.getName().trim());
		}
		
		return null;
	}
	
}
