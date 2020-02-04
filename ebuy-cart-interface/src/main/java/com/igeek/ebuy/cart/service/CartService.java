package com.igeek.ebuy.cart.service;

import java.util.List;

import com.igeek.ebuy.pojo.TbItem;
import com.igeek.ebuy.util.pojo.BuyResult;

/**
 * @author www.igeekhome.com
 * 
 * @description TODO
 *
 * 2019年4月9日 上午11:26:41
 */
public interface CartService {
	/**
	 * 登录状态将一个商品加入购物车
	 * @return
	 */
	public BuyResult addCart(long userId,long itemId,int num);
	/**
	 * 修改购物车商品数量
	 * @param userId
	 * @param itemId
	 * @param num
	 * @return
	 */
	public BuyResult updateCart(long userId,long itemId,int num);
	/**
	 * 删除购物车商品数量
	 * @param userId
	 * @param itemId
	 * @return
	 */
	public BuyResult deleteCart(long userId,long itemId);
	/**
	 * 合并购物车
	 * @param userId
	 * @param cartList
	 * @return
	 */
	public BuyResult mergeCart(long userId,List<TbItem> cartList);
	/**
	 * 获取服务端的购物车列表
	 * @param userId
	 * @return
	 */
	public List<TbItem> getCartList(long userId);
}
