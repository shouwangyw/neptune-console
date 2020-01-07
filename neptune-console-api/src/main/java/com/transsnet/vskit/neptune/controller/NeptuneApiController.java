package com.transsnet.vskit.neptune.controller;

import com.transsnet.vskit.neptune.model.Card;
import com.transsnet.vskit.neptune.model.LoadCard;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.service.QueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yangwei
 * @date 2019-12-09 19:09
 */
@Api(tags = "Neptune可视化界面调用API接口")
@RestController
public class NeptuneApiController {
    @Resource
    private QueryService queryService;

    @ApiOperation("初始化操作面板")
    @GetMapping("/api/v1/board")
    public LoadCard load() {
        return new LoadCard()
                .setCard(new Card())
                .setStartTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis());
    }

    @ApiOperation("执行操作面板")
    @PostMapping("/api/v1/board")
    public QueryResult execute(@RequestBody Card card) {
        return queryService.executeQuery(card.getCode());
    }
}