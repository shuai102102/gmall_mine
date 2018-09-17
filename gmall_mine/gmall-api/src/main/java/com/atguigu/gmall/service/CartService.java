package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {


    void addCart(CartInfo cartInfo);

   

    CartInfo ifCartExist(String userId, String skuId);

    List<CartInfo> getCartListCacheByUser(String userId);

    List<CartInfo> cartCache(String userId);

    void updateCartChecked(CartInfo cartInfo);

    void mergeCart(String id, List<CartInfo> cartList);

    List<CartInfo> getCartListCheckedCacheByUser(String userId);

    void cleanCart(List<String> cartIds, String userId);
}
