package com.igeek.ebuy.cart.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igeek.ebuy.cart.service.CartService;
import com.igeek.ebuy.manager.mapper.TbItemMapper;
import com.igeek.ebuy.pojo.TbItem;
import com.igeek.ebuy.util.jedis.JedisClient;
import com.igeek.ebuy.util.json.JsonUtils;
import com.igeek.ebuy.util.pojo.BuyResult;

/**
 * @author www.igeekhome.com
 * 
 * @description TODO
 *
 *              2019年4月9日 上午11:29:56
 */
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private JedisClient client;

	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public BuyResult addCart(long userId, long itemId, int num) {
		// 判断商品在redis中是否存在
		String itemJson = client.hget(userId + "", itemId + "");
		TbItem item = null;
		if (StringUtils.isNoneBlank(itemJson)) {
			// 商品存在
			item = JsonUtils.jsonToPojo(itemJson, TbItem.class);
			item.setNum(item.getNum() + num);
		} else {
			// 商品不存在
			// 查询商品
			item = itemMapper.selectByPrimaryKey(itemId);
			item.setNum(num);
			//处理图片
			String image = item.getImage();
			if(StringUtils.isNoneBlank(image)) {
				image = image.split(",")[0];
				item.setImage(image);
			}
		}
		// 将商品写入redis
		client.hset(userId + "", itemId + "", JsonUtils.objectToJson(item));
		return BuyResult.ok();
	}

	@Override
	public BuyResult updateCart(long userId, long itemId, int num) {
		// 判断商品在redis中是否存在
		String itemJson = client.hget(userId + "", itemId + "");
		if (StringUtils.isNoneBlank(itemJson)) {
			// 商品存在
			TbItem item = JsonUtils.jsonToPojo(itemJson, TbItem.class);
			item.setNum(num);
			// 将商品写入redis
			client.hset(userId + "", itemId + "", JsonUtils.objectToJson(item));
		}
		return BuyResult.ok();
	}

	@Override
	public BuyResult deleteCart(long userId, long itemId) {
		client.hdel(userId + "", itemId + "");
		return BuyResult.ok();
	}

	@Override
	public BuyResult mergeCart(long userId, List<TbItem> cartList) {
		for (TbItem item : cartList) {
			//效率上并不是最好的，有可能会将已经查询过的商品再次查询。
			//可以考虑不调用addCart方法直接写业务。
			addCart(userId,item.getId(),item.getNum());
		}
		return BuyResult.ok();
	}

	@Override
	public List<TbItem> getCartList(long userId) {
		List<String> items = client.hvals(userId+"");
		List<TbItem> cartList = new ArrayList<TbItem>();
		for (String itemJson : items) {
			TbItem item = JsonUtils.jsonToPojo(itemJson, TbItem.class);
			cartList.add(item);
		}
		return cartList;
	}

}
