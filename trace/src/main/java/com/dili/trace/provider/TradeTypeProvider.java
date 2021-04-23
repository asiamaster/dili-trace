package com.dili.trace.provider;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author jcy
 * @createTime 2018/11/1 17:17
 */

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.trace.domain.TradeType;
import com.dili.trace.service.TradeTypeService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 交易区提供者
 * @author asiamaster
 */
@Component
public class TradeTypeProvider implements ValueProvider {


    @Autowired
    TradeTypeService tradeTypeService;

    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        try{
            List<TradeType> tradeTypes = tradeTypeService.findAll();
            List<ValuePair<?>> buffer = new ArrayList<ValuePair<?>>();
            if(CollectionUtils.isNotEmpty(tradeTypes)) {
                tradeTypes.forEach(o -> {
                    buffer.add(new ValuePairImpl(o.getTypeName(), o.getTypeId()));
                });
            }
            return buffer;
        }catch (Exception e){
            return Lists.newArrayList();
        }
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }
}
