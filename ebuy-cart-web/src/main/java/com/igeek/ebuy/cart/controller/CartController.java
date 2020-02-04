package com.igeek.ebuy.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igeek.ebuy.cart.service.CartService;
import com.igeek.ebuy.pojo.TbItem;
import com.igeek.ebuy.pojo.TbUser;
import com.igeek.ebuy.service.ItemService;
import com.igeek.ebuy.util.cookie.CookieUtils;
import com.igeek.ebuy.util.json.JsonUtils;
import com.igeek.ebuy.util.pojo.BuyResult;

/**
 * @author www.igeekhome.com
 * 
 * @description TODO
 *
 *              2019年4月9日 上午9:40:06
 */
@Controller
public class CartController {

	@Value("${CART_NAME}")
	private String CART_NAME;

	@Autowired
	private ItemService itemService;

	@Autowired
	private CartService cartService;

	@RequestMapping("/cart/delete/{itemId}")
	public String deleteCart(@PathVariable long itemId, HttpServletRequest request, HttpServletResponse response) {
		// 判断用户是否登录
		Object userObj = request.getAttribute("loginUser");
		if (userObj != null) {// 已经登录
			TbUser user = (TbUser) userObj;
			cartService.deleteCart(user.getId(), itemId);
			return "redirect:/cart/cart.html";
		}

		// 修改购物车商品数量
		List<TbItem> cartList = getCart(request);
		for (TbItem item : cartList) {
			if (item.getId() == itemId) {
				cartList.remove(item);
				break;
			}
		}
		// 将购物车再次写入cookie
		CookieUtils.setCookie(request, response, CART_NAME, JsonUtils.objectToJson(cartList), 60 * 60 * 24 * 7, false);
		// 重定向到/cart/cart
		return "redirect:/cart/cart.html";
	}

	@RequestMapping("/cart/update/num/{itemId}/{num}")
	@ResponseBody
	public BuyResult updateNum(@PathVariable long itemId, @PathVariable int num, HttpServletRequest request,
			HttpServletResponse response) {
		// 判断用户是否登录
		Object userObj = request.getAttribute("loginUser");
		if (userObj != null) {// 已经登录
			TbUser user = (TbUser) userObj;
			cartService.updateCart(user.getId(), itemId, num);
			return BuyResult.ok();
		}
		// 修改购物车商品数量
		List<TbItem> cartList = getCart(request);
		for (TbItem item : cartList) {
			if (item.getId() == itemId) {
				item.setNum(num);
				break;
			}
		}
		// 将购物车再次写入cookie
		CookieUtils.setCookie(request, response, CART_NAME, JsonUtils.objectToJson(cartList), 60 * 60 * 24 * 7, false);
		return BuyResult.ok();
	}

	@RequestMapping("/cart/cart")
	public String cartList(HttpServletRequest request,HttpServletResponse response) {
		// 从cookie获取购物车列表
		List<TbItem> cartList = getCart(request);
		// 先判断是否登录 如果登录，则合并购物车，从服务端获取购物车列表
		// 判断用户是否登录
		Object userObj = request.getAttribute("loginUser");
		if (userObj != null) {// 已经登录
			TbUser user = (TbUser) userObj;
			//合并购物车
			if(cartList!=null && cartList.size()>0) {
				cartService.mergeCart(user.getId(), cartList);
				//清空cookie中的购物车
				CookieUtils.deleteCookie(request, response, CART_NAME);
			}
			//查询购物车列表
			cartList = cartService.getCartList(user.getId());
		}
		request.setAttribute("cartList", cartList);
		return "cart";
	}

	@RequestMapping("/cart/add/{itemId}")
	public String cartAdd(@PathVariable long itemId, int num, HttpServletRequest request,
			HttpServletResponse response) {
		// 判断用户是否登录
		Object userObj = request.getAttribute("loginUser");
		if (userObj != null) {// 已经登录
			TbUser user = (TbUser) userObj;
			cartService.addCart(user.getId(), itemId, num);
			return "cartSuccess";
		}
		// 根据传递的商品的ID和数量创建或者添加购物车
		// 先查询购物车
		List<TbItem> cartList = getCart(request);
		// 标记商品在购物车中不存在
		boolean isExists = false;
		// 检查要添加的商品是否已经存在
		for (TbItem cartItem : cartList) {
			if (cartItem.getId() == itemId) {// 商品存在，增加数量
				cartItem.setNum(cartItem.getNum() + num);
				isExists = true;
				break;
			}
		}
		if (!isExists) {// 购物车中不存在
			// 查询商品信息
			TbItem item = itemService.queryById(itemId);
			// 修改数量
			item.setNum(num);
			// 调整图片
			String image = item.getImage();
			if (StringUtils.isNoneBlank(image)) {
				image = image.split(",")[0];
			}
			item.setImage(image);
			// 将商品加入购物车
			cartList.add(item);
		}
		// 将购物车再次写入cookie
		CookieUtils.setCookie(request, response, CART_NAME, JsonUtils.objectToJson(cartList), 60 * 60 * 24 * 7, false);
		return "cartSuccess";
	}

	/**
	 * 从cookie中获取购物车信息
	 * 
	 * @param request
	 * @return
	 */
	private List<TbItem> getCart(HttpServletRequest request) {
		List<TbItem> cartList = new ArrayList<TbItem>();
		String cartJson = CookieUtils.getCookieValue(request, CART_NAME, false);
		if (StringUtils.isNoneBlank(cartJson)) {
			cartList = JsonUtils.jsonToList(cartJson, TbItem.class);
		}
		return cartList;
	}
}
