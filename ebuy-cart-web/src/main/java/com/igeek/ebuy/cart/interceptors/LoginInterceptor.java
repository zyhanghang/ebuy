package com.igeek.ebuy.cart.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.igeek.ebuy.sso.service.TokenService;
import com.igeek.ebuy.util.cookie.CookieUtils;
import com.igeek.ebuy.util.pojo.BuyResult;

/**
 * @author www.igeekhome.com
 * 
 * @description TODO
 *
 * 2019年4月9日 上午11:14:30
 */
public class LoginInterceptor implements HandlerInterceptor {

	@Value("${TOKEN_COOKIE_NAME}")
	private String TOKEN_COOKIE_NAME;
	
	@Autowired
	private TokenService tokenService;
	/**
	 * 进入handler之前拦截
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//取出cookie
		String token = CookieUtils.getCookieValue(request, TOKEN_COOKIE_NAME);
		if(StringUtils.isNoneBlank(token)) {
			//通过token取用户
			BuyResult result = tokenService.getUserByToken(token);
			if(result.getStatus()==200) {
				//如果获取到用户的信息，则直接存入request中
				request.setAttribute("loginUser", result.getData());
			}
		}
		return true;//无论是否登录，都放行
	}
	/**
	 * handler返回之前拦截
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		//重定向，转发，修改视图，修改model
	}
	/**
	 * handler返回完成之后拦截
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

}
