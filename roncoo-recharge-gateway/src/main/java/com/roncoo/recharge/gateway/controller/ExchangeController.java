/**
 * Copyright 2015-现在 广州市领课网络科技有限公司
 */
package com.roncoo.recharge.gateway.controller;

import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.roncoo.recharge.api.request.ExchangeOrderRequest;
import com.roncoo.recharge.api.request.ExchangeRequest;
import com.roncoo.recharge.api.response.ExchangeOrderResponse;
import com.roncoo.recharge.api.response.ExchangeResponse;
import com.roncoo.recharge.gateway.service.ExchangeService;
import com.roncoo.recharge.util.base.BaseController;
import com.roncoo.recharge.util.base.Result;
import com.roncoo.recharge.util.enums.ResultEnum;
import com.xiaoleilu.hutool.http.HttpUtil;

/**
 * 兑换业务
 */
@RestController
@RequestMapping(value = "/exchange")
public class ExchangeController extends BaseController {

	@Autowired
	private ExchangeService service;

	@RequestMapping
	public Result<ExchangeResponse> recharge(@Validated ExchangeRequest exchangeRequest, BindingResult bindingResult, HttpServletRequest request) {
		logger.warn("兑换业务-兑换接口：请求报文-IP={}, 参数={}", HttpUtil.getClientIP(request), exchangeRequest);

		// 参数校验
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				sb.append(fieldError.getDefaultMessage()).append(",");
			}
			logger.warn("兑换业务-兑换接口：参数错误={}", sb);
			return Result.error(sb.substring(0, sb.length() - 1));
		}

		// 兑换业务
		Result<ExchangeResponse> result = service.exchange(exchangeRequest);
		logger.warn("兑换业务-兑换接口：返回报文={}", result);
		return result;
	}

	@RequestMapping(value = "/notify/{supplyCode}")
	public String notify(@PathVariable(value = "supplyCode") String supplyCode, HttpServletRequest request) {
		TreeMap<String, Object> paramMap = getParamMap(request);
		if (paramMap == null || paramMap.isEmpty()) {
			logger.error("兑换业务-回调接口：获取不到任何参数");
			return "fail";
		}
		logger.warn("兑换业务-回调接口：IP={}，参数={}", HttpUtil.getClientIP(request), paramMap);

		// 回调处理
		Result<String> result = service.notify(supplyCode, paramMap);

		if (!result.getCode().equals(ResultEnum.SUCCESS.getCode())) {
			logger.error("兑换业务-回调接口：异常原因={}", result.getMsg());
			return "fail";
		}

		return result.getData();
	}

	/**
	 * 订单查询接口
	 */
	@RequestMapping(value = "/order")
	public Result<ExchangeOrderResponse> queryOrder(@Validated ExchangeOrderRequest exchangeOrderRequest, BindingResult bindingResult, HttpServletRequest request) {
		logger.warn("订单查询接口：IP={}, 参数={}", HttpUtil.getClientIP(request), exchangeOrderRequest);

		// 参数校验
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				sb.append(fieldError.getDefaultMessage()).append(",");
			}
			logger.warn("订单查询接口：参数错误={}", sb);
			return Result.error(sb.substring(0, sb.length() - 1));
		}

		// 查询
		return service.queryOrder(exchangeOrderRequest);
	}

}
